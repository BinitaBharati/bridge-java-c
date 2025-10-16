package bharati.binita.cache1.impl.ffi;

import bharati.binita.cache1.common.helpers.CustomerTransactionWriter;
import bharati.binita.cache1.contract.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FFICacheServiceImpl implements CacheService {
    private static boolean isLoaded;
    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup lookup = SymbolLookup.loaderLookup();
    public static final int CUSTOMER_INFO_JSON_STR_SIZE = 200;//should be same as the size of son string returned by C code.Unit in bytes
    public static final int CUSTOMER_TRXN_INFO_JSON_STR_SIZE = 600;//should be same as the size of son string returned by C code.Unit in bytes
    /**
     * ThreadLocal maintains a map of values, one per thread.
     *
     * The lambda () -> new byte[...] is called once per thread, the first time that thread calls .get().
     *
     * After that, each thread sees its own array every time it calls .get().
     * So, if you are handling multiple customer ids in a thread, then you must clear CUSTOMER_INFO_JSON_STR_BUFFER (set all bytes to 0)
     * for every custId call.
     */
    private final ThreadLocal<byte[]> CUSTOMER_INFO_JSON_STR_BUFFER =
            ThreadLocal.withInitial(() -> new byte[CUSTOMER_INFO_JSON_STR_SIZE]);
    private final ThreadLocal<byte[]> CUSTOMER_TRXN_INFO_JSON_STR_BUFFER =
            ThreadLocal.withInitial(() -> new byte[CUSTOMER_TRXN_INFO_JSON_STR_SIZE]);

    private static  MethodHandle initHashTable;
    private static  MethodHandle onboardCustomer;
    private static  MethodHandle getBasicCacheEntry;
    private static  MethodHandle updateBasicCustomerInfo;
    private static  MethodHandle addCustomerTransaction;
    private static  MethodHandle getCustomerLatestTransactions;
    private static  MethodHandle lookUpCustomerBalance;

    private static final Logger log = LoggerFactory.getLogger(FFICacheServiceImpl.class);


    public FFICacheServiceImpl(String sharedLibRef) {
        if (!isLoaded) {
            System.loadLibrary(sharedLibRef);
            initHashTable = linker.downcallHandle(
                    lookup.find("ffi_init_hash_table").orElseThrow(),
                    FunctionDescriptor.ofVoid()//FunctionDescriptor.ofVoid means return type is void. Contrast with FunctionDescriptor.of(...
            );

            onboardCustomer  = linker.downcallHandle(
                    lookup.find("ffi_insert_to_hash_table").orElseThrow(),
                    FunctionDescriptor.ofVoid(
                            ValueLayout.JAVA_INT,               // custId
                            ValueLayout.ADDRESS,                // firstName (char*)
                            ValueLayout.ADDRESS,                // lastName
                            ValueLayout.ADDRESS,                // phone
                            ValueLayout.ADDRESS ,               // email
                            ValueLayout.JAVA_DOUBLE             //balance
                    )
            );

            getBasicCacheEntry  = linker.downcallHandle(
                    lookup.find("ffi_look_up_basic_customer_info").orElseThrow(),
                    FunctionDescriptor.ofVoid(
                            ValueLayout.JAVA_INT,               // custId
                            ValueLayout.ADDRESS                 // returnJsonBasicInfo
                    )
            );

            updateBasicCustomerInfo  = linker.downcallHandle(
                    lookup.find("ffi_update_basic_customer_info").orElseThrow(),
                    FunctionDescriptor.ofVoid(
                            ValueLayout.JAVA_INT,               // custId
                            ValueLayout.ADDRESS,                // phone
                            ValueLayout.ADDRESS                 // email
                    )
            );

            addCustomerTransaction  = linker.downcallHandle(
                    lookup.find("ffi_addCustomerTransaction").orElseThrow(),
                    FunctionDescriptor.ofVoid(
                            ValueLayout.JAVA_INT,               // custId
                            ValueLayout.JAVA_LONG,              // trxn date
                            ValueLayout.JAVA_INT,               // opType
                            ValueLayout.JAVA_DOUBLE             // amount
                    )
            );

            getCustomerLatestTransactions  = linker.downcallHandle(
                    lookup.find("ffi_look_up_customer_trxns").orElseThrow(),
                    FunctionDescriptor.ofVoid(
                            ValueLayout.JAVA_INT,               // custId
                            ValueLayout.ADDRESS                 // returnTransactionJson
                    )
            );

            lookUpCustomerBalance = linker.downcallHandle(
                    lookup.find("ffi_look_up_customer_balance").orElseThrow(),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_DOUBLE, // return double, represents customerBalance
                            ValueLayout.JAVA_INT //input arg unsigned int representing customerId
                    )
            );
        }

    }

    @Override
    public void initCache() throws Throwable {
        initHashTable.invoke();
    }

    @Override
    public void onboardCustomer(int custId, String firstName, String lastName, String phone, String email, double balance) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            // Allocate C strings in native memory (UTF-8 null-terminated)
            MemorySegment cFirst = arena.allocateFrom(firstName);
            MemorySegment cLast  = arena.allocateFrom(lastName);
            MemorySegment cPhone = arena.allocateFrom(phone);
            MemorySegment cEmail = arena.allocateFrom(email);

            // Call native function
            onboardCustomer.invokeExact(
                    custId, cFirst, cLast, cPhone, cEmail, balance
            );
        }
    }

    @Override
    public String getBasicCustomerInfo(int custId, MemorySegment buffer) throws Throwable {

            byte[] result = getBasicCustomerInfo2(custId, buffer);
            int nullTerminatorIndex = 0;
            for (int j = 0 ; j < result.length ; j++) {
                nullTerminatorIndex = j;
                if (result[j] == 0) {//NULL terminator index
                    break;
                }
            }
            if (nullTerminatorIndex == 0) {
                return null;
            }
            return new String(result, 0, nullTerminatorIndex, StandardCharsets.UTF_8);//Will this turn to be costly ?
    }

    @Override
    public byte[] getBasicCustomerInfo2(int custId, MemorySegment buffer) throws Throwable {
        buffer.fill((byte) 0);

        /**
         * We are sending empty buffer from Java , and C will only fill it. This is to ensure that
         * C does not have to worry about when to free a string that C otherwise would have to return to Java.
         *
         * Here, Java itself is owner of the buffer, and JVM will take care of freeing up memory used by it.
         */
        getBasicCacheEntry.invoke(custId, buffer);
        byte[] bytes = CUSTOMER_INFO_JSON_STR_BUFFER.get();
        Arrays.fill(bytes, (byte) 0);
        int i = 0;
        while (i < bytes.length) {
            byte b = buffer.get(ValueLayout.JAVA_BYTE, i);
            if (b == 0) break;
            bytes[i++] = b;
        }
        return bytes;
    }

    @Override
    public void updateBasicCustomerInfo(int custId, String phone, String email) throws Throwable {
        /**
         * The memory for cPhone and cEmail is owned by the arena you created (Arena.ofConfined()).
         *
         * When the try block ends,
         * the arena is automatically closed (AutoCloseable).
         *
         * When an arena is closed, it deallocates all memory segments it allocated.
         *
         * So the memory for cPhone and cEmail is freed automatically at the end of the try block.
         */
        try (Arena arena = Arena.ofConfined()) {
            // Allocate C strings in native memory (UTF-8 null-terminated)
            MemorySegment cPhone = arena.allocateFrom(phone);
            MemorySegment cEmail = arena.allocateFrom(email);
            // Call native function
            updateBasicCustomerInfo.invokeExact(
                    custId, cPhone, cEmail
            );
        }
    }

    @Override
    public void addTransactionEntry(int custId, long trxnDate, int opType, double amount) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            // Allocate C strings in native memory (UTF-8 null-terminated)

            // Call native function
            addCustomerTransaction.invokeExact(
                    custId, trxnDate, opType, amount
            );
        }
    }

    @Override
    public String getLatestTrxnsForCustomer(int custId, MemorySegment buffer) throws Throwable{
        /**
         * We are sending empty buffer from Java , and C will only fill it. This is to ensure that
         * C does not have to worry about when to free a string that C otherwise would have to return to Java.
         *
         * Here, Java itself is owner of the buffer, and JVM will take care of freeing up memory used by it.
         */
        byte[] bytes = CUSTOMER_TRXN_INFO_JSON_STR_BUFFER.get();
        Arrays.fill(bytes, (byte) 0);
        int i = 0;
        while (i < bytes.length) {
            byte b = buffer.get(ValueLayout.JAVA_BYTE, i);
            if (b == 0) break;
            bytes[i++] = b;
        }
        if (i == 0) return null;
        return new String(bytes, 0, i, StandardCharsets.UTF_8);
    }

    @Override
    public double getCustomerBalance(int custId) throws Throwable {
        // unsigned int fits in Java int if positive
        return (double) lookUpCustomerBalance.invoke(custId);
    }
}