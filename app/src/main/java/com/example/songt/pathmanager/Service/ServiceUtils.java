package com.example.songt.pathmanager.Service;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

public class ServiceUtils {
    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        Log.i("MainActivity","开始检测服务:"+ServiceName+"是否运行！");
        if (("").equals(ServiceName) || ServiceName == null){
            Log.i("ServiceUtils","服务名“"+ServiceName+"”为空！");
            return false;
        }
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(200);
        Log.i("MainActivity","服务总数："+runningService.size());
        int i;
        for (i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                Log.i("MainActivity","正在遍历服务："+runningService.get(i).service.getClassName().toString());
                Log.i("ServiceUtils","服务"+ServiceName+"正在运行！");
                return true;
            }
        }
        Log.i("ServiceUtils","检测服务个数："+i);
        Log.i("ServiceUtils","服务"+ServiceName+"未运行！");
        return false;
    }

}
