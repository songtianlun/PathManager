package com.example.songt.pathmanager.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.songt.pathmanager.R;
import com.example.songt.pathmanager.Service.Person;
import com.example.songt.pathmanager.Toole.ActivityCollector;
import com.example.songt.pathmanager.Toole.MyApplication;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.restlet.security.User;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import static com.example.songt.pathmanager.Toole.MyApplication.getContext;

public class LoginActivity extends AppCompatActivity {


    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private SharedPreferences UserInformation;
    private SharedPreferences.Editor editor_UserInformation;
    private CheckBox rememberPass;
    private CheckBox autologin;
    private Person p2;
    private TextView lgUser;
    private TextView lgPassword;
    QMUIRoundButton btn_ok,btn_rg;
    QMUITopBar mTopBar;//标题栏
    Context context;
    QMUITipDialog tipDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_login);
        Bmob.initialize(this,"03c262c2b3adc4f4d9f92b3f0f586f2c");//Bmob初始化

        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(this);
        mTopBar = this.findViewById(R.id.topbar);
        mTopBar.setTitle("请登陆");

        addControl();
        RememberPass();
        AutoLogin();
        addLogin();

        context = this;

        //tipDialog.dismiss();
        btn_rg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            new QMUIDialog.MessageDialogBuilder(this)
                    .setTitle("关闭程序")
                    .setMessage("确定要关闭程序吗？")
                    .addAction("取消", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction(0, "关闭", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            //Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            ActivityCollector.finishAll();
                        }
                    })
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void addLogin() {
        btn_rg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDialog = new QMUITipDialog.Builder(context)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("正在登录")
                        .create();
                tipDialog.show();

                BmobQuery<Person> query=new BmobQuery<Person>();
                query.findObjects(LoginActivity.this,new FindListener<Person>(){

                    String lgU=lgUser.getText().toString().trim();
                    String lgp=md5(lgPassword.getText().toString().trim());
                    int panduan=1;

                    @Override
                    public void onSuccess(List<Person> list) {
                        for(int i=0;i<list.size();i++){
                            String name=list.get(i).getName();
                            String password=list.get(i).getPassword();
                            Log.e("user","唯一 id:"+list.get(i).getObjectId()+"----"+name+"---"+password);
                            if(name.equals(lgU) && password.equals(lgp)){
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                                editor = pref.edit();//是否记住密码
                                if(autologin.isChecked()){
                                    editor.putBoolean("auto_login",true);
                                }
                                else{
                                    editor.putBoolean("auto_login",false);
                                }
                                if(rememberPass.isChecked()){
                                    editor.putBoolean("remember_password",true);
                                    editor.putString("account",lgU);
                                    editor.putString("password",lgPassword.getText().toString().trim());
                                }
                                else{
                                    editor.clear();
                                }
                                editor.apply();

                                //记住用户信息
                                UserInformation = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                                editor_UserInformation = UserInformation.edit();
                                editor_UserInformation.putInt("item",i);
                                editor_UserInformation.putString("name",list.get(i).getName());
                                editor_UserInformation.putString("nickname",list.get(i).getNickname());
                                editor_UserInformation.putInt("sex",list.get(i).getSex());
                                editor_UserInformation.putString("School",list.get(i).getschool());
                                editor_UserInformation.putString("id",list.get(i).getObjectId());
                                editor_UserInformation.putString("CreatedAt",list.get(i).getCreatedAt());
                                editor_UserInformation.apply();

                                panduan=2;
                                //成功后panduan等于2,则跳出该循环,并且把输入快都清空,跳转到指定页面
                                //lgUser.setText("");
                                //lgPassword.setText("");
                                tipDialog.dismiss();
                                //跳转
                                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            }

                        }
                        if(panduan==1){
                            tipDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onError(int i, String s) {

                    }
                });
            }

        });


    }

    private void addControl() {

        lgUser = (TextView) findViewById(R.id.idEditText);
        lgPassword = (TextView) findViewById(R.id.pwEditText);
        btn_ok = (QMUIRoundButton) this.findViewById(R.id.QMUIRB_Login);
        btn_rg = (QMUIRoundButton) this.findViewById(R.id.QMUIRB_Register);
        rememberPass = (CheckBox)this.findViewById(R.id.RemPass);
        autologin = (CheckBox)this.findViewById(R.id.AutoLogin);
        autologin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(autologin.isChecked()){
                    rememberPass.setChecked(true);
                    autologin.setChecked(true);
                }
            }
        });
        rememberPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(rememberPass.isChecked()==false){
                    autologin.setChecked(false);
                    rememberPass.setChecked(false);
                }
            }
        });
    }

    //MD5加密
    public static String md5(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException",e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    //记住密码
    private void RememberPass(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemember = pref.getBoolean("remember_password",false);
        if(isRemember){
            String account = pref.getString("account","");
            String password = pref.getString("password","");
            lgUser.setText(account);
            lgPassword.setText(password);
            rememberPass.setChecked(true);
        }
        boolean antoLogin = pref.getBoolean("auto_login",false);
        if(antoLogin){
            autologin.setChecked(true);
        }
    }

    //自动登录
    private void AutoLogin(){
        if(autologin.isChecked()){
            //btn_ok.performClick();
           // Log.i("LoginActivity","触发登录按钮");

        }
        else{
            return;
        }
    }
}
