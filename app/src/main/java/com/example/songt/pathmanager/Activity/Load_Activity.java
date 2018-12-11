package com.example.songt.pathmanager.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.songt.pathmanager.R;
import com.example.songt.pathmanager.Toole.ActivityCollector;

public class Load_Activity extends AppCompatActivity {

    //在活动第一次被创建时调用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏以及状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**标题是属于View的，所以窗口所有的修饰部分被隐藏后标题依然有效,需要去掉标题**/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_load_activity);

        //设置背景图片
        ImageView imageView = (ImageView) findViewById(R.id.image);
        Drawable draw = this.getResources().getDrawable(R.drawable.welcome1080p);
        imageView.setImageDrawable(draw);

            handler.sendEmptyMessageDelayed(0,1000);
    }
    //活动由不可见变为可见时调用（加载资源）
    @Override
    protected void onStart(){
        super.onStart();
    }
    //活动准备好和用户交互时调用
    @Override
    protected void onResume(){
        super.onResume();
    }
    //系统准备去启动或者回复另一个活动时调用
    @Override
    protected void onPause(){

        super.onPause();
    }
    //活动完全不可见时调用（释放资源）
    @Override
    protected void onStop(){
        super.onStop();
    }
    //活动由停止状态变为运行状态是调用
    @Override
    protected void onRestart(){
        super.onRestart();
    }
    //活动被销毁之前调用
    protected void onDestroy(){
        super.onDestroy();
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getHome();
        }
    };
    public void getHome(){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean antoLogin = pref.getBoolean("auto_login",false);
        if(antoLogin){
            Log.i("Load_Activity","自动登陆");
            //跳转
            Intent intent=new Intent(Load_Activity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(Load_Activity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
