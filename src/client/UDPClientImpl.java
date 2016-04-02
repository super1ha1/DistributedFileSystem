package client;


import server.Const;
import utils.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

public class UDPClientImpl {
    private static BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
    private static final boolean SIMULATE = true;
    private static final int WRONG_PORT = 3000;
    private static Random randomGenerator = new Random();
    private static UDPClient client;
    private static String requestStr, replyStr,replyContent;
    private static int requestIdInReply;
    private static boolean retransmit = false;
    private  static  byte[] buffer;
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    public static void main(String args[])
    {
        randomGenerator.setSeed(1);
        try
        {
            client = startClient();

            while(true)
            {

                try {
                    if(!client.isRegistered()){
                        Utils.echo("Enter command or -1 to terminate : ");
                        requestStr =  cin.readLine();
                        if(requestStr.trim().equals("-1")){
                            break;
                        }
                        requestStr = client.getRequestId() + " " + requestStr;
                        sendACommand(requestStr);
                    }

                    retransmit = receiveReply();

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        catch(Exception e)
        {
            System.err.println("Exception " + e);
            e.printStackTrace();
        }
    }

    private static boolean receiveReply() throws Exception{
        buffer = new byte[65536];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        client.getSocket().receive(reply);

        byte[] data = reply.getData();

        //Sample reply: 1 "this is the reply"
        replyStr = new String(data, 0, reply.getLength());

        String [] array = replyStr.trim().split("\"");
        requestIdInReply = Integer.valueOf(array[0].trim());
        replyContent = array[1].trim();

        // Update registered
        if(replyContent.equals(Const.MESSAGE.REGISTER_SUCCESS)){
            client.setRegistered(true);
        } else if(replyContent.equals(Const.MESSAGE.REGISTER_EXPIRE)) {
            client.setRegistered(false);
        }

        Utils.echo(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + requestIdInReply + " : " + replyContent);

        if(requestIdInReply == client.getRequestId()){ //only when receive a reply with request id match
            client.increaseRequestId();
            return false;
        }else {
            Utils.echo("requestId " +  client.getRequestId() + " does not match with requestID in reply " + requestIdInReply);
            return true;
        }

    }

    private static void sendACommand(String requestStr) throws Exception {
        retransmit = true;
        byte[] b = requestStr.getBytes();
        DatagramPacket dp = new DatagramPacket(b , b.length , client.getHost() , client.getPort());
        if(SIMULATE){ //simulate to control failure when send
            int random = randomGenerator.nextInt(1000);
            Utils.echo("Random number: " + random);

            // Use a random number to control the result of request
            if(random % 2 == 0){
                client.getSocket().send(dp); //send to correct port
            }else {
                dp = new DatagramPacket(b,b.length, client.getHost(), WRONG_PORT);
                client.getSocket().send(dp); // send to wrong port
            }
        }else {
            client.getSocket().send(dp);
        }

        final String finalRequestStr = requestStr;
        service.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.echo("Hello, in runnable here!, retransmit: " + retransmit);
                    if(retransmit){
                        Utils.echo("resend requestId: " + client.getRequestId());
                        sendACommand(finalRequestStr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, client.getRetransmitInterval(), TimeUnit.SECONDS);
    }

    private static UDPClient startClient() throws Exception {
        Utils.echo("Please start the client using command: start server_ip port cache_interval retransmit_interval");
        Utils.echo("Ex: start 10.20.4.120 7777 60 2");
        Utils.echo("Enter command: ");
        String command = cin.readLine();

        String [] firstSplit = command.trim().replaceAll("( )+", " ").split(" "); //split start 10.20.4.120 7777 60 2
        String serverIP = firstSplit[1].trim();
        int port = Integer.valueOf(firstSplit[2].trim());
        long cacheInterval = Integer.valueOf(firstSplit[3].trim());
        long transmitInterval = Integer.valueOf(firstSplit[4].trim());

        UDPClient client = new UDPClient(InetAddress.getByName(serverIP), port, cacheInterval, transmitInterval);
        Utils.echo("Client has been started successfully!");
        return client;
    }


}
