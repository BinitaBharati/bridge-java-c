package bharati.binita.common.contract;

public interface CacheContract {

    public void updateCache(int key, String value);
    public String lookUpKey(int key);
}
