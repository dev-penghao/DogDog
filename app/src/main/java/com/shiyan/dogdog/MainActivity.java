package com.shiyan.dogdog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

import com.shiyan.activity.AboutActivity;
import com.shiyan.activity.SearchActivity;
import com.shiyan.activity.SignInActivity;
import com.shiyan.fragment.ContactsFragment;
import com.shiyan.fragment.NewsFragment;
import com.shiyan.fragment.ZoneFrament;

import java.util.ArrayList;
import java.util.List;

import im.penghao.sdk.IMClient;
import im.penghao.sdk.IMService;
import im.penghao.sdk.LoginCallBack;

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

//        Intent intent=new Intent(this,IMService.TestService.class);
//        startService(intent);
//        if (true)return;

        initIMSDK();
        initView();

        doLogin();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

                } else {
                    finish();
                }
                break;
                default :
        }
    }

    public void initIMSDK(){
        IMClient.service=new IMService(getApplicationContext());
    }

    public void initView(){
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        navigationView=findViewById(R.id.nav_view_main);
        navigationView.setCheckedItem(R.id.nav_item1);
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
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_item1:
                        Intent intent=new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(intent);
                        break;

                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void doLogin(){
        isNetWorkAvailable=isNetWorkAvailable();
        if (isNetWorkAvailable) {// 如果有网络就登录 | Do login if network is available
            SharedPreferences preferences = getSharedPreferences("main", MODE_PRIVATE);
            isLogined = preferences.getBoolean("isLogined", false);
            if (isLogined) {// Auto Login if this client is logined before
                String num, password;
                num = preferences.getString("num", null);
                password = preferences.getString("password", null);
                if (num == null || password == null) {// 一旦出错，则放弃自动登录而跳到登录页面手动登录 | Give up auto login and do manually login if something are wrong
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivityForResult(intent, 1);
                    return;
                }
                IMClient.service.login(num, password, new LoginCallBack() {
                    @Override
                    public void onSuccess() {
                        IMClient.ME=num;
                    }

                    @Override
                    public void onFailed(String errorCode) {
                        runOnUiThread(() -> Snackbar.make(viewPager,"We are so sorry that something got wrong.",Snackbar.LENGTH_LONG).setAction("I learned",null));
                    }
                });
            } else {// Go to LoginActivity and do manually login
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivityForResult(intent, 1);
            }
        } else {// We'll load dates from localhost if network is unavailable. But now we'll do nothing

        }
    }


    // 判断是否打开网络
    public boolean isNetWorkAvailable(){
        boolean isAvailable = false ;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (cm != null) {
            networkInfo = cm.getActiveNetworkInfo();
        }
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