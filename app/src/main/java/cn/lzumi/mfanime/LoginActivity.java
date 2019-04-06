package cn.lzumi.mfanime;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;

import java.util.HashMap;
import java.util.Map;

import cn.lzumi.mfanime.tools.BaseActivity;
import cn.lzumi.mfanime.tools.Constant;
import cn.lzumi.mfanime.tools.HttpRequest;
import cn.lzumi.mfanime.tools.MD5Utils;
import cn.lzumi.mfanime.tools.WaitingInterface;
import cn.lzumi.mfanime.tools.loginInfo;

public class LoginActivity extends BaseActivity {

    private EditText editUser, editPasswd;
    private String userName, password;

    RequestQueue requestQueue;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //设置此界面为竖屏
        ActionBar actionbar = getSupportActionBar();//隐藏自带标题栏
        if (actionbar != null) {
            actionbar.hide();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //HTTPSTrustManager.allowAllSSL();
        requestQueue = HttpRequest.getRequestQueue(this);
        initLogin();
    }


    //登陆界面初始化
    public void initLogin() {
        editUser = findViewById(R.id.input_userName);
        editPasswd = findViewById(R.id.input_password);
        Button button_login = findViewById(R.id.btn_login);
        TextView createUser = findViewById(R.id.link_signup);

        //登录按钮的点击事件
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取用户名密码、并去掉空格
                userName = editUser.getText().toString().trim();
                password = editPasswd.getText().toString().trim();

                if (TextUtils.isEmpty(userName)) {
                    Toast toast = Toast.makeText(LoginActivity.this, null, Toast.LENGTH_SHORT);
                    toast.setText("用户名为空");
                    toast.show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast toast = Toast.makeText(LoginActivity.this, null, Toast.LENGTH_SHORT);
                    toast.setText("密码为空");
                    toast.show();
                } else {
                    String md5Psw = MD5Utils.md5(password);
                    tryLogin(userName, md5Psw);
                }
            }
        });

        createUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    /**
     * 尝试登陆
     *
     * @param userName
     * @param md5Psw
     */
    private void tryLogin(final String userName, final String md5Psw) {
        //等待进度条
        final Dialog dialog = WaitingInterface.createLoadingDialog(LoginActivity.this, "");

        final Map<String, String> params = new HashMap<>();
        //params.put("\"username\"", "\"12345678@bjtu.edu.cn\"");
        params.put("name", userName);
        params.put("password", md5Psw);

        HttpRequest.httpStringPost(Constant.prefix + "/user/login/", requestQueue, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String string = response.substring(0, 4);
                System.out.println(string);
                switch (string) {
                    case "登陆成功": {
                        String token = response.substring(4);
                        loginInfo.saveLoginToken(token, userName, LoginActivity.this);
                        WaitingInterface.closeDialog(dialog);
                        Toast toast = Toast.makeText(LoginActivity.this, "mi", Toast.LENGTH_SHORT);
                        toast.setText(string);
                        toast.show();
                        //销毁登录界面
                        LoginActivity.this.finish();
                        //跳转到主界面，登录成功的状态传递到 Fragment_order 中
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        break;
                    }
                    case "密码错误": {
                        System.out.println(md5Psw);
                        WaitingInterface.closeDialog(dialog);
                        Toast toast = Toast.makeText(LoginActivity.this, null, Toast.LENGTH_SHORT);
                        toast.setText(string);
                        toast.show();
                        break;
                    }
                    default:
                        WaitingInterface.closeDialog(dialog);
                        System.out.println(response);
                        break;
                }
            }
        });
    }
}