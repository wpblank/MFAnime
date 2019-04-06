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
import cn.lzumi.mfanime.tools.WaitingInterface;

public class RegisterActivity extends BaseActivity {
    private EditText editUser, editPasswd;
    private String userName, password;

    RequestQueue requestQueue;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //设置此界面为竖屏
        ActionBar actionbar = getSupportActionBar();//隐藏自带标题栏
        if (actionbar != null) {
            actionbar.hide();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //HTTPSTrustManager.allowAllSSL();
        requestQueue = HttpRequest.getRequestQueue(this);
        initRegister();
    }

    //注册界面初始化
    public void initRegister() {
        editUser = findViewById(R.id.input_registerName);
        editPasswd = findViewById(R.id.input_registerPassword);
        Button button_register = findViewById(R.id.btn_register);
        TextView login = findViewById(R.id.link_login);

        //注册按钮的点击事件
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取用户名密码、并去掉空格
                userName = editUser.getText().toString().trim();
                password = editPasswd.getText().toString().trim();

                if (TextUtils.isEmpty(userName)) {
                    Toast toast = Toast.makeText(RegisterActivity.this, null, Toast.LENGTH_SHORT);
                    toast.setText("用户名为空");
                    toast.show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast toast = Toast.makeText(RegisterActivity.this, null, Toast.LENGTH_SHORT);
                    toast.setText("密码为空");
                    toast.show();
                } else {
                    //String md5Psw = MD5Utils.md5(password);
                    tryRegister(userName, password);
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
            }
        });
    }

    private void tryRegister(final String userName, String password) {

        final Map<String, String> params = new HashMap<>();
        final Dialog dialog = WaitingInterface.createLoadingDialog(RegisterActivity.this, "");
        params.put("name", userName);
        params.put("password", password);

        HttpRequest.httpStringPost(Constant.prefix + "/user/", requestQueue, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                switch (response) {
                    case "创建成功": {
                        WaitingInterface.closeDialog(dialog);
                        Toast toast = Toast.makeText(RegisterActivity.this, "mi", Toast.LENGTH_SHORT);
                        toast.setText("注册成功");
                        toast.show();

                        RegisterActivity.this.finish();
                        //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        break;
                    }
                    case "创建失败": {
                        WaitingInterface.closeDialog(dialog);
                        //判断是否是因为用户名已存在而导致的创建失败
                        HttpRequest.httpStringGet(Constant.prefix + "/user/register/" + userName, requestQueue, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast toast = Toast.makeText(RegisterActivity.this, null, Toast.LENGTH_SHORT);
                                if ("用户名已存在".equals(response))
                                    toast.setText("用户名已存在");
                                else toast.setText("创建失败");
                                toast.show();
                            }
                        });
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
