package com.example.songt.pathmanager.Toole;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class HttpCon {

    private static Context context;
    private static String returnCode;
    private static Map<String,String> params;
    private static String url;
    private static boolean flag =false;
    /**
     * 访问服务器，如登录操作
     * @param param      要上传的数据，键值对Map
     * @param urlString  url
     * @param con        上下文对象
     * @return true 与服务器交互成功，false 与服务器交互失败
     */
    public static boolean  startLink(Map<String,String> param, String urlString, Context con){
        params = param;
        url = urlString;
        context = con;

        //访问服务器
        startVisit();
        //判断 returnCode 值
        return flag;
    }

    //开始访问服务器
    private static void startVisit(){
        MyThread my = new MyThread();
        my.start();
        returnCode = "0";
        //判断上传是否完成
        Log.i("AAAA","开始阻塞");
//        while (returnCode.equals("0")){ }
        try {
            my.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("AAAA","结束阻塞");
    }

    //子线程 访问网络
    private static class MyThread extends Thread {
        @Override
        public void run() {
            String strResult= submitPostData(url,params, "UTF-8");
            Log.i("AAAA","strResult = " + strResult);
                returnCode = strResult;
            Log.i("AAAA","startLink-----");
            if(returnCode.equals("1")){
                openToast("服务器正在开小差");
                flag =  false;
            }else if(returnCode.equals("true")){
//            openToast("访问成功");
                flag =  true;
            }else if(returnCode.equals("2")){
                openToast("服务器被外星人偷走了");
            }else{
//            openToast("账号或密码错误");
                flag =  false;
            }
            }
        }
    /*
     * Function  :   发送Post请求到服务器
     * Param     :   params请求体内容，encode编码格式
     */
    private static String submitPostData(String strUrlPath, Map<String, String> params, String encode) {

        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        Log.i("AAAA", "submitPostData: +data:"+data.toString());
        OutputStream outputStream = null;
        Log.i("AAAA","URL = " + strUrlPath);
        try {
            URL url = new URL(strUrlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(30000);     //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            //解决中文乱码问题
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            Log.i("AAAA","22222");
            outputStream = httpURLConnection.getOutputStream();

            Log.i("AAAA","000 = " + data.length);
            outputStream.write(data);
            Log.i("AAAA","000-----0000--" + new String(data).toString());
            //共享位置信息
            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            Log.i("AAAA","返回码 = " + response);
            if(response == HttpURLConnection.HTTP_OK) {
                Log.i("AAAA","3333333333333333333333");
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream);                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            //e.printStackTrace();
            Log.i("AAAA", "submitPostData: "+e.getMessage().toString());
            //return "err: " + e.getMessage().toString();
            return "2";
        }finally {
            try {
                if(outputStream!=null){
                    outputStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return "1";
    }
    /*
     * Function  :   封装请求体信息
     * Param     :   params请求体内容，encode编码格式
     */
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    /*
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     */
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        Log.i("AAAA","服务器传回来的数据为 = " + resultData);
        return resultData;
    }
    //弹出消息
    private static void openToast(String strMsg){
        Toast.makeText(context, strMsg, Toast.LENGTH_LONG).show();
    }
}
