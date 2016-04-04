package server.operation;


import server.UDPServer;
import utils.Utils;

import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WriteAllOperation extends Operation{
    public WriteAllOperation(DatagramSocket socket, DatagramPacket incoming, UDPServer udpServer, int requestId){
        super(socket, incoming, udpServer, requestId);
    }

    @Override
    public void process() throws Exception {
        String replyMsg;
        byte[] data = super.getIncoming().getData();
        String command = new String(data, 0, getIncoming().getLength());


        // 1 write_all "file_path" "new_content"
        String [] firstSplit = command.trim().split("\"");
        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 append
        int requestId = Integer.valueOf(secondSplit[0].trim());

        String filePath = firstSplit[1];
        String newContent = firstSplit[3];

        if(Files.notExists(Paths.get(filePath))){
            replyMsg = Utils.addRequestId(requestId, "Error: file not exist");
            super.reply(replyMsg.getBytes());
        }else {
            FileOutputStream out = null;
            try {

                out = new FileOutputStream(filePath, false);
                out.write(newContent.getBytes());

                replyMsg = Utils.addRequestId(requestId,  "Write all to file successfully!");
                super.reply(replyMsg.getBytes());

                super.getServer().onFileChanged();

            } catch (Exception e){

                replyMsg = Utils.addRequestId(requestId, "Error: Exception when writing");
                super.reply(replyMsg.getBytes());
                e.printStackTrace();

            }
            finally {
                if(out != null){
                    out.close();
                }
            }
        }

    }
}
