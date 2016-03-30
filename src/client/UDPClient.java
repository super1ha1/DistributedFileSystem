package client;


import server.Const;
import utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient implements CallBack {

    private static final int SENTINEL = -1;
    private static final int CHECK_VALUE = 0;


    private InetAddress serverIP;
    private int port;
    private long cacheRefreshInterval;
    private long retransmitInterval;

    private boolean registered;
    private long registerInterval;
    private long registerTime;

    public UDPClient(long retransmitInterval, long cacheRefreshInterval, int port, InetAddress serverIP) {
        this.retransmitInterval = retransmitInterval;
        this.cacheRefreshInterval = cacheRefreshInterval;
        this.port = port;
        this.serverIP = serverIP;
    }

    public UDPClient() {
        this.registered = false;
        this.registerTime = SENTINEL;
        this.registerInterval = SENTINEL;
    }

    @Override
    public void onWrite(byte[] newContent) {
        Utils.echo(new String(newContent));
    }

    public boolean expired(){
        return this.registerTime + this.registerInterval < (System.currentTimeMillis()/ 1000);
    }
    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public long getRegisterInterval() {
        return registerInterval;
    }

    public void setRegisterInterval(long registerInterval) {
        this.registerInterval = registerInterval;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
