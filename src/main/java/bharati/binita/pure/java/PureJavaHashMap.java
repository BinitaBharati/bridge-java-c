package bharati.binita.pure.java;

import bharati.binita.common.contract.CacheContract;

import java.util.concurrent.ConcurrentHashMap;

public class PureJavaHashMap implements CacheContract {

    private ConcurrentHashMap<Integer, String> cache;

    public PureJavaHashMap () {
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public void updateCache(int key, String value) {
        cache.put(key, value);
    }

    @Override
    public String lookUpKey(int key) {
        return cache.get(key);
    }

}
