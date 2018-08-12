package com.shiyan.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Request {

    private String request;

    public Request(){}

    public Request(String request){
        this.request=request;
    }

    public void putType(String type) {
        request=type+"/";
    }

    public void putContent(String item) {
        request+=item+"/";
    }

    @Override
    public String toString() {
        return request;
    }

    public String sendRequest(){
        try {
            Socket socket=new Socket(GlobalSocket.SERVER_HOST,38380);
            PrintStream ps=new PrintStream(socket.getOutputStream());
            BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps.println(request);
            String result = br.readLine();
            br.close();
            ps.close();
            socket.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException"+e.toString();
        }
    }
}
