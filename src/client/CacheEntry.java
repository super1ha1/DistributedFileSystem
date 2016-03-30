package client;

public class CacheEntry {
    private byte [] content;
    private long lastValidateTime, lastModifiedTime;

    public CacheEntry(byte[] content,  long lastValidateTime, long lastModifiedTime) {
        this.content = content;
        this.lastModifiedTime = lastModifiedTime;
        this.lastValidateTime = lastValidateTime;
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
}
