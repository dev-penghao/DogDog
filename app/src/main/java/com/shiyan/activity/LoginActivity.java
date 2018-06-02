package com.shiyan.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.shiyan.dogdog.MainActivity;
import com.shiyan.dogdog.R;
import com.shiyan.nets.GlobalSocket;
import com.shiyan.nets.Me;
import com.shiyan.nets.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    Button sign_up,sign_in;
    EditText num,password;
    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ("OK".equals(msg.obj.toString())){
                Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this,"出错："+msg.obj.toString(),Toast.LENGTH_LONG).show();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sign_up=findViewById(R.id.sign_up);
        sign_in=findViewById(R.id.sign_in);
        num=findViewById(R.id.user_num);
        password=findViewById(R.id.user_password);

        sign_in.setOnClickListener(v -> new Thread(() -> {
            String result = "";
            try {
                GlobalSocket.socket=new Socket(GlobalSocket.SERVER_HOST0,38380);
                GlobalSocket.ps=new PrintStream(GlobalSocket.socket.getOutputStream());
                GlobalSocket.br=new BufferedReader(new InputStreamReader(GlobalSocket.socket.getInputStream()));
                String request="sign_in/"+num.getText().toString()+"/"+password.getText().toString()+"/";
                GlobalSocket.ps.println(request);
                result = GlobalSocket.br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message msg=new Message();
            msg.obj=result;
            handler.sendMessage(msg);

            Request request1=new Request();
            request1.putType("find_user");
            request1.putContent(num.getText().toString());
            result=request1.sendRequest();
            Me.name=result;
            Me.num=num.getText().toString();
        }).start());
    }
}

