package com.example.songt.pathmanager.Activity;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.songt.pathmanager.Fragement.FragementPersonal;
import com.example.songt.pathmanager.Fragement.FragementRecord;
import com.example.songt.pathmanager.Fragement.FragementReport;
import com.example.songt.pathmanager.R;
import com.example.songt.pathmanager.Service.GPSService;
import com.example.songt.pathmanager.Service.ServiceUtils;
import com.example.songt.pathmanager.Toole.ActivityCollector;
import com.example.songt.pathmanager.Toole.CopyZipFileToSD;
import com.example.songt.pathmanager.Toole.DateTimeUtil;
import com.example.songt.pathmanager.Toole.MyApplication;
import com.example.songt.pathmanager.Toole.UnzipAssets;
import com.example.songt.pathmanager.supermapservice;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUIEmptyView;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.supermap.data.CoordSysTranslator;
import com.supermap.data.Dataset;
import com.supermap.data.DatasetVector;
import com.supermap.data.Datasource;
import com.supermap.data.FieldInfos;
import com.supermap.data.GeoPoint;
import com.supermap.data.Geometry;
import com.supermap.data.Point2D;
import com.supermap.data.Point2Ds;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.Recordset;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.mapping.Map;
import com.supermap.mapping.MapControl;
import com.supermap.mapping.MapView;
import com.supermap.navi.Navigation;
import com.supermap.plugin.LocationManagePlugin;
import com.supermap.services.DataUploadService;
import com.supermap.services.FeatureSet;
import com.supermap.services.ResponseCallback;
import com.supermap.track.Track;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import haut.lifemanager.dbscan.DBScan;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static com.supermap.data.CursorType.DYNAMIC;
import static com.supermap.data.CursorType.STATIC;

public class MainActivity extends AppCompatActivity {
    //程序状态代码
    private int AppState = 0;//0:未请求权限 -1：拒绝请求权限 1：允许请求权限
    private int SavePointNum = 0;//判定定位是否失效

    private static final int PERMISSIONS_REQUEST_CODE = 0;//缺少权限个数

    private Workspace workspace;
    private MapView mapView;
    private MapControl mapControl;
    private Map map;
    private Track track;
    private Navigation navigation;
    private Datasource dataSource;
    private Datasource DaydataSource;
    private Datasource MonthdataSource;
    private Datasource Analyse_day;
    private Datasource Analyse_month;
    Intent startIntent;//服务

    //本地广播（得到服务开启信号）
    private LocalBroadcastManager StartServer_localBroadcastManager;
    private IntentFilter StartServer_intentFilter;
    private LR_StateSaveToUDb StartServer_localReceiver;
    //本地广播（得到服务关闭信号）
    private LocalBroadcastManager StopServer_localBroadcastManager;
    private IntentFilter StopServer_intentFilter;
    private LR_StateSaveToUDb StopServer_localReceiver;
    //本地广播(接受位置改变广播并得到当前坐标)
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private GPSService.GetLocation getLocation;
    private double latitude;
    private double longtiude;
    private double altitude;
    private float accuracy=1;//定位精度
    private int provider=-1;//定位来源
    //本地广播之自动制作月数据完成
    private BR_AutoMMDBuccess LR_AutoMMUDBSuccess;
    private LocalBroadcastManager LBM_AutoMMDBSuccess;
    private IntentFilter AutoMMDBSuccess_intentFilter;

    //腾讯地图定位
    private TencentLocationManager TencentLocationManager;
    TencentLocationListener listener;
    TencentLocationRequest request;

    DataUploadService dataUploadService;//数据上传

    private QMUIEmptyView qmuiEmptyView;
    Context context;
    PrjCoordSys prjCoordSys;

    private long time;

    private FragementPersonal fragementPersonal = new FragementPersonal();
    private FragementReport fragementReport = new FragementReport();
    private FragementRecord fragementRecord = new FragementRecord();

    //集成类
    public supermapservice mapservice;


    //活动第一次被创建时启用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);//加入到活动管理器
        setContentView(R.layout.activity_main);

        time = Calendar.getInstance().getTimeInMillis();
        Log.i("MainActivity","当前时间："+time);
        context = this;
        qmuiEmptyView = (QMUIEmptyView)findViewById(R.id.QMUIEmptyView);
        qmuiEmptyView.bringToFront();


        InputBasicsData();//导入许可和地图
        initPrivilege();//初始化权限
        initBroadcast();//初始化广播接收器
        initMapSave();//初始化轨迹记录组件


