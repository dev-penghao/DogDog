package com.shiyan.dogdog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    View view1,view2,view3;
    ViewPager viewPager;
    TabLayout tabLayout;
    List<View> viewList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView text1=new TextView(MainActivity.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        viewPager=findViewById(R.id.viewpager);
        tabLayout=findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setText("消息"));
        tabLayout.addTab(tabLayout.newTab().setText("好友"));
        tabLayout.addTab(tabLayout.newTab().setText("动态"));

        text1.setText("\n" +
                "\n" +
                "    public class MainActivity extends Activity {    \n" +
                "    private String[] data = { \"Apple\", \"Banana\", \"Orange\", \"Watermelon\",    \n" +
                "    \"Pear\", \"Grape\", \"Pineapple\", \"Strawberry\", \"Cherry\", \"Mango\" };    \n" +
                "    @Override    \n" +
                "    protected void onCreate(Bundle savedInstanceState) {    \n" +
                "    super.onCreate(savedInstanceState);    \n" +
                "    setContentView(R.layout.activity_main);    \n" +
                "    ArrayAdapter<String> adapter = new ArrayAdapter<String>(    \n" +
                "    MainActivity.this, android.R.layout.simple_list_item_1, data);    \n" +
                "    ListView listView = (ListView) findViewById(R.id.list_view);    \n" +
                "    listView.setAdapter(adapter);    \n" +
                "    }    \n" +
                "    }    \n" +
                "\n" +
                "    public class MainActivity extends Activity {    \n" +
                "    private String[] data = { \"Apple\", \"Banana\", \"Orange\", \"Watermelon\",    \n" +
                "    \"Pear\", \"Grape\", \"Pineapple\", \"Strawberry\", \"Cherry\", \"Mango\" };    \n" +
                "    @Override    \n" +
                "    protected void onCreate(Bundle savedInstanceState) {    \n" +
                "    super.onCreate(savedInstanceState);    \n" +
                "    setContentView(R.layout.activity_main);    \n" +
                "    ArrayAdapter<String> adapter = new ArrayAdapter<String>(    \n" +
                "    MainActivity.this, android.R.layout.simple_list_item_1, data);    \n" +
                "    ListView listView = (ListView) findViewById(R.id.list_view);    \n" +
                "    listView.setAdapter(adapter);    \n" +
                "    }    \n" +
                "    }    \n" +
                "\n" +
                "    public class MainActivity extends Activity {    \n" +
                "    private String[] data = { \"Apple\", \"Banana\", \"Orange\", \"Watermelon\",    \n" +
                "    \"Pear\", \"Grape\", \"Pineapple\", \"Strawberry\", \"Cherry\", \"Mango\" };    \n" +
                "    @Override    \n" +
                "    protected void onCreate(Bundle savedInstanceState) {    \n" +
                "    super.onCreate(savedInstanceState);    \n" +
                "    setContentView(R.layout.activity_main);    \n" +
                "    ArrayAdapter<String> adapter = new ArrayAdapter<String>(    \n" +
                "    MainActivity.this, android.R.layout.simple_list_item_1, data);    \n" +
                "    ListView listView = (ListView) findViewById(R.id.list_view);    \n" +
                "    listView.setAdapter(adapter);    \n" +
                "    }    \n" +
                "    }    \n" +
                "\n" +
                "    public class MainActivity extends Activity {    \n" +
                "    private String[] data = { \"Apple\", \"Banana\", \"Orange\", \"Watermelon\",    \n" +
                "    \"Pear\", \"Grape\", \"Pineapple\", \"Strawberry\", \"Cherry\", \"Mango\" };    \n" +
                "    @Override    \n" +
                "    protected void onCreate(Bundle savedInstanceState) {    \n" +
                "    super.onCreate(savedInstanceState);    \n" +
                "    setContentView(R.layout.activity_main);    \n" +
                "    ArrayAdapter<String> adapter = new ArrayAdapter<String>(    \n" +
                "    MainActivity.this, android.R.layout.simple_list_item_1, data);    \n" +
                "    ListView listView = (ListView) findViewById(R.id.list_view);    \n" +
                "    listView.setAdapter(adapter);    \n" +
                "    }    \n" +
                "    }    ");

        LayoutInflater inflater=getLayoutInflater();
        view1=text1;
        view2=inflater.inflate(R.layout.view2,null);
        view3=inflater.inflate(R.layout.view3,null);


        viewList=new ArrayList<>();
        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);

        viewPager.setAdapter(adapter);

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    OkHttpClient client=new OkHttpClient();
//                    Request request=new Request.Builder()
//                            .url("http://192.168.43.184:8888")
//                            .build();
//                    Response response= null;
//                    try {
//                        response = client.newCall(request).execute();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    String body= null;
//                    try {
//                        body = response.body().string();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d("返回",body);
//                }
//            }).start();

    }

    PagerAdapter adapter=new PagerAdapter() {
        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view==object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(viewList.get(position));
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(viewList.get(position));

            return viewList.get(position);
        }
    };
}