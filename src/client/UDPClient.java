package client;


import server.Const;
import utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient implements CallBack {

    @Override
    public void onWrite(byte[] newContent) {
        Utils.echo(new String(newContent));
    }
}
