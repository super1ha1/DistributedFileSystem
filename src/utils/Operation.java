package utils;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public abstract class Operation {

    private DatagramSocket socket;
    private DatagramPacket incoming;
    private DatagramPacket reply;

    public Operation(DatagramSocket socket, DatagramPacket incoming){
        this.socket = socket;
        this.incoming = incoming;
    }

    public void reply(byte [] array) throws Exception{
        this.reply = new DatagramPacket(array, array.length, incoming.getAddress(), incoming.getPort());
        Utils.echo("Reply: " + new String(array));
        socket.send(reply);
    }

    public abstract void process() throws Exception;

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public DatagramPacket getReply() {
        return reply;
    }

    public void setReply(DatagramPacket reply) {
        this.reply = reply;
    }

    public DatagramPacket getIncoming() {
        return incoming;
    }

    public void setIncoming(DatagramPacket incoming) {
        this.incoming = incoming;
    }
}
