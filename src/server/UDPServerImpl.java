package server;


import utils.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServerImpl {

    private static final int SERVER_PORT = 7777;
    private static final int BUFFER_SIZE = 65536;
    private static  UDPServer udpServer;
    private static BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String args[])
    {
        DatagramSocket sock = null;
        try
        {
            sock = new DatagramSocket(SERVER_PORT);
            udpServer = new UDPServer(sock);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

           startServer();

            //communication loop
            while(true)
            {
                try {
                    sock.receive(incoming);
                    processCommand(sock, incoming);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        catch(Exception e)
        {
            System.err.println("IOException " + e);
            e.printStackTrace();
        }
    }

    private static void startServer() throws Exception {
        Utils.echo("Please start the server using command: start invocation_semantic");
        Utils.echo("invocation_semantic can be either at_least_1 or at_most_1");
        Utils.echo("Enter command: ");
        String command = cin.readLine();

        String [] firstSplit = command.trim().replaceAll("( )+", " ").split(" "); //split start at_least_1
        String semantic = firstSplit[1].trim();
        udpServer.setInvocationSemantic(semantic);

        Utils.echo("Server socket created with semantic " + semantic + ". Waiting for incoming data...");
    }

    private static void processCommand(DatagramSocket sock, DatagramPacket incoming) throws Exception{
        byte[] data = incoming.getData();
        String command = new String(data, 0, incoming.getLength());

        //echo the details of incoming data - client ip : client port - client message
        Utils.echo("Request: " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + command);

        // 1 read "file.txt" 0 10
        String [] firstSplit = command.trim().split("\"");
        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");
        int requestId = Integer.valueOf(secondSplit[0].trim());


        if(udpServer.isAtMostOne() && udpServer.isOldRequest(incoming.getAddress(), incoming.getPort(), requestId)){
            udpServer.sendOldReply(incoming.getAddress(), incoming.getPort(), requestId);
            return;
        }

        String action = secondSplit[1].trim();
        switch (action){
            case Const.REQUEST_TYPE.READ:
                ReadOperation readOperation = new ReadOperation(sock, incoming, udpServer, requestId);
                readOperation.process();
                break;

            case Const.REQUEST_TYPE.WRITE:
                WriteOperation writeOperation = new WriteOperation(sock, incoming, udpServer, requestId);
                writeOperation.process();
                break;

            case Const.REQUEST_TYPE.REGISTER:
                RegisterOperation registerOperation = new RegisterOperation(sock, incoming, udpServer, requestId);
                registerOperation.process();
                break;

            case Const.REQUEST_TYPE.APPEND:
                AppendOperation appendOperation = new AppendOperation(sock, incoming, udpServer, requestId);
                appendOperation.process();
                break;

            case Const.REQUEST_TYPE.LAST_UPDATE:
                GetLastUpdateOperation getLastUpdateOperation = new GetLastUpdateOperation(sock, incoming, udpServer,  requestId);
                getLastUpdateOperation.process();
                break;

            case Const.REQUEST_TYPE.READ_ALL_FILE:
                ReadAllFileOperation readAllFileOperation = new ReadAllFileOperation(sock, incoming, udpServer, requestId);
                readAllFileOperation.process();
                break;

            default:
                String errorMsg = Utils.addRequestId(requestId, "Error: command is not recognized");
                DatagramPacket packet = new DatagramPacket(errorMsg.getBytes(), errorMsg.getBytes().length,
                        incoming.getAddress(), incoming.getPort());
                sock.send(packet);
                break;
        }
    }

}
