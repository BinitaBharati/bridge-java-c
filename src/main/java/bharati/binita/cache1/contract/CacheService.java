package bharati.binita.cache1.contract;

import java.lang.foreign.MemorySegment;

public interface CacheService {

    public void initCache() throws Throwable;

    public void onboardCustomer(int custId, String firstName, String lastName,
                                String phone, String email, double balance) throws Throwable;

    /**
     *
     * You must wonder why there are 2 versions of getBasicCustomerInfo, ie getBasicCustomerInfo and getBasicCustomerInfo2.
     * There are multiple aspects to this.
     *
     * Lets talk about when your interface to C code is FFI based.
     * Best practice for returning a C string via FFI (safe & fast)
     * 1️⃣ Option A — Java owns the buffer (recommended)
     *
     * Java allocates a buffer (using Arena or MemorySegment.allocateNative),
     * passes it to C,
     * C fills the string value into that buffer,
     * Java reads it back.
     *
     * Now, the passed and read back variable is of type java.lang.foreign.MemorySegment (allocated offheap and managed by JVM, but not GC collected).
     * You must still construct a java.lang.String (allocated in heap, JVM managed and GC collected) out of the bytes contained inside MemorySegment.
     *
     * Option B — C allocates and returns a pointer (less safe)
     * This is faster for short-lived use, but dangerous if you forget to call free() on the C-allocated string.
     * This also means, you must also expose a FFI wrapper method to free C native memory pointed by the returned pointer.
     * In my cache case, I wanted concurrent reads and writes to happen fas.
     *
     * There are two ways to return a pointer from C code. I am talking in terms of my cache use case that has requirement for
     * fast and concurrent reads and writes.
     *
     * Zero copy - in this case the original pointer in the C structure has to be sent back to Java. In this case, the thread safety lock
     * within C can not be released till Java is done reading the returned pointer value. After that Java needs to call the freeLock wrapper
     * that should release the thread safety lock. This will cause other threads that are trying to read/update the same customerId to stall,
     * till freeLock wrapper is called.
     *
     * Copy from original pointer to a new pointer and return to Java. You should also This way, the thread safety lock can be released as soon as
     * copying is complete. And other threads waiting on same customerId won't be stalled. However, it will be Java code's responsibility to call suitable
     * wrapper that can free the memory occupied by the returned C pointer.Also, in FFI context, you can not plainly use the returned C pointer in Java.
     * The returned C pointer will still be an instance of MemorySegment, that you will need to convert to java.lang.String.
     *
     *
     *
     *
     *
     * This is the “zero-copy, Java-managed” pattern.
     * In C code, there are two ways to return a string (char*) irrespective of if the interface is JNI/FFI based.
     * 1) You can
     *
     *
     *
     *
     */
    public String getBasicCustomerInfo(int custId, MemorySegment buffer) throws Throwable;

    public byte[] getBasicCustomerInfo2(int custId, MemorySegment buffer) throws Throwable;

    public void updateBasicCustomerInfo(int custId, String phone, String email) throws Throwable;

    public void addTransactionEntry(int custId, long trxnDate, int opType, double amount) throws Throwable;

    public String getLatestTrxnsForCustomer(int custId, MemorySegment buffer) throws Throwable;

    public double getCustomerBalance(int custId) throws Throwable;
}