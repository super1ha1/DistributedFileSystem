/*
* CallBack interface
* Will be implemented by UDPClient
*
* */


package client;


public interface CallBack {
    void onWrite(byte[] newContent);
}
