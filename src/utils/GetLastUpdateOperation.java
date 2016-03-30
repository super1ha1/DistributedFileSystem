package utils;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetLastUpdateOperation extends Operation {
    public GetLastUpdateOperation(DatagramSocket socket, DatagramPacket incoming){
        super(socket, incoming);
    }


    @Override
    public void process() throws Exception {
        String exceptionMsg;
        byte[] data = super.getIncoming().getData();
        String command = new String(data, 0, getIncoming().getLength());

        // 1 get_last_update "file.txt"
        String [] firstSplit = command.trim().split("\"");

        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 get_last_update

        int requestId = Integer.valueOf(secondSplit[0].trim());
        String filePath = firstSplit[1];

        if(Files.notExists(Paths.get(filePath))){
            exceptionMsg = Utils.addRequestId(requestId, "Error: file not exist");
            super.reply(exceptionMsg.getBytes());
        }else {
            File file = new File(filePath);
            long lastUpdate = file.lastModified()/1000;
            super.reply(Utils.addRequestId(requestId, String.valueOf(lastUpdate)).getBytes());
        }

    }
}
