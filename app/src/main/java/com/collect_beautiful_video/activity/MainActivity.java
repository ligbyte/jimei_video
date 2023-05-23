package com.collect_beautiful_video.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.Navigation;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.collect_beautiful_video.R;
import com.collect_beautiful_video.fragment.MainFragment;
import com.collect_beautiful_video.fragment.MineFragment;
import com.collect_beautiful_video.util.FFmpegCmdUtil;
import com.collect_beautiful_video.view.FixFragmentNavigator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yhd.mediaplayer.MediaPlayerHelper;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //去掉标题栏
    this.getSupportActionBar().hide();//注意是在 setContentView(R.layout.activity_main)后
    // 获取页面上的底部导航栏控件
    BottomNavigationView navView = findViewById(R.id.nav_view);
    navView.setItemIconTintList(null);

    // 配置navigation与底部菜单之间的联系
    // 底部菜单的样式里面的item里面的ID与navigation布局里面指定的ID必须相同，否则会出现绑定失败的情况
    AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            R.id.iv_home,R.id.iv_mine)
            .build();
    // 建立fragment容器的控制器，这个容器就是页面的上的fragment容器

    Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
    //fragment的重复加载问题和NavController有关
    final NavController navController = NavHostFragment.findNavController(fragmentById);

    FixFragmentNavigator fixFragmentNavigator=new FixFragmentNavigator(this, fragmentById.getChildFragmentManager(), fragmentById.getId());
    navController.getNavigatorProvider().addNavigator(fixFragmentNavigator);
    navController.setGraph(initNavGraph(navController.getNavigatorProvider(),fixFragmentNavigator));
    // 启动
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationUI.setupWithNavController(navView, navController);

    navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navController.navigate(item.getItemId());
        if(item.getItemId()==R.id.iv_mine){
          LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("com.refresh.userinfo"));
        }
        return true;

      }
    });

  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
  }

  private NavGraph initNavGraph(NavigatorProvider provider, FixFragmentNavigator fragmentNavigator) {
    NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));

    //用自定义的导航器来创建目的地
    FragmentNavigator.Destination destination1 = fragmentNavigator.createDestination();
    destination1.setId(R.id.iv_home);
    destination1.setClassName(MainFragment.class.getCanonicalName());
    navGraph.addDestination(destination1);


    FragmentNavigator.Destination destination2 = fragmentNavigator.createDestination();
    destination2.setId(R.id.iv_mine);
    destination2.setClassName(MineFragment.class.getCanonicalName());
    navGraph.addDestination(destination2);


    navGraph.setStartDestination(destination1.getId());

    return navGraph;
  }

  @Override
  protected void onStop() {
    super.onStop();
    MediaPlayerHelper.getInstance().release();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

  }

}
