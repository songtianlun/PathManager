package com.example.songt.pathmanager.Toole;

import android.util.Log;


import com.example.songt.pathmanager.Activity.ShareLocation;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 * 	处理消息收发
 * @author 123
 *
 */
public class MyClientHandler extends IoHandlerAdapter {

	private ShareLocation ma;
	/**
	 * 网络连接出现异常
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        Log.i("LINK","网络连接出现异常");

	}
	/**
	 * 收到消息
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		Log.i("LINK","客户端收到的消息 : " + message + "---"+ message.toString().length());
		if(message.toString().length() > 10){
			Log.i("LINK","客户端收到的消息 =======: " + message.toString().length());
			String[] mes = message.toString().split(",");//经度，纬度，标识
			if(!mes[2].equals("0")){//拿到对方位置
				ma.showYouLocation(mes[0],mes[1]);
			}
		}
//		session.write(message + " 消息已收到");
	}
	/**
	 * 消息发送
	 */
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
        Log.i("LINK","消息发送");
	}
	/**
	 * 客户端和服务端连接关闭
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
        Log.i("LINK","客户端和服务端连接关闭");
		ma.removeMark();
//		System.exit(0);
	}
	/**
	 * 会话创建
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
        Log.i("LINK","会话创建");
		ma = new ShareLocation();
	}
	/**
	 * 会话进入空闲状态
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        Log.i("LINK","会话进入空闲状态");
	}
	/**
	 * 会话打开
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
        Log.i("LINK","会话打开");
	}

	
}
