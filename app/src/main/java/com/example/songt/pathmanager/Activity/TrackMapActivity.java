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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.songt.pathmanager.R;
import com.example.songt.pathmanager.Service.GPSService;
import com.example.songt.pathmanager.Toole.DateTimeUtil;
import com.example.songt.pathmanager.Toole.MyApplication;
import com.example.songt.pathmanager.supermapservice;
import com.hdl.calendardialog.CalendarView;
import com.hdl.calendardialog.CalendarViewDialog;
import com.hdl.calendardialog.DateUtils;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.supermap.data.Color;
import com.supermap.data.CoordSysTranslator;
import com.supermap.data.Dataset;
import com.supermap.data.DatasetVector;
import com.supermap.data.DatasetVectorInfo;
import com.supermap.data.Datasets;
import com.supermap.data.Datasource;
import com.supermap.data.Environment;
import com.supermap.data.GeoLine;
import com.supermap.data.GeoPoint;
import com.supermap.data.GeoStyle;
import com.supermap.data.Geometry;
import com.supermap.data.LicenseType;
import com.supermap.data.Maps;
import com.supermap.data.Point2D;
import com.supermap.data.Point2Ds;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.Recordset;
import com.supermap.data.Rectangle2D;
import com.supermap.data.Size2D;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.mapping.Action;
import com.supermap.mapping.Layer;
import com.supermap.mapping.Layers;
import com.supermap.mapping.Map;
import com.supermap.mapping.MapControl;
import com.supermap.mapping.MapView;
import com.supermap.mapping.ScaleView;
import com.supermap.mapping.imChart.ChartPoint;
import com.supermap.navi.Navigation;
import com.supermap.navi.SuperMapPatent;
import com.supermap.plugin.LocationManagePlugin;
import com.supermap.services.DataUploadService;
import com.supermap.services.FeatureSet;
import com.supermap.services.ResponseCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.example.songt.pathmanager.Toole.MyApplication.getContext;
import static com.supermap.data.CursorType.DYNAMIC;

public class TrackMapActivity extends AppCompatActivity {

    QMUITopBar mTopBar;//标题栏
    private Workspace A_workspace = null;//工作空间
    private Maps m_maps = null;//地图数据集合
    private MapControl m_mapcontrol = null;//地图控件
    private Map A_map = null;//地图
    ScaleView scaleView1=null;
    private MapView A_mapView = null;//地图控件
    PrjCoordSys prjCoordSys;
    private static Navigation mNavigation;//导航类
    private Point2D point2D;//点对象
    private LocationManagePlugin.GPSData gpsData;
    private Location location;

    Point2Ds point2dse=new Point2Ds();//定义轨迹点集
    private Datasource day_Datasource     = null;
    private Datasource month_Datasource   = null;
    private Datasource Analyse_day;
    private Datasource Analyse_month;
    private String datasetName;

    //本地广播
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private GPSService.GetLocation getLocation;
    private double latitude;
    private double longtiude;
    private double altitude;
    private IntentFilter ReadSucce_intentfilter;
    private ReadSucceLocalReceiver ReadSucce_licalReceiver;
    private LocalBroadcastManager ReadSucce_localBroadcastManager;
    String rootPath;
    Context context;
    private RelativeLayout view;
    QMUITipDialog ReadChat_tipDialog;
    QMUIRadiusImageView QMUIMonth;
    QMUIRadiusImageView QMUIDay;
    QMUIRadiusImageView QMUImap;
    //热力图
    private com.supermap.mapping.imChart.HeatMap A_heatMap;
    DataUploadService dataUploadService;//数据上传

    private Point2Ds point2Ds = null;//用于存储当次打开的轨迹
    private SharedPreferences UserInformation;

    private List<Long> markDays = new ArrayList<>();//日期选择器标记日期

    private supermapservice Mapservice;
    private String MapName="轨迹图";
    //活动第一次被创建时启用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        initiMobile();//初始化iMobile
        setContentView(R.layout.activity_track_map);

        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(this);
        mTopBar = this.findViewById(R.id.topbar);
        mTopBar.setTitle("请选择展示轨迹图日期");
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //活动由不可见变为可见时调用（加载资源）
    @Override
    protected void onStart(){
        super.onStart();
        view  = (RelativeLayout) findViewById(R.id.Map_Relative);
        OpenMap();
        //设置比例尺
        scaleView1 = new ScaleView(this);
        scaleView1 = (ScaleView) this.findViewById(R.id.scrolvoew);
        scaleView1.setMapView(A_mapView);
        //scaleView1.setScaleType(ScaleType.Chinese);
        inviteBroadcast();//初始化广播
        //绑定服务
        Log.i("MapACtivity","绑定服务");
        Intent bindIntent = new Intent(getContext(),GPSService.class);
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
        day_Datasource     = m_mapcontrol.getMap().getWorkspace().getDatasources().get("track_day");
        month_Datasource   = m_mapcontrol.getMap().getWorkspace().getDatasources().get("track_month");
        Analyse_day = m_mapcontrol.getMap().getWorkspace().getDatasources().get("track_analyse_day");
        Analyse_month = m_mapcontrol.getMap().getWorkspace().getDatasources().get("track_analyse_month");


        QMUImap = (QMUIRadiusImageView)findViewById(R.id.QMUIswitchMap);
        QMUIMonth = (QMUIRadiusImageView)findViewById(R.id.QMUIMonth);
        QMUIDay = (QMUIRadiusImageView)findViewById(R.id.QMUIDay);
        QMUIMonth.bringToFront();
        QMUIDay.bringToFront();

        //A_heatMap = new com.supermap.mapping.imChart.HeatMap(this,A_mapView);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);//年
        int month = calendar.get(Calendar.MONTH)+1;//月
        String monthname = "Dataset_"+year+"_"+month;
        //ViewLoad();
        //ViewHotMap((DatasetVector)month_Datasource.getDatasets().get(monthname));//显示热力图
        //ViewGeoLine((DatasetVector)month_Datasource.getDatasets().get(monthname));//测试显示轨迹图
        QMUIMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Datasets month = month_Datasource.getDatasets();
                int n = month.getCount();
                final String[] items = new String[n];
                for(int i=0;i<n;i++){
                    items[i] = month.get(i).getName();
                    items[i] = items[i].substring(8);
                }
                //String[] items = new String[]{"选项1", "选项2", "选项3"};
                new QMUIDialog.MenuDialogBuilder(context)
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(context, "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                final int num = which;
                                dialog.dismiss();
                                ViewLoad();
                                DatasetVector month = (DatasetVector) month_Datasource.getDatasets().get("Dataset_"+items[num]);
                                ViewGeoLine(month);
                                Rectangle2D rectangle2D = month.computeBounds();//获取数据集最小外接矩形
                                ViewFull(rectangle2D);//全幅显示
                            }
                        })
                        .show();
            }
        });
        QMUIDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Datasets day = day_Datasource.getDatasets();
                int n = day.getCount();
                final String[] items = new String[n];
                Date date = null;
                for(int i=0;i<n;i++){
                    items[i] = day.get(i).getName();
                    items[i] = items[i].substring(8);//截断字符串保留日期
                    date = DateTimeUtil.StrToDateDataset(items[i]);//根据日期字符串返回日期
                    if(date!=null){
                        markDays.add(date.getTime());//标记日期
                    }
                }

                //GitHub优秀的DatepickerDialog
                CalendarViewDialog.getInstance()
                        .init(context)
                        .addMarks(markDays)
                        .setLimitMonth(false)
                        .show(new CalendarView.OnCalendarClickListener() {
                            @Override
                            public void onDayClick(Calendar daySelectedCalendar) {
                                CalendarViewDialog.getInstance().close();
                                //Toast.makeText(context, "选择的天数 : " + DateUtils.getDateTime(daySelectedCalendar.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                                Log.i("MapActivity","选择的天数 : " + DateUtils.getDateTime(daySelectedCalendar.getTimeInMillis()));
                                int month = daySelectedCalendar.get(Calendar.MONTH)+1;
                                String datesetname = "Dataset_"+daySelectedCalendar.get(Calendar.YEAR)+"_"+month+"_"+daySelectedCalendar.get(Calendar.DATE);
                                ViewLoad();
                                DatasetVector day = (DatasetVector) day_Datasource.getDatasets().get(datesetname);
                                ViewGeoLine(day);
                                ViewDataset(Analyse_day.getDatasets().get(datesetname));//显示驻留点
                                Rectangle2D rectangle2D = day.computeBounds();//获取数据集最小外接矩形
                                ViewFull(rectangle2D);//全幅显示
                                StopLoad();
                            }

                            @Override
                            public void onDayNotMarkClick(Calendar daySelectedCalendar) {
                                Toast.makeText(context, "选择的日期没有记录，请选择有标记的日期进行数据显示！", Toast.LENGTH_SHORT).show();
                            }
                        });

                //String[] items = new String[]{"选项1", "选项2", "选项3"};
                /*
                new QMUIDialog.MenuDialogBuilder(context)
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(context, "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                final int num = which;
                                ViewLoad();
                                dialog.dismiss();
                                DatasetVector day = (DatasetVector) day_Datasource.getDatasets().get(items[num]);
                                ViewGeoLine(day);
                                Rectangle2D rectangle2D = day.computeBounds();//获取数据集最小外接矩形
                                ViewFull(rectangle2D);//全幅显示
                            }
                        })
                        .show();
                        */

            }
        });
        QMUImap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mapnum = m_maps.getCount();//获取地图数量
                final String[] items = new String[mapnum];
                for(int i=0;i<mapnum;i++){
                    items[i] = m_maps.get(i);
                }
                new QMUIDialog.MenuDialogBuilder(context)
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Log.i("MapActivity","选择了地图："+items[which]);
                                A_map.open(items[which]);
                                A_map.refresh();

                            }
                        })
                        .show();
            }
        });
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
        //A_mapView.getMapControl().getMap().getTrackingLayer().clear();
    }
    //活动被销毁之前调用
    protected void onDestroy(){
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);//注销本地广播监听器
        ReadSucce_localBroadcastManager.unregisterReceiver(ReadSucce_licalReceiver);
        unbindService(connection);//解绑服务
        A_map.close();
        m_mapcontrol.dispose();
        A_workspace.dispose();
    }


    //初始化iMobile
    private void initiMobile(){
        //iMobile初始化
//        rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//        //设置一些系统需要用到的路径
//        Environment.setOpenGLMode(true);
//        Environment.setLicensePath(rootPath + "/SuperMap/License/");
//        Environment.setLicenseType(LicenseType.UUID);
//        Environment.setTemporaryPath(rootPath + "/SuperMap/temp/");
//        Environment.setWebCacheDirectory(rootPath + "/SuperMap/WebCatch");
//        Log.i("MainActivity", "许可路径：" + rootPath + "/SuperMap/License/");
//        //组件功能必须在Environment初始化之后才能调用
//        Environment.initialization(this);
//        Log.i("MainActivity", "Environment初始化完毕！");

        point2Ds = new Point2Ds();
    }
    //初始化广播
    private void inviteBroadcast(){
        //注册本地广播监听器
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcasttest.My_BROADCASTLOCATION");
        localReceiver = new LocalReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);

        //数据制作完毕广播
        ReadSucce_localBroadcastManager = LocalBroadcastManager.getInstance(this);
        ReadSucce_intentfilter = new IntentFilter();
        ReadSucce_intentfilter.addAction("com.example.broadcasttest.My_BROADCASTMAKESUCCE");
        ReadSucce_licalReceiver = new ReadSucceLocalReceiver();
        ReadSucce_localBroadcastManager.registerReceiver(ReadSucce_licalReceiver,ReadSucce_intentfilter);
    }
    private void OpenMap(){
        Mapservice = new supermapservice();
        Mapservice.init(context);
        A_workspace = Mapservice.getWorkplace();
        A_mapView = Mapservice.getMaoView();
        m_mapcontrol = Mapservice.getMapcontrol();
        m_maps=Mapservice.getMaps();
        A_map=Mapservice.getMap();
        A_mapView = Mapservice.openMap(MapName);

//        //iMobile地图组件
//        //打开工作空间
//        A_workspace = new Workspace();
//        WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
//        info.setServer(rootPath + "/SuperMap/LifeManager/BasicMap/BasicMap.smwu");
//        info.setType(WorkspaceType.SMWU);
//        A_workspace.open(info);
//        m_maps = A_workspace.getMaps();
//
//        //将地图与工作空间关联
//        A_mapView = new MapView(this);
//        m_mapcontrol = A_mapView.getMapControl();
//        A_map = m_mapcontrol.getMap();
//        A_map.setWorkspace(A_workspace);
//
//
//        //打开工作空间中的基本地图
//        A_map.open("轨迹图");
//        A_map.refresh();
        view.addView(A_mapView,0);
        /*
        //打开iService数据
        DatasourceConnectionInfo dsInfo = new DatasourceConnectionInfo();
        //设置地图服务地址
        dsInfo.setServer("http://58.87.124.133:8090/iserver/services/map-BasicMap/rest/maps/BasicMap");
        //设置引擎类型
        dsInfo.setEngineType(EngineType.Rest);
        //设置数据源别名
        dsInfo.setAlias("map-Population");
        //打开数据源
        Datasource ds = A_workspace.getDatasources().open(dsInfo);
        if(ds != null){
            m_mapcontrol.getMap().getLayers().add(ds.getDatasets().get(0), true);
            m_mapcontrol.getMap().refresh();
            Toast.makeText(MapActivity.this,"打开数据源成功！",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(MapActivity.this,"打开数据源失败！",Toast.LENGTH_SHORT).show();
        }
        */
        Log.i("MapActivity","地图加载完成！");
    }
    /**
     * 收到位置变化广播后读取位置更新地图中心
     */
    public void locating(){
        point2D = new Point2D();
        gpsData = new LocationManagePlugin.GPSData();
        //读取数据
        Log.i("MapACtivity","读取数据");
        Date date = new Date(System.currentTimeMillis());
        gpsData.lTime=date.getTime();//点位获取时间（毫秒）
        gpsData.dLatitude=latitude;
        gpsData.dLongitude=longtiude;
        gpsData.dAltitude=altitude;
        if(gpsData.dLatitude==0.0){
            Log.i("MapActivity","无效数据（0）！");
        }
        //Toast.makeText(this,"当前经纬度为："+point2D.getX()+","+point2D.getY(),Toast.LENGTH_SHORT).show();
        //获取地图相关对象
        //A_mapView = (MapView)findViewById(R.id.AMap_View);
        Point2D point2d0=new Point2D();
        point2d0.setX(gpsData.dLongitude);
        point2d0.setY(gpsData.dLatitude);
        CoordSysTranslator coordSysTranslator=new CoordSysTranslator();
        //获取地图坐标系
        PrjCoordSys prjCoordSys = m_mapcontrol.getMap().getPrjCoordSys();
        Log.i("MapActivity","投影坐标系对象："+prjCoordSys.getName());

        //坐标系转换
        Log.i("MapActivity","转换前的经纬度为："+point2d0.getX()+","+point2d0.getY());
        Point2Ds point2ds=new Point2Ds();
        point2ds.add(point2d0);
        Boolean isOk=coordSysTranslator.forward(point2ds,prjCoordSys);
        point2D=point2ds.getItem(0);
        Log.i("MapActivity","转换后的经纬度为："+point2D.getX()+","+point2D.getY());

        if(point2D.getX()!=1.0){
            Log.i("MapActivity","记录！");
            //A_map.setCenter(point2D);
            //PointLocation(point2D);
            //A_map.setScale(6.971914893617021E-5);
            A_map.refresh();
            //Toast.makeText(getContext(),"发现你啦！",Toast.LENGTH_SHORT).show();
        }

        GeoPoint geoPoint = new GeoPoint(point2D);
        GeoStyle geoStyle_L = new GeoStyle();
        geoStyle_L.setMarkerSize(new Size2D(3.6,3.6));
        geoStyle_L.setPointColor(new Color(0,0,255));
        geoPoint.setStyle(geoStyle_L);
        m_mapcontrol.getMap().getTrackingLayer().deleteUserField("Location");
        m_mapcontrol.getMap().getTrackingLayer().add(geoPoint, "Location");
        A_map.refresh();
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
            location = getLocation.getlocation();//获取location

            Log.i("MapActivity","获得数据："+longtiude+","+latitude+","+altitude);
            Log.i("MapActivity","获得数据（location）："+location.getLongitude()+","+location.getLatitude()+","+location.getAltitude());
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("MapActivity","通讯断开！");
        }
    };
    //接受本地广播
    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MapActivity","地图活动收到来自定位服务的广播！");
            //Toast.makeText(MyApplication.getContext(),"收到广播！",Toast.LENGTH_SHORT).show();
            locating();
        }
    }
    //显示加载中
    private void ViewLoad(){
        ReadChat_tipDialog= new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(" 正在加载 ")
                .create();
        ReadChat_tipDialog.show();
        Log.i("TrackMapActivity","加载中！");
    }
    //结束加载中
    private void StopLoad(){
        Intent intent = new Intent("com.example.broadcasttest.My_BROADCASTMAKESUCCE");
        ReadSucce_localBroadcastManager.sendBroadcast(intent);
        Log.i("MapActivity","加载结束！");
    }

    //打开指定数据集并显示当前地图
    private void ViewDataset(Dataset dataset){
        if (dataset==null){
            Log.e("TrackMapActivity","数据集空，无法打开！");
            return;
        }
        Log.i("TrackMapActivity","共显示驻留点个数："+((DatasetVector)dataset).getRecordset(false,DYNAMIC).getRecordCount());
        /*
        //普通图层显示
        Layer layer;
        Layers layers = A_map.getLayers();
        layer = layers.add(dataset,true);

        String name = layer.getName();
        int index = layers.indexOf(name);
        layers.moveToTop(index);//移动当前图层到顶层
        layers.add(layer);
        */

        //追踪图层显示
        DatasetVector datasetVector = (DatasetVector) dataset;
        Recordset recordset=datasetVector.getRecordset(false,DYNAMIC);
        GeoPoint geometry = new GeoPoint();
        recordset.moveFirst();
        GeoStyle geoStyle_P = new GeoStyle();
        //geoStyle_L.setFillOpaqueRate(60);
        geoStyle_P.setPointColor(new Color(0,0,255));
        geoStyle_P.setMarkerSize(new Size2D(10.1,10.1));
        int resum = recordset.getRecordCount();
        Log.i("TrackMapActivity","添加到驻留点追踪图层点个数："+resum);
        m_mapcontrol.getMap().getTrackingLayer().deleteUserField("StopPoint");//删除上一次显示的驻留点图层
        for(int i = 0;i<resum;i++){
            Point2D point2D = PrjTransToMap(recordset.getGeometry().getInnerPoint());
            geometry = (GeoPoint) recordset.getGeometry();
            geometry.setX(point2D.getX());
            geometry.setY(point2D.getY());
            geometry.setStyle(geoStyle_P);//设置主题
            m_mapcontrol.getMap().getTrackingLayer().add(geometry, "StopPoint");
            recordset.move(1);
        }
        m_mapcontrol.getMap().getTrackingLayer().labelMoveTop(";StopPoint");//移动图层到顶层
        Log.i("TrackMapActivity","当前跟踪图层几何对象个数："+m_mapcontrol.getMap().getTrackingLayer().getCount());

        //获取地图坐标系
        prjCoordSys = m_mapcontrol.getMap().getPrjCoordSys();
        Log.i("MapActivity","投影坐标系对象："+prjCoordSys.getName());
        A_map.setDynamicProjection(true,prjCoordSys);//动态投影
        //Toast.makeText(getContext(),"加载数据集+"+datasetName,Toast.LENGTH_SHORT).show();
        Log.i("MapActivity","加载数据集+"+datasetName);
    }
    //读取图标点数据集
    private ArrayList<ChartPoint> ReadChartPoint(Recordset recordset){
        Log.i("MapActivity","开始读取数据集点！共需读取数据："+recordset.getRecordCount()+"个");
        ArrayList<ChartPoint> chartPoint = new ArrayList<ChartPoint>();
        Random rand = new Random();
        int n = recordset.getRecordCount();
        for(int i=0;i<5000;i++){
            int index = rand.nextInt(n);
            recordset.moveTo(index);
            float dw = rand.nextInt(100)+1;
            Point2D point2D = PrjTransToMap(recordset.getGeometry().getInnerPoint());//坐标投影转换
            if(point2D==null){
                Log.i("MaooActivity","加载到空点！");
            }
            //Log.i("MaooActivity","点位权值："+dw);
            chartPoint.add(new ChartPoint(point2D,dw));
        }
        Log.i("MapActivity","读取数据集到图标点数据集完成！共读取数据："+recordset.getRecordCount()+"个");
        recordset.dispose();
        return chartPoint;
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
                dataUploadService.addRecordset(url,recordset);

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
    //根据某数据集显示热力图
    private void ViewHotMap(DatasetVector datasetVector){
        if(datasetVector==null){
            Log.i("MapActivity","数据集为空！");
            return;
        }

        GeoLine geoLine = datasetVector.convertToLine();
        Log.i("MapActivity","转化为"+geoLine.getPartCount()+"个对象！");
        GeoStyle geoStyle_L = new GeoStyle();
        geoStyle_L.setLineColor(new Color(107,117,207));
        geoStyle_L.setLineSymbolID(15);
        geoStyle_L.setLineWidth(1.0);
        geoLine.setStyle(geoStyle_L);
        m_mapcontrol.getMap().getTrackingLayer().add(geoLine, "Track");

        //读取用户昵称
        UserInformation = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String nickname = UserInformation.getString("nickname","");
        /*
        for(int i=0;i<A_map.getLayers().getCount();i++){
            Layer layer = A_map.getLayers().get(i);
            layer.setVisible(false);
        }
        Layers layers = A_map.getLayers();
        Dataset dataset = A_workspace.getDatasources().get("iServerREST").getDatasets().get("BasicMap");
        Layer layer = layers.add(dataset,true);
        layer.setOpaqueRate(60);
        */
        mTopBar.setTitle(TralName(nickname+"的"+datasetVector.getName())+"活动热力图");

        Rectangle2D rectangle2D = datasetVector.computeBounds();//获取数据集最小外接矩形
        ViewFull(rectangle2D);//全幅显示
        //A_mapView.setBackgroundColor(0x0);
        A_mapView.getMapControl().setAction(Action.PAN);//漫游模式
        A_mapView.getMapControl().getMap().getTrackingLayer().clear();
        A_map.getLayers().get(2).setOpaqueRate(80);
        //A_heatMap = new com.supermap.mapping.imChart.HeatMap(this,A_mapView);
        A_heatMap.setRadious(8);
        A_heatMap.setSmoothTransColor(true);
        //A_heatMap.setTitle(TralName(datasetVector.getName())+"热力图");
        ArrayList<ChartPoint> mHotDatas = new ArrayList<ChartPoint>();
        mHotDatas = ReadChartPoint(datasetVector.getRecordset(false,DYNAMIC));
        A_heatMap.addChartDatas(mHotDatas);
        Log.i("MapActivity","打开热力图!");
        //A_heatMap.startPlay();

        //获取地图坐标系
        prjCoordSys = m_mapcontrol.getMap().getPrjCoordSys();
        Log.i("MapActivity","投影坐标系对象："+prjCoordSys.getName());
        A_map.setDynamicProjection(true,prjCoordSys);//动态投影
        //A_map.open("BasicMap@iServerREST");
        A_map.refresh();
        StopLoad();
    }
    //全幅显示最小外接矩形
    private void ViewFull(Rectangle2D rectangle2D){
        Point2D point2D = rectangle2D.getCenter();//获取数据集最小外接矩形的中心点
        Log.i("MapActivity","获取到当前数据集最小外接矩形的宽度为："+rectangle2D.getWidth()+",高度为："+rectangle2D.getHeight());
        point2D = PrjTransToMap(point2D);//中心点投影转换
        Point2D LeftBottom = PrjTransToMap(new Point2D(rectangle2D.getLeft(),rectangle2D.getBottom()));//左下角坐标转换
        Point2D RightTop = PrjTransToMap(new Point2D(rectangle2D.getRight(),rectangle2D.getTop()));//右上点坐标换换
        Rectangle2D Prj = new Rectangle2D(LeftBottom,RightTop);
        A_map.setViewBounds(Prj);//设置数据集最小外接矩形为地图当前可视范围
        A_map.setCenter(new Point2D(point2D.getX(),point2D.getY()));//设置数据集最小外接矩形中点为地图中心
    }

    private Point2Ds ReadDatavrateToPoint2ds(Recordset recordset){
        Log.i("MapActivity","开始读取数据集点！共需读取数据："+recordset.getRecordCount()+"个");
        Point2Ds point2Ds = new Point2Ds();
        int n = recordset.getRecordCount();
        for(int i=0;i<n;i++){
            recordset.moveTo(i);
            Point2D point2D = PrjTransToMap(recordset.getGeometry().getInnerPoint());//坐标投影转换
            if(point2D==null){
                Log.i("MaooActivity","加载到空点！");
            }
            point2Ds.add(point2D);
        }
        Log.i("MapActivity","读取数据集到图标点数据集完成！共读取数据："+recordset.getRecordCount()+"个");
        return point2Ds;
    }

    //根据点集显示轨迹
    private void ViewGeoLine(DatasetVector datasetVector){
        if(datasetVector==null){
            Log.i("MapActivity","数据集为空！");
            return;
        }

        //读取用户昵称
        UserInformation = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String nickname = UserInformation.getString("nickname","");

        mTopBar.setTitle(TralName(nickname+"的"+datasetVector.getName())+"活动轨迹图");

        A_mapView.getMapControl().setAction(Action.PAN);//漫游模式
        A_mapView.getMapControl().getMap().getTrackingLayer().clear();

        Point2Ds point2Ds = ReadDatavrateToPoint2ds(datasetVector.getRecordset(false,DYNAMIC));
        if(point2Ds.getCount()<2){
            Log.i("TrackActivity","点数小于2！无法显示轨迹图！");
            Toast.makeText(this,"数据不足！",Toast.LENGTH_SHORT).show();
            return;
        }
        GeoLine geoLine = new GeoLine();
        geoLine.addPart(point2Ds);

        Log.i("MapActivity","转化为"+geoLine.getPartCount()+"个对象！");
        GeoStyle geoStyle_L = new GeoStyle();
        //geoStyle_L.setFillOpaqueRate(60);
        geoStyle_L.setLineColor(new Color(255,119,119));
        geoStyle_L.setLineSymbolID(96);
        //geoStyle_L.setLineWidth(0.7);
        geoLine.setStyle(geoStyle_L);
        m_mapcontrol.getMap().getTrackingLayer().add(geoLine, "Track");

        Rectangle2D rectangle2D = datasetVector.computeBounds();//获取数据集最小外接矩形
        ViewFull(rectangle2D);//全幅显示

        //获取地图坐标系
        prjCoordSys = m_mapcontrol.getMap().getPrjCoordSys();
        Log.i("MapActivity","投影坐标系对象："+prjCoordSys.getName());
        A_map.setDynamicProjection(true,prjCoordSys);//动态投影
        A_map.refresh();
        StopLoad();
    }
    //投影转换
    private Point2D PrjTransToMap(Point2D point2D){
        Point2Ds point2Ds = new Point2Ds();
        point2Ds.add(point2D);
        //获取地图坐标系
        prjCoordSys = m_mapcontrol.getMap().getPrjCoordSys();
        CoordSysTranslator coordSysTranslator = new CoordSysTranslator();
        coordSysTranslator.forward(point2Ds,prjCoordSys);
        return point2Ds.getItem(0);
    }

    //加载完毕广播接收器
    class ReadSucceLocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            Log.i("MapActivity","热力图数据读取完毕！");
            ReadChat_tipDialog.dismiss();
        }
    }

    //数据集记录名称转日期
    private String TralName(String name){
        return name.replace("Dataset_","");
    }
}
