package com.example.songt.pathmanager.Fragement;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.songt.pathmanager.Activity.LoginActivity;
import com.example.songt.pathmanager.Activity.MainActivity;
import com.example.songt.pathmanager.Activity.MapActivity;
import com.example.songt.pathmanager.R;
import com.example.songt.pathmanager.Toole.DateTimeUtil;
import com.example.songt.pathmanager.Toole.MyApplication;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.supermap.data.DatasetVector;
import com.supermap.data.Datasource;
import com.supermap.data.Recordset;
import com.supermap.mapping.LegendView;
import com.supermap.mapping.imChart.ChartView;
import com.supermap.mapping.imChart.ChartsView;
import com.supermap.mapping.imChart.LineChart;
import com.supermap.mapping.imChart.LineChartData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static com.example.songt.pathmanager.Toole.DateTimeUtil.gainCurrentDate;
import static com.supermap.data.CursorType.DYNAMIC;

public class FragementReport extends Fragment{
    QMUITopBar mTopBar;//标题栏
    LineChartView month_chart;
    LineChartView day_chart;
    LineChartView week_chart;

    private TextView day_textview;
    private TextView month_textview;

    private Datasource dataSource;
    private Datasource DaydataSource;
    private Datasource MonthdataSource;
    private Datasource Analyse_day;
    private Datasource Analyse_month;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        changStatusIconCollor(false);
        view = inflater.inflate(R.layout.fragement_report_layout, container, false);
        final MainActivity mainActivity = (MainActivity)getActivity();
        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(mainActivity);
        mTopBar = view.findViewById(R.id.topbar_report);
        mTopBar.setTitle("个人报表");


        //获取当前时间
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month;//月份
        month = calendar.get(Calendar.MONTH)+1;
        String day_month = "Dataset_"+calendar.get(Calendar.YEAR)+"_"+month;
        String day = day_month+"_"+calendar.get(Calendar.DATE);

        initChartView();
        ViewDayChart(day);
        ViewWeakChart();
        ViewMonthChart(day_month);

        return view;
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

    private void initChartView(){
        final MainActivity mainActivity = (MainActivity)getActivity();

        //图表
        day_chart = (LineChartView) view.findViewById(R.id.day_chart);
        week_chart = (LineChartView)view.findViewById(R.id.week_chart);
        month_chart =(LineChartView) view.findViewById(R.id.month_chart);


        dataSource = mainActivity.getDataSource();
        DaydataSource = mainActivity.getDaydataSource();
        MonthdataSource = mainActivity.getMonthdataSource();
        Analyse_day = mainActivity.getAnalyse_day();
        Analyse_month = mainActivity.getAnalyse_month();

        day_textview = (TextView)view.findViewById(R.id.day_textview);
        month_textview = (TextView)view.findViewById(R.id.month_textview);
    }


    //初始化数据
    private lecho.lib.hellocharts.model.LineChartData initDatas(List<Line> lines,Axis x,Axis y) {
        lecho.lib.hellocharts.model.LineChartData data = new lecho.lib.hellocharts.model.LineChartData(lines);
        data.setAxisYLeft(x);
        data.setAxisXBottom(y);
        return data;
    }
    /**
     * 当前显示区域
     *
     * @param left
     * @param right
     * @return
     */
    private Viewport initViewPort(float left, float right) {
        Viewport port = new Viewport();
        port.top = 150;
        port.bottom = 0;
        port.left = left;
        port.right = right;
        return port;
    }

    /**
     * 最大显示区域
     *
     * @param right
     * @return
     */
    private Viewport initMaxViewPort(float right,int top) {
        Viewport port = new Viewport();
        port.top = top;
        port.bottom = 0;
        port.left = 0;
        port.right = right;
        return port;
    }

    private void ViewDayChart(String day){
        //获取当前时间
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH)+1;//月份

        String text = calendar.get(Calendar.YEAR)+"年"+month+"月"+calendar.get(Calendar.DATE)+"日"+"活力报表";
        day_textview.setText(text);

        Log.i("MainActivity","开始加载日报表！");
        String MonthDatasetName = day;
        //检查数据源是否可用
        if(dataSource==null){
            Log.i("MainActivity","数据源为空！");
            return;
        }

