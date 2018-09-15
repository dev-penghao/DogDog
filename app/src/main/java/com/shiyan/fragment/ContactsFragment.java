package com.shiyan.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.shiyan.tools.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    RecyclerView contacts;
    SwipeRefreshLayout refreshLayout;
    List<JSONObject> friendList = new ArrayList<>();

    ContactsAdapter contactsAdapter;
    Context context;

    final String debug="ContactsFragment";

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                for (int i = 0; i < friendList.size(); i++) {
                    contactsAdapter.notifyItemChanged(i);// 这里不能使用Insert,否则会有奇怪的BUG
                }
                refreshLayout.setRefreshing(false);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view2, container, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        contacts = view.findViewById(R.id.view2_contacts_recyclerview);
        refreshLayout = view.findViewById(R.id.view2_swipeRefreshLayout);

        contacts.setLayoutManager(layoutManager);
        contactsAdapter = new ContactsAdapter();
        contacts.setAdapter(contactsAdapter);
        refreshLayout.setOnRefreshListener(this::flushFriendList);

//        recoverFriendList();
        flushFriendList();
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
//        saveFriendList();
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
    }

    // 保存列表到本地
    private void saveFriendList(){
        File dogdog=new File(Me.dataPath);
        if ((!dogdog.isDirectory())||!dogdog.exists()){// 名为DogDog的文件不是文件夹或者不存在
            if (!dogdog.mkdirs()){// 试图创建，如果不成功就不管了
                return;
            }
        }
        // 到这里DogDog文件夹已经确保存在了
        File firendList=new File(Me.dataPath+"friendList");
        try {
            FileOutputStream fos=new FileOutputStream(firendList);
            JSONArray jsonArray=new JSONArray(friendList);
            fos.write(jsonArray.toString().getBytes(Charset.forName("UTF-8")));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recoverFriendList(){
        File friendList=new File(Me.dataPath+"friendList");
        if (!friendList.exists()){
            flushFriendList();// 如果文件不存在则通过网络获取
            return;
        }
        try {
            FileInputStream fis=new FileInputStream(friendList);
            byte[] bytes=new byte[fis.available()];// 一次性全部读完
            fis.read(bytes);
            JSONArray jsonArray=new JSONArray(new String(bytes,Charset.forName("UTF-8")));
            for (int i=0;i<jsonArray.length();i++){
                this.friendList.add(jsonArray.getJSONObject(i));
            }
            fis.close();
            handler.sendEmptyMessage(0);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized void flushFriendList() {
        new Thread(() -> {
            Request request = new Request();
            request.putType("get_friend_list");
            request.putContent(Me.num);
            String result = request.sendRequest();
            try {
                friendList.clear();
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    friendList.add(obj);
                }
                handler.sendEmptyMessage(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView userName;
            TextView isOnline;
            View itemView;

            ViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                userName = itemView.findViewById(R.id.contacts_item_user_name);
                isOnline = itemView.findViewById(R.id.contacts_item_isOnline);
            }
        }

        String name, num;
        boolean isOnline;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_item_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                name=friendList.get(position).getString("name");
                num=friendList.get(position).getString("num");
                isOnline=friendList.get(position).getBoolean("isOnline");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            holder.userName.setText(name);
            holder.isOnline.setText(isOnline?"[在线]":"[离线]");
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), TalkActivity.class);
                try {
                    intent.putExtra("num", friendList.get(position).getString("num"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return friendList.size();
        }
    }
}