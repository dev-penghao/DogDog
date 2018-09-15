package com.shiyan.tools;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;

public class GlobalSocket{

    public static String SERVER_HOST="192.168.43.184";
//    public static String SERVER_HOST="114.116.50.200";
//    public static String SERVER_HOST="192.168.1.102";
//    public static String SERVER_HOST="192.168.1.103";
//    public static String SERVER_HOST="192.168.1.105";
//    public static String SERVER_HOST="192.168.1.106";
//    public static String SERVER_HOST="127.0.0.1";

    public static Socket socket;
    public static PrintStream ps;
    public static MyInputStream mis;
}