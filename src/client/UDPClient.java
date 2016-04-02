package client;


import server.Const;
import utils.CacheCallBack;
import utils.Utils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UDPClient implements CallBack {

    private static final int SENTINEL = -1;
    public static final int FIRST_ID = 0;
    private static final boolean SIMULATE_RETRANSMIT = false;
    private static final int WRONG_PORT = 3000;


    private static Random randomGenerator = new Random();
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private Map<Integer, CacheCallBack> requestCallBackMap = new HashMap<>();

    private InetAddress host;
    private int port;
    private long cacheRefreshInterval;
    private long retransmitInterval;
    private DatagramSocket socket;
    private boolean registered;
    private int requestId;

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

    private boolean isReadOperation(String requestStr) {
        // 1 read "file" 0 10
        // 1 read_all "file" 0 10
        String [] firstSplit = requestStr.trim().split("\"");
        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");
        String action = secondSplit[1].trim();

        return action.equals(Const.REQUEST_TYPE.READ);
    }

    private boolean hasFileInCache(String filePath) {
        return cache.keySet().contains(filePath);
    }

    private void getDataFromCache(CacheEntry entry, int offset, int length) throws Exception {
        Utils.echo("Get data from cache: " + " data: " + new String(entry.getContent()) +
                " offset " + offset + " len: " + length);

        InputStream in = new ByteArrayInputStream(entry.getContent());

        int avail = in.available();
        if(offset >= avail ){
            Utils.echo("Request: " + requestId +  " Error: offset larger than length: offset " + offset + " len: " + avail);
            return;
        }

        byte[] output = new byte[length];
        for( int i = 0; i < offset; i++){
            in.read();
        }

        int result = in.read(output, 0, length); //  read from input stream, fill in from index 0 to length of output
        Utils.echo("Request: " + requestId + " Read from cache: " + new String(output));
    }

    private void fetchLatestUpdate(String filePath, int offset, int length) throws Exception{
        String request = composeGetLastUpdate( filePath);
        Utils.echo("request to fetch lastest time: " + request);
        CacheCallBack cacheCallBack = new CacheCallBack() {
            @Override
            public void onDataReceive(String data) throws Exception{
                Utils.echo("fetch lastest time success: " + data);
                CacheEntry entry =  cache.get(filePath);
                long newUpdateTime = Integer.valueOf(data.trim());
                entry.setServerModifyTime(newUpdateTime);
                processLatestUpdate(filePath, offset, length);
            }
        };
        processCommand(request, cacheCallBack);
    }

    private String composeReadAllRequest(String filePath) {
        // read_all "file"
        return Const.REQUEST_TYPE.READ_ALL_FILE + " " + "\"" + filePath + "\"";
    }

    private String composeGetLastUpdate(String filePath){
        //get_last_update "file"
        return Const.REQUEST_TYPE.LAST_UPDATE + " " + "\"" + filePath + "\"";
    }
    public  void sendACommand(int requestId, String requestStr) throws Exception {
        retransmit = true;
        final String composedRequestStr = Utils.composeRequest(requestId, requestStr);
        byte[] b = composedRequestStr.getBytes();
        DatagramPacket dp = new DatagramPacket(b , b.length , host , port);

        if(isReadOperation(composedRequestStr)){

            // 1 read "file" 0 10
            String [] firstSplit = requestStr.trim().split("\"");

            String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 read
            String [] thirdSplit = firstSplit[2].trim().replaceAll("( )+", " ").split(" "); //split of:  0 10

            String filePath = firstSplit[1];
            int offset = Integer.valueOf(thirdSplit[0].trim());
            int length = Integer.valueOf(thirdSplit[1].trim());
            Utils.echo("file Path: " + filePath + " offset: " + offset  + " length: " + length);

            if(hasFileInCache(filePath)){
                Utils.echo("has file in cache: ");
                CacheEntry entry = cache.get(filePath);
                if(entry.dataIsFresh(cacheRefreshInterval)){
                    Utils.echo("data is fresh: ");
                    getDataFromCache(entry, offset, length);
                }else {
                    Utils.echo("data is not fresh, fetch latest modify time");
                    fetchLatestUpdate(filePath, offset, length);
                }
            }else {
                Utils.echo("not in cache, download file: ");
                saveFileToCache(filePath, offset, length);
            }
            return;
        }

        if(isWriteOperation(composedRequestStr)){

            // 1 write "E:\IdeaProjects\codeforces\a.txt" 0 "z"
            String [] firstSplit = composedRequestStr.trim().split("\"");
            String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 write

            String filePath = firstSplit[1];
            int offset = Integer.valueOf(firstSplit[2].trim());
            String insert = firstSplit[3];

            Utils.echo("file Path: " + filePath + " offset: " + offset  + " content: " + insert);


            if(hasFileInCache(filePath)) {
                Utils.echo("has file in cache: ");
                CacheEntry entry = cache.get(filePath);

                InputStream in = null;
                try {

                    in = new ByteArrayInputStream(entry.getContent());

                    int avail = in.available();
                    if(offset >= avail){
                        Utils.echo(Utils.addRequestId(requestId, "Error: offset larger than length, offset: " + offset  + " len: " + avail));
                        return;
                    }

                    byte[] currentByte = new byte[avail];
                    in.read(currentByte);
                    String currentContent = new String(currentByte);
                    currentContent = currentContent.substring(0, offset) + insert + currentContent.substring(offset);

                    entry.setContent(currentContent.getBytes());

                    Utils.echo(Utils.addRequestId(requestId,  "Write to file successfully!"));

                    String request = composeWriteAllRequest(filePath, currentContent);
                    Utils.echo("request to write all files: " + request);
                    processCommand(request, null);

                } catch (Exception e){

                    Utils.echo(Utils.addRequestId(requestId, "Error: Exception when writing"));
                    e.printStackTrace();

                }
                finally {
                    if (in != null) {
                        in.close();
                    }
                }

                return;
            }
        }

        if(isAppendOperation(composedRequestStr)){

            // 1 append "file_path" "content"
            String [] firstSplit = composedRequestStr.trim().split("\"");
            String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");//Split of: 1 append

            String filePath = firstSplit[1];
            String insert = firstSplit[3];

            Utils.echo("file Path: " + filePath  + " content: " + insert);

            if(hasFileInCache(filePath)) {
                Utils.echo("has file in cache: ");
                CacheEntry entry = cache.get(filePath);
                try {

                    String currentContent = new String(entry.getContent());
                    currentContent = currentContent + insert;

                    entry.setContent(currentContent.getBytes());

                    Utils.echo(Utils.addRequestId(requestId,  "Append to file successfully!"));

                    String request = composeWriteAllRequest(filePath, currentContent);
                    Utils.echo("request to write all files: " + request);
                    processCommand(request, null);

                } catch (Exception e){

                    Utils.echo(Utils.addRequestId(requestId, "Error: Exception when writing"));
                    e.printStackTrace();

                }
                return;
            }
        }


        if(SIMULATE_RETRANSMIT){ //simulate to control failure when send
            int random = randomGenerator.nextInt(1000);
            Utils.echo("Random number: " + random + " request sent: " + (random % 2 == 0 ? " success " : " lost "));
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

       setRetransmit(receiveReply());

    }

    private boolean isWriteOperation(String composedRequestStr) {
        // 1 write "file" 0 "content"
        String [] firstSplit = composedRequestStr.trim().split("\"");
        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");
        String action = secondSplit[1].trim();

        return action.equals(Const.REQUEST_TYPE.WRITE);
    }


    private boolean isAppendOperation(String composedRequestStr) {
        // 1 append "E:\IdeaProjects\codeforces\in.txt" "z"
        String [] firstSplit = composedRequestStr.trim().split("\"");
        String [] secondSplit = firstSplit[0].trim().replaceAll("( )+", " ").split(" ");
        String action = secondSplit[1].trim();

        return action.equals(Const.REQUEST_TYPE.APPEND);
    }

    private String composeWriteAllRequest(String filePath, String newContent) {
        // write_all "file" "new_content"
        return Const.REQUEST_TYPE.WRITE_ALL_FILE
                + " " + "\"" + filePath + "\""
                + " " + "\"" + newContent + "\""  ;
    }

    private void saveFileToCache(String filePath, int offset, int length) throws Exception{
        String request = composeReadAllRequest(filePath);
        CacheCallBack cacheCallBack = new CacheCallBack() {
            @Override
            public void onDataReceive(String data) throws Exception{
                if(data.contains(Const.MESSAGE.ERROR)){
                    Utils.echo("Error occur, so do not save file content to cache!");
                    return;
                }
                CacheEntry cacheEntry = new CacheEntry(data.getBytes(), (System.currentTimeMillis()/1000),
                        (System.currentTimeMillis()/1000));
                //Default when download a new file, set last modify to current time
                cache.put(filePath,  cacheEntry);
                getDataFromCache(cacheEntry, offset, length);
            }
        };
        processCommand(request, cacheCallBack);
    }

    private void processLatestUpdate(String filePath, int offset,int length) throws Exception{
        Utils.echo("processLatestUpdate: " );

        CacheEntry entry = cache.get(filePath);
        if(entry.isValid()){
            Utils.echo("latest update is valid, get data from cache " );
            entry.setLastValidateTime(System.currentTimeMillis()/1000);
            getDataFromCache(entry, offset, length);
        }else {
            String request = composeReadAllRequest(filePath);
            Utils.echo("latest update is not valid, send: " + request );
            CacheCallBack cacheCallBack = new CacheCallBack() {
                @Override
                public void onDataReceive(String data) throws Exception{
                    Utils.echo("Get all file success: " + data );
                    CacheEntry cacheEntry = cache.get(filePath);
                    cacheEntry.setContent(data.getBytes());
                    cacheEntry.setLastValidateTime(System.currentTimeMillis()/1000);
                    cacheEntry.setLastModifiedTime(cacheEntry.getServerModifyTime());

                    getDataFromCache(entry, offset, length);
                }
            };
            processCommand(request, cacheCallBack);
        }
    }

    public  boolean receiveReply() throws Exception{
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

        if(requestCallBackMap.keySet().contains(requestIdInReply)){ //only when receive a reply with request id match

            CacheCallBack cacheCallBack = requestCallBackMap.get(requestIdInReply);
            if(cacheCallBack != null){
                cacheCallBack.onDataReceive(replyContent);
            }
            requestCallBackMap.remove(requestIdInReply);
            return false;
        }
        return false;

    }


    public void processCommand(String requestStr, CacheCallBack cacheCallBack) throws Exception {
        nextRequestId += 1;
        requestCallBackMap.put(nextRequestId, cacheCallBack);
        sendACommand(nextRequestId, requestStr);
    }

    public void setRetransmit(boolean retransmit) {
        this.retransmit = retransmit;
    }

    public int getNextRequestId() {
        return nextRequestId;
    }
}
