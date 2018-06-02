package com.shiyan.nets;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Request {

    private String request;

    public Request(){

    }

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
            Socket socket=new Socket(GlobalSocket.SERVER_HOST0,38380);
            PrintStream ps=new PrintStream(socket.getOutputStream());
            BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Log.e("注意",request);
            ps.println(request);
            String result = br.readLine();
            Log.e("注意",request+" : "+result);
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
