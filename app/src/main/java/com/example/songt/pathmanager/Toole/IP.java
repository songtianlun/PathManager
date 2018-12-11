package com.example.songt.pathmanager.Toole;

public class IP {
//    static String ip = "192.168.1.116";
    static String ip = "212.64.1.40";
    public static String getUrl(){
        return "http://" + ip + ":8080";
    }
    public static String getIp(){
        return ip;
    }
}
