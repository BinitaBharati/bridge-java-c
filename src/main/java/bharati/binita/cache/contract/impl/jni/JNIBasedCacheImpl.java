package bharati.binita.cache.contract.impl.jni;

import bharati.binita.cache.contract.CacheContract;

/**
 * JNI based hashmap.
 * At C end, same header file will be used in for both thread and non-thread safe
 * implementations of the NativeHashMap. Hence, both versions of the NativeHashMap
 * implementations, will import the same generated header;only internal implementation details
 * will be different.
 */
public class JNIBasedCacheImpl implements CacheContract {

    private static boolean isLoaded = false;

    public JNIBasedCacheImpl(String jniLibRef) {
        if (!isLoaded) {
            System.loadLibrary(jniLibRef);
            isLoaded = true;
        }
    }

    native public void init_hash_table();
    native public void insert_to_hash_table(int key, String valueName);
    native public String hash_table_look_up(int key);
    native public void free_string(String returnedNameFromLookUp);
    native public boolean delete_key_fromhashtable(int key);

    @Override
    public void updateCache(int key, String value) {
        insert_to_hash_table(key, value);
    }

    @Override
    public String lookUpKey(int key) {
        return hash_table_look_up(key);
    }
}
