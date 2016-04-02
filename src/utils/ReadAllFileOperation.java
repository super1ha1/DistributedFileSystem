package utils;


import server.UDPServer;

import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadAllFileOperation extends Operation{
    public ReadAllFileOperation(DatagramSocket socket, DatagramPacket incoming, UDPServer server, int requestId){
        super(socket, incoming, server, requestId);
    }


    @Override
    public void process() throws Exception {
        String exceptionMsg;
        byte[] data = super.getIncoming().getData();
        String command = new String(data, 0, getIncoming().getLength());

        // 1 read_all "file.txt"
        String [] firstSplit = command.trim().split("\"");

        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 read_all

        int requestId = Integer.valueOf(secondSplit[0].trim());
        String filePath = firstSplit[1];

        if(Files.notExists(Paths.get(filePath))){
            exceptionMsg = Utils.addRequestId(requestId, "Error: file not exist");
            super.reply(exceptionMsg.getBytes());
        }else {
            FileInputStream in = null;
            try {
                in = new FileInputStream(filePath);
                int avail = in.available();
                byte[] output = new byte[avail];
                
                int result = in.read(output, 0, avail); //  read all from input stream,
                super.reply(Utils.addRequestId(requestId, new String(output)).getBytes());

            }catch (IndexOutOfBoundsException indexException){
                indexException.printStackTrace();
                exceptionMsg = Utils.addRequestId(requestId, "Error: IndexOutOfBoundsException");
                super.reply(exceptionMsg.getBytes());
            }
            catch (Exception e){
                e.printStackTrace();
                exceptionMsg = Utils.addRequestId(requestId, "Error: Exception");
                super.reply(exceptionMsg.getBytes());
            }
            finally {
                if (in != null) {
                    in.close();
                }
            }
        }

    }
}
