package bharati.binita.cache.contract.impl.ffi;

/**
 * FFI based hashmap.
 */

import bharati.binita.cache.contract.CacheContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

public class FFIBasedCacheImpl implements CacheContract {
    private static final Logger log = LoggerFactory.getLogger(FFIBasedCacheImpl.class);

    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup lookup = SymbolLookup.loaderLookup();
    private static boolean isLoaded = false;

    private static  MethodHandle initHashTable;
    private static  MethodHandle insertToHashTable;
    private static  MethodHandle hashTableLookUp;
    private static  MethodHandle freeMemoryUsedByReturnedName;

    public FFIBasedCacheImpl(String jniLibRef) {
        if (!isLoaded) {
            System.loadLibrary(jniLibRef);

            try {
                initHashTable = linker.downcallHandle(
                        lookup.find("init_hash_table").orElseThrow(),
                        FunctionDescriptor.ofVoid()
                );

                insertToHashTable = linker.downcallHandle(
                        lookup.find("insert_to_hash_table").orElseThrow(),
                        FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
                );

                hashTableLookUp = linker.downcallHandle(
                        lookup.find("hash_table_look_up").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
                );

                freeMemoryUsedByReturnedName = linker.downcallHandle(
                        lookup.find("free_string").orElseThrow(),
                        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
                );

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            isLoaded = true;
        }
    }

    public static void init() throws Throwable {
        initHashTable.invoke();
    }

    @Override
    public void updateCache(int key, String value) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cStr = arena.allocateFrom(value);//jdk 22 onwards
            insertToHashTable.invoke(key, cStr);
        }
    }

    @Override
    public String lookUpKey(int key) throws Throwable {
        MemorySegment ptr = (MemorySegment) hashTableLookUp.invoke(key);

        if (ptr == null || ptr.equals(MemorySegment.NULL)) {
            return null;
        }

        String retVal = ptr.reinterpret(Long.MAX_VALUE)//expected length of string passed from C.
                .getString(0, StandardCharsets.UTF_8);
        // ✅ new API
        log.info("key = {}, retVal = {}",key, retVal);

        if (ptr != null) {
            freeMemoryUsedByReturnedName.invoke(ptr);
        }
        return retVal;
    }
}
