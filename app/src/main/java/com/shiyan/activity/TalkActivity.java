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
import com.shiyan.tools.GlobalSocket;
import com.shiyan.tools.Me;
import com.shiyan.tools.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.shiyan.tools.Me.msgNow;

public class TalkActivity extends AppCompatActivity{

    Button button;
    RecyclerView recyclerView;
    EditText editText;
    MyAdapter myAdapter;
    List<Message> list=new ArrayList<>();
    TalkingReceiver talkingReceiver;

    String talkObj;// 当前对话的对象
    final int MESSAGE_BYTE_MAX_LENGTH=1024;

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

//        loadMessage();

        button.setOnClickListener(v -> {
            Message msg=new Message();
            msg.setFrom(Me.num);
            msg.setTo(talkObj);
            msg.setMsgSize(0);
            msg.setType(0);
            msg.setWhen(System.currentTimeMillis());
            msg.setTextContent(editText.getText().toString());
            byte[] bytes=msg.toString().getBytes(Charset.forName("UTF-8"));
            if (bytes.length>MESSAGE_BYTE_MAX_LENGTH){
                Snackbar.make(v,"消息过长",Snackbar.LENGTH_LONG).setAction("确定",v1 -> {}).show();
                return;
            }
            Me.msgNow=msg;
            this.sendBroadcast(new Intent().setAction("new_message"));
            new Thread(() -> {
                try {
                    GlobalSocket.ps.write(bytes);
                    GlobalSocket.ps.write((new byte[1])[0]=0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            list.add(msg);
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

    private void loadMessage(){
        if (new File("/sdcard/DogDog/"+talkObj).isFile()){
            for (int i=10;i>0;i--){
                String msgByString=loadNLine("/sdcard/DogDog/"+talkObj,i);
                System.out.println(msgByString);
                if (msgByString==null) break;
                list.add(new Message(msgByString));
                myAdapter.notifyItemInserted(list.size()-1);
            }
            recyclerView.scrollToPosition(list.size()-1);
        }
    }

    private String loadNLine(String path,int lineNum){
        File inFile=new File(path);
        if (!inFile.exists()) return null;// 如果文件不存在就不加载了
        try {
            RandomAccessFile raf=new RandomAccessFile(inFile,"r");
            long fileLength=raf.length();
            long pos=fileLength-1;
            long pos0=pos;
            int count=0;
            while (pos>0){
                raf.seek(pos);
                if (raf.readByte()==0) {
                    count+=1;
                    pos--;
                    if (count>=lineNum) {
                        break;
                    }
                    pos0=pos;
                } else {
                    pos--;
                }
            }
            if (pos==0) {
                raf.seek(0);
            } else {
                raf.seek(pos+2);
            }
            byte[] bytes=new byte[(int) (pos0-pos-1)];
            raf.read(bytes);
            return new String(bytes,Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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