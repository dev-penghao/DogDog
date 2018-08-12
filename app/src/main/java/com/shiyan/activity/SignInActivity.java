package com.shiyan.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.shiyan.dogdog.MainActivity;
import com.shiyan.dogdog.R;
import com.shiyan.tools.GlobalSocket;
import com.shiyan.tools.Me;
import com.shiyan.tools.MyInputStream;
import com.shiyan.tools.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class SignInActivity extends AppCompatActivity {

    Button sign_up,sign_in;
    EditText num,password;
    CheckBox checkBox;

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ("OK".equals(msg.obj.toString())){
                Intent intent=new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(SignInActivity.this,"出错："+msg.obj.toString(),Toast.LENGTH_LONG).show();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        sign_up=findViewById(R.id.sign_up);
        sign_in=findViewById(R.id.sign_in);
        num=findViewById(R.id.sign_in_user_num);
        password=findViewById(R.id.sign_in_user_password);
        checkBox=findViewById(R.id.sign_in_checkbox);

        SharedPreferences preferences=getSharedPreferences("login",MODE_PRIVATE);
        num.setText(preferences.getString("num",""));
        password.setText(preferences.getString("password",""));
        checkBox.setChecked(preferences.getBoolean("isChecked",false));

        sign_in.setOnClickListener(v -> new Thread(() -> {
            String result = "";
            try {
                GlobalSocket.socket=new Socket(GlobalSocket.SERVER_HOST,38380);
                GlobalSocket.ps=new PrintStream(GlobalSocket.socket.getOutputStream());
                GlobalSocket.mis=new MyInputStream(GlobalSocket.socket.getInputStream());
                String request="sign_in/"+num.getText().toString()+"/"+password.getText().toString()+"/";
                GlobalSocket.ps.println(request);
                result = GlobalSocket.mis.readLine();
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

        sign_up.setOnClickListener(v -> {
            Intent intent=new Intent(SignInActivity.this,SignUpActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor preferences=getSharedPreferences("login",MODE_PRIVATE).edit();
        if (checkBox.isChecked()){
            preferences.putString("num",num.getText().toString());
            preferences.putString("password",password.getText().toString());
            preferences.putBoolean("isChecked",true);
        } else {
            preferences.putBoolean("isChecked",false);
        }
        preferences.apply();
    }
}

