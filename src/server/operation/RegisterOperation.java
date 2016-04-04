package server.operation;


import server.UDPServer;
import utils.Const;
import server.RegisteredClient;
import utils.Utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RegisterOperation extends Operation {

    public RegisterOperation(DatagramSocket socket, DatagramPacket incoming, UDPServer udpServer, int requestId){
        super(socket, incoming, udpServer, requestId);
    }


    @Override
    public void process() throws Exception {

        String replyMsg;
        byte[] data = super.getIncoming().getData();
        String command = new String(data, 0, getIncoming().getLength());

        // 1 register "file_path" 100
        String [] firstSplit = command.trim().split("\"");
        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 register
        int requestId = Integer.valueOf(secondSplit[0].trim());

        String filePath = firstSplit[1];
        long interval  = Integer.valueOf(firstSplit[2].trim());

        try {
            RegisteredClient newClient = new RegisteredClient(
                    super.getSocket(),
                    super.getIncoming().getAddress(), super.getIncoming().getPort(),
                    filePath,
                    interval, requestId);

            String key = Utils.encodeAddressAndPortToKey(super.getIncoming().getAddress(), super.getIncoming().getPort());

            super.getServer().getCbMap().put(key, newClient);
            replyMsg = Utils.addRequestId(requestId, Const.MESSAGE.REGISTER_SUCCESS);
            super.reply(replyMsg.getBytes());

        } catch (Exception e){
            e.printStackTrace();
            String exceptionMsg = Utils.addRequestId(requestId, "Error: Exception");
            super.reply(exceptionMsg.getBytes());
        }
    }
}
