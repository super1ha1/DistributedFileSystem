package server;


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
            //1. creating a server socket, parameter is local port number
            sock = new DatagramSocket(7777);

            //buffer to receive incoming data
            byte[] buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

            //2. Wait for an incoming data
            echo("Server socket created. Waiting for incoming data...");

            //communication loop
            while(true)
            {
                sock.receive(incoming);
                byte[] data = incoming.getData();
                String s = new String(data, 0, incoming.getLength());

                processCommand(sock, incoming, s);

            }
        }

        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
    }

    private static void processCommand(DatagramSocket sock, DatagramPacket incoming, String s) throws IOException{
        //echo the details of incoming data - client ip : client port - client message
        echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

        String [] array = s.trim().replaceAll("( )+", " ").split(" ");
        switch (array[0]){
            case Const.REQUEST_TYPE.READ:
                if(Files.exists(Paths.get(array[1]))){
                    //byte[] bytes = readFile(array);
                    echo("file exist");
                }else { //File not exist
                    String msg = "File not exist";
                    DatagramPacket dp = new DatagramPacket(msg.getBytes() , msg.getBytes().length , incoming.getAddress() , incoming.getPort());
                    sock.send(dp);
                }
                break;

            default:
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
