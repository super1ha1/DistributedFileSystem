package client;


import server.Const;
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
        boolean register = false;
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));

        try
        {
            sock = new DatagramSocket();

            InetAddress host = InetAddress.getByName("localhost");

            while(true)
            {
                if(!register){
                    //take input and send the packet
                    Utils.echo("Enter command or -1 to terminate : ");
                    s = (String)cin.readLine();
                    if(s.trim().equals("-1")){
                        break;
                    }
                    byte[] b = s.getBytes();
                    DatagramPacket dp = new DatagramPacket(b , b.length , host , port);
                    sock.send(dp);

                    if(s.contains("register")){
                        register = true;
                    }
                }


                //now receive reply
                //buffer to receive incoming data
                byte[] buffer = new byte[65536];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                sock.receive(reply);

                byte[] data = reply.getData();
                s = new String(data, 0, reply.getLength());


                if(s.contains(Const.REQUEST_TYPE.CALLBACK)){
                    String content  = s.substring(s.indexOf(Const.REQUEST_TYPE.CALLBACK));
                    client.onWrite(content.getBytes());
                }else {
                    //echo the details of incoming data - client ip : client port - client message
                    Utils.echo(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + s);
                }

            }
        }

        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
    }

}
