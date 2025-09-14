package bharati.binita.bridge.javaToC.ffi;

/**
 * FFI based hashmap.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

public class NativeHashMapFFI {
    private static final Logger log = LoggerFactory.getLogger(NativeHashMapFFI.class);

    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup lookup = SymbolLookup.loaderLookup();
    private static boolean isLoaded = false;

    private static  MethodHandle initHashTable;
    private static  MethodHandle insertToHashTable;
    private static  MethodHandle hashTableLookUp;
    private static  MethodHandle deleteKeyFromHashtable;
    private static  MethodHandle freeMemoryUsedByReturnedName;

    public NativeHashMapFFI(String jniLibRef) {
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

                deleteKeyFromHashtable = linker.downcallHandle(
                        lookup.find("delete_key_from_hashtable").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT)
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

    public static void insert(int key, String value) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            //MemorySegment cStr = arena.allocateUtf8String(value);//jdk21
            MemorySegment cStr = arena.allocateFrom(value);//jdk 22 onwards
            insertToHashTable.invoke(key, cStr);
        }
    }

    /*public static String lookup(int key) throws Throwable {
        MemorySegment ptr = (MemorySegment) hashTableLookUp.invoke(key);
        if (ptr == null || ptr.address() == 0) {
            return null; // no entry
        }
        MemorySegment zeroLen = MemorySegment.ofAddress(ptr.address());
        MemorySegment stringSeg = zeroLen.reinterpret(
                100);
        String retVal =  stringSeg.getUtf8String(0);
        if (ptr != null) {
            freeMemoryUsedByReturnedName.invoke(ptr);
        }
        return retVal;
    }*/

    public static String lookup(int key) throws Throwable {
        MemorySegment ptr = (MemorySegment) hashTableLookUp.invoke(key);
        /*if (ptr == null || ptr.address() == 0) {
            return null; // no entry
        }*/
        if (ptr == null || ptr.equals(MemorySegment.NULL)) {
            return null;
        }

        // reinterpret with a max length guess
        //MemorySegment stringSeg = ptr.reinterpret(100);

        // ✅ new API
        String retVal = ptr.getString(0, StandardCharsets.UTF_8);
        log.info("key = {}, retVal = {}",key, retVal);

        if (ptr != null) {
            freeMemoryUsedByReturnedName.invoke(ptr);
        }
        return retVal;
    }

    public static boolean delete(int key) throws Throwable {
        int result = (int) deleteKeyFromHashtable.invoke(key);
        return result != 0;
    }

    /*public static void freeMemory(String value) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cStr = arena.allocateUtf8String(value);
            freeMemoryUsedByReturnedName.invoke(cStr);
        }
    }*/
}
