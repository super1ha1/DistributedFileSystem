package client;


import utils.Utils;

import java.net.InetAddress;

public class UDPClient implements CallBack {

    private static final int SENTINEL = -1;
    private static final int FIRST_ID = 0;


    private InetAddress serverIP;
    private int port;
    private long cacheRefreshInterval;
    private long retransmitInterval;

    private boolean registered;

    private int requestId;

    public UDPClient(long retransmitInterval, long cacheRefreshInterval, int port, InetAddress serverIP) {
        this.retransmitInterval = retransmitInterval;
        this.cacheRefreshInterval = cacheRefreshInterval;
        this.port = port;
        this.serverIP = serverIP;
    }

    public UDPClient() {
        this.registered = false;
        this.requestId = FIRST_ID;
    }

    @Override
    public void onWrite(byte[] newContent) {
        Utils.echo(new String(newContent));
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public void increaseRequestId() {
        this.requestId++;
    }
}
