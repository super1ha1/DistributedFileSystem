package server;


import utils.ReadOperation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UDPServer {

    public static void main(String args[])
    {
        DatagramSocket sock = null;

        try
        {
            sock = new DatagramSocket(7777);

            byte[] buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

            echo("Server socket created. Waiting for incoming data...");

            //communication loop
            while(true)
            {
                sock.receive(incoming);
                processCommand(sock, incoming);

            }
        }

        catch(Exception e)
        {
            System.err.println("IOException " + e);
        }
    }

    private static void processCommand(DatagramSocket sock, DatagramPacket incoming) throws Exception{
        byte[] data = incoming.getData();
        String command = new String(data, 0, incoming.getLength());

        //echo the details of incoming data - client ip : client port - client message
        echo("Request: " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + command);

        String [] firstSplit = command.trim().split("\"");
        String action = firstSplit[0].trim();
        switch (action){
            case Const.REQUEST_TYPE.READ:
                ReadOperation readOperation = new ReadOperation(sock, incoming);
                readOperation.process();
                break;

            default:
                String errorMsg = "Error: command is not recognized";
                DatagramPacket packet = new DatagramPacket(errorMsg.getBytes(), errorMsg.getBytes().length,
                        incoming.getAddress(), incoming.getPort());
                sock.send(packet);
                break;
        }
    }

    private static byte[] readFile(String[] array) {
        try {
            FileInputStream in = null;
            FileOutputStream out = null;

            try {
                in = new FileInputStream("xanadu.txt");
                out = new FileOutputStream("outagain.txt");
                int c;

                while ((c = in.read()) != -1) {
                    out.write(c);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new byte[0];
    }


    //simple function to echo data to terminal
    public static void echo(String msg)
    {
        System.out.println(msg);
    }
}
