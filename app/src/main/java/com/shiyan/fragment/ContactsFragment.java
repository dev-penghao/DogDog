package com.shiyan.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.TextView;

import com.shiyan.activity.TalkActivity;
import com.shiyan.dogdog.R;
import com.shiyan.nets.Me;
import com.shiyan.nets.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    Button button;
    RecyclerView contacts;
    SwipeRefreshLayout refreshLayout;
    List<String[]> friendList = new ArrayList<>();// 数组会有3个元素，0是昵称，1是账号，2是消息

    ContactsAdapter contactsAdapter;
    Context context;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                for (String[] ss : friendList) {
                    Log.d("注意", ss[0] + " : " + ss[1]);
                }
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
        button = view.findViewById(R.id.view2_button);

        contacts.setLayoutManager(layoutManager);
        contactsAdapter = new ContactsAdapter();
        contacts.setAdapter(contactsAdapter);
        refreshLayout.setOnRefreshListener(this::flushFriendList);
        button.setOnClickListener(v -> flushFriendList());
        return view;
    }

    public void flushFriendList() {
        new Thread(() -> {
            Request request = new Request();
            request.putType("get_friend_list");
            request.putContent(Me.num);
            String result = request.sendRequest();
            try {
                List<String> lastMessage = new ArrayList<>();
                if (!friendList.isEmpty()) {
                    for (int i = 0; i < friendList.size(); i++) {
                        lastMessage.add(friendList.get(i)[2]);
                    }
                }
                friendList.clear();
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String[] user = new String[3];
                    user[0] = obj.getString("name");
                    user[1] = obj.getString("num");
                    if (!lastMessage.isEmpty()) {
                        user[2] = lastMessage.get(i);
                    }
                    friendList.add(i, user);
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
            TextView content;
            View itemView;

            ViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                userName = itemView.findViewById(R.id.contacts_item_user_name);
                content = itemView.findViewById(R.id.contacts_item_content);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_item_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.userName.setText(friendList.get(position)[0]);
            // 如果有第三项
//            if (friendList.get(position).length>2){
            holder.content.setText(friendList.get(position)[2]);
//            }
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), TalkActivity.class);
                intent.putExtra("num", friendList.get(position)[1]);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return friendList.size();
        }

    }
}