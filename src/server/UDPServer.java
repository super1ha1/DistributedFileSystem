package server;


import client.CallBack;
import utils.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
}
