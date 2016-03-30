package client;


import server.Const;
import sun.misc.CEFormatException;
import utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClientImpl {


    public static void main(String args[])
    {
        UDPClient client = new UDPClient();
        DatagramSocket sock = null;
        int port = 7777;
        String s;
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        int registerInterval = -1;
        try
        {
            sock = new DatagramSocket();

            InetAddress host = InetAddress.getByName("localhost");

            while(true)
            {
                if(!client.isRegistered()){
                    //take input and send the packet
                    Utils.echo("Enter command or -1 to terminate : ");
                    s = (String)cin.readLine();
                    if(s.trim().equals("-1")){
                        break;
                    }
                    byte[] b = s.getBytes();
                    DatagramPacket dp = new DatagramPacket(b , b.length , host , port);
                    sock.send(dp);

                    // register "file_path" 100
                    String [] arrayStr = s.trim().split("\"");

                    String commandCode = arrayStr[0].trim().replaceAll("( )+", " ");
                    if(commandCode.equals("register")){
                        registerInterval = Integer.valueOf(arrayStr[2].trim().replaceAll("( )+", " "));
                    }
                }

                //now receive reply
                //buffer to receive incoming data
                byte[] buffer = new byte[65536];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                sock.receive(reply);

                byte[] data = reply.getData();
                s = new String(data, 0, reply.getLength());

                if(s.equals(Const.MESSAGE.REGISTER_SUCCESS)){
                    client.setRegistered(true);
                }

                if(s.equals(Const.MESSAGE.REGISTER_EXPIRE)){
                    client.setRegistered(false);
                }

                Utils.echo(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + s);

            }
        }

        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
    }

}
