package utils;


import server.UDPServer;

import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RegisterOperation extends Operation {

    private UDPServer udpServer;
    public RegisterOperation(DatagramSocket socket, DatagramPacket incoming, UDPServer udpServer){
        super(socket, incoming);
        this.udpServer = udpServer;
    }


    @Override
    public void process() throws Exception {

        String replyMsg;
        byte[] data = super.getIncoming().getData();
        String command = new String(data, 0, getIncoming().getLength());

        // register "file_path" 100
        String [] arrayStr = command.trim().split("\"");

        String filePath = arrayStr[1];
        long interval  = Integer.valueOf(arrayStr[2].trim().replaceAll("( )+", " "));

        RegisteredClient newClient = new RegisteredClient(
                super.getSocket(),
                super.getIncoming().getAddress(), super.getIncoming().getPort(),
                filePath,
                interval);

        this.udpServer.getCbList().add(newClient);
        replyMsg = "You have registered successfully!";
        super.reply(replyMsg.getBytes());

    }
}
