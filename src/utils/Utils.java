/*
* Utils helper class
*
* This holds a list of general methods that frequently used in the application for both client and server side
*
* */


package utils;


import java.net.InetAddress;

public class Utils {
    public static void echo(String s) {
        System.out.println(s);
    }

    public static String addRequestId(int requestId, String mes){
        return requestId + " " + "\"" + mes + "\"";
    }

    public static String encodeAddressAndPortToKey(InetAddress address, int port) {
        return address.toString() + ":" + port;
    }

    public static String composeRequest(int requestId, String requestStr) {
        return requestId + " " + requestStr;
    }
}
