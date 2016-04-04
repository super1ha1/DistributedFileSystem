package server;


import utils.Const;
import utils.Utils;

import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RegisteredClient {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private String filePath;
    private long numSeconds;
    private long registeredTime;
    private int lastRequestId;

    public RegisteredClient(DatagramSocket socket, InetAddress address, int port, String filePath, long numSeconds, int lastRequestId){
        this.address = address;
        this.port = port;
        this.filePath = filePath;
        this.numSeconds = numSeconds;
        this.registeredTime = System.currentTimeMillis() / 1000;
        this.socket = socket;
        this.lastRequestId = lastRequestId;
    }

    public long getRegisteredTime() {
        return registeredTime;
    }

    public long getNumSeconds() {
        return numSeconds;
    }

    public void onCallBack() throws Exception{
        String responseMsg = "";
        FileInputStream in = null;

        try {
            in = new FileInputStream(this.filePath);
            int avail = in.available();
            byte[] output = new byte[avail];
            int result = in.read(output);
            responseMsg = Utils.addRequestId(this.lastRequestId, new String(output));

        }
        catch (Exception e){
            e.printStackTrace();
            responseMsg = Utils.addRequestId(this.lastRequestId, "Error: IndexOutOfBoundsException");

        }
        finally {
            if (in != null) {
                in.close();
            }

            DatagramPacket packet = new DatagramPacket(responseMsg.getBytes(), responseMsg.getBytes().length, this.address, this.port);
            this.socket.send(packet);

        }
    }

    public void onRemove() throws Exception {
        String responseMsg = Utils.addRequestId(this.lastRequestId, Const.MESSAGE.REGISTER_EXPIRE);
        DatagramPacket packet = new DatagramPacket(responseMsg.getBytes(), responseMsg.getBytes().length, this.address, this.port);
        this.socket.send(packet);

    }
}
