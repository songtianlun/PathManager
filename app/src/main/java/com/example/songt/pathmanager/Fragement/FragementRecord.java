package com.example.songt.pathmanager.Fragement;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.songt.pathmanager.Activity.MainActivity;
import com.example.songt.pathmanager.Activity.MapActivity;
import com.example.songt.pathmanager.Activity.TrackMapActivity;
import com.example.songt.pathmanager.R;
import com.example.songt.pathmanager.Toole.ActivityCollector;
import com.example.songt.pathmanager.Toole.DeleteFileUtil;
import com.example.songt.pathmanager.Toole.MyApplication;
import com.example.songt.pathmanager.UI.DynamicButton;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.supermap.android.maps.BoundingBox;
import com.supermap.android.maps.CoordinateReferenceSystem;
import com.supermap.android.maps.LayerView;
import com.supermap.android.maps.MapController;
import com.supermap.android.maps.MapView;
import com.supermap.android.maps.Point2D;

public class FragementRecord extends Fragment{
    private static final String DEFAULT_URL = "http://39.106.27.150:8090/iserver/services/map-BasicMap/rest/maps/SimpleHotMap";

    //动态按钮及按钮状态指示
    private DynamicButton pythButton;
    private int value = 1;
    //当前经纬度
    private double latitude=0;
    private double longtiude=0;
    CoordinateReferenceSystem coordinateReferenceSystem;//获取地理坐标参考系
    protected Point2D location;
    protected com.supermap.android.data.Point2D location_data;
    protected MapView MapView;
    protected MapController MapController;
    private LocationManager locationManager;
    View view;
    String rootPath;

    //碎片和活动建立关系时调用
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    //活动第一次被创建时调用
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity mainActivity = (MainActivity)getActivity();
        QMUIStatusBarHelper qmuiStatusBarHelper;
        qmuiStatusBarHelper = new QMUIStatusBarHelper();
        qmuiStatusBarHelper.setStatusBarLightMode(mainActivity);
    }

    //为碎片加载布局时调用
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        final MainActivity mainActivity = (MainActivity)getActivity();
        QMUIStatusBarHelper qmuiStatusBarHelper;
        qmuiStatusBarHelper = new QMUIStatusBarHelper();
        qmuiStatusBarHelper.setStatusBarLightMode(mainActivity);
        longtiude=mainActivity.getlocation_longitude();
        latitude=mainActivity.getlocation_latitude();
        Log.i("FeagementRecord", "碎片通讯，接收：longitude"+longtiude+",latitude"+longtiude);
        location = new Point2D();
        location_data=new com.supermap.android.data.Point2D();
        location.x=latitude;
        location.y=longtiude;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragement_record_layout, container, false);

        //按钮服务
        pythButton = (DynamicButton) view.findViewById(R.id.pythbutton);


        pythButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                value = value + 1;
                if (value % 2 == 1) {
                    morphToSquare(pythButton, 500);
                    Log.i("yzy", "morphToSquare..");
                    //停止记录位置服务
                    mainActivity.stopLocationService();
                } else {
                    morphToSuccess2(pythButton);
                    Log.i("yzy", "morphToSuccess..");
                    //开始记录位置服务
                    mainActivity.startLocationService();
                }
            }
        });


        //创建地图窗口
        MapView = (MapView) view.findViewById(R.id.MapView);
        MapView.startClearCacheTimer(2);//2分钟清理一次缓存
        //创建地图图层，并指向iServer提供的地图服务
        LayerView layerView = new LayerView(getActivity());
        layerView.setURL(DEFAULT_URL);
        //layerView.setBounds(-20037044.6718795,-9504576.31626961,20037044.5115795,12934035.4614068);
        //设置地图缩放
        //MapView.setBuiltInZoomControls(true);
        //设置显示比例尺控件
        //MapView.showScaleControl(true);
        //加载地图图层
        MapView.addLayer(layerView);
        coordinateReferenceSystem=MapView.getCRS();
        //BoundingBox boundingBox = new BoundingBox(new Point2D(-20037044.6718795,12934035.4614068),new Point2D(20037044.5115795,-9504576.31626961));
        //MapView.setViewBounds(boundingBox);
        MapController = MapView.getController();
        //MapController.setCenter(location);
        Log.i("FragementRecord", "我的位置：longitude"+location.y+",latitude"+location.x);
        MapController.setCenter(new Point2D(12961198.020164,4860192.568357));
        //MapController.animateTo(new Point2D(12639899.156435,4141095.472867));
        MapController.setZoom(2);

        changStatusIconCollor(false);
        return view;
    }

    //确保与碎片相关联的活动一定已经创建完毕的时候调用
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        final MainActivity mainActivity = (MainActivity)getActivity();

        //MapView.clearTilesInDB();
        if(mainActivity.SetviceState()){
            morphToSuccess2(pythButton);
            Log.i("FragementRecord","检测到服务启动！");
            value = 2;
        }
        else{
            morphToSquare(pythButton, 500);
            Log.i("FragementRecord","检测到服务未启动！");
            value = 1;
        }

        //设置圆形图片
        final QMUIRadiusImageView QMUI_Map_tadius = view.findViewById(R.id.QMUIIMap);
        QMUI_Map_tadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainActivity.SetviceState()){
                    Intent intent = new Intent(mainActivity, TrackMapActivity.class);
                    startActivity(intent);
                }
                else{
                    new QMUIDialog.MessageDialogBuilder(mainActivity)
                            .setTitle("请求服务")
                            .setMessage("展示私人定制可视化地图需要服务支持，是否开启服务？")
                            .addAction("取消", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    Log.i("MainActivity", "用户选择了取消！（不允许软件无服务运行）");
                                    Toast.makeText(mainActivity, "取消服务", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addAction("开启", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    Log.i("MainActivity", "用户选择了确定！");
                                    mainActivity.startLocationService();
                                    Intent intent = new Intent(mainActivity, TrackMapActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }

            }
        });

        //设置圆形图片
        final QMUIRadiusImageView QMUI_HotMap_tadius = view.findViewById(R.id.QMUIHotMap);
        QMUI_HotMap_tadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainActivity.SetviceState()){
                    Intent intent = new Intent(mainActivity, MapActivity.class);
                    startActivity(intent);
                }
                else{
                    new QMUIDialog.MessageDialogBuilder(mainActivity)
                            .setTitle("请求服务")
                            .setMessage("展示私人定制可视化地图需要服务支持，是否开启服务？")
                            .addAction("取消", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    Log.i("MainActivity", "用户选择了取消！（不允许软件无服务运行）");
                                    Toast.makeText(mainActivity, "取消服务", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addAction("开启", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    Log.i("MainActivity", "用户选择了确定！");
                                    mainActivity.startLocationService();
                                    Intent intent = new Intent(mainActivity, MapActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }

            }
        });

        //开始记录位置服务
        //morphToSuccess2(pythButton);
        //mainActivity.startLocationService();
        //value=2;
    }

    @Override
    public void onStart(){
        super.onStart();
    }
    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    public void onStop(){
        super.onStop();
    }

    //当与碎片关联的师徒被移出的时候调用
    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }
    //当碎片与活动解除关联的时候调用
    @Override
    public void onDetach(){
        super.onDetach();
        final MainActivity mainActivity = (MainActivity)getActivity();
        //mainActivity.stopLocationService();
        MapView.destroy();//销毁对象
        //清理缓存目录
        //DeleteFileUtil deleteFileUtil = new DeleteFileUtil();
        //deleteFileUtil.deleteDirectory(rootPath+"/SuperMap/tiles");
    }

    private void morphToSquare(final DynamicButton btnMorph, long duration) {
        DynamicButton.PropertyParam square = DynamicButton.PropertyParam.build()
                .duration(duration)
                .setCornerRadius(dimen(R.dimen.mb_corner_radius_2))
                .setWidth(dimen(R.dimen.mb_width_100))
                .setHeight(dimen(R.dimen.mb_height_36))
                .setColor(color(R.color.mb_blue))
                .setPressedColor(color(R.color.mb_blue_dark))
                .text(getString(R.string.mb_button));
        btnMorph.startChange(square);
    }

    private void morphToSuccess2(final DynamicButton btnMorph) {
        DynamicButton.PropertyParam circle = DynamicButton.PropertyParam.build()
                .duration(300)
                .setCornerRadius(dimen(R.dimen.mb_height_56))
                .setWidth(dimen(R.dimen.mb_width_120))
                .setHeight(dimen(R.dimen.mb_height_36))
                .setColor(color(R.color.mb_green))
                //.icon(drawable(R.drawable.ic_done))
                .text("正在监测")

                .setPressedColor(color(R.color.mb_green_dark));

        btnMorph.startChange(circle);
    }

    public int dimen(@DimenRes int resId) {
        return (int) getResources().getDimension(resId);
    }

    public int color(@ColorRes int resId) {
        return getResources().getColor(resId);
    }

    public Drawable drawable(int resId) {
        return getResources().getDrawable(resId);
    }

    public void changStatusIconCollor(boolean setDark) {
        final MainActivity mainActivity = (MainActivity)getActivity();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            View decorView = mainActivity.getWindow().getDecorView();
            if(decorView != null){
                int vis = decorView.getSystemUiVisibility();
                if(setDark){
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else{
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(vis);
            }
        }
    }
}
