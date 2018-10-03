package com.shiyan.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shiyan.activity.TalkActivity;
import com.shiyan.dogdog.R;

import java.util.ArrayList;
import java.util.List;

import im.penghao.sdk.IMClient;
import im.penghao.sdk.Message;

public class NewsFragment extends Fragment{

    Context context;
    List<String[]> existed_dialog=new ArrayList<>();

    RecyclerView recyclerView;
    NewsAdapter newsAdapter;
    private Message mMsgNow;

    final String debug="NewsFragment";

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what==0){
                Message message= (Message) msg.obj;
                boolean isExist=false;
                for (int i=0;i<existed_dialog.size();i++){
                    if ((message.getFrom().equals(existed_dialog.get(i)[0]))|(message.getTo().equals(existed_dialog.get(i)[0]))){
                        existed_dialog.set(i,new String[]{message.getFrom(),message.getContent()});
//                        newsAdapter.notifyItemChanged(i);
                        isExist=true;
                        break;
                    }
                }
                if (!isExist){
                    if (message.getFrom().equals(IMClient.ME)){
                        existed_dialog.add(0,new String[]{message.getTo(),message.getContent()});
                    } else {
                        existed_dialog.add(0,new String[]{message.getFrom(),message.getContent()});
                    }

//                    newsAdapter.notifyItemChanged(0);
                }
                for (int i=0;i<existed_dialog.size();i++){
                    newsAdapter.notifyItemChanged(i);
                }
            }

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getContext();
        Log.d(debug,"onCreate()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.view1,container,false);
        recyclerView=view.findViewById(R.id.view1_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        newsAdapter=new NewsAdapter();
        recyclerView.setAdapter(newsAdapter);

        IMClient.service.setOnMessageReceiveListener(msg -> {
            mMsgNow=msg;
            android.os.Message message=new android.os.Message();
            message.obj=msg;
            handler.sendMessage(message);
        });
        Log.d(debug,"onCreateView()");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(debug,"onStart()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(debug,"onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(debug,"onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(debug, "onDestroy");
//        context.unregisterReceiver(newsReceiver);
    }

    class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{
            View itemView;
            TextView from,content;
            ViewHolder(View itemView) {
                super(itemView);
                this.itemView=itemView;
                from=itemView.findViewById(R.id.news_item_from_textView);
                content=itemView.findViewById(R.id.news_item_content_textView);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item_view,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.from.setText(existed_dialog.get(position)[0]);
            holder.content.setText(existed_dialog.get(position)[1]);
            Log.d("NOTE:","The position in methed is:"+position);
            holder.itemView.setOnClickListener(v -> {
                Log.d("NOTE:","The position in ClickListener is:"+position);
                Log.d("NOTE:","The size of the list is:"+existed_dialog.size());
                Intent intent = new Intent(getActivity(), TalkActivity.class);
                intent.putExtra("num", existed_dialog.get(position)[0]);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return existed_dialog.size();
        }
    }
}