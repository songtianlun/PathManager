package com.example.songt.pathmanager.Fragement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.songt.pathmanager.Activity.Load_Activity;
import com.example.songt.pathmanager.Activity.LoginActivity;
import com.example.songt.pathmanager.Activity.MainActivity;
import com.example.songt.pathmanager.Activity.ShareLocation;
import com.example.songt.pathmanager.R;
import com.example.songt.pathmanager.Toole.ActivityCollector;
import com.example.songt.pathmanager.Toole.MyApplication;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIAnimationListView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import static android.content.Context.MODE_PRIVATE;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.songt.pathmanager.UI.ProgressButton.TAG;

public class FragementPersonal extends Fragment{
    QMUIGroupListView qmuiGroupListView;
    QMUITopBar mTopBar;//标题栏
    private SharedPreferences UserInformation;
    private SharedPreferences.Editor editor_UserInformation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final MainActivity mainActivity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.fragement_personal_layout, container, false);
        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(mainActivity);
        mTopBar = view.findViewById(R.id.topbar_person);
        mTopBar.setTitle("个人中心");
        qmuiGroupListView = view.findViewById(R.id.QMUIGLV);
        changStatusIconCollor(false);

        initQMUIGroupListView();
        return view;
    }

    private void initQMUIGroupListView(){

        //记住用户信息
        UserInformation = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String id = UserInformation.getString("id","");
        String name = UserInformation.getString("name","");
        String nickname = UserInformation.getString("nickname","");
        String school = UserInformation.getString("School","");
        int sex = UserInformation.getInt("sex",1);
        String CreateData = UserInformation.getString("CreatedAt","");
        //用户ID
        QMUICommonListItemView itemWithDetail = qmuiGroupListView.createItemView("用户ID");
        itemWithDetail.setDetailText(id);//设置右边详细信息
        //用户账号
        QMUICommonListItemView itemUsername = qmuiGroupListView.createItemView("账号");
        itemUsername.setDetailText(name);
        //itemUsername.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        //用户昵称
        QMUICommonListItemView itemVersion = qmuiGroupListView.createItemView("昵称");
        //itemVersion.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        itemVersion.setDetailText(nickname);
        //性别
        QMUICommonListItemView itemExpireDate = qmuiGroupListView.createItemView("性别");
        //itemExpireDate.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        if(sex==1)
            itemExpireDate.setDetailText("男");
        else
            itemExpireDate.setDetailText("女");
        //学校
        QMUICommonListItemView itemSchoolDetail = qmuiGroupListView.createItemView("学校");
        itemSchoolDetail.setDetailText(school);//设置右边详细信息
        //注册时间
        QMUICommonListItemView itemCreatDataDetail = qmuiGroupListView.createItemView("注册时间");
        itemCreatDataDetail.setDetailText(CreateData);//设置右边详细信息

        QMUICommonListItemView itemLogout = qmuiGroupListView.createItemView("退出登录");
        itemLogout.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        itemLogout.getTextView().setTextColor(getResources().getColor(R.color.qmui_config_color_red));

        QMUICommonListItemView itemSharelocation = qmuiGroupListView.createItemView("位置共享");
        itemLogout.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);


        QMUIGroupListView.newSection(getContext())
                .setTitle("用户中心")
                .addItemView(itemWithDetail,mOnClickListenerGroup)
                .addItemView(itemUsername, mOnClickListenerGroup)
                .addItemView(itemVersion, mOnClickListenerGroup)
                .addItemView(itemExpireDate, mOnClickListenerGroup)
                .addItemView(itemSchoolDetail,mOnClickListenerGroup)
                .addItemView(itemCreatDataDetail,mOnClickListenerGroup)
                .addItemView(itemLogout, mOnClickListenerGroup)
                .addTo(qmuiGroupListView);

        QMUIGroupListView.newSection(getContext())
                .setTitle("工具箱")
                .addItemView(itemSharelocation,mOnClickListenerGroup)
                .addTo(qmuiGroupListView);
    }

    //统一处理选项点击事件
    private View.OnClickListener mOnClickListenerGroup = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            QMUICommonListItemView viewList = (QMUICommonListItemView) view;
            Log.d(TAG, "选项：" + viewList.getText().toString() + " 点击了");
            switch (viewList.getText().toString()) {
                case "账号":
                    break;
                case "用户版本":
                    break;
                case "到期时间":
                    break;
                case "退出登录":
                    new QMUIDialog.MessageDialogBuilder(getActivity())
                            .setTitle("登出确认")
                            .setMessage("确定要退出登录吗？")
                            .addAction("取消", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .addAction(0, "退出", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    //Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    //跳转
                                    MainActivity mainActivity = (MainActivity)getActivity();
                                    if(mainActivity.SetviceState()){
                                        mainActivity.stopLocationService();
                                    }
                                    Intent intent=new Intent(mainActivity,LoginActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .show();
                    break;
                case "位置共享":
                    //跳转
                    MainActivity mainActivity = (MainActivity)getActivity();
                    Intent intent=new Intent(mainActivity,ShareLocation.class);
                    startActivity(intent);
                    break;
            }
            //Toast.makeText(MyApplication.getContext(),"选项：" + viewList.getText().toString() + " 点击了",Toast.LENGTH_SHORT).show();
        }
    };

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
