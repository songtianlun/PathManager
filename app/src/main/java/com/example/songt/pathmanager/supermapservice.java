package com.example.songt.pathmanager;


import android.content.Context;
import android.util.Log;
import android.view.View;

import com.supermap.data.Datasource;
import com.supermap.data.Environment;
import com.supermap.data.LicenseType;
import com.supermap.data.Maps;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.mapping.Map;
import com.supermap.mapping.MapControl;
import com.supermap.mapping.MapView;
import com.supermap.navi.Navigation;
import com.supermap.navi.SuperMapPatent;

public class supermapservice {

    private Workspace workspace = null;//工作空间
    private Maps maps = null;//地图数据集合
    private MapControl mapcontrol = null;//地图控件
    private Map map = null;//地图
    private MapView mapView = null;//地图控件
    PrjCoordSys prjCoordSys;
    private static Navigation mNavigation;//导航类
    private Datasource track = null;
    private Datasource day_Datasource     = null;
    private Datasource month_Datasource   = null;
    private Datasource Analyse_day;
    private Datasource Analyse_month;
    private String rootPath;

    public void init(Context context){
        //iMobile初始化
        rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        //设置一些系统需要用到的路径
        Environment.setOpenGLMode(true);
        Environment.setLicensePath(rootPath + "/SuperMap/License/");
        Environment.setLicenseType(LicenseType.UUID);
        Environment.setTemporaryPath(rootPath + "/SuperMap/temp/");
        Environment.setWebCacheDirectory(rootPath + "/SuperMap/WebCatch");
        Log.i("MainActivity", "许可路径：" + rootPath + "/SuperMap/License/");
        //组件功能必须在Environment初始化之后才能调用
        Environment.initialization(context);
        Log.i("MainActivity", "Environment初始化完毕！");

        //打开工作空间
        workspace = new Workspace();
        WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
        info.setServer(rootPath + "/SuperMap/LifeManager/BasicMap/BasicMap.smwu");
        info.setType(WorkspaceType.SMWU);
        workspace.open(info);
        maps = workspace.getMaps();

        //将地图与工作空间关联
        mapView = new MapView(context);
        mapcontrol = mapView.getMapControl();
        map = mapcontrol.getMap();
        map.setWorkspace(workspace);

        //从导航组件,可进行加密
        mNavigation = mapcontrol.getNavigation();//获取导航模块
        mNavigation.setEncryption(new SuperMapPatent());//设置加密器
        //地图投影
        prjCoordSys=mapcontrol.getMap().getPrjCoordSys();

        //获取数据源
        track = mapcontrol.getMap().getWorkspace().getDatasources().get("track");
        day_Datasource     = mapcontrol.getMap().getWorkspace().getDatasources().get("track_day");
        month_Datasource   = mapcontrol.getMap().getWorkspace().getDatasources().get("track_month");
        Analyse_day = mapcontrol.getMap().getWorkspace().getDatasources().get("track_analyse_day");
        Analyse_month = mapcontrol.getMap().getWorkspace().getDatasources().get("track_analyse_month");
    }

    public Workspace getWorkplace(){
        return workspace;
    }
    public MapView getMaoView(){
        return mapView;
    }
    public MapView openMap(String mapname){
        map.open(mapname);
        map.refresh();
        return mapView;
    }
    public MapControl getMapcontrol() {
        return mapcontrol;
    }
    public Datasource getDay_Datasource() {
        return day_Datasource;
    }
    public Datasource getMonth_Datasource() {
        return month_Datasource;
    }
    public Datasource gettrack(){
        return track;
    }
    public Datasource getAnalyse_month() {
        return Analyse_month;
    }
    public Datasource getAnalyse_day() {
        return Analyse_day;
    }
    public Navigation getNavigation(){
        return mNavigation;
    }

    public Map getMap() {
        return map;
    }

    public PrjCoordSys getPrjCoordSys() {
        return prjCoordSys;
    }

    public Maps getMaps() {
        return maps;
    }
}
