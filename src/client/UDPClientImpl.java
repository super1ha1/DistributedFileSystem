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
        String requestStr, replyStr;
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        int requestIdInReply;
        String replyContent;
        try
        {
            sock = new DatagramSocket();
            InetAddress host = InetAddress.getByName("localhost");

            while(true)
            {

                try {
                    if(!client.isRegistered()){
                        //take input and send the packet
                        Utils.echo("Enter command or -1 to terminate : ");
                        requestStr =  cin.readLine();
                        if(requestStr.trim().equals("-1")){
                            break;
                        }
                        requestStr = client.getRequestId() + " " + requestStr;
                        byte[] b = requestStr.getBytes();
                        DatagramPacket dp = new DatagramPacket(b , b.length , host , port);
                        sock.send(dp);

                    }

                    //now receive reply
                    //buffer to receive incoming data
                    byte[] buffer = new byte[65536];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    sock.receive(reply);

                    byte[] data = reply.getData();

                    //Sample reply: 1 "this is the reply"
                    replyStr = new String(data, 0, reply.getLength());

                    String [] array = replyStr.trim().split("\"");
                    requestIdInReply = Integer.valueOf(array[0].trim());
                    replyContent = array[1].trim();


                    if(replyContent.equals(Const.MESSAGE.REGISTER_SUCCESS)){
                        client.setRegistered(true);
                    } else if(replyContent.equals(Const.MESSAGE.REGISTER_EXPIRE)) {
                        client.setRegistered(false);
                    }

                    if(requestIdInReply == client.getRequestId()){
                        client.increaseRequestId();
                    }

                    Utils.echo(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + requestIdInReply + " : " + replyContent);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
    }


}
