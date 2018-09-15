package com.shiyan.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    Button sign_up,sign_in,connectPoint;
    EditText num,password;
    CheckBox checkBox;

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ("OK".equals(msg.obj.toString())){
                Intent intent=new Intent();
                intent.putExtra("result","OK");
                setResult(RESULT_OK,intent);
                finish();
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
        connectPoint=findViewById(R.id.sign_in_connect_point);

        sign_in.setOnClickListener(this);
        sign_up.setOnClickListener(this);
        connectPoint.setOnClickListener(this);

        SharedPreferences preferences=getSharedPreferences("main",MODE_PRIVATE);
        num.setText(preferences.getString("num",""));
        password.setText(preferences.getString("password",""));
        checkBox.setChecked(preferences.getBoolean("isChecked",false));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in:
                new Thread(() -> {
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
                    Me.dataPath="/sdcard/DogDog/"+Me.num;
                }).start();
                break;
            case R.id.sign_up:
                Intent intent=new Intent(SignInActivity.this,SignUpActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_in_connect_point:
                final EditText editText=new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(SignInActivity.this)
                        .setIcon(R.mipmap.ic_launcher_round)//设置标题的图片
                        .setTitle("设置接入点")//设置对话框的标题
                        .setView(editText)
                        .setNegativeButton("取消", (dialog1, which) -> dialog1.dismiss())
                        .setPositiveButton("确定", (dialog1, which) -> {
                            GlobalSocket.SERVER_HOST = editText.getText().toString();
                            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor preferences=getSharedPreferences("main",MODE_PRIVATE).edit();
                            preferences.putString("SERVER_HOST",editText.getText().toString());
                            preferences.apply();
                        }).show();
                break;
                default:break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor preferences=getSharedPreferences("main",MODE_PRIVATE).edit();
        if (checkBox.isChecked()){
            preferences.putString("num",num.getText().toString());
            preferences.putString("password",password.getText().toString());
            preferences.putBoolean("isChecked",true);
        } else {
            preferences.putBoolean("isChecked",false);
        }
        preferences.putBoolean("isLogined",true);
        preferences.apply();
    }
}

