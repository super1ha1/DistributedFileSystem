package utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WriteOperation  extends Operation{

    public WriteOperation(DatagramSocket socket, DatagramPacket incoming){
        super(socket, incoming);
    }

    @Override
    public void process() throws Exception {
        String replyMsg;
        byte[] data = super.getIncoming().getData();
        String command = new String(data, 0, getIncoming().getLength());

        //write "file_path" offset "content"
        String [] arrayStr = command.trim().split("\"");

        String filePath = arrayStr[1];
        int offset = Integer.valueOf(arrayStr[2].trim().replaceAll("( )+", " ").split(" ")[0]);
        String insert = arrayStr[3];

        if(Files.notExists(Paths.get(filePath))){
            replyMsg = "Error: file not exist";
            super.reply(replyMsg.getBytes());
        }else {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(filePath);

                int avail = in.available();
                if(offset >= avail){
                    replyMsg = "Error: offset larger than length, offset: " + offset  + " len: " + avail;
                    super.reply(replyMsg.getBytes());
                    return;
                }

                byte[] currentByte = new byte[avail];
                in.read(currentByte);
                String currentContent = new String(currentByte);
                currentContent = currentContent.substring(0, offset) + insert + currentContent.substring(offset);

                out = new FileOutputStream(filePath, false);
                out.write(currentContent.getBytes());

                replyMsg = "Write to file successfully!";
                super.reply(replyMsg.getBytes());

            } catch (Exception e){

                replyMsg = "Error: Exception when writing";
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
