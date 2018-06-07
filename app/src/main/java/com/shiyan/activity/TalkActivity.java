package com.shiyan.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shiyan.dogdog.R;
import com.shiyan.nets.GlobalSocket;
import com.shiyan.nets.NetMessage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TalkActivity extends AppCompatActivity{

    Button button;
    RecyclerView recyclerView;
    EditText editText;
    MyAdapter myAdapter;
    List<NetMessage> list=new ArrayList<>();
    TalkingReceiver talkingReceiver;

    String talkObj;// 当前对话的对象

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        Toolbar toolbar=findViewById(R.id.talk_toolbar);
        setSupportActionBar(toolbar);

        talkObj=getIntent().getStringExtra("num");

        // 注册广播接收器
        IntentFilter filter=new IntentFilter();
        talkingReceiver=new TalkingReceiver();
        filter.addAction("new_message");
        registerReceiver(talkingReceiver,filter);

        button=findViewById(R.id.talk_send_button);
        recyclerView=findViewById(R.id.talk_recyclerview);
        editText=findViewById(R.id.talk_editText);

        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        myAdapter=new MyAdapter();
        recyclerView.setAdapter(myAdapter);

        button.setOnClickListener(v -> {
            String message=editText.getText().toString();
            if (message.length()>4000){
                Snackbar.make(v,"消息过长",Snackbar.LENGTH_LONG).setAction("确定",v1 -> {}).show();
                return;
            }
            new Thread(() -> {
                    GlobalSocket.ps.print(talkObj+"/"+message);
            }).start();
            list.add(new NetMessage(1,talkObj,message));
            myAdapter.notifyItemInserted(list.size()-1);
            recyclerView.scrollToPosition(list.size()-1);
            editText.setText("");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 卸载广播接收器
        unregisterReceiver(talkingReceiver);
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{

            LinearLayout leftLayout,rightLayout;
            TextView leftText,rightText;
            ViewHolder(View itemView) {
                super(itemView);
                leftLayout=itemView.findViewById(R.id.left_layout);
                rightLayout=itemView.findViewById(R.id.right_layout);
                leftText=itemView.findViewById(R.id.left_text);
                rightText=itemView.findViewById(R.id.right_text);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.talk_item_view,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NetMessage message=list.get(position);
            if (message.getType()==0){// 如果是接收的消息则显示在左边
                holder.leftLayout.setVisibility(View.VISIBLE);
                holder.rightLayout.setVisibility(View.GONE);
                holder.leftText.setText(message.getMessage());
            }else if (message.getType()==1){// 如果是发送的消息则显示在右边
                holder.rightLayout.setVisibility(View.VISIBLE);
                holder.leftLayout.setVisibility(View.GONE);
                holder.rightText.setText(message.getMessage());
            }

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

    }

    class TalkingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("new_message".equals(intent.getAction())){
                String msg=intent.getStringExtra("new");
                String[] ss=msg.split("/");
                // 如果消息不是当前对话对象发来的则不予理睬
                if (!ss[0].equals(talkObj)){
                    return;
                }
                NetMessage message=new NetMessage(0,ss[0],ss[1]);
                list.add(message);
                myAdapter.notifyItemInserted(list.size()-1);
                recyclerView.scrollToPosition(list.size()-1);
            }
        }
    }
}