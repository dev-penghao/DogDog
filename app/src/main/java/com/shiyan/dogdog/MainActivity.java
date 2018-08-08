package com.shiyan.dogdog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.shiyan.activity.SearchActivity;
import com.shiyan.fragment.ContactsFragment;
import com.shiyan.fragment.NewsFragment;
import com.shiyan.fragment.ZoneFrament;
import com.shiyan.nets.Me;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    String[] title={"消息","联系人","动态"};
    List<Fragment> fragmentList=new ArrayList<>();
    TextView myName, myNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        myName=findViewById(R.id.main_myname);
        myNum=findViewById(R.id.main_mynum);
        myName.setText(Me.name);
        myNum.setText(Me.num);

        viewPager=findViewById(R.id.viewpager);
        tabLayout=findViewById(R.id.tabs);
        fragmentList.add(new NewsFragment());
        fragmentList.add(new ContactsFragment());
        fragmentList.add(new ZoneFrament());
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        Intent intent=new Intent(MainActivity.this,MainService.class);
        startService(intent);
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