package com.shiyan.activity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

import im.penghao.sdk.IMClient;
import im.penghao.sdk.Message;
import im.penghao.sdk.MyDatabaseHelper;
import im.penghao.sdk.SendMessageCallBack;

public class TalkActivity extends AppCompatActivity{

    private Button button;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private EditText editText;
    private MyAdapter myAdapter;
    private List<Message> list=new ArrayList<>();

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

        button=findViewById(R.id.talk_send_button);
        recyclerView=findViewById(R.id.talk_recyclerview);
        editText=findViewById(R.id.talk_editText);
        swipeRefresh=findViewById(R.id.talk_swipeRefresh);

        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        myAdapter=new MyAdapter();
        recyclerView.setAdapter(myAdapter);

        IMClient.service.setOnMessageReceiveListener(msg -> runOnUiThread(() -> {
            // 如果消息不是当前对话对象发来的则不予理睬
            if (!msg.getFrom().equals(talkObj)){
                return;
            }
            list.add(msg);
            myAdapter.notifyItemInserted(list.size()-1);
            recyclerView.scrollToPosition(list.size()-1);
        }));

        button.setOnClickListener(v -> {
            // 创建消息
            Message msg=new Message();
            msg.setType(0);
            msg.setWhen(System.currentTimeMillis());
            msg.setFrom(IMClient.ME);
            msg.setTo(talkObj);
            msg.setContent(editText.getText().toString());
            IMClient.service.sendMessage(msg, new SendMessageCallBack() {
                @Override
                public void onSuccess(Message msg) {
                    runOnUiThread(() -> {
                        // 更新本界面 | Update this interface
                        list.add(msg);
                        myAdapter.notifyItemInserted(list.size()-1);
                        recyclerView.scrollToPosition(list.size()-1);
                        editText.setText("");
                    });
                }

                @Override
                public void onFailed(Message msg, String errorCode) {

                }
            });
        });

        swipeRefresh.setOnRefreshListener(() -> {
            MyDatabaseHelper sqlHelper=new MyDatabaseHelper(TalkActivity.this,"MsgLibs.db",null,1);
            SQLiteDatabase db=sqlHelper.getReadableDatabase();
            Cursor cursor=db.query(talkObj,null,null,null,null,null,"id desc",list.size()+",5");
            if (cursor.moveToFirst()){
                do {
                    Message message=new Message();
                    message.setType(cursor.getInt(cursor.getColumnIndex("type")));
                    message.setWhen(cursor.getLong(cursor.getColumnIndex("msg_when")));
                    message.setFrom(cursor.getString(cursor.getColumnIndex("msg_from")));
                    message.setTo(cursor.getString(cursor.getColumnIndex("msg_to")));
                    message.setContent(cursor.getString(cursor.getColumnIndex("content")));
                    list.add(0,message);
                    myAdapter.notifyItemInserted(0);
                }while (cursor.moveToNext());
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
                message.setType(cursor.getInt(cursor.getColumnIndex("type")));
                message.setWhen(cursor.getLong(cursor.getColumnIndex("msg_when")));
                message.setFrom(cursor.getString(cursor.getColumnIndex("msg_from")));
                message.setTo(cursor.getString(cursor.getColumnIndex("msg_to")));
                message.setContent(cursor.getString(cursor.getColumnIndex("content")));
                list.add(message);
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
                holder.leftText.setText(message.getContent());
            }else if (message.getFrom().equals(IMClient.ME)){// 如果是发送的消息则显示在右边
                holder.rightLayout.setVisibility(View.VISIBLE);
                holder.leftLayout.setVisibility(View.GONE);
                holder.rightText.setText(message.getContent());
            }

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

    }
}