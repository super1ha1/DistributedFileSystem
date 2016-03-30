package utils;


public class Utils {
    public static void echo(String s) {
        System.out.println(s);
    }

    public static String addRequestId(int requestId, String mes){
        return requestId + " " + "\"" + mes + "\"";
    }
}
