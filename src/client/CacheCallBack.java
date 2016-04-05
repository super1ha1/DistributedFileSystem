/*
* CacheCallBack interface
* Will be implemented inside some methods of UDPClient for caching client-side data, such as save file to cache, get the latest update from server
*
* */


package client;


public interface CacheCallBack {
    void onDataReceive(String data) throws Exception;
}
