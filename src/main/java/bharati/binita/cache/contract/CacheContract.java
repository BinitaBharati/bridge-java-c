package bharati.binita.cache.contract;

public interface CacheContract {

    public void updateCache(int key, String value) throws Throwable;
    public String lookUpKey(int key) throws Throwable;
}
