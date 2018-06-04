package com.shiyan.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import com.shiyan.dogdog.R;
import com.shiyan.nets.NetMessage;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment{

    Context context;
    NewsReceiver newsReceiver;
    NetMessage new_message;
    List<String> exist_dialog=new ArrayList<>();

    RecyclerView recyclerView;
    NewsAdapter newsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getContext();
        // 装载广播接收器
        IntentFilter filter=new IntentFilter();
        newsReceiver=new NewsReceiver();
        filter.addAction("new_message");
        context.registerReceiver(newsReceiver,filter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.view1,container,false);
        recyclerView=view.findViewById(R.id.view1_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        newsAdapter=new NewsAdapter();
        recyclerView.setAdapter(newsAdapter);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(newsReceiver);
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
            holder.from.setText(new_message.getObj());
            holder.content.setText(new_message.getMessage());
        }

        @Override
        public int getItemCount() {
            Log.e("注意",exist_dialog.size()+"");
            return exist_dialog.size();
        }
    }

    class NewsReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("new_message".equals(intent.getAction())){
                String[] ss=intent.getStringExtra("new").split("/");
                new_message=new NetMessage(0,ss[1],ss[0]);
                boolean isExist=false;
                for (int i=0;i<exist_dialog.size();i++){
                    if (new_message.getObj().equals(exist_dialog.get(i))){
                        newsAdapter.notifyItemChanged(i);
                        isExist=true;
                        break;
                    }
                }
                if (!isExist){
                    exist_dialog.add(0,new_message.getObj());
                    newsAdapter.notifyItemInserted(0);
                    newsAdapter.notifyItemRangeChanged(0,exist_dialog.size());
                }
            }
        }
    }
}