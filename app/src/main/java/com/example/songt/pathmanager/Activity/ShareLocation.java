package com.example.songt.pathmanager.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.songt.pathmanager.R;
import com.example.songt.pathmanager.Service.GPSService;
import com.example.songt.pathmanager.Service.Person;
import com.example.songt.pathmanager.Toole.HttpCon;
import com.example.songt.pathmanager.Toole.IP;
import com.example.songt.pathmanager.Toole.MyApplication;
import com.example.songt.pathmanager.Toole.MyClientHandler;
import com.example.songt.pathmanager.supermapservice;
import com.supermap.data.Color;
import com.supermap.data.CoordSysTranslator;

import com.supermap.data.GeoPoint;
import com.supermap.data.GeoStyle;
import com.supermap.data.Maps;
import com.supermap.data.Point2D;
import com.supermap.data.Point2Ds;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.PrjCoordSysType;
import com.supermap.data.Size2D;
import com.supermap.data.Workspace;
import com.supermap.mapping.CallOut;
import com.supermap.mapping.CalloutAlignment;
import com.supermap.mapping.Map;
import com.supermap.mapping.MapControl;
import com.supermap.mapping.MapView;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import static com.example.songt.pathmanager.Toole.MyApplication.getContext;


public class ShareLocation extends AppCompatActivity implements View.OnClickListener {