        //设置hellochart数据
        List<Line> linesList;
        List<PointValue> pointValueList;
        List<PointValue> points;
        Axis axisY, axisX;
        lecho.lib.hellocharts.model.LineChartData lineChartData;
        List<PointValue> values;
        pointValueList = new ArrayList<>();
        linesList = new ArrayList<>();
        int maxy=0;

        //初始化坐标轴
        axisY = new Axis();
        //添加坐标轴的名称
        axisY.setLineColor(Color.parseColor("#aab2bd"));
        axisY.setTextColor(Color.parseColor("#aab2bd"));
        axisX = new Axis();
        axisX.setLineColor(Color.parseColor("#aab2bd"));


        //初始化数据
        lineChartData = new lecho.lib.hellocharts.model.LineChartData();
        lineChartData.setAxisYLeft(axisY);
        lineChartData.setAxisXBottom(axisX);

        day_chart.setLineChartData(lineChartData);
        //当前显示区域
        Viewport port = new Viewport();
        port.top = 100000;
        port.bottom = 0;
        port.left = 0;
        port.right = 50;
        day_chart.setCurrentViewportWithAnimation(port);
        day_chart.setInteractive(false);
        day_chart.setScrollEnabled(true);
        day_chart.setValueTouchEnabled(true);
        day_chart.setFocusableInTouchMode(true);
        day_chart.setViewportCalculationEnabled(false);
        day_chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        day_chart.startDataAnimation();
        points = new ArrayList<>();

