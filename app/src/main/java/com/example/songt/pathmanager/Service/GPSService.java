package com.example.songt.pathmanager.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.songt.pathmanager.Activity.MainActivity;
import com.example.songt.pathmanager.R;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GPSService extends Service implements TencentLocationListener{


    private LocalBroadcastManager localBroadcastManager;
    private LocalBroadcastManager SendStartLocalBM;
    private LocalBroadcastManager SendStopLocalBM;

    private Location sendlocation;
    private GetLocation getLocation = new GetLocation();
    private double latitude=1.0;
    private double longtiude=1.0;
    private double altitude=1.0;//大地高
    private float accuracy=1;//定位精度
    private String provider=null;//定位来源


    //腾讯地图定位
    private TencentLocationManager TencentLocationManager;
    TencentLocationListener listener;
    TencentLocationRequest request;

    //实现服务和活动之间的通讯
     public class GetLocation extends Binder{
        public double getlocation_longitude(){
            Log.i("GPSService", "当前位置：latitude="+latitude);
            return longtiude;
        }
        public double getlocation_latitude(){
            Log.i("GPSService", "当前位置：longtiude="+longtiude);
            return latitude;
        }
        public double getlocation_altitude(){
            Log.i("GPSService","当前高程："+altitude);
            return altitude;
        }
        public Location getlocation(){
            Log.i("GPSService","SendLoaction"+"当前位置：latitude="+latitude+","+"longtiude="+longtiude);
            return sendlocation;
        }
        public TencentLocationManager getTencentLocationManager(){
            return TencentLocationManager;
        }
        public TencentLocationListener getTencentLocationListener(){
            return listener;
        }
        public TencentLocationRequest getTexcentLocationRequest(){
            return request;
        }
        public float getlocation_accuracy (){return accuracy;}
        public int getlocation_provider(){
            if(provider=="gps"){
                return 1;
            }else if(provider=="network"){
                return 2;
            }
            else {
                return -1;
            }
        }

    }
    @Override
    public IBinder onBind(Intent intent){
        Log.d("GPSService","绑定服务成功！");
        return getLocation;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("GPSService","开始服务，设置前台！");
        //设置前台运行
        Log.i("GPSService","onCreate executed");
        Intent intent = new Intent (this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("智能生活管家正在追随你")
                .setContentText("科技改变生活")
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true) // 点击跳转后自动销毁
                .setContentIntent(pi)
                .build();
        startForeground(1,notification);
        //获取实例
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        SendStartLocalBM = LocalBroadcastManager.getInstance(this);
        SendStopLocalBM = LocalBroadcastManager.getInstance(this);

        //腾讯定位
        TencentLocationManager = TencentLocationManager.getInstance(this);
        listener = this;
        /* 保证调整坐标系前已停止定位 */
        TencentLocationManager.removeUpdates(null);
        // 设置 wgs84 坐标系
        TencentLocationManager
                .setCoordinateType(TencentLocationManager.COORDINATE_TYPE_WGS84);
    }

    @Nullable

    //服务执行的操作
    @SuppressLint("MissingPermission")
    @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
         SendStartService();//发送服务启动广播
        Log.i("GPSService","服务开启，开始记录点位信息！");
        File sdCardDir = Environment.getExternalStorageDirectory();
        String path = "/SuperMap/LifeManager/";
        //如果不存在，就创建目录
        File dir = new File(sdCardDir + path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        sendlocation = new Location("locationManager");
        sendlocation.setLatitude(1);
        sendlocation.setLongitude(1);
        sendlocation.setAltitude(1);
        Log.i("GPS", "Start");

        /*
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 检查定位服务是否打开
        assert locationManager != null;
        if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null
                || locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
            Log.i("GPS", "正在定位");
            // Permission check
            //设置数据更新的条件，参数分别为1，使用GPS 2，最小时间间隔 3000毫秒 3，最短距离 5  4,设置事件监听者 this(类继承了Locationlistener)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i("GPS.onLocationChanged", location.getLatitude() + " , " + location.getLongitude());
                    Log.i("GPS", location.getTime() + "");
                    //保存记录
                    sendlocation = location;
                    saveGPSPoint(location);
                    sendBroadcast(location);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                    Log.e("GPS.onStatusChanged", "changed");
                }

                @Override
                public void onProviderEnabled(String s) {
                    Log.e("GPS.onProviderEnabled", "changed");
                }

                @Override
                public void onProviderDisabled(String s) {
                    Log.e("GPS.onProviderDisabled", "changed");
                }
            });
            Log.i("GPS", "Onece");
        } else {
            Log.i("GPS", "无法定位");
        }
        */
        //定位请求
        request = TencentLocationRequest.create();
        // 修改定位请求参数, 定位周期 5000 ms
        request.setInterval(5000);
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO);//定位请求信息级别为0号接口，只有经纬度
        //request.setAllowGPS(false);//不允许使用GPS
        request.setQQ("1305647042");//设置QQ号
        int error = TencentLocationManager.requestLocationUpdates(request, listener);
        Log.i("GpsService","腾讯定位监听器注册返回值："+error);

        return super.onStartCommand(intent, flags, startId);
    }

    //销毁服务时调用
    @Override
    public void onDestroy() {
        super.onDestroy();
        SendStopService();//发送服务停止广播
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //移除标记为id的通知 (只是针对当前Context下的所有Notification)
        notificationManager.cancel(1);

        //停止定位
        TencentLocationManager.removeUpdates(listener);
        Log.i("GPSService","服务关闭，停止 记录！");
    }

    private void saveGPSPoint(Location location){
        String TAG = "GPS";
        String point = location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + ","+ location.getTime() +"\n\r";
        File sdCardDir = Environment.getExternalStorageDirectory();
        try {
            String path = "/SuperMap/LifeManager/";
            //如果不存在，就创建目录
            File dir = new File(sdCardDir + path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File targetFile = new File(sdCardDir.getCanonicalPath() + "/SuperMap/LifeManager/","GPSPoint.txt");
            //使用RandomAccessFile是在原有的文件基础之上追加内容，
            //而使用outputstream则是要先清空内容再写入
            RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
            //光标移到原始文件最后，再执行写入
            raf.seek(targetFile.length());
            raf.write(point.getBytes());
            raf.close();
            Log.e(TAG, "Successful");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        longtiude=tencentLocation.getLongitude();
        latitude=tencentLocation.getLatitude();
        altitude = tencentLocation.getAltitude();//大地高
        accuracy=tencentLocation.getAccuracy();//获取精度
        provider=tencentLocation.getProvider();//获取定位方式
        sendBroadcast(tencentLocation);
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    private void sendBroadcast(TencentLocation location){
        Log.i("位置：", location.getLatitude() + " , " + location.getLongitude());

        longtiude=location.getLongitude();
        latitude=location.getLatitude();
        altitude = location.getAltitude();//大地高
        accuracy=location.getAccuracy();//获取精度
        provider=location.getProvider();//获取定位方式
        Intent intent = new Intent("com.example.broadcasttest.My_BROADCASTLOCATION");
        localBroadcastManager.sendBroadcast(intent);//发送本地广播;
        Log.i("GPSService","本地广播！待发送位置："+latitude + " , " + longtiude+ " , 定位方式:" +provider+",定位精度:"+accuracy);
    }

    //发送服务开启信号
    private void SendStartService(){
         Log.i("GPSService","定位服务开启！");
         Intent intent = new Intent("com.example.broadcasttest.My_BROADCASTSTARTSERVICE") ;
         SendStartLocalBM.sendBroadcast(intent);//发送本地广播
    }

    //发送服务关闭信号
    private void SendStopService(){
        Log.i("GPSService","定位服务关闭！");
        Intent intent = new Intent("com.example.broadcasttest.My_BROADCASTSTOPSERVICE") ;
        SendStopLocalBM.sendBroadcast(intent);//发送本地广播
    }

    /*****
     *
     * 百度定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
//    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {
//
//        @Override
//        public void onReceiveLocation(BDLocation location) {
//            // TODO Auto-generated method stub
//            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
//                StringBuffer sb = new StringBuffer(256);
//                sb.append("time : ");
//                /**
//                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
//                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
//                 */
//                sb.append(location.getTime());
//                sb.append("\nlocType : ");// 定位类型
//                sb.append(location.getLocType());
//                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
//                sb.append(location.getLocTypeDescription());
//                sb.append("\nlatitude : ");// 纬度
//                sb.append(location.getLatitude());
//                sb.append("\nlontitude : ");// 经度
//                sb.append(location.getLongitude());
//                sb.append("\nradius : ");// 半径
//                sb.append(location.getRadius());
//                sb.append("\nCountryCode : ");// 国家码
//                sb.append(location.getCountryCode());
//                sb.append("\nCountry : ");// 国家名称
//                sb.append(location.getCountry());
//                sb.append("\ncitycode : ");// 城市编码
//                sb.append(location.getCityCode());
//                sb.append("\ncity : ");// 城市
//                sb.append(location.getCity());
//                sb.append("\nDistrict : ");// 区
//                sb.append(location.getDistrict());
//                sb.append("\nStreet : ");// 街道
//                sb.append(location.getStreet());
//                sb.append("\naddr : ");// 地址信息
//                sb.append(location.getAddrStr());
//                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
//                sb.append(location.getUserIndoorState());
//                sb.append("\nDirection(not all devices have value): ");
//                sb.append(location.getDirection());// 方向
//                sb.append("\nlocationdescribe: ");
//                sb.append(location.getLocationDescribe());// 位置语义化信息
//                sb.append("\nPoi: ");// POI信息
//                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
//                    for (int i = 0; i < location.getPoiList().size(); i++) {
//                        Poi poi = (Poi) location.getPoiList().get(i);
//                        sb.append(poi.getName() + ";");
//                    }
//                }
//                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
//                    sb.append("\nspeed : ");
//                    sb.append(location.getSpeed());// 速度 单位：km/h
//                    sb.append("\nsatellite : ");
//                    sb.append(location.getSatelliteNumber());// 卫星数目
//                    sb.append("\nheight : ");
//                    sb.append(location.getAltitude());// 海拔高度 单位：米
//                    sb.append("\ngps status : ");
//                    sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
//                    sb.append("\ndescribe : ");
//                    sb.append("gps定位成功");
//                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
//                    // 运营商信息
//                    if (location.hasAltitude()) {// *****如果有海拔高度*****
//                        sb.append("\nheight : ");
//                        sb.append(location.getAltitude());// 单位：米
//                    }
//                    sb.append("\noperationers : ");// 运营商信息
//                    sb.append(location.getOperators());
//                    sb.append("\ndescribe : ");
//                    sb.append("网络定位成功");
//                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//                    sb.append("\ndescribe : ");
//                    sb.append("离线定位成功，离线定位结果也是有效的");
//                } else if (location.getLocType() == BDLocation.TypeServerError) {
//                    sb.append("\ndescribe : ");
//                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
//                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//                    sb.append("\ndescribe : ");
//                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
//                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//                    sb.append("\ndescribe : ");
//                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
//                }
//                //logMsg(sb.toString());
//                //Toast.makeText(MyApplication.getContext(),sb,Toast.LENGTH_SHORT).show();
//                Log.i("GpsServer","定位到"+location.getLongitude()+","+location.getLatitude());
//
//                Point point = new Point();
//                point = new CoordinateTransform().wgs84togcj02(location.getLongitude(),location.getLatitude());
//                Log.i("GpsServer","转换到wgs84:"+point.getLon()+","+point.getLat());
//                //location.setLongitude(point.getLon());
//                //location.setLatitude(point.getLat());
//
//                longtiude=location.getLongitude();
//                latitude=location.getLatitude();
//                altitude = location.getAltitude();//大地高
//
//                ///sendBroadcast(location);
//            }
//        }
//
//    };
}
