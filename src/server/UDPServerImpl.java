package server;


import utils.*;

import javax.rmi.CORBA.Util;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServerImpl {

    private static final int SERVER_PORT = 7777;
    private static final int BUFFER_SIZE = 65536;
    private static  UDPServer udpServer;

    public static void main(String args[])
    {
        DatagramSocket sock = null;

        try
        {
            sock = new DatagramSocket(SERVER_PORT);
            udpServer = new UDPServer(sock);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

            Utils.echo("Server socket created. Waiting for incoming data...");

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

    private static void processCommand(DatagramSocket sock, DatagramPacket incoming) throws Exception{
        byte[] data = incoming.getData();
        String command = new String(data, 0, incoming.getLength());

        //echo the details of incoming data - client ip : client port - client message
        Utils.echo("Request: " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + command);

        // 1 read "file.txt" 0 10
        String [] firstSplit = command.trim().split("\"");
        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");
        int requestId = Integer.valueOf(secondSplit[0].trim());
        String action = secondSplit[1].trim();
        switch (action){
            case Const.REQUEST_TYPE.READ:
                ReadOperation readOperation = new ReadOperation(sock, incoming);
                readOperation.process();
                break;

            case Const.REQUEST_TYPE.WRITE:
                WriteOperation writeOperation = new WriteOperation(sock, incoming, udpServer);
                writeOperation.process();
                break;

            case Const.REQUEST_TYPE.REGISTER:
                RegisterOperation registerOperation = new RegisterOperation(sock, incoming, udpServer);
                registerOperation.process();
                break;

            case Const.REQUEST_TYPE.APPEND:
                AppendOperation appendOperation = new AppendOperation(sock, incoming, udpServer);
                appendOperation.process();
                break;

            case Const.REQUEST_TYPE.LAST_UPDATE:
                GetLastUpdateOperation getLastUpdateOperation = new GetLastUpdateOperation(sock, incoming);
                getLastUpdateOperation.process();
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
