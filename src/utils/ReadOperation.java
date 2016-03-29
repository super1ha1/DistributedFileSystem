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

    private DatagramPacket reply;
    private InetAddress address;
    private int port;
    private String request;

    private String filePath;
    private int offset, length;

    public ReadOperation(DatagramSocket socket, DatagramPacket incoming){
        super(socket, incoming);
    }


    @Override
    public void process() throws Exception {
        String exceptionMsg;
        byte[] data = super.getIncoming().getData();
        String command = new String(data, 0, getIncoming().getLength());

        String [] arrayStr = command.trim().split("\"");

        String filePath = arrayStr[1];
        int offset = Integer.valueOf(arrayStr[2].trim().replaceAll("( )+", " ").split(" ")[0]);
        int length = Integer.valueOf(arrayStr[2].trim().replaceAll("( )+", " ").split(" ")[1]);

        if(Files.notExists(Paths.get(filePath))){
            exceptionMsg = "Error: file not exist";
            super.reply(exceptionMsg.getBytes());
        }else {
            FileInputStream in = null;
            try {
                in = new FileInputStream(filePath);
                int avail = in.available();
                if(offset >= avail){
                    exceptionMsg = "Error: offset larger than length";
                    super.reply(exceptionMsg.getBytes());
                }
                byte[] output = new byte[length];
                int result = in.read(output, offset, length);
                super.reply(output);

            }catch (IndexOutOfBoundsException indexException){
                exceptionMsg = "Error: IndexOutOfBoundsException";
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
