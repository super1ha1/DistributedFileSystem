package client;


import server.Const;
import utils.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UDPClientImpl {
    private static final boolean SIMULATE = false;
    private static BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
    private static final int WRONG_PORT = 3000;
    public static final int FIRST_ID = 0;

    private static Random randomGenerator = new Random();
    private static UDPClient client;
    private static String requestStr;
    private static boolean retransmit = false;
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private static int currentRequestId = FIRST_ID;

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
                        client.processCommand(requestStr, null);
                    }else {
                        client.setRetransmit(client.receiveReply());
                    }
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
