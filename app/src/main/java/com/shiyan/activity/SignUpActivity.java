package com.shiyan.activity;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.shiyan.dogdog.R;

import im.penghao.sdk.IMClient;


/*
 *  Created by Penghao on 2018.06.06
 */
public class SignUpActivity extends AppCompatActivity {

    EditText name, num, password, password_again;
    Button sign_up;
    ScrollView from;

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ("sign_up success".equals(msg.obj)){
                Toast.makeText(SignUpActivity.this,"注册成功",Toast.LENGTH_LONG).show();
                finish();
            } else {
                Snackbar.make(from,"注册失败："+msg.obj,Snackbar.LENGTH_LONG).setAction("确定",v -> {}).show();
            }
        }
    };

    /*
     *  Created by Penghao on 2018.06.06
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initView();
    }

    /*
     *  Created by Penghao on 2018.06.06
     */
    private void initView(){
        name=findViewById(R.id.sign_up_name);
        num=findViewById(R.id.sign_up_num);
        password=findViewById(R.id.sign_up_password);
        password_again=findViewById(R.id.sign_up_password_again);
        sign_up=findViewById(R.id.sign_up_button);
        from=findViewById(R.id.sign_up_form);

        sign_up.setOnClickListener(v -> {
            String name=this.name.getText().toString(), num=this.num.getText().toString(), password=this.password.getText().toString(), password_again=this.password_again.getText().toString();
            analysis(name,num,password,password_again);
        });
    }

    /*
     *  Created by Penghao on 2018.06.06
     */
    private void analysis(String name,String num,String password,String password_again) {
        char[] temp=(name+num+password).toCharArray();
        for (char aTemp : temp) {
            if (aTemp == '/') {
                Snackbar.make(from, "任何中都不允许出现字符“/”", Snackbar.LENGTH_LONG).setAction("确定", v -> {
                }).show();
                return;
            }
        }
        if (name.length()<21 && num.length()<11 && password.length()<17){
            if (!password.equals(password_again)){
                Snackbar.make(from,"密码不一致！",Snackbar.LENGTH_LONG).show();
                return;
            }
            new Thread(() -> {
                Message msg=new Message();
                msg.obj=IMClient.service.register(name, num, password);;
                handler.sendMessage(msg);
            }).start();
        }
    }
}