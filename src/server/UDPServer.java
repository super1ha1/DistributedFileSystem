package server;


import client.CallBack;
import utils.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {

    private ArrayList<RegisteredClient> cbList = new ArrayList<>();
    private String invocationSemantic;
    private DatagramSocket socket;
    private Map<String, Map<String, byte[]>> history = new HashMap<>();

    public UDPServer(DatagramSocket socket){
        this.socket = socket;
    }


    public ArrayList<RegisteredClient> getCbList() {
        return cbList;
    }

    public void setCbList(ArrayList<RegisteredClient> cbList) {
        this.cbList = cbList;
    }

    public void onFileChanged() throws Exception{
        for( RegisteredClient client: cbList){
            if(stillValid(client)){
                client.onCallBack();
            }else {
                client.onRemove();
                this.cbList.remove(client);
            }
        }
    }

    // valid = registerTime  + interval >= currentTime
    private boolean stillValid(RegisteredClient client) {
        return client.getRegisteredTime() + client.getNumSeconds() >= (System.currentTimeMillis()/1000);
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public Map<String, Map<String, byte[]>> getHistory() {
        return history;
    }

    public void setInvocationSemantic(String invocationSemantic) {
        this.invocationSemantic = invocationSemantic;
    }

    public  boolean isAtMostOne(){
        return this.invocationSemantic.equals(Const.SEMANTIC.AT_MOST_1);
    }


    public boolean isOldRequest(InetAddress address, int port, int requestId) {
        String uniqueClient = Utils.encodeAddressAndPortToKey(address, port);

        if(!history.keySet().contains(uniqueClient)){
            return  false;
        }

        Map<String, byte[]> requestMap = history.get(uniqueClient);
        if(!requestMap.keySet().contains(String.valueOf(requestId))){
            return false;
        }

        return  true;
    }

    public void sendOldReply(InetAddress address, int port, int requestId) throws Exception {
        String uniqueClient = Utils.encodeAddressAndPortToKey(address, port);
        Map<String, byte[]> requestMap = history.get(uniqueClient);
        byte[] response = requestMap.get(String.valueOf(requestId));

        Utils.echo("Get from history: " + uniqueClient + " request: " + requestId);

        DatagramPacket packet = new DatagramPacket(response, response.length, address, port);
        this.socket.send(packet);
    }

    public void recordAReply(InetAddress address, int port, int requestId, byte[] array) throws Exception {
        String uniqueClient = Utils.encodeAddressAndPortToKey(address, port);

        Map<String, byte[]> requestMap;
        if(this.history.keySet().contains(uniqueClient)){
            requestMap = this.history.get(uniqueClient);

        }else {
            requestMap = new HashMap<>();
            this.history.put(uniqueClient, requestMap);
        }

        requestMap.put(String.valueOf(requestId), array);
        Utils.echo("Stored to history: " + uniqueClient + " request: " + requestId);

    }

}
