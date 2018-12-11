package com.example.songt.pathmanager.Service;

import cn.bmob.v3.BmobObject;

public class Person extends BmobObject {
    private String nickname;//用户名
    private String password;//密码
    private String name;//昵称
    private int sex;//性别
    private String school;//学校
    private String profession;//专业

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public int getSex(){
        return sex;
    }
    public void setSex(int sex){
        this.sex = sex;
    }

    public String getNickname(){
        return nickname;
    }
    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public String getschool(){
        return school;
    }
    public void setschool(String school){
        this.school = school;
    }

    public String getProfession(){return profession;}
    public void setProfession(String profession){this.profession = profession;}


}
