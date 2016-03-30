package utils;


import server.UDPServer;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WriteOperation  extends Operation{

    public WriteOperation(DatagramSocket socket, DatagramPacket incoming, UDPServer udpServer, int requestId){
        super(socket, incoming, udpServer, requestId);
    }

    @Override
    public void process() throws Exception {
        String replyMsg;
        byte[] data = super.getIncoming().getData();
        String command = new String(data, 0, getIncoming().getLength());


        // 1 write "file_path" offset "content"
        String [] firstSplit = command.trim().split("\"");
        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 write
        int requestId = Integer.valueOf(secondSplit[0].trim());

        String filePath = firstSplit[1];
        int offset = Integer.valueOf(firstSplit[2].trim());
        String insert = firstSplit[3];

        if(Files.notExists(Paths.get(filePath))){
            replyMsg = Utils.addRequestId(requestId, "Error: file not exist");
            super.reply(replyMsg.getBytes());
        }else {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(filePath);

                int avail = in.available();
                if(offset >= avail){
                    replyMsg = Utils.addRequestId(requestId, "Error: offset larger than length, offset: " + offset  + " len: " + avail);
                    super.reply(replyMsg.getBytes());
                    return;
                }

                byte[] currentByte = new byte[avail];
                in.read(currentByte);
                String currentContent = new String(currentByte);
                currentContent = currentContent.substring(0, offset) + insert + currentContent.substring(offset);

                out = new FileOutputStream(filePath, false);
                out.write(currentContent.getBytes());

                replyMsg = Utils.addRequestId(requestId,  "Write to file successfully!");
                super.reply(replyMsg.getBytes());

                super.getServer().onFileChanged();

            } catch (Exception e){

                replyMsg = Utils.addRequestId(requestId, "Error: Exception when writing");
                super.reply(replyMsg.getBytes());
                e.printStackTrace();

            }
            finally {
                if (in != null) {
                    in.close();
                }
                if(out != null){
                    out.close();
                }
            }
        }

    }
}