        //在子进程中完成数据制作
        new Thread(new Runnable() {
            @Override
            public void run() {
                AutoMakeDayUDB();//自动制作近三天数据
                AutoMakeMonthUDB();//制作本月数据
                //试验驻停点算法
                //AnalyseDay("Dataset_2018_8_12");
                //AnalyseMonth("Dataset_2018_8");
            }
        }).start();


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
        QMUITabState();//加载QMUITab UI
        replaceFragement(fragementRecord);

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
        workspace.dispose();
        StartServer_localBroadcastManager.unregisterReceiver(StartServer_localReceiver);//注销本地广播接收器
        StopServer_localBroadcastManager.unregisterReceiver(StopServer_localReceiver);
        localBroadcastManager.unregisterReceiver(localReceiver);
        LBM_AutoMMDBSuccess.unregisterReceiver(LR_AutoMMUDBSuccess);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            new QMUIDialog.MessageDialogBuilder(this)
                    .setTitle("关闭程序确认")
                    .setMessage("退出程序将失去智能化体验，确认退出？")
                    .addAction("退出程序", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            //推出程序
                            dialog.dismiss();
                            Log.i("MainActivity", "退出程序");
                            stopLocationService();//停止服务
                            ActivityCollector.finishAll();
                            //android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    })
                    .addAction("后台运行", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            //程序后台运行
                            Log.i("MainActivity", "程序后台运行");
                            //启动一个意图,回到桌面
                            Intent backHome = new Intent(Intent.ACTION_MAIN);
                            backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            backHome.addCategory(Intent.CATEGORY_HOME);
                            startActivity(backHome);
                        }
                    })
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    //QMUITabSegment
    protected void QMUITabState(){
        //QMUITabSegment
        final QMUITabSegment myQTabSegment = findViewById(R.id.tabs);
        myQTabSegment.reset();//重置
        myQTabSegment.setHasIndicator(true);  //是否需要显示indicator
        myQTabSegment.setIndicatorPosition(false);//true 时表示 indicator 位置在 Tab 的上方, false 时表示在下方
        myQTabSegment.setIndicatorWidthAdjustContent(false);//设置 indicator的宽度是否随内容宽度变化
        myQTabSegment.setDefaultTabIconPosition(QMUITabSegment.ICON_POSITION_TOP);//设置图标在左边
        int normalColor = QMUIResHelper.getAttrColor(this, R.attr.qmui_config_color_gray_6);
        int selectColor = QMUIResHelper.getAttrColor(this, R.attr.qmui_config_color_blue);
        myQTabSegment.setDefaultNormalColor(normalColor);    //设置tab正常下的颜色
        myQTabSegment.setDefaultSelectedColor(selectColor);    //设置tab选中下的颜色
        Drawable normalDrawable_path = ContextCompat.getDrawable(this, R.drawable.ic_path_record);
        normalDrawable_path.setBounds(0, 0, 80, 80);
        QMUITabSegment.Tab path_recore = new QMUITabSegment.Tab(
                normalDrawable_path,//设置图标
                null,//选中图标
                "生活记录", true,false
        );
        Drawable normalDrawable_report = ContextCompat.getDrawable(this, R.drawable.ic_personal_report);
        normalDrawable_report.setBounds(0, 0, 80, 80);
        QMUITabSegment.Tab personal_report = new QMUITabSegment.Tab(
                normalDrawable_report,
                null,
                "个人报表", true,false
        );
        Drawable normalDrawable_center = ContextCompat.getDrawable(this, R.drawable.ic_personal_center);
        normalDrawable_center.setBounds(0, 0, 80, 80);
        QMUITabSegment.Tab personal_center = new QMUITabSegment.Tab(
                normalDrawable_center,
                null,
                "个人中心", true,false
        );
        myQTabSegment.addTab(path_recore);
        myQTabSegment.addTab(personal_report);
        myQTabSegment.addTab(personal_center);
        myQTabSegment.notifyDataChanged();//刷新
        myQTabSegment.selectTab(0);
        //mTabSegment选项被选中的监听
        myQTabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                //myQTabSegment.hideSignCountView(index);//隐藏红点
                switch(index)
                {
                    case 0:
                        replaceFragement(fragementRecord);
                        break;
                    case 1:
                        replaceFragement(fragementReport);
                        break;
                    case 2:
                        replaceFragement(fragementPersonal);
                        break;
                    default:
                        break;
                }
            }
            //Tab被取消点击
            @Override
            public void onTabUnselected(int index) {

            }
            //Tab再次被点击
            @Override
            public void onTabReselected(int index) {
                //myQTabSegment.hideSignCountView(index);
            }
            //Tab被双击
            @Override
            public void onDoubleTap(int index) {
            }
        });
    }

    //初始化权限
    private void initPrivilege(){
        DynamicPrivilege();
        //检查权限是否完整
        if(checkPermissionAllGranted(Privilege)) {
            Log.i("MainActivity", "权限完整！");
            //Toast.makeText(MainActivity.this,"管家开始工作啦！",Toast.LENGTH_SHORT).show();
            AppState=2;//授权成功
        }
        else{
            Log.i("MainActivity", "需要获取权限！");
            //GMUIDDynamicPrivilege();
            DynamicPrivilege();
        }
        //检查授权是否成功
        if (checkPermissionAllGranted(Privilege)==false){
            GMUIDNoServiceState();
        }
    }
    //初始化广播
    private void initBroadcast(){
        //注册本地广播监听器(服务开启)
        StartServer_intentFilter = new IntentFilter();
        StartServer_intentFilter.addAction("com.example.broadcasttest.My_BROADCASTSTARTSERVICE");
        StartServer_localReceiver = new LR_StateSaveToUDb();
        StartServer_localBroadcastManager = LocalBroadcastManager.getInstance(this);
        StartServer_localBroadcastManager.registerReceiver(StartServer_localReceiver,StartServer_intentFilter);

        //注册本地广播监听器（服务结束）
        StopServer_intentFilter = new IntentFilter();
        StopServer_intentFilter.addAction("com.example.broadcasttest.My_BROADCASTSTOPSERVICE");
        StopServer_localReceiver = new LR_StateSaveToUDb();
        StopServer_localBroadcastManager = LocalBroadcastManager.getInstance(this);
        StopServer_localBroadcastManager.registerReceiver(StopServer_localReceiver,StopServer_intentFilter);

        //注册本地广播监听器（位置变化）
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcasttest.My_BROADCASTLOCATION");
        localReceiver = new LocalReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);

        //注册月数据制作完成广播监听器
        AutoMMDBSuccess_intentFilter = new IntentFilter();
        AutoMMDBSuccess_intentFilter.addAction("com.example.broadcasttest.My_BROADCASTMMDBSUCCESS");
        LR_AutoMMUDBSuccess = new BR_AutoMMDBuccess();
        LBM_AutoMMDBSuccess = LocalBroadcastManager.getInstance(this);//获取实例
        LBM_AutoMMDBSuccess.registerReceiver(LR_AutoMMUDBSuccess,AutoMMDBSuccess_intentFilter);
    }
    //导入许可和地图
    private void InputBasicsData(){

        //初始化超图组件许可信息
        String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        //导入许可文件
        if(fileIsExists(rootPath+"/SuperMap/License/SuperMap iMobile Trial.slm")==false){
            Log.i("MainActivity","缺少许可文件，开始导入！");
            CopyZipFileToSD copyPermissionFileToSD = new CopyZipFileToSD(MyApplication.getContext(),"SuperMap iMobile Trial.slm",rootPath + "/SuperMap/License/");
            copyPermissionFileToSD.copy();
        }
        if(fileIsExists(rootPath+"/SuperMap/LifeManager/BasicMap/BasicMap.smwu")==false) {
            Log.i("MainActivity","缺少基础地图，开始导入！");
            try{
                UnzipAssets unzipAssets = new UnzipAssets();
                unzipAssets.unZip(MyApplication.getContext(),"BasicMap.zip",rootPath + "/SuperMap/LifeManager/BasicMap/");
            }catch (Exception e){
                Log.i("MainActivity","解压出错！");
            }
        }

//        //在新进程中导入许可信息和基础地图
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }).start();
    }
    //判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f=new File(strFile);
            if(!f.exists()) { return false; }
        } catch (Exception e) { return false; }
        return true;
    }
    //启动服务
     final public void startLocationService()
    {

        Log.i("MainActivity", "试图启动服务！");
        startIntent = new Intent(this, GPSService.class);
        startService(startIntent);
        bindService(startIntent,connection,BIND_AUTO_CREATE);//绑定服务
        Log.i("MainActivity", "服务运行！");


        //注册日期变化系统广播监听器
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(TimeChangeReceiver,filter);
        StartSavetoUdb();//开始准备存储数据集

    }
    //停止服务
    public void stopLocationService()
    {

        Log.i("MainActivity", "停止服务！");
        Intent stopIntent = new Intent(this, GPSService.class);
        //unbindService(connection);//解绑服务
        stopService(stopIntent);
        Log.i("MainActivity", "服务未在运行！");
        Log.i("yzy", "morphToSquare..");

    }

    //获取服务运行状态
    public boolean SetviceState(){
         if(ServiceUtils.isServiceRunning(this,"com.example.songt.pathmanager.Service.GPSService"))
             return true;
         else
             return false;
    }

    //获取数据源

    public Datasource getDataSource() {
        return dataSource;
    }

    public Datasource getMonthdataSource() {
        return MonthdataSource;
    }

    public Datasource getDaydataSource() {
        return DaydataSource;
    }

    public Datasource getAnalyse_day() {
        return Analyse_day;
    }

    public Datasource getAnalyse_month() {
        return Analyse_month;
    }

    //GMUIDialog询问是否无服务开启软件
    private boolean GMUIDNoServiceState(){
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("服务授权")
                .setMessage("程序完整服务需要获取权限，权限不完整将无法开启所有功能，是否继续？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        AppState=-3;
                        Log.i("MainActivity", "用户选择了取消！（不允许软件无服务运行）");
                        Toast.makeText(getApplicationContext(), "我会一直等你回来！", Toast.LENGTH_SHORT).show();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        Log.i("MainActivity", "用户选择了确定！（允许软件无服务运行）");
                        Toast.makeText(getApplicationContext(), "软件启动", Toast.LENGTH_SHORT).show();
                        AppState=3;//在未授权的情况下启动软件
                    }
                })
                .show();

        if(AppState==-3) {
            return false;
        }
        else{
            return true;
        }
    }
    //GMUIDialog动态申请权限
    private boolean GMUIDDynamicPrivilege(){
        //动态获取权限
            new QMUIDialog.MessageDialogBuilder(this)
                    .setTitle("服务授权")
                    .setMessage("为了您更智能的体验感受需要获取一些权限，开始服务授权？")
                    .addAction("取消", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            //不允许获取权限
                            dialog.dismiss();
                            AppState=-1;//记录软件状态为不允许授权
                            Log.i("MainActivity", "用户选择了取消！（不允许请求权限）");
                            //android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    })
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            //允许获取权限
                            AppState=1;//记录软件状态为允许授权
                            Log.i("MainActivity", "用户选择了确定！（允许请求权限）");
                            Log.i("MainActivity", "软件状态代码为："+AppState);
                            //动态获取权限
                            DynamicPrivilege();
                        }
                    })
                    .show();
            if(AppState==1)
            {
                Log.i("MainActivity", "用户允许请求权限！");
                Toast.makeText(getApplicationContext(), "授权成功", Toast.LENGTH_SHORT).show();
                return true;
            }
            else if(AppState==-1)
            {
                Log.i("MainActivity", "用户不允许请求权限！");
                return false;
            }
        Log.i("MainActivity", "请求权限窗口加载异常！");
            return false;
    }

    //定义需要的特殊权限
    static String Privilege[] = new String[] {
            //Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,//允许挂载和反挂载文件系统可移动存储
            Manifest.permission.CHANGE_WIFI_STATE,//改变WiFi状态
            Manifest.permission.ACCESS_FINE_LOCATION,//定位权限
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//SD卡写入
            Manifest.permission.READ_PHONE_STATE,//允许修改话机状态，如电源，人机接口等
            Manifest.permission.ACCESS_COARSE_LOCATION//允许一个程序访问CellID或WiFi热点来获取粗略的位置
    };
    //动态获取权限
    public boolean DynamicPrivilege()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CHANGE_WIFI_STATE);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (checkSelfPermission(ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(ACCESS_COARSE_LOCATION);
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(
                        new String[permissions.size()]), PERMISSIONS_REQUEST_CODE);
            }
        }
        //返回值异常，不可信
        if(checkPermissionAllGranted(Privilege)){
            Log.i("MainActivity", "请求权限完整！");
            return true;
        }
        else{
            Log.i("MainActivity", "请求权限不完整！");
            return false;
        }
    }
    //是否需要解释权限
    protected boolean ExpleinPrivilege(String[] privilege){
        int num = 0;
        for(String permission : privilege){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
                num++;
            }
        }
        if(num>0){
            Log.i("MainActivity","需要解释权限数量："+num);
            return true;
        }
        return false;
    }
    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        int num=0;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                num++;
                Log.i("MainActivity","缺少权限："+permission);
            }
        }
        if(num>0){
            Log.i("MainActivity","缺少权限数量："+num);
            return false;
        }
            return true;
    }

    /**
     * 打开 APP 的详情设置
     */
    private void openAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("备份通讯录需要访问 “通讯录” 和 “外部存储器”，请到 “应用信息 -> 权限” 中授予！");
        builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    //替换碎片布局布局
        private void replaceFragement(android.support.v4.app.Fragment fragment)
        {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction= fragmentManager.beginTransaction();
        transaction.replace(R.id.fragement,fragment);
        //transaction.addToBackStack(null);//放入返回栈
        transaction.commit();
    }

    public double getlocation_longitude(){
        Log.i("MainActivity", "当前位置：latitude="+latitude);
        return longtiude;
    }
    public double getlocation_latitude(){
        Log.i("MainActivity", "当前位置：longtiude="+longtiude);
        return latitude;
    }
    public void ServiceLocation()
    {
        Log.i("MainActitvity", "服务通讯：longitude"+longtiude+",latitude"+longtiude);
        if(latitude==0.0||longtiude==0.0)
        {
            Log.i("MainActivity","定位失败！");
            latitude=34.82980333333333;
            longtiude=113.5440166666666;

            latitude=0;
            longtiude=0;
        }
        else
        {
            Log.i("MainActivity","当前位置非0！");
        }
        Log.i("MainActitvity", "碎片通讯，发送：longitude"+longtiude+",latitude"+latitude);
    }

    //和服务建立通讯获取最新的经纬度
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("MapActivity","地图活动已建立和服务的通讯！");
            getLocation = (GPSService.GetLocation)service;
            altitude=getLocation.getlocation_altitude();//获取高程
            longtiude=getLocation.getlocation_longitude();//获取经度
            latitude=getLocation.getlocation_latitude();//获取纬度
            //获取腾讯定位参数
            TencentLocationManager = getLocation.getTencentLocationManager();//获取腾讯定位管理
            listener = getLocation.getTencentLocationListener();
            request = getLocation.getTexcentLocationRequest();

            Log.i("MapActivity","获得数据："+longtiude+","+latitude+","+altitude);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("MapActivity","通讯断开！");
        }
    };

    private void initMapSave(){

        mapservice = new supermapservice();
        mapservice.init(context);
        workspace = mapservice.getWorkplace();
        mapView = mapservice.getMaoView();
        mapControl = mapservice.getMapcontrol();
        navigation = mapservice.getNavigation();
        map = mapservice.getMap();
        prjCoordSys=mapservice.getPrjCoordSys();
        dataSource= mapservice.gettrack();
        DaydataSource = mapservice.getDay_Datasource();
        MonthdataSource = mapservice.getMonth_Datasource();
        Analyse_month = mapservice.getAnalyse_month();
        Analyse_day=mapservice.getAnalyse_day();


//        //iMobile初始化
//        String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//        //设置一些系统需要用到的路径
//        com.supermap.data.Environment.setLicensePath(rootPath + "/SuperMap/License/");
//        Log.i("MainActivity", "许可路径：" + rootPath + "/SuperMap/License/");
//        //组件功能必须在Environment初始化之后才能调用
//        com.supermap.data.Environment.initialization(this);
//        Log.i("MainActivity", "Environment初始化完毕！");
//        //打开工作空间
//        workspace = new Workspace();
//        WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
//        info.setServer(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/SuperMap/LifeManager/BasicMap/BasicMap.smwu");
//        info.setType(WorkspaceType.SMWU);
//        workspace.open(info);
//
//        mapView = new MapView(this);
//        mapControl = mapView.getMapControl();
//        navigation = mapControl.getNavigation();
//        map = mapControl.getMap();
//        map.setWorkspace(workspace);
//
//        prjCoordSys = mapControl.getMap().getPrjCoordSys();
//
//        dataSource = mapControl.getMap().getWorkspace().getDatasources().get("track");
//        DaydataSource = mapControl.getMap().getWorkspace().getDatasources().get("track_day");
//        MonthdataSource = mapControl.getMap().getWorkspace().getDatasources().get("track_month");
//        Analyse_day = mapControl.getMap().getWorkspace().getDatasources().get("track_analyse_day");
//        Analyse_month = mapControl.getMap().getWorkspace().getDatasources().get("track_analyse_month");

        track = new Track(this);
        track.setCustomLocation(true);            // 设置用户传入GPS数据
        track.setDistanceInterval(1);             // 设置距离间隔为1米
        track.setTimeInterval(1);                // 设置时间间隔为1s
        //设置抓路数据集
        Datasource trmatch = mapControl.getMap().getWorkspace().getDatasources().get("Road");//打开数据源
        if(trmatch != null) {
            track.setMatchDatasets(trmatch.getDatasets());
            Log.i("MainActivity","设置抓路数据集在Road数据源下！");
        }

        if(dataSource==null){
            Log.i("MainActivity","数据源为空！");
        }

    }
    int locationerror = 0;
    private void StartSavetoUdb(){
        Log.i("MainActivity","开始准备轨迹记录数据集！");
        //获取系统的日期
        Calendar calendar = Calendar.getInstance();
        //年
        int year = calendar.get(Calendar.YEAR);
        //月
        int month = calendar.get(Calendar.MONTH)+1;
        //日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        //获取系统时间
        // 小时
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //分钟
        int minute = calendar.get(Calendar.MINUTE);
        //秒
        int second = calendar.get(Calendar.SECOND);
        String datasetName ="Dataset_"+year+"_"+month+"_"+day+"_"+hour;
        Log.i("MainActivity","准备数据集："+datasetName);
        //不存在则创建，存在则打开
        if(dataSource.getDatasets().contains(datasetName)){
            Log.i("MainActivity","准备打开数据集+"+datasetName);
            DatasetVector dataset = (DatasetVector) dataSource.getDatasets().get(datasetName);
            Log.i("MainActivity","打开数据集+"+datasetName);
            if(dataset.getRecordset(false,DYNAMIC).getRecordCount()==SavePointNum){
                Log.e("MainActivity","检测到网络定位异常！");
                locationerror++;//错误次数加一
            }
            if(locationerror>=0){
                if(locationerror>100){
                    Log.e("MainActivity","检测到网络定位异常过多！请求新位置！");
                    TencentLocationManager.removeUpdates(listener);
                    TencentLocationManager.requestLocationUpdates(request,listener);
                    locationerror=0;
                    Toast.makeText(this,"网络定位异常！",Toast.LENGTH_SHORT).show();
                }

            }
            else{
                locationerror=0;
            }
            SavePointNum =dataset.getRecordset(false,DYNAMIC).getRecordCount();
            Log.i("MainACTIVITY","当前数据集数据个数："+SavePointNum);

            track.setDataset(dataset);                                                 // 设置记录轨迹的点数据集
            track.startTrack();                                                        // 开始记录轨迹
            //Toast.makeText(MyApplication.getContext(),"打开数据集+"+datasetName,Toast.LENGTH_SHORT).show();
        }
        else{
            Log.i("MainActivity","准备创建数据集："+datasetName);
            Dataset Cdataset = track.createDataset(dataSource, datasetName);    // 创建数据集
            Log.i("MainActivity","创建数据集："+datasetName);
            track.setDataset(Cdataset);                                                 // 设置记录轨迹的点数据集
            track.startTrack();                                                        // 开始记录轨迹
            //Toast.makeText(MyApplication.getContext(),"创建数据集："+datasetName,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 收到位置变化广播后读取位置存入数据集
     */
    public void locating(){
        Point2D point2D = new Point2D();
        LocationManagePlugin.GPSData gpsData = new LocationManagePlugin.GPSData();
        //读取数据
        Log.i("MapACtivity","读取数据");
        Date date = new Date(System.currentTimeMillis());
        gpsData.lTime=date.getTime();//点位获取时间（毫秒）
        gpsData.dLatitude=latitude;
        gpsData.dLongitude=longtiude;
        gpsData.dAltitude=altitude;
        gpsData.dBearing=accuracy;//方向字段记录定位精度
        gpsData.dSpeed=provider;//速度字段记录定位方式

        if(gpsData.dLatitude==0.0){
            Log.i("MapActivity","无效数据（0）！");
        }

//        Point2D point2d0=new Point2D();
//        point2d0.setX(gpsData.dLongitude);
//        point2d0.setY(gpsData.dLatitude);
//        CoordSysTranslator  coordSysTranslator=new CoordSysTranslator();
//        //获取地图坐标系
//        PrjCoordSys prjCoordSys = mapControl.getMap().getPrjCoordSys();
//        Log.i("MapActivity","投影坐标系对象："+prjCoordSys.getName());
//        //坐标系转换
//        Point2Ds point2ds=new Point2Ds();
//        point2ds.add(point2d0);
//        Boolean isOk=coordSysTranslator.forward(point2ds,prjCoordSys);
//        point2D=point2ds.getItem(0);
//
//        Log.i("MapActivity","转换后的经纬度为："+point2D.getX()+","+point2D.getY());
//
//        if(point2D.getX()!=1.0){
//            //Toast.makeText(MyApplication.getContext(),"发现你啦！",Toast.LENGTH_SHORT).show();
//            SaveGpsData(gpsData);//记录数据到数据源中中
//        }

        SaveGpsData(gpsData);//记录数据到数据源中中
    }

    //记录点到数据集
    public void SaveGpsData(LocationManagePlugin.GPSData gpsData){
        if(track != null){
            //获取当前时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateTimeUtil.gainCurrentDate());

            //加密算法和地图有关，地图投影改变，加密方式改变
//            Point2D mPoint = navigation.encryptGPS(gpsData.dLongitude, gpsData.dLatitude);//加密算法
//            gpsData.dLongitude = mPoint.getX();
//            gpsData.dLatitude = mPoint.getY();

            //gpsData.dLongitude = mPoint.getX();
            //gpsData.dLatitude = mPoint.getY();
            //gpsData.dAltitude = altitude;
            gpsData.lTime = calendar.getTimeInMillis();
            gpsData.nDay = calendar.get(Calendar.DAY_OF_MONTH);
            gpsData.nMonth = calendar.get(Calendar.MONTH)+1;
            gpsData.nYear = calendar.get(Calendar.YEAR);
            gpsData.nHour = calendar.get(Calendar.HOUR);
            gpsData.nMinute = calendar.get(Calendar.MINUTE);
            gpsData.nSecond = calendar.get(Calendar.SECOND);

            track.setGPSData(gpsData);            // 设置GPS数据 ，在setCustomLocation(true)时，设置的数据有效
            DatasetVector datasetVector = (DatasetVector) track.getDataset();
            int num = datasetVector.getRecordset(true,STATIC).getRecordCount();
            Log.i("MainActivity","已记录位置！longitude："+gpsData.dLongitude+",Latitude："+gpsData.dLatitude+",altitude"+gpsData.dAltitude);
            StartSavetoUdb();
            //Toast.makeText(MyApplication.getContext(),"我记住你的位置啦！",Toast.LENGTH_SHORT).show();
        }
    }

    //接受本地广播
    class LocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MapActivity","地图活动收到来自定位服务的广播！");
            //Toast.makeText(MyApplication.getContext(),"收到广播！",Toast.LENGTH_SHORT).show();
            bindService(startIntent,connection,BIND_AUTO_CREATE);//绑定服务
            altitude=getLocation.getlocation_altitude();//获取高程
            longtiude=getLocation.getlocation_longitude();//获取经度
            latitude=getLocation.getlocation_latitude();//获取纬度
            provider=getLocation.getlocation_provider();//获取定位方式
            accuracy=getLocation.getlocation_accuracy();//获取精度
            Log.i("MapActivity","获得数据："+longtiude+","+latitude+","+altitude+"定位方式:"+provider+" 定位精度:"+accuracy);
            locating();
        }
    }

    public class LR_StateSaveToUDb extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MapActivity","主活动收到服务启动广播！");

            //Toast.makeText(MyApplication.getContext(),"收到广播！",Toast.LENGTH_SHORT).show();
        }
    }

    public class LR_StopSaveToUDB extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MapActivity","主活动收到服务关闭广播！");

            //Toast.makeText(MyApplication.getContext(),"收到广播！",Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver TimeChangeReceiver = new BroadcastReceiver() {

        int oldhour=0;
        int newhour=0;

        @Override
        public void onReceive(Context context, Intent intent) {

                Log.i("MapActivity","主活动收到时间变化系统广播！");
                //获取系统的日期
                Calendar calendar = Calendar.getInstance();
                //年
                int year = calendar.get(Calendar.YEAR);
                //月
                int month = calendar.get(Calendar.MONTH)+1;
                //日
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                //获取系统时间
                // 小时
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                //分钟
                int minute = calendar.get(Calendar.MINUTE);
                //秒
                int second = calendar.get(Calendar.SECOND);
                String Time =year+"."+month+"."+day+"  "+hour+":"+minute+":"+second;
                Log.i("MainActivity","接收到时间变化广播！当前时间："+Time);
                //当小时变化指定新的数据集
                newhour = hour;

                if(oldhour!=newhour){
                    oldhour = newhour;
                    DatasetVector datasetVector = (DatasetVector) dataSource.getDatasets().get("Dataset_"+year+"_"+month+"_"+day+"_"+oldhour);
                    if(datasetVector!=null){
                        Log.i("MainActivity","上传数据集非空！");
                        UploadData(datasetVector);//上传旧时间段数据到服务器
                    }
                    track.stopTrack();;//停止记录
                    StartSavetoUdb();//指定新的轨迹数据集并开始记录
                    Log.i("MainActivity","发现新的小时数，重新指定数据集！旧时间："+oldhour+" 新时间："+newhour);
                }
            }
    };

    private void MakeDayUDB(String day){
        String DayDatasetName = day;
        if(DaydataSource==null){
            Log.i("MainActivity","数据源为空！");
            return;
        }
        //不存在则创建，存在则打开
        DatasetVector DayDataset = null;
        if(DaydataSource.getDatasets().contains(DayDatasetName)){
            Log.i("MainActivity","日数据集已存在！删除后创建！");
            DaydataSource.getDatasets().delete(DayDatasetName);//删除
            DayDataset = track.createDataset(DaydataSource, DayDatasetName);    // 创建数据集
            Log.i("MainActivity","创建数据集："+DayDatasetName);
        }
        else{
            Log.i("MainActivity","准备创建数据集："+DayDatasetName);
            DayDataset = track.createDataset(DaydataSource, DayDatasetName);    // 创建数据集
            Log.i("MainActivity","创建数据集："+DayDatasetName);

        }
        for(int i=0;i<24;i++){
            String name = day+"_"+i;
            //存在则叠加
            if(dataSource.getDatasets().contains(name)){
                DatasetVector hourDV = (DatasetVector) dataSource.getDatasets().get(name);//打开数据集
                Recordset recordset = hourDV.getRecordset(false,DYNAMIC);//false返回所有记录的记录集，动态游标可修改记录集
                DayDataset.append(recordset);//追加记录集
            }
        }
        Log.i("MainActivity","日数据集工作完成！");
        if(DayDataset.getRecordset(false,DYNAMIC).getRecordCount()<1){
            Log.i("MainActivity","数据集为空，删除数据集！");
            DaydataSource.getDatasets().delete(day);
        }

        AnalyseDay(DayDatasetName);//分析日数据
    }

    private void MakeMonthUDB(String month){
        String MonthDatasetName = month;
        if(MonthdataSource==null){
            Log.i("MainActivity","数据源为空！");
            return;
        }
        //不存在则创建，存在则打开
        DatasetVector MonthDataset = null;
        if(MonthdataSource.getDatasets().contains(MonthDatasetName)){
            Log.i("MainActivity","本月数据集已存在！删除后创建！");
            MonthdataSource.getDatasets().delete(MonthDatasetName);//删除
            MonthDataset = track.createDataset(MonthdataSource, MonthDatasetName);    // 创建数据集
            Log.i("MainActivity","创建数据集："+MonthDatasetName);
        }
        else{
            Log.i("MainActivity","准备创建数据集："+MonthDatasetName);
            MonthDataset = track.createDataset(MonthdataSource, MonthDatasetName);    // 创建数据集
            Log.i("MainActivity","创建数据集："+MonthDatasetName);

        }
        for(int i=0;i<31;i++){
            String name = month+"_"+i;
            //存在则叠加
            if(DaydataSource.getDatasets().contains(name)){
                DatasetVector hourDV = (DatasetVector) DaydataSource.getDatasets().get(name);//打开数据集
                Recordset recordset = hourDV.getRecordset(false,DYNAMIC);//false返回所有记录的记录集，动态游标可修改记录集
                MonthDataset.append(recordset);//追加记录集
            }
        }
        Log.i("MainActivity","月数据集工作完成！");
        if(MonthDataset.getRecordset(false,DYNAMIC).getRecordCount()<1){
            Log.i("MainActivity","数据集为空，删除数据集！");
            MonthdataSource.getDatasets().delete(month);
        }

        AnalyseMonth(MonthDatasetName);//分析月数据
    }

    //制作三天内的数据集集合
    private void AutoMakeDayUDB(){
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int month;//月份
                month = calendar.get(Calendar.MONTH)+1;

                //向后退三天
                date = DateTimeUtil.subDateTime(date,48);
                calendar.setTime(date);
                month = calendar.get(Calendar.MONTH)+1;

                Log.i("MainActivity","开始自动生成三天数据从"+calendar.get(Calendar.YEAR)+"_"+month+"_"+calendar.get(Calendar.DATE)+"开始制作！");
                MakeDayUDB("Dataset_"+calendar.get(Calendar.YEAR)+"_"+month+"_"+calendar.get(Calendar.DATE));
                date = DateTimeUtil.addDateTime(date,24);
                calendar.setTime(date);
                month = calendar.get(Calendar.MONTH)+1;
                MakeDayUDB("Dataset_"+calendar.get(Calendar.YEAR)+"_"+month+"_"+calendar.get(Calendar.DATE));
                date = DateTimeUtil.addDateTime(date,24);
                calendar.setTime(date);
                month = calendar.get(Calendar.MONTH)+1;
                MakeDayUDB("Dataset_"+calendar.get(Calendar.YEAR)+"_"+month+"_"+calendar.get(Calendar.DATE));

    }

    //制作一个月内的数据集合
    private void AutoMakeMonthUDB() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month;//月份
        month = calendar.get(Calendar.MONTH) + 1;
        Log.i("MainActivity", "开始自动生成本月数据：" + calendar.get(Calendar.YEAR) + "_" + month);
        MakeMonthUDB("Dataset_" + calendar.get(Calendar.YEAR) + "_" + month);
        Intent intent = new Intent("com.example.broadcasttest.My_BROADCASTMMDBSUCCESS");
        LBM_AutoMMDBSuccess.sendBroadcast(intent);//发送任务完成广播
    }
    public class BR_AutoMMDBuccess extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MapActivity","自动月数据制作完毕！");
            //Toast.makeText(MyApplication.getContext(),"收到广播！",Toast.LENGTH_SHORT).show();
            qmuiEmptyView.setVisibility(View.GONE);
        }
    }
    //上传数据集到云服务器
    private void UploadData(DatasetVector datasetVector){
        dataUploadService = new DataUploadService("http://39.106.27.150:8090");
        dataUploadService.setResponseCallback(new ResponseCallback() {
            @Override
            public void requestFailed(String s) {
                Log.i("MapActivity","上传请求失败！");
                Log.e("Upload: ", s);
                Toast.makeText(context,"同步失败！",Toast.LENGTH_SHORT).show();
                //StopLoad();
            }

            @Override
            public void requestSuccess() {
                Log.i("MapActivity","上传成功！");
                Toast.makeText(context,"同步成功！",Toast.LENGTH_SHORT).show();
                //StopLoad();
            }

            @Override
            public void addFeatureSuccess(int i) {
                Log.i("MapActivity","上传addFeatureSuccess");
            }

            @Override
            public void receiveResponse(FeatureSet featureSet) {
                Log.i("MapActivity","上传receiveResponse");
            }

            @Override
            public void dataServiceFinished(String s) {
                Log.i("MapActivity","上传dataServiceFinished");
            }
        });
        final String url = "http://39.106.27.150:8090/iserver/services/data-BasicMap/rest/data/datasources/track_month/datasets/Everyone";
        final Recordset recordset = datasetVector.getRecordset(false,DYNAMIC);
        if(recordset.getRecordCount()<1){
            Log.i("MapActivity","记录集为空，不可上传！");
            return;
        }
        //dataUploadService.addRecordset(url,recordset);
        //在子进程中完成耗时操作
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //在此执行耗时操作
                Log.i("MapActivity","向："+url+"上传数据集！");
                try{
                    dataUploadService.addRecordset(url,recordset);
                }catch (Exception e){
                    Log.e("MainActivity","上传出错，原因："+e.getMessage());
                }


            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                new Handler().post(runnable);//在子线程中直接去new 一个handler
                Looper.loop();
            }
        }).start();

        //dataUploadService.commitDataset(url,datasetVector);
    }

    private Point2Ds ReadDataveratrToPoint2d(Recordset recordset){
        Log.i("MapActivity","开始读取数据集点！共需读取数据："+recordset.getRecordCount()+"个");
        Point2Ds point2Ds = new Point2Ds();
        int n = recordset.getRecordCount();
        for(int i=0;i<n;i++){
            recordset.moveTo(i);
            Point2D point2D = recordset.getGeometry().getInnerPoint();//坐标投影转换
            if(point2D==null){
                Log.i("MaooActivity","加载到空点！");
            }
            //Log.i("MaooActivity","点位权值："+dw);
            point2Ds.add(point2D);
        }
        Log.i("MapActivity","读取数据集到图标点数据集完成！共读取数据："+recordset.getRecordCount()+"个");
        return point2Ds;
    }

    //投影转换
    private Point2D PrjTransToMap(Point2D point2D){
        Point2Ds point2Ds = new Point2Ds();
        point2Ds.add(point2D);
        //获取地图坐标系
        prjCoordSys = mapControl.getMap().getPrjCoordSys();
        CoordSysTranslator coordSysTranslator = new CoordSysTranslator();
        coordSysTranslator.forward(point2Ds,prjCoordSys);
        return point2Ds.getItem(0);
    }

    //分析日数据
    private void AnalyseDay(String day){
        //开始计时
        Date date = new Date();
        Log.i("MainActivity","开始分析日数据！开始时间:"+date.getTime());
        String DayDatasetName = day;
        //检车源数据源和分析数据源是否同时存在
        if(Analyse_day==null||DaydataSource==null){
            Log.i("MainActivity","数据源为空！");
            return;
        }
        //检查分析数据集是否存在
        if(DaydataSource.getDatasets().contains(DayDatasetName)==false){
            Log.i("MainActivity","分析数据集不存在，停止分析！");
            return;
        }
        DatasetVector A_DayDataset = null;
        DatasetVector DayDataset = (DatasetVector)DaydataSource.getDatasets().get(day);
        //不存在则创建，存在则覆盖
        if(Analyse_day.getDatasets().contains(DayDatasetName)){
            Log.i("MainActivity","数据集已存在！删除后创建！");
            Analyse_day.getDatasets().delete(DayDatasetName);//删除
            A_DayDataset = track.createDataset(Analyse_day, DayDatasetName);    // 创建数据集
            if(A_DayDataset==null){
                Log.i("MainActivity","创建数据集："+DayDatasetName+"失败！");
            }

        }
        else{
            Log.i("MainActivity","准备创建数据集："+DayDatasetName);
            /*
            DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
            datasetVectorInfo.setName(day);
            datasetVectorInfo.setType(DatasetType.POINT);
            Analyse_day.getDatasets().create(datasetVectorInfo);
            */
            A_DayDataset = track.createDataset(Analyse_day, DayDatasetName);    // 创建数据集
            if(A_DayDataset==null){
                Log.i("MainActivity","创建数据集："+DayDatasetName+"失败！");
            }
            //Log.i("MainActivity","创建数据集："+DayDatasetName);

        }

        Point2Ds point2Ds = ReadDataveratrToPoint2d(DayDataset.getRecordset(false,DYNAMIC));

        if(point2Ds.getCount()<300){
            Log.i("MainActivity","数据分析数据量不足！");
            Analyse_day.getDatasets().delete(DayDatasetName);//删除刚刚新建的数据集
            return;
        }

        DBScan dbScan = new DBScan();
        Point2Ds gread = dbScan.begin(point2Ds);//开始分析
        Recordset recordset = A_DayDataset.getRecordset(false,DYNAMIC);//返回空记录集
        int num = gread.getCount();
        recordset.moveFirst();



        //将points存储到recordset中
        // 获得记录集对应的批量更新对象
        Recordset.BatchEditor editor = recordset.getBatch();
        // 设置批量更新每次提交的记录数目
        editor.setMaxRecordCount(50);
        // 从 World 数据集中读取几何对象和字段值，批量更新到 example 数据集中
        editor.begin();
        Point2D point2D;
        for(int i=0;i<num;i++){
            point2D = gread.getItem(i);
            //Log.i("MainActivity","分析结果"+i+"号数据："+point2D.getX()+","+point2D.getY());
            Geometry geoPoint = new GeoPoint(point2D);
            //Log.i("MainActivity","转存结果"+i+"号数据："+geoPoint.getX()+","+geoPoint.getY());
            if(recordset.addNew(geoPoint)==false){
                Log.i("MainActivity","存储失败！");
            }
            recordset.addNew(geoPoint);
            geoPoint.dispose();
        }
        // 批量操作统一提交
        editor.update();

        Log.i("MainActivity","驻停点分析工作完成！分析点数："+point2Ds.getCount());
        //A_DayDataset.append(recordset);
        if(A_DayDataset.getRecordset(false,DYNAMIC).getRecordCount()<1){
            Log.i("MainActivity","数据集为空，删除数据集！");
            Analyse_day.getDatasets().delete(day);
        }else{
            Log.i("MainActivity","成果个数："+A_DayDataset.getRecordset(false,DYNAMIC).getRecordCount());
        }
        Date datefinish = new Date();
        long time = datefinish.getTime()-date.getTime();
        time/=1000;//进为秒
        Log.i("MainActivity",day+"耗费时间："+time+"秒");
    }

    //分析月数据
    private void AnalyseMonth(String month){
        //开始计时
        Date date=new Date();
        Log.i("MainActivity","开始分析时间:"+date.getTime());
        Log.i("MainActivity","开始分析月数据！");
        String MonthDatasetName = month;
        //检车源数据源和分析数据源是否同时存在
        if(Analyse_month==null||MonthdataSource==null){
            Log.i("MainActivity","数据源为空！");
            return;
        }
        //检查分析数据集是否存在
        if(MonthdataSource.getDatasets().contains(MonthDatasetName)==false){
            Log.i("MainActivity","分析数据集不存在，停止分析！");
            return;
        }
        DatasetVector A_MonthDataset = null;
        DatasetVector MonthDataset = (DatasetVector)MonthdataSource.getDatasets().get(MonthDatasetName);
        //不存在则创建，存在则覆盖
        if(Analyse_month.getDatasets().contains(MonthDatasetName)){
            Log.i("MainActivity","数据集已存在！删除后创建！");
            Analyse_month.getDatasets().delete(MonthDatasetName);//删除
            A_MonthDataset = track.createDataset(Analyse_month, MonthDatasetName);    // 创建数据集
            Log.i("MainActivity","创建数据集："+MonthDatasetName);
        }
        else{
            Log.i("MainActivity","准备创建数据集："+MonthDatasetName);
            /*
            DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
            datasetVectorInfo.setName(MonthDatasetName);
            datasetVectorInfo.setType(DatasetType.POINT);
            Analyse_month.getDatasets().create(datasetVectorInfo);
            */
            A_MonthDataset = track.createDataset(Analyse_month, MonthDatasetName);    // 创建数据集
            Log.i("MainActivity","创建数据集："+MonthDatasetName);
        }

        Point2Ds point2Ds = ReadDataveratrToPoint2d(MonthDataset.getRecordset(false,DYNAMIC));
        if(point2Ds.getCount()<300){
            Log.i("MainActivity","数据分析数据量不足！");
            Analyse_day.getDatasets().delete(MonthDatasetName);//删除刚刚新建的数据集
            return;
        }

        DBScan dbScan = new DBScan();
        Point2Ds gread = dbScan.begin(point2Ds);//开始分析
        Recordset recordset = A_MonthDataset.getRecordset(false,DYNAMIC);//返回空记录集
        int num = gread.getCount();
        recordset.moveFirst();

        // 获得记录集对应的批量更新对象
        Recordset.BatchEditor editor = recordset.getBatch();
        // 设置批量更新每次提交的记录数目
        editor.setMaxRecordCount(300);
        // 从 World 数据集中读取几何对象和字段值，批量更新到 example 数据集中
        editor.begin();
        Point2D point2D;
        for(int i=0;i<num;i++){
            point2D = gread.getItem(i);
            //Log.i("MainActivity","分析结果"+i+"号数据："+point2D.getX()+","+point2D.getY());
            Geometry geoPoint = new GeoPoint(point2D);
            //Log.i("MainActivity","转存结果"+i+"号数据："+geoPoint.getX()+","+geoPoint.getY());
            if(recordset.addNew(geoPoint)==false){
                Log.i("MainActivity","存储失败！");
            }
            recordset.addNew(geoPoint);
            geoPoint.dispose();
        }
        // 批量操作统一提交
        editor.update();

        Log.i("MainActivity","驻停点分析工作完成！分析点数："+point2Ds.getCount());
        //A_DayDataset.append(recordset);
        if(A_MonthDataset.getRecordset(false,DYNAMIC).getRecordCount()<1){
            Log.i("MainActivity","数据集为空，删除数据集！");
            Analyse_day.getDatasets().delete(month);
        }else{
            Log.i("MainActivity","成果个数：："+A_MonthDataset.getRecordset(false,DYNAMIC).getRecordCount());
        }

        Date finishdate = new Date();
        Long time = finishdate.getTime()-date.getTime();
        time/=1000;//进为秒
        Log.i("MainActivity",month+"数据分析花费时间："+time+"秒");

    }
}
