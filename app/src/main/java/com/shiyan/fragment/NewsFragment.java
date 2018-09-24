package com.shiyan.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import com.shiyan.activity.TalkActivity;
import com.shiyan.dogdog.R;
import com.shiyan.tools.Me;
import com.shiyan.tools.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.shiyan.tools.Me.msgNow;

public class NewsFragment extends Fragment{

    Context context;
    NewsReceiver newsReceiver;// 本页面的广播接收器
    List<String> existed_dialog=new ArrayList<>();

    RecyclerView recyclerView;
    NewsAdapter newsAdapter;

    final String debug="NewsFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getContext();
        // 装载广播接收器
        IntentFilter filter=new IntentFilter();
        newsReceiver=new NewsReceiver();
        filter.addAction("new_message");
        context.registerReceiver(newsReceiver,filter);
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

//        JSONArray jsonArray=loadJSONArray(Me.dataPath+"news");
//        if (jsonArray!=null){
//            msgNow=new Message();
//            for (int i=0;i<jsonArray.length();i++){
//                try {
//                    String from=jsonArray.getJSONObject(i).getString("from");
//                    String content=jsonArray.getJSONObject(i).getString("content");
//                    msgNow.setFrom(from);
//                    msgNow.setTextContent(content);
//                    newsAdapter.notifyItemInserted(0);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            msgNow=null;
//        }
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
//        JSONArray jsonArray=new JSONArray();
//        for (int i=0;i<recyclerView.getChildCount();i++){
//            JSONObject jsonObject=new JSONObject();
//            try {
//                jsonObject.put("from",((TextView)recyclerView.getChildAt(i).findViewById(R.id.news_item_from_textView)).getText());
//                jsonObject.put("content",((TextView)recyclerView.getChildAt(i).findViewById(R.id.news_item_content_textView)).getText());
//                jsonArray.put(jsonObject);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        saveJSONArray(jsonArray, Me.dataPath+"news");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(debug,"onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(debug,"onDestroy");
        context.unregisterReceiver(newsReceiver);
    }

    private boolean saveJSONArray(JSONArray jsonArray,String path){
        File outFile=new File(path);
        if (!outFile.getParentFile().exists()){// 父文件夹若不存在
            if (!outFile.getParentFile().mkdirs()){// 试图创建父文件夹，若失败
                return false;
            }
        }
        // 确保父文件夹存在后
        try {
            FileOutputStream fos=new FileOutputStream(path);
            fos.write(jsonArray.toString().getBytes(Charset.forName("UTF-8")));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private JSONArray loadJSONArray(String path){
        File inFile=new File(path);
        if (!inFile.isFile()){// 判断一个不存在的文件是否是文件会返回false
            return null;
        }
        // 确保文件存在后
        try {
            FileInputStream fis=new FileInputStream(inFile);
            byte[] bytes=new byte[fis.available()];// 一次性全部读完
            fis.read(bytes);
            return new JSONArray(new String(bytes,Charset.forName("UTF-8")));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
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
            if (msgNow.getFrom().equals(Me.num)){
                holder.from.setText(msgNow.getTo());
            } else {
                holder.from.setText(msgNow.getFrom());
            }
            holder.content.setText(msgNow.getTextContent());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), TalkActivity.class);
                intent.putExtra("num", existed_dialog.get(position));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return existed_dialog.size();
        }
    }

    class NewsReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("new_message".equals(intent.getAction())){// 收到一条消息
                boolean isExist=false;
                for (int i=0;i<existed_dialog.size();i++){
                    if ((msgNow.getFrom().equals(existed_dialog.get(i)))|(msgNow.getTo().equals(existed_dialog.get(i)))){
                        newsAdapter.notifyItemChanged(i);
                        isExist=true;
                        break;
                    }
                }
                if (!isExist){
                    if (msgNow.getFrom().equals(Me.num)){
                        existed_dialog.add(0,msgNow.getTo());
                    } else {
                        existed_dialog.add(0,msgNow.getFrom());
                    }
                    newsAdapter.notifyItemInserted(0);
                }
            }
        }
    }
}