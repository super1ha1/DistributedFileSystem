package client;


import server.Const;
import utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UDPClient implements CallBack {

    private static final int SENTINEL = -1;
    public static final int FIRST_ID = 0;
    private static final boolean SIMULATE = false;
    private static final int WRONG_PORT = 3000;


    private static Random randomGenerator = new Random();
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private InetAddress host;
    private int port;
    private long cacheRefreshInterval;
    private long retransmitInterval;
    private DatagramSocket socket;
    private boolean registered;
    private int requestId;

    private String filePath;
    private int offset, length;
    private Map<String, CacheEntry> cache = new HashMap<>();
    private boolean retransmit;
    private int nextRequestId = FIRST_ID;

    public UDPClient(InetAddress host, int port, long cacheRefreshInterval, long retransmitInterval) throws Exception {
        this.retransmitInterval = retransmitInterval;
        this.cacheRefreshInterval = cacheRefreshInterval;
        this.port = port;
        this.host = host;
        this.socket = new DatagramSocket();

        this.requestId = FIRST_ID;
        this.registered = false;

    }

    public UDPClient() {
        this.registered = false;
        this.requestId = FIRST_ID;
    }

    @Override
    public void onWrite(byte[] newContent) {
        Utils.echo(new String(newContent));
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public void increaseRequestId() {
        this.requestId++;
    }

    public InetAddress getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public long getCacheRefreshInterval() {
        return cacheRefreshInterval;
    }

    public long getRetransmitInterval() {
        return retransmitInterval;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public boolean isReadOperation(String requestStr) {
        // 1 read "file" 0 10
        // 1 read_all "file" 0 10
        String [] firstSplit = requestStr.trim().split("\"");
        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");
        String action = secondSplit[1].trim();

        return action.equals(Const.REQUEST_TYPE.READ);
    }

    public void setFilePathOffsetAndLength(String requestStr) {
        // 1 read "file" 0 10
        String [] firstSplit = requestStr.trim().split("\"");

        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 read
        String [] thirdSplit = firstSplit[2].trim().replaceAll("( )+", " ").split(" "); //split of:  0 10

        int requestId = Integer.valueOf(secondSplit[0].trim());
        this.filePath = firstSplit[1];
        this.offset = Integer.valueOf(thirdSplit[0].trim());
        this.length = Integer.valueOf(thirdSplit[1].trim());
    }

    public boolean hasFileInCache() {
        return this.cache.keySet().contains(filePath);
    }

    public boolean dataIsFresh() {
        CacheEntry entry = cache.get(filePath);
        return (System.currentTimeMillis()/1000) - entry.getLastValidateTime() < cacheRefreshInterval;
    }

    public void showData() throws Exception {
        CacheEntry entry = cache.get(filePath);
        InputStream in = new ByteArrayInputStream(entry.getContent());

        int avail = in.available();
        if(offset >= avail ){
            Utils.echo("Request: " + requestId +  "Error: offset larger than length: offset " + offset + " len: " + avail);
            return;
        }

        byte[] output = new byte[length];
        for( int i = 0; i < offset; i++){
            in.read();
        }

        int result = in.read(output, 0, length); //  read from input stream, fill in from index 0 to length of output
        Utils.echo("Request: " + requestId + "Read from cache: " + new String(output));
    }

    public void fetchLatestUpdate() {
        String request = composeReadAllRequest();

    }

    private String composeReadAllRequest() {
        // read_all "file"
        return Const.REQUEST_TYPE.READ_ALL_FILE + " " + "\"" + filePath + "\"";
    }

    public  void sendACommand(int requestId, String requestStr) throws Exception {
        retransmit = true;
        final String composedRequestStr = Utils.composeRequest(requestId, requestStr);
        byte[] b = composedRequestStr.getBytes();
        DatagramPacket dp = new DatagramPacket(b , b.length , host , port);

//        if(client.isReadOperation(composedRequestStr)){
//            client.setFilePathOffsetAndLength(composedRequestStr);
//            if(client.hasFileInCache()){
//                if(client.dataIsFresh()){
//                    client.showData();
//                }else {
//                    client.fetchLatestUpdate();
//                    client.processLatestUpdate();
//                }
//                return;
//            }
//        }

        if(SIMULATE){ //simulate to control failure when send
            int random = randomGenerator.nextInt(1000);
            Utils.echo("Random number: " + random);

            // Use a random number to control the result of request
            if(random % 2 == 0){
                socket.send(dp); //send to correct port
            }else {
                dp = new DatagramPacket(b, b.length,host , WRONG_PORT);
                socket.send(dp); // send to wrong port
            }
        }else {
            socket.send(dp);
        }

        service.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.echo("Hello, in runnable here!, retransmit: " + retransmit);
                    if(retransmit){
                        Utils.echo("resend requestId: " + requestId);
                        sendACommand(requestId, requestStr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, retransmitInterval, TimeUnit.SECONDS);
    }

    public   boolean receiveReply(int requestId) throws Exception{
        byte[] buffer = new byte[65536];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        socket.receive(reply);

        byte[] data = reply.getData();

        //Sample reply: 1 "this is the reply"
        String replyStr = new String(data, 0, reply.getLength());

        String [] array = replyStr.trim().split("\"");
        int requestIdInReply = Integer.valueOf(array[0].trim());
        String replyContent = array[1].trim();

        // Update registered
        if(replyContent.equals(Const.MESSAGE.REGISTER_SUCCESS)){
            setRegistered(true);
        } else if(replyContent.equals(Const.MESSAGE.REGISTER_EXPIRE)) {
            setRegistered(false);
        }

        Utils.echo(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + requestIdInReply + " : " + replyContent);

        if(requestIdInReply == requestId){ //only when receive a reply with request id match
            return false;
        }else {
            Utils.echo("requestId " +  requestId + " does not match with requestID in reply " + requestIdInReply);
            return true;
        }

    }


    public void processCommand(String requestStr) throws Exception {
        nextRequestId += 1;
        sendACommand(nextRequestId, requestStr);
        retransmit = receiveReply(nextRequestId);
    }
}