        for(int i=0;i<24;i++){
            String hour = day+"_"+i;
            //存在则添加值
            if(dataSource.getDatasets().contains(hour)){
                DatasetVector hourDV = (DatasetVector) dataSource.getDatasets().get(hour);//打开数据集
                Recordset recordset = hourDV.getRecordset(false,DYNAMIC);//false返回所有记录的记录集，动态游标可修改记录集
                Object value = recordset.getRecordCount();
                //lineChartData.addValue((Integer)value);

                if(recordset.getRecordCount()>maxy){
                    maxy=recordset.getRecordCount();
                }
                //实时添加新的点
                PointValue value1 = new PointValue(i, recordset.getRecordCount());
                value1.setLabel(i+"");
                pointValueList.add(value1);

                float x = value1.getX();
                //根据新的点的集合画出新的线
                Line line = new Line(pointValueList);
                line.setColor(Color.RED);
                line.setShape(ValueShape.CIRCLE);
                line.setCubic(true);//曲线是否平滑，即是曲线还是折线

                linesList.clear();
                linesList.add(line);
                lineChartData = initDatas(linesList,axisX,axisY);
                day_chart.setLineChartData(lineChartData);
                //视图范围
                port=initViewPort(0,24);
                day_chart.setCurrentViewport(port);//当前窗口

                Viewport maPort = initMaxViewPort(x,maxy+100);
                day_chart.setMaximumViewport(maPort);//最大窗口
                Log.i("MainActivity","找到"+i+"时刻的共"+recordset.getRecordCount()+"个点！");
            }
            else{
                //Valuse.add(0);//否则为0
            }
        }
        //适应数据调整统计表可视域
        port.top=maxy+100;
        day_chart.setCurrentViewportWithAnimation(port);
        //((LineChartView)day_chart).addData(lineChartData);
    }


    //显示7天数据
    private void ViewWeakChart(){
        //检查数据源是否可用
        if(DaydataSource==null){
            Log.i("MainActivity","数据源为空！");
            return;
        }
        //设置hellochart数据
        List<Line> linesList;
        List<PointValue> pointValueList;
        List<PointValue> points;
        Axis axisY, axisX;
        lecho.lib.hellocharts.model.LineChartData lineChartData;
        List<PointValue> values;
        pointValueList = new ArrayList<>();
        linesList = new ArrayList<>();
        int maxy=0;

        //初始化坐标轴
        axisY = new Axis();
        //添加坐标轴的名称
        axisY.setLineColor(Color.parseColor("#aab2bd"));
        axisY.setTextColor(Color.parseColor("#aab2bd"));
        axisX = new Axis();
        axisX.setLineColor(Color.parseColor("#aab2bd"));


        //初始化数据
        lineChartData = new lecho.lib.hellocharts.model.LineChartData();
        lineChartData.setAxisYLeft(axisY);
        lineChartData.setAxisXBottom(axisX);

        week_chart.setLineChartData(lineChartData);
        //当前显示区域
        Viewport port = new Viewport();
        port.top = 100000;
        port.bottom = 0;
        port.left = 0;
        port.right = 50;
        week_chart.setCurrentViewportWithAnimation(port);
        week_chart.setInteractive(false);
        week_chart.setScrollEnabled(true);
        week_chart.setValueTouchEnabled(true);
        week_chart.setFocusableInTouchMode(true);
        week_chart.setViewportCalculationEnabled(false);
        week_chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        week_chart.startDataAnimation();
        points = new ArrayList<>();

        String WeekDatasetName = null;
        DatasetVector hourDV;
        Recordset recordset;
        Object value;
        //获取当前时间
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month;//月份
        int pointnum;
        month = calendar.get(Calendar.MONTH)+1;

        //回退7天
        date = DateTimeUtil.subDateTime(date,168);
        calendar.setTime(date);//标准化时间
        month = calendar.get(Calendar.MONTH)+1;

        Log.i("MainActivity","开始加载七日报表！");


        //循环7次，一次增一天
        for(int i=1;i<16;i++){
            WeekDatasetName = "Dataset_"+calendar.get(Calendar.YEAR)+"_"+month+"_"+calendar.get(Calendar.DATE);

            //存在则添加值
            if(DaydataSource.getDatasets().contains(WeekDatasetName)){
                hourDV = (DatasetVector) DaydataSource.getDatasets().get(WeekDatasetName);//打开数据集
                recordset = hourDV.getRecordset(false,DYNAMIC);//false返回所有记录的记录集，动态游标可修改记录集
                pointnum = recordset.getRecordCount();
                //实时添加新的点
                PointValue value1 = new PointValue(i, pointnum);
                value1.setLabel(i+"日");
                pointValueList.add(value1);

                if(pointnum>maxy){
                    maxy=pointnum;
                }

                int x = (int)value1.getX();
                //根据新的点的集合画出新的线
                Line line = new Line(pointValueList);
                line.setColor(Color.RED);
                line.setShape(ValueShape.CIRCLE);
                line.setCubic(true);//曲线是否平滑，即是曲线还是折线

                linesList.clear();
                linesList.add(line);
                lineChartData = initDatas(linesList,axisX,axisY);
                week_chart.setLineChartData(lineChartData);
                //视图范围
                port=initViewPort(0,8);
                week_chart.setCurrentViewport(port);//当前窗口

                Viewport maPort = initMaxViewPort(x,maxy+200);
                week_chart.setMaximumViewport(maPort);//最大窗口


                Log.i("MainActivity","找到"+i+"日的共"+pointnum+"个点！");

                //增一天
                date = DateTimeUtil.addDateTime(date,24);
                calendar.setTime(date);//标准化时间
                month = calendar.get(Calendar.MONTH)+1;
            }
            else{
                //lineChartData.addValue(0);//否则为0
                //增一天
                date = DateTimeUtil.addDateTime(date,24);
                calendar.setTime(date);//标准化时间
                month = calendar.get(Calendar.MONTH)+1;
            }
        }
        //适应数据调整统计表可视域
        port.top=maxy+260;
        week_chart.setCurrentViewportWithAnimation(port);
    }
    private void ViewMonthChart(String month){

        //获取系统的日期
        Calendar calendar = Calendar.getInstance();
        //年
        int day_year = calendar.get(Calendar.YEAR);
        //月
        int day_month = calendar.get(Calendar.MONTH)+1;//上个月
        //日
        int day_day = calendar.get(Calendar.DAY_OF_MONTH);

        String text = day_year+"年"+day_month+"月"+"活力报表";
        month_textview.setText(text);

        int endday = day_day;
        if(endday<7){
            //显示回退一个月数据
            day_month-=1;
            month = "Dataset_"+day_year+"_"+day_month;
            day_month+=1;
        }
        Log.i("MainActivity","开始加载月报表！");
        String MonthDatasetName = month;
        //检查数据源是否可用
        if(DaydataSource==null||Analyse_day==null){
            Log.i("MainActivity","数据源为空！");
            return;
        }

        //设置hellochart数据
        List<Line> linesList;
        List<PointValue> pointValueList;
        List<PointValue> points;
        Axis axisY, axisX;
        lecho.lib.hellocharts.model.LineChartData lineChartData;
        List<PointValue> values;
        pointValueList = new ArrayList<>();
        linesList = new ArrayList<>();
        int maxy=0;

        //初始化坐标轴
        axisY = new Axis();
        //添加坐标轴的名称
        axisY.setLineColor(Color.parseColor("#aab2bd"));
        axisY.setTextColor(Color.parseColor("#aab2bd"));
        axisX = new Axis();
        axisX.setLineColor(Color.parseColor("#aab2bd"));


        //初始化数据
        lineChartData = new lecho.lib.hellocharts.model.LineChartData();
        lineChartData.setAxisYLeft(axisY);
        lineChartData.setAxisXBottom(axisX);

        month_chart.setLineChartData(lineChartData);
        //当前显示区域
        Viewport port = new Viewport();
        port.top = 1000;
        port.bottom = 0;
        port.left = 0;
        port.right = 30;
        month_chart.setCurrentViewportWithAnimation(port);
        month_chart.setInteractive(false);
        month_chart.setScrollEnabled(true);
        month_chart.setValueTouchEnabled(true);
        month_chart.setFocusableInTouchMode(true);
        month_chart.setViewportCalculationEnabled(false);
        month_chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        month_chart.startDataAnimation();
        points = new ArrayList<>();

        //lineChartData.setGeoID(IDs[i]);		//设置图表子项关系的几何对象SmID
        //lineChartData.setLabel(day);	//设置图表子项的标签
        for(int i=1;i<endday;i++){
            String day = month+"_"+i;
            //存在则添加值
            if(DaydataSource.getDatasets().contains(day)){
                DatasetVector hourDV = (DatasetVector) DaydataSource.getDatasets().get(day);//打开数据集
                Recordset recordset = hourDV.getRecordset(false,DYNAMIC);//false返回所有记录的记录集，动态游标可修改记录集
                Object value = recordset.getRecordCount();

                if(recordset.getRecordCount()>maxy){
                    maxy=recordset.getRecordCount();
                }
                //实时添加新的点
                PointValue value1 = new PointValue(i, recordset.getRecordCount());
                value1.setLabel(month+"_"+i);
                pointValueList.add(value1);

                float x = value1.getX();
                //根据新的点的集合画出新的线
                Line line = new Line(pointValueList);
                line.setColor(Color.RED);
                line.setShape(ValueShape.CIRCLE);
                line.setCubic(true);//曲线是否平滑，即是曲线还是折线

                linesList.clear();
                linesList.add(line);
                lineChartData = initDatas(linesList,axisX,axisY);
                month_chart.setLineChartData(lineChartData);

                //视图范围
                port=initViewPort(0,31);
                month_chart.setCurrentViewport(port);//当前窗口

                Viewport maPort = initMaxViewPort(x,maxy+400);
                month_chart.setMaximumViewport(maPort);//最大窗口
                Log.i("MainActivity","找到"+i+"日的共"+recordset.getRecordCount()+"个点！");
            }
            else{
                //lineChartData.addValue(0);//否则为0
            }

            //适应数据调整统计表可视域
            port.top=maxy+260;
            month_chart.setCurrentViewportWithAnimation(port);
        }

//        //驻留点
//        LineChartData analyse_lineChartData = new LineChartData();
//        analyse_lineChartData.setColor(Color.rgb(255,255,0));	//设置图标子项的颜色
//        //lineChartData.setGeoID(IDs[i]);		//设置图表子项关系的几何对象SmID
//        //lineChartData.setLabel(day);	//设置图表子项的标签
//        for(int i=1;i<endday;i++){
//            String day = month+"_"+i;
//            //存在则添加值
//            if(Analyse_day.getDatasets().contains(day)){
//                DatasetVector hourDV = (DatasetVector) Analyse_day.getDatasets().get(day);//打开数据集
//                Recordset recordset = hourDV.getRecordset(false,DYNAMIC);//false返回所有记录的记录集，动态游标可修改记录集
//                Object value = recordset.getRecordCount();
//                analyse_lineChartData.addValue((Integer)value);
//                Log.i("MainActivity","找到"+i+"日的共"+recordset.getRecordCount()+"个点！");
//            }
//            else{
//                analyse_lineChartData.addValue(0);//否则为0
//            }
//        }
//        ((LineChartView)month_chart).addData(analyse_lineChartData);
//
//        for(int i=1; i<endday; i++){
//            ((LineChartView)month_chart).addXLabel(i+"");
//        }

        //month_chart.setChartTitle("日轨迹点统计表");	//设置图表标题
    }



}
