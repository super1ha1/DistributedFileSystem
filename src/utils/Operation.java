package utils;


import server.UDPServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class Operation {

    private static final int WRONG_PORT = 3000;
    private static final boolean SIMULATE = true; //simulate to control the reply failure

    private UDPServer server;
    private int requestId;
    private DatagramSocket socket;
    private DatagramPacket incoming;
    private DatagramPacket reply;
    private static Random randomGenerator = new Random();


    public Operation(DatagramSocket socket, DatagramPacket incoming, UDPServer server, int requestId){
        this.socket = socket;
        this.incoming = incoming;
        this.server = server;
        this.requestId = requestId;
        randomGenerator.setSeed(0);
    }

    public void reply(byte [] array) throws Exception{
        this.reply = new DatagramPacket(array, array.length, incoming.getAddress(), incoming.getPort());
        Utils.echo("Reply: " + new String(array));

        if(SIMULATE){
//            int random = randomGenerator.nextInt(1000);
//            Utils.echo("Random number: " + random);
//
//            // Use a random number to control the result of request
//            if(random % 2 == 0){
//                socket.send(reply); //send to correct port
//            }else {
//                reply = new DatagramPacket(array,array.length, incoming.getAddress(), WRONG_PORT);
//                socket.send(reply); // send to wrong port
//            }
        }else {
            socket.send(reply);
        }

        if(this.server.isAtMostOne()){
            this.server.recordAReply(incoming.getAddress(), incoming.getPort(), requestId, array);
        }
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

    public UDPServer getServer() {
        return server;
    }
}
