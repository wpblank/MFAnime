package cn.lzumi.mfanime.tools;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class loginInfo {
    /**
     * 保存登录状态和登录用户名到SharedPreferences中
     */
    public static void saveLoginToken(String token, String userName, Context context) {
        //saveLoginStatus(true, userName);
        //loginInfo表示文件名  SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginInfo", MODE_PRIVATE);
        //获取编辑器
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //存入boolean类型的登录状态
        //editor.putBoolean("isLogin", status);
        //存入登录状态时的token
        editor.putString("token", token);
        //存入用户昵称
        editor.putString("userName", userName);
        //提交修改
        editor.apply();
    }

    //读取用户名
    public static String readLoginUserName(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginInfo",Context.MODE_PRIVATE);
        return sharedPreferences.getString("userName","未登录");
    }

    //获取token
    public static String readToken(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginInfo",Context.MODE_PRIVATE);
        return sharedPreferences.getString("token","");
    }

    //清除登录状态
    public static void cleanLoginStatus(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginInfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", "");
        editor.putString("userName","");
        editor.apply();
    }
}
