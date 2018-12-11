package com.example.songt.pathmanager.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.songt.pathmanager.R;
import com.example.songt.pathmanager.Service.Person;
import com.example.songt.pathmanager.Toole.ActivityCollector;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.bmob.v3.listener.SaveListener;

public class RegisterActivity extends AppCompatActivity {


    QMUIRoundButton register_ok,QMUIRB_Cancle;
    QMUITopBar mTopBar;//标题栏
    private TextView register_user;
    private TextView register_password;
    private TextView register_nickname;
    private TextView register_school;
    private TextView register_profession;
    private RadioGroup register_sex;
    private Person p2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(this);
        mTopBar = this.findViewById(R.id.topbar);
        mTopBar.setTitle("注册中心");

        addControl();//加载控件
        addRegisterShow();//注册方法
    }

    private void addRegisterShow() {
        register_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String rUser=register_user.getText().toString();
                String rPassword=register_password.getText().toString();
                String rNickname=register_nickname.getText().toString();
                String rSchool=register_school.getText().toString();
                String profession=register_profession.getText().toString();
                int id=register_sex.getCheckedRadioButtonId();
                RadioButton choise = (RadioButton) findViewById(id);// 获取这个RadioButton的text内容
                String output = choise.getText().toString();
                int rSex=2;
                if(output.equals("男")){
                    rSex=1;
                }

                //判断用户名和密码是否为空,如果为空则不能进去。
                if(rUser.length()>0&&rPassword.length()>0){
                    p2 = new Person();
                    p2.setName(rUser);
                    p2.setPassword(md5(rPassword));
                    p2.setNickname(rNickname);
                    p2.setSex(rSex);
                    p2.setschool(rSchool);
                    p2.setProfession(profession);
                    //插入方法
                    p2.save(RegisterActivity.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            // TODO Auto-generated method stub
                            register_password.setText("");
                            register_user.setText("");
                            register_nickname.setText("");
                            register_school.setText("");
                            register_profession.setText("");
                            Log.i("ResigisterActivity","添加数据成功，返回objectId为："+ p2.getObjectId());
                            Toast.makeText(RegisterActivity.this, "注册成功！" , Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onFailure(int code, String msg) {
                            // TODO Auto-generated method stub
                            Toast.makeText(RegisterActivity.this, "创建数据失败：" + msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this, "用户名或者密码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addControl(){
        register_ok = (QMUIRoundButton)this.findViewById(R.id.Register_QMUIRB_OK);
        QMUIRB_Cancle = (QMUIRoundButton)this.findViewById(R.id.Register_QMUIRB_Cancle);
        register_user = (EditText)this.findViewById(R.id.Register_idEditText);
        register_password = (EditText)this.findViewById(R.id.Register_pwEditText);
        register_nickname = (EditText)this.findViewById(R.id.Register_nnEditText);
        register_sex = (RadioGroup)this.findViewById(R.id.Register_seRadioGroup);
        register_school = (EditText)this.findViewById(R.id.Register_smEditText);

        QMUIPopup qmuiPopup = new QMUIPopup(this);
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
}
