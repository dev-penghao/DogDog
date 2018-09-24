package com.shiyan.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.shiyan.tools.GlobalSocket;
import com.shiyan.tools.Me;
import com.shiyan.tools.Message;
import com.shiyan.tools.MyDatabaseHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.shiyan.tools.Me.msgNow;
import static com.shiyan.tools.Me.num;

public class TalkActivity extends AppCompatActivity{

    Button button;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefresh;
    EditText editText;
    MyAdapter myAdapter;
    List<Message> list=new ArrayList<>();
    TalkingReceiver talkingReceiver;

    String talkObj;// 当前对话的对象
    final int MESSAGE_BYTE_MAX_LENGTH=1024;

    @SuppressLint("Recycle")
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
        swipeRefresh=findViewById(R.id.talk_swipeRefresh);

        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        myAdapter=new MyAdapter();
        recyclerView.setAdapter(myAdapter);

        button.setOnClickListener(v -> {
            // 创建消息
            Message msg=new Message();
            msg.setFrom(Me.num);
            msg.setTo(talkObj);
            msg.setMsgSize(0);
            msg.setType(0);
            msg.setWhen(System.currentTimeMillis());
            msg.setTextContent(editText.getText().toString());
            byte[] bytes=msg.toString().getBytes(Charset.forName("UTF-8"));
            // 消息过长则不予发送
            if (bytes.length>MESSAGE_BYTE_MAX_LENGTH){
                Snackbar.make(v,"消息过长",Snackbar.LENGTH_LONG).setAction("确定",v1 -> {}).show();
                return;
            }
            // 开始发送消息
            new Thread(() -> {
                try {
                    GlobalSocket.ps.write(bytes);
                    GlobalSocket.ps.write((new byte[1])[0]=0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            // 更新本界面
            list.add(msg);
            myAdapter.notifyItemInserted(list.size()-1);
            recyclerView.scrollToPosition(list.size()-1);
            editText.setText("");
            // 更新NewsFragment界面
            Me.msgNow=msg;
            this.sendBroadcast(new Intent().setAction("new_message"));
            // 将本条消息存入数据库中
            MyDatabaseHelper myDBHelper=new MyDatabaseHelper(this,"MsgLibs.db",null,1);
            SQLiteDatabase db=myDBHelper.getWritableDatabase();
            ContentValues values=new ContentValues();
            values.put("msg_from",msgNow.getFrom());
            values.put("msg_to",msgNow.getTo());
            values.put("msg_when",msgNow.getWhen());
            values.put("msgSize",msgNow.getMsgSize());
            values.put("type",msgNow.getType());
            values.put("textContent",msgNow.getTextContent());
            try{
                db.insert(msgNow.getTo(),null,values);
            } catch (SQLException e){
                e.printStackTrace();
            }
            db.close();
        });

        swipeRefresh.setOnRefreshListener(() -> {
            MyDatabaseHelper sqlHelper=new MyDatabaseHelper(TalkActivity.this,"MsgLibs.db",null,1);
            SQLiteDatabase db=sqlHelper.getReadableDatabase();
            Cursor cursor=db.query(talkObj,null,"msg_when="+list.get(0).getWhen(),null,null,null,null);
            if (cursor.moveToFirst()){
                String when=String.valueOf(cursor.getLong(cursor.getColumnIndex("msg_when")));
                cursor=db.query(talkObj, null,"msg_when<" + when,null,null,null,null,"10");
                if (cursor.moveToFirst()){
                    do {
                        Message message=new Message();
                        message.setFrom(cursor.getString(cursor.getColumnIndex("msg_from")));
                        message.setTo(cursor.getString(cursor.getColumnIndex("msg_to")));
                        message.setWhen(cursor.getLong(cursor.getColumnIndex("msg_when")));
                        message.setMsgSize(cursor.getLong(cursor.getColumnIndex("msgSize")));
                        message.setType(cursor.getInt(cursor.getColumnIndex("type")));
                        message.setTextContent(cursor.getString(cursor.getColumnIndex("textContent")));
                        list.add(0,message);
                    }while (cursor.moveToNext());
                    myAdapter.notifyItemInserted(0);
                }
            } else {

            }
            cursor.close();
            swipeRefresh.setRefreshing(false);
        });

        //SELECT * FROM table_name order by user_id（字段） DESC limit 10;
        MyDatabaseHelper sqlHelper=new MyDatabaseHelper(this,"MsgLibs.db",null,1,talkObj);
        SQLiteDatabase db=sqlHelper.getReadableDatabase();
        Cursor cursor=db.query(talkObj,null,null,null,null,null,"id desc","10");
        if(cursor.moveToLast()){
            do {
                Message message=new Message();
                message.setFrom(cursor.getString(cursor.getColumnIndex("msg_from")));
                message.setTo(cursor.getString(cursor.getColumnIndex("msg_to")));
                message.setWhen(cursor.getLong(cursor.getColumnIndex("msg_when")));
                message.setMsgSize(cursor.getLong(cursor.getColumnIndex("msgSize")));
                message.setType(cursor.getInt(cursor.getColumnIndex("type")));
                message.setTextContent(cursor.getString(cursor.getColumnIndex("textContent")));
                list.add(message);
//                message.setFrom();
//                message.setTo();
//                message.setWhen();
//                message.setMsgSize();
//                message.setType();
//                message.setTextContent();
            }while (cursor.moveToPrevious());
        }
        cursor.close();
        for (int i=0;i<list.size();i++){
            myAdapter.notifyItemInserted(list.size()-1);
        }
        recyclerView.scrollToPosition(list.size()-1);
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
            Message message=list.get(position);
            if (message.getFrom().equals(talkObj)){// 如果是接收的消息则显示在左边
                holder.leftLayout.setVisibility(View.VISIBLE);
                holder.rightLayout.setVisibility(View.GONE);
                holder.leftText.setText(message.getTextContent());
            }else if (message.getFrom().equals(Me.num)){// 如果是发送的消息则显示在右边
                holder.rightLayout.setVisibility(View.VISIBLE);
                holder.leftLayout.setVisibility(View.GONE);
                holder.rightText.setText(message.getTextContent());
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

                // 如果消息不是当前对话对象发来的则不予理睬
                if (!msgNow.getFrom().equals(talkObj)){
                    return;
                }
                list.add(msgNow);
                myAdapter.notifyItemInserted(list.size()-1);
                recyclerView.scrollToPosition(list.size()-1);
            }
        }
    }
}