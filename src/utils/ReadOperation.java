package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadOperation extends Operation {

    public ReadOperation(DatagramSocket socket, DatagramPacket incoming){
        super(socket, incoming);
    }


    @Override
    public void process() throws Exception {
        String exceptionMsg;
        byte[] data = super.getIncoming().getData();
        String command = new String(data, 0, getIncoming().getLength());

        // 1 read "file.txt" 0 10
        String [] firstSplit = command.trim().split("\"");

        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 read
        String [] thirdSplit = firstSplit[2].trim().replaceAll("( )+", " ").split(" "); //split of:  0 10

        int requestId = Integer.valueOf(secondSplit[0].trim());
        String filePath = firstSplit[1];
        int offset = Integer.valueOf(thirdSplit[0].trim());
        int length = Integer.valueOf(thirdSplit[1].trim());

        if(Files.notExists(Paths.get(filePath))){
            exceptionMsg = Utils.addRequestId(requestId, "Error: file not exist");
            super.reply(exceptionMsg.getBytes());
        }else {
            FileInputStream in = null;
            try {
                in = new FileInputStream(filePath);
                int avail = in.available();
                if(offset >= avail){
                    exceptionMsg = Utils.addRequestId(requestId, "Error: offset larger than length: offset " + offset + " len: " + avail );
                    super.reply(exceptionMsg.getBytes());
                }
                byte[] output = new byte[length];
                for( int i = 0; i < offset; i++){
                    in.read();
                }

                int result = in.read(output, 0, length); //  read from input stream, fill in from index 0 to length of output
                super.reply(Utils.addRequestId(requestId, new String(output)).getBytes());

            }catch (IndexOutOfBoundsException indexException){
                exceptionMsg = Utils.addRequestId(requestId, "Error: IndexOutOfBoundsException");
                super.reply(exceptionMsg.getBytes());
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                if (in != null) {
                    in.close();
                }
            }
        }

    }
}
