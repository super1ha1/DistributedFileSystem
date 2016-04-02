package client;


import utils.Utils;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient implements CallBack {

    private static final int SENTINEL = -1;
    private static final int FIRST_ID = 0;


    private InetAddress host;
    private int port;
    private long cacheRefreshInterval;
    private long retransmitInterval;
    private DatagramSocket socket;
    private boolean registered;

    private int requestId;

    public UDPClient(InetAddress host, int port, long cacheRefreshInterval, long retransmitInterval) throws Exception {
        this.retransmitInterval = retransmitInterval;
        this.cacheRefreshInterval = cacheRefreshInterval;
        this.port = port;
        this.host = host;
        this.socket = new DatagramSocket();
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

    public InetAddress getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public long getCacheRefreshInterval() {
        return cacheRefreshInterval;
    }

    public long getRetransmitInterval() {
        return retransmitInterval;
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}
