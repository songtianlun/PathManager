package com.example.songt.pathmanager.Toole;

import android.content.Context;
import android.util.Log;

import com.example.songt.pathmanager.Activity.ShareLocation;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import java.util.HashMap;
import java.util.Map;


public class MessageReceiver extends XGPushBaseReceiver {

    private static Map<String, String> mes = new HashMap<>();
    // 通知展示
    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult notifiShowedRlt) {

        Log.i("LINK","通知展示 : " + notifiShowedRlt);

    }

    //注册的回调
    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {
        Log.i("LINK","注册的回调"+ " i = " + i + " xgPushRegisterResult = " + xgPushRegisterResult);
    }

    //反注册的回调
    @Override
    public void onUnregisterResult(Context context, int i) {
        Log.i("TAG","反注册的回调"+ " i = " + i);
    }

    //设置tag的回调
    @Override
    public void onSetTagResult(Context context, int i, String s) {
        Log.i("LINK","设置tag的回调"+ " i = " + i + " s = " + s);
    }

    //删除tag的回调
    @Override
    public void onDeleteTagResult(Context context, int i, String s) {
        Log.i("LINK","删除tag的回调 " + " i = " + i + " s = " + s);
    }

    // 消息透传的回调
    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        Log.i("LINK","消息透传的回调 = " + message);
        String content = message.getTitle();
        Log.i("LINK", "content = " + content);

//        new MainActivity().showDialog("用户：" + title + "请求与您分享位置信息");
           ShareLocation.showDialog(content,"用户ID：" + content + "\n请求与您共享位置信息");
//        }

        
    }

    // 通知点击回调 actionType=1为该消息被清除，
    // actionType=0为该消息被点击。此处不能做点击消息跳转
    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult message) {
        Log.i("LINK","通知点击回调 " + message);
    }

}