    private static Context context;
    private static double latitude;//自己位置
    private static double longitude;//自己位置
    private static double youlatitude;//对方位置
    private static double youlongitude;//对方位置
    private static Button cancel_share;//取消分享
    private static boolean keepShare = false;//是否分享位置
    private static boolean showLocal = false;//是否显示对方位置
    private static SharedPreferences UserInformation;
    private static String myID;
    private static boolean success = true;
    private String MapName = "基础地图";
    private supermapservice Mapservice;
    private MapControl m_mapcontrol;
    private PrjCoordSys prjCoordSys;
    private Workspace A_workspace = null;//工作空间
    private MapView A_mapView = null;//地图控件
    private Maps m_maps = null;//地图数据集合
    private Map A_map = null;//地图
    private RelativeLayout view;
    //本地广播
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private GPSService.GetLocation getLocation;
    private IntentFilter ReadSucce_intentfilter;
    private LocalBroadcastManager ReadSucce_localBroadcastManager;
    private ImageView myImage;
    private CallOut mycallout;
    private ImageView youImage;
    private CallOut youcallout;
    private EditText input_edittext; //输入框
    private TextView share;//分享
    //和服务建立通讯获取最新的经纬度
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("MapActivity", "地图活动已建立和服务的通讯！");
            getLocation = (GPSService.GetLocation) service;
            longitude = getLocation.getlocation_longitude();//获取经度
            latitude = getLocation.getlocation_latitude();//获取纬度

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("MapActivity", "通讯断开！");
        }
    };

    /**
     * 与服务器建立长连接
     *
     * @param ID
     */
    private static void startMina(final String ID) {
        Log.i("LINK", "与服务器建立长连接");
        new Thread() {
            @Override
            public void run() {
                NioSocketConnector connection = new NioSocketConnector();
                connection.setHandler(new MyClientHandler());
                connection.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory()));
                ConnectFuture connect = connection.connect(new InetSocketAddress(IP.getIp(), 10087));
                connect.awaitUninterruptibly();//阻塞进程等待建立连接
                IoSession session = connect.getSession();
                while (true) {
                    //myID,youID,纬度，经度
                    //        String.format("%.2f", d)
                    Log.i("LINK","mima 传输的ID为：" + myID + "---" + ID);
                    session.write(myID + "," + ID + "," + String.format("%.10f", latitude) + "," + String.format("%.10f", longitude));
                    if (!keepShare) {
//                        session.getService().dispose();
                        session.close();
                        showLocal = false;
                        break;
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                Log.i("LINK", "Mainactivity 连接已断开");
            }
        }.start();
    }

    //活动第一次被创建时启用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharelocation);
        context = this;
        UserInformation = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        myID = UserInformation.getString("name", "");
        pushMessage();//信鸽推送注册

    }    //活动准备好和用户交互时调用
    @Override
    protected void onResume(){
        //退出后启动调用--2
        Log.i("ShareLocation","onResume");
        super.onResume();
    }
    //系统准备去启动或者回复另一个活动时调用
    @Override
    protected void onPause(){
        //后台运行--1
        Log.i("ShareLocation","onPause");
        if (A_mapView.getCallOut("my") != null) {
            A_mapView.removeCallOut("my");
        }
        if (A_mapView.getCallOut("you") != null) {
            A_mapView.removeCallOut("you");
        }
        super.onPause();
    }
    //活动完全不可见时调用（释放资源）
    @Override
    protected void onStop(){
        //后台运行--2
        Log.i("ShareLocation","onStop");

        super.onStop();
    }
    //活动由停止状态变为运行状态是调用
    @Override
    protected void onRestart(){
        //退出后启动时调用--2
        Log.i("ShareLocation","onRestart");
        super.onRestart();
        //A_mapView.getMapControl().getMap().getTrackingLayer().clear();
    }

    //活动由不可见变为可见时调用（加载资源）
    @Override
    protected void onStart() {
        super.onStart();
        view = (RelativeLayout) findViewById(R.id.map);
        myImage = new ImageView(context);
        youImage = new ImageView(context);
        mycallout = new CallOut(context);
        mycallout.setStyle(CalloutAlignment.LEFT_BOTTOM);
        mycallout.setCustomize(true);

        youcallout = new CallOut(context);
        youcallout.setStyle(CalloutAlignment.LEFT_BOTTOM);
        youcallout.setCustomize(true);

        input_edittext = findViewById(R.id.input_edittext);
        share = findViewById(R.id.share);
        share.setOnClickListener(this);
        cancel_share = findViewById(R.id.cancel);
        cancel_share.setVisibility(View.GONE);
        cancel_share.setOnClickListener(this);

        OpenMap();


        inviteBroadcast();//初始化广播
        //绑定服务
        Log.i("MapACtivity", "绑定服务");
        Intent bindIntent = new Intent(getContext(), GPSService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    //活动被销毁之前调用
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);//注销本地广播监听器
        unbindService(connection);//解绑服务

    }

    private void OpenMap() {
        Mapservice = new supermapservice();
        Mapservice.init(context);
        A_workspace = Mapservice.getWorkplace();
        A_mapView = Mapservice.getMaoView();
        m_mapcontrol = Mapservice.getMapcontrol();
        m_maps = Mapservice.getMaps();
        A_map = Mapservice.getMap();

        A_mapView = Mapservice.openMap(MapName);
        view.addView(A_mapView, 0);
        Log.i("MapActivity", "地图加载完成！");
    }

    //初始化广播
    private void inviteBroadcast() {
        //注册本地广播监听器
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcasttest.My_BROADCASTLOCATION");
        localReceiver = new LocalReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cancel://取消分享
                keepShare = false;
                cancel_share.setVisibility(View.GONE);
                share.setEnabled(true);
                if (A_mapView.getCallOut("you") != null) {
                    A_mapView.removeCallOut("you");
                }
                A_map.refresh();
                break;
            case R.id.share://分享
                share.setEnabled(false);
                Log.i("AAAA", "发送分享消息");
                if (myID.equals(input_edittext.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "请输入对方ID", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            Vertifyidentify(input_edittext.getText().toString().trim());
                        }
                    }.start();
                }
                break;
        }
    }

    //投影转换--PCS_USER_DEFINED
    private Point2D PrjTransToMap(Point2D point2D) {
        Point2Ds point2Ds = new Point2Ds();
        point2Ds.add(point2D);
        //获取地图坐标系
        prjCoordSys = m_mapcontrol.getMap().getPrjCoordSys();
        Log.i("TAG", "坐标系是：--" + prjCoordSys.getType() + "---");
        CoordSysTranslator coordSysTranslator = new CoordSysTranslator();
        coordSysTranslator.forward(point2Ds, prjCoordSys);
        Log.i("TAG", "-----");
        return point2Ds.getItem(0);
    }

    private void ShowMyPoint(double lon, double lat) {
        Point2D point2D = new Point2D(lon, lat);
        point2D = PrjTransToMap(point2D);

        myImage.setImageResource(R.drawable.my);
        if (A_mapView.getCallOut("my") != null) {
            A_mapView.removeCallOut("my");
        }
        mycallout.setLocation(point2D.getX(), point2D.getY());
        mycallout.setContentView(myImage);
        A_mapView.addCallout(mycallout, "my");
//        A_mapView.refresh();
        A_map.refresh();
    }

    private void ShowYouPoint(double lon, double lat) {
        Log.i("LINK","开始显示对方位置--" + lon + "--" + lat);
        Point2D point2D = new Point2D(lon, lat);
        point2D = PrjTransToMap(point2D);

        youImage.setImageResource(R.drawable.youlocationbig);
        if (A_mapView.getCallOut("you") != null) {
            A_mapView.removeCallOut("you");
        }
        youcallout.setLocation(point2D.getX(), point2D.getY());
        youcallout.setContentView(youImage);
        A_mapView.addCallout(youcallout, "you");
//        A_mapView.refresh();
        A_map.refresh();
    }

    /**
     * 信鸽推送注册
     */
    private void pushMessage() {
        XGPushManager.registerPush(context, myID, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object o, int i) {
                Log.i("LINK", "信鸽注册成功 " + " object = " + o + " i = " + i);
            }

            @Override
            public void onFail(Object o, int i, String s) {
                Log.i("LINK", "信鸽注册失败 " + " object = " + o + " i = " + i + "S = " + s);
            }
        });
    }

    /**
     * 显示对方位置
     * 需要在广播中
     * @param lon
     * @param lat
     */
    public void showYouLocation(String lon, String lat) {
        youlongitude = Double.parseDouble(lon);
        youlatitude = Double.parseDouble(lat);
        Log.i("LINK","接收到的对方位置数据为:" + youlongitude + "---" + youlatitude);
//        ShowYouPoint(LON, LAT);
//        ShowYouPoint(youlongitude,youlatitude);
        showLocal = true;

    }

    /**
     * 移除对方位置
     */
    public void removeMark() {
        if (A_mapView.getCallOut("you") != null) {
            A_mapView.removeCallOut("you");
        }
    }

    private void Vertifyidentify(final String username) {
        BmobQuery<Person> query = new BmobQuery<Person>();
        query.findObjects(ShareLocation.this, new FindListener<Person>() {
            @Override
            public void onSuccess(List<Person> list) {
                Log.i("AAAA", "username = " + username);
                success = true;
                for (int i = 0; i < list.size(); i++) {
                    String name = list.get(i).getName();
//                    Log.i("LINK", "唯一 id:" + list.get(i).getObjectId() + "----" + name);
                    if (name.equals(username)) {

                        if (Visit(myID, username)) {
                            //访问服务器成功
                            Log.i("AAAA", "访问成功");
                            cancel_share.setVisibility(View.VISIBLE);
                            keepShare = true;
                            startMina(input_edittext.getText().toString().trim());
                        } else {
                            Log.i("AAAA", "访问失败");
                        }
                        break;
                    } else {
                        success = false;
//                        Log.i("LINK","用户数据查询--" + name);
                    }
                }
                if(!success){
                    Toast.makeText(getApplicationContext(),"该用户不存在",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(int i, String s) {
//                success = false;
                Log.i("LINk", "用户信息检索失败！");

            }
        });
    }

    /**
     * 访问服务器
     *
     * @return 是否访问成功
     */
    private boolean Visit(String myId, String youID) {
        java.util.Map<String, String> param = new HashMap<String, String>();
        param.put("myID", myId);
        param.put("youID", youID);

        String url = IP.getUrl() + "/ShareLocal/servlet/ShareLocation";
        Boolean fa = HttpCon.startLink(param, url, getApplication());
        Log.i("AAAA", String.valueOf(fa));
        return fa;
    }

    /**
     * 分享位置对话框
     *
     * @param id
     * @param message
     */
    public static void showDialog(String id, String message) {
        final String ID = id;
        Log.i("LINK", "MESSAGE == " + message);
//        youlocationMarker.remove();
        android.support.v7.app.AlertDialog.Builder normalDialog = new android.support.v7.app.AlertDialog.Builder(context);
        normalDialog.setIcon(R.mipmap.ic_launcher);
        normalDialog.setTitle("提示！");
        normalDialog.setMessage(message);
        normalDialog.setCancelable(false);
        normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("LINK", "确定");

                cancel_share.setVisibility(View.VISIBLE);
                keepShare = true;
                startMina(ID);
            }
        });
        normalDialog.setNegativeButton("忽略", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("LINK", "忽略 == ");
            }
        });
        normalDialog.show();
    }

    //接受本地广播
    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MapActivity", "地图活动收到来自定位服务的广播！");
            Log.i("LINK", "获得数据：" + longitude + "," + latitude);
            ShowMyPoint(longitude, latitude);

            //显示对方位置
            if(showLocal){
                Log.i("LINK","ShowYouPoint");
                ShowYouPoint(youlongitude,youlatitude);
            }
        }
    }

}