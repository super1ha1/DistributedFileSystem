/*
* CacheEntry class
*
* The data structure used for storing each cache entries.
* A single entry is set up with the file content, last validate time of that content and last modified time of the cache entry
* After a specific time interval, cache entry will be updated based on the data from server to ensure information refreshness
*
* */


package client;

public class CacheEntry {
    private byte [] content;
    private long lastValidateTime, lastModifiedTime, serverModifyTime;

    public CacheEntry(byte[] content,  long lastValidateTime, long lastModifiedTime) {
        this.content = content;
        this.lastValidateTime = lastValidateTime;
        this.lastModifiedTime = lastModifiedTime;
    }

    public byte[] getContent() {
        return content;
    }

    public long getLastValidateTime() {
        return lastValidateTime;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public void setLastValidateTime(long lastValidateTime) {
        this.lastValidateTime = lastValidateTime;
    }

    public long getServerModifyTime() {
        return serverModifyTime;
    }

    public void setServerModifyTime(long serverModifyTime) {
        this.serverModifyTime = serverModifyTime;
    }

    public boolean isValid() {
        return lastModifiedTime == serverModifyTime;
    }

    public boolean dataIsFresh(long cacheInterval) {
        return (System.currentTimeMillis()/1000) - lastValidateTime < cacheInterval ;
    }
}
