package com.shiyan.dogdog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.shiyan.activity.SearchActivity;
import com.shiyan.activity.SignInActivity;
import com.shiyan.fragment.ContactsFragment;
import com.shiyan.fragment.NewsFragment;
import com.shiyan.fragment.ZoneFrament;
import com.shiyan.tools.GlobalSocket;
import com.shiyan.tools.Me;
import com.shiyan.tools.MyInputStream;
import com.shiyan.tools.Request;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static boolean isNetWorkAvailable=false;

    private boolean isLogined=false;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private String[] title={"消息","联系人","动态"};
    private List<Fragment> fragmentList=new ArrayList<>();
    private TextView myName, myNum;
    private NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        navigationView=findViewById(R.id.nav_view_main);
        myName=navigationView.getHeaderView(0).findViewById(R.id.nav_myname);
        myNum=navigationView.getHeaderView(0).findViewById(R.id.nav_mynum);
        // 直接像下面这两行这样做是不行的
//        myName=findViewById(R.id.nav_myname);
//        myNum=findViewById(R.id.nav_mynum);

        viewPager=findViewById(R.id.viewpager);
        tabLayout=findViewById(R.id.tabs);
        fragmentList.add(new NewsFragment());
        fragmentList.add(new ContactsFragment());
        fragmentList.add(new ZoneFrament());
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

//        Intent intent=new Intent(MainActivity.this,MainService.class);
//        startService(intent);
        SharedPreferences preferences=getSharedPreferences("main",MODE_PRIVATE);
        isLogined=preferences.getBoolean("isLogined",false);
        GlobalSocket.SERVER_HOST=preferences.getString("SERVER_HOST","192.168.1.106");// 我的电脑在我家的局域网里的IP
        isNetWorkAvailable=isNetWorkAvailable(this);
        if (isNetWorkAvailable){// 如果有网络就登录
            if (isLogined){// 如果之前有登录过，那么自动登录
                new Thread(() -> {
                    String result = "";
                    String num,password;
                    num=preferences.getString("num",null);
                    password=preferences.getString("password",null);
                    if (num==null||password==null){// 一旦出错，则放弃自动登录而跳到登录页面手动登录
                        return;
                    }
                    try {
                        GlobalSocket.socket=new Socket(GlobalSocket.SERVER_HOST,38380);
                        GlobalSocket.ps=new PrintStream(GlobalSocket.socket.getOutputStream());
                        GlobalSocket.mis=new MyInputStream(GlobalSocket.socket.getInputStream());
                        String request="sign_in/"+num+"/"+password+"/";
                        GlobalSocket.ps.println(request);
                        result = GlobalSocket.mis.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if ("OK".equals(result)){// 登录成功
                        Intent intent=new Intent(MainActivity.this,MainService.class);
                        startService(intent);
                    } else {
                        Toast.makeText(this,"出错："+result,Toast.LENGTH_LONG).show();
                    }
                    Request request1=new Request();
                    request1.putType("find_user");
                    request1.putContent(num);
                    result=request1.sendRequest();

                    Me.name=result;
                    Me.num=num;
                    Me.dataPath="/sdcard/DogDog/"+Me.num;

                    runOnUiThread(() -> {// 更新UI
                        myName.setText(Me.name);
                        myNum.setText(Me.num);
                    });
                }).start();
            } else {//　之前没有登录过，那么跳到登录页面登录
                Intent intent=new Intent(MainActivity.this,SignInActivity.class);
                startActivityForResult(intent,1);
            }
        } else {// 没有网络则从本地获取数据

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent=new Intent(MainActivity.this,MainService.class);
        stopService(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_search_menu:
                Intent intent=new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
            default :
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode==RESULT_OK){
                    String result=data.getStringExtra("result");
                    if ("OK".equals(result)){
                        Intent intent=new Intent(this,MainService.class);
                        startService(intent);
                    }
                } else {
                    finish();
                }
                break;
                default :
        }
    }

    /**
     * 判断是否打开网络
     * @param context
     * @return
     */
    public static boolean isNetWorkAvailable(Context context){
        boolean isAvailable = false ;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isAvailable()){
            isAvailable = true;
        }
        return isAvailable;
    }

    class MyFragmentPagerAdapter extends FragmentPagerAdapter{

        MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }
}