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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import im.penghao.sdk.IMClient;

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
//        flushFriendList();
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

    public synchronized void flushFriendList() {
        new Thread(() -> {
//            Request request = new Request();
//            request.putType("get_friend_list");
//            request.putContent(Me.num);
//            String result = request.sendRequest();
            String result=IMClient.service.getFriendList(IMClient.ME);
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
            Log.d("NOTE:","The position in methed is:"+position);

            holder.itemView.setOnClickListener(v -> {
                Log.d("NOTE:","The position in ClickListener is:"+position);
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