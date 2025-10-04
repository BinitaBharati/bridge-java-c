package bharati.binita.cache1.impl.ffi;

import bharati.binita.cache1.contract.CacheService;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

public class FFICacheServiceImpl implements CacheService {
    private static boolean isLoaded;
    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup lookup = SymbolLookup.loaderLookup();
    public static final int CUSTOMER_INFO_JSON_STR_SIZE = 200;//should be same as the size of son string returned by C code.

    private static  MethodHandle initHashTable;
    private static  MethodHandle onboardCustomer;
    private static  MethodHandle getBasicCacheEntry;


    public FFICacheServiceImpl(String sharedLibRef) {
        if (!isLoaded) {
            System.loadLibrary(sharedLibRef);
            initHashTable = linker.downcallHandle(
                    lookup.find("initHashTable").orElseThrow(),
                    FunctionDescriptor.ofVoid()//FunctionDescriptor.ofVoid means return type is void. Contrast with FunctionDescriptor.of(...
            );

            onboardCustomer  = linker.downcallHandle(
                    lookup.find("onboardCustomer").orElseThrow(),
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
                    lookup.find("lookUpBasicCustomerInfo").orElseThrow(),
                    FunctionDescriptor.ofVoid(
                            ValueLayout.JAVA_INT,               // custId
                            ValueLayout.ADDRESS                 // returnJsonBasicInfo
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
    public String getBasicCustomerInfo(int custId) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            int bufferSize = CUSTOMER_INFO_JSON_STR_SIZE;
            MemorySegment buffer = arena.allocate(bufferSize);

            /**
             * We are sending empty buffer from Java , and C will only fill it. This is to ensure that
             * C does not have to worry about when to free a string that C otherwise would have to return to Java.
             *
             * Here, Java itself is owner of the buffer, and JVM will take care of freeing up memory used by it.
             */
            getBasicCacheEntry.invoke(custId, buffer);

            // Read null-terminated UTF-8 string from native buffer
            byte[] bytes = buffer.toArray(ValueLayout.JAVA_BYTE);
            int length = 0;
            while (length < bytes.length && bytes[length] != 0) {
                length++;
            }
            return new String(bytes, 0, length, StandardCharsets.UTF_8);
        }
    }
}

