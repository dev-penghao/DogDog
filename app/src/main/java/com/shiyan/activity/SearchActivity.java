package com.shiyan.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.shiyan.dogdog.R;
import com.shiyan.nets.Me;
import com.shiyan.nets.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    RecyclerView recyclerView;
    SearchAdapter searchAdapter;

    List<JSONObject> searchResult=new ArrayList<>();

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0){
//                for (int i=0;i<searchResult.size();i++){
//                    searchAdapter.notifyItemInserted(i);
//                }
                recyclerView.setAdapter(searchAdapter);
            } else if (msg.what==1){
                Toast.makeText(SearchActivity.this,(String)msg.obj,Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar=findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView=findViewById(R.id.search_recyclerView);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        searchAdapter=new SearchAdapter();
        recyclerView.setAdapter(searchAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu,menu);
        MenuItem menuItem= menu.findItem(R.id.search_search_view);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.home :
                finish();
                break;
            default :
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return true;
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onQueryTextSubmit(String query) {
        Request request=new Request();
        request.putType("search_user");
        request.putContent(query);
        new Thread(() -> {
            String result=request.sendRequest();
            try {
                searchResult.clear();
                JSONArray jsonArray= new JSONArray(result);
                for (int i=0;i<jsonArray.length();i++){
                    searchResult.add(jsonArray.getJSONObject(i));
                }
                handler.sendEmptyMessage(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
        return true;
    }

    class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{
            View view;
            Button button;
            TextView user_name, user_num;
            ViewHolder(View itemView) {
                super(itemView);
                view=itemView;
                user_name=itemView.findViewById(R.id.search_item_user_name);
                user_num=itemView.findViewById(R.id.search_item_user_num);
                button=itemView.findViewById(R.id.search_item_button);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_view,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                holder.user_name.setText(searchResult.get(position).getString("name"));
                holder.user_num.setText(searchResult.get(position).getString("num"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            holder.button.setOnClickListener(v -> {
                Request request=new Request();
                request.putType("add_friend");
                request.putContent(Me.num);
                try {
                    request.putContent(searchResult.get(position).getString("num"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new Thread(() -> {
                    String result=request.sendRequest();
                    Message msg=new Message();
                    msg.obj=result;
                    msg.what=1;
                    handler.sendMessage(msg);
                }).start();
            });
        }

        @Override
        public int getItemCount() {
            return searchResult.size();
        }
    }
}