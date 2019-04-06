package cn.lzumi.mfanime;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;

import java.util.HashMap;
import java.util.Map;

import cn.lzumi.mfanime.tools.BaseActivity;
import cn.lzumi.mfanime.tools.Constant;
import cn.lzumi.mfanime.tools.HttpRequest;
import cn.lzumi.mfanime.tools.WaitingInterface;

import static cn.lzumi.mfanime.tools.loginInfo.readLoginUserName;

public class AddAnimeActivity extends BaseActivity {

    RequestQueue requestQueue;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_anime);
        //设置此界面为竖屏
        ActionBar actionbar = getSupportActionBar();//隐藏自带标题栏
        if (actionbar != null) {
            actionbar.hide();
        }
        Toolbar toolbar = findViewById(R.id.toolbar_addanime);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //HTTPSTrustManager.allowAllSSL();
        requestQueue = HttpRequest.getRequestQueue(AddAnimeActivity.this);
        AddAnime();
    }

    private void AddAnime() {
        final Map<String, String> params = new HashMap<>();
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = WaitingInterface.createLoadingDialog(AddAnimeActivity.this, "");
                CardView cv_add_anime_name = findViewById(R.id.cv_add_anime_name);
                EditText add_anime_name = cv_add_anime_name.findViewById(R.id.add_anime_name);
                params.put("name", add_anime_name.getText().toString().trim());

                params.put("userName",readLoginUserName(AddAnimeActivity.this));

                CardView cv_add_anime_comment = findViewById(R.id.cv_add_anime_comment);
                EditText add_anime_comment = cv_add_anime_comment.findViewById(R.id.add_anime_comment);
                params.put("comment", add_anime_comment.getText().toString().trim());

                CardView cv_add_anime_pic = findViewById(R.id.cv_add_anime_pic);
                EditText add_anime_pic = cv_add_anime_pic.findViewById(R.id.add_anime_pic);
                params.put("pic", add_anime_pic.getText().toString().trim());

                CardView cv_add_anime_bilibili = findViewById(R.id.cv_add_anime_bilibili);
                EditText add_anime_bilibili = cv_add_anime_bilibili.findViewById(R.id.add_anime_bilibili);
                params.put("bilibili", add_anime_bilibili.getText().toString().trim());

                HttpRequest.httpStringPost(Constant.prefix + "/anime/", requestQueue, params, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response) {
                            case "添加成功": {
                                WaitingInterface.closeDialog(dialog);
                                Toast toast = Toast.makeText(AddAnimeActivity.this, "mi", Toast.LENGTH_SHORT);
                                toast.setText("添加成功");
                                toast.show();
                                AddAnimeActivity.this.finish();
                                break;
                            }
                            case "添加失败": {
                                WaitingInterface.closeDialog(dialog);
                                Toast toast = Toast.makeText(AddAnimeActivity.this, "mi", Toast.LENGTH_SHORT);
                                toast.setText("添加失败");
                                toast.show();
                                break;
                            }
                            default: {
                                WaitingInterface.closeDialog(dialog);
                                Toast toast = Toast.makeText(AddAnimeActivity.this, "mi", Toast.LENGTH_SHORT);
                                toast.setText("未知错误");
                                toast.show();
                                System.out.println(response);
                                break;
                            }
                        }
                    }
                });
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                showNormalDialog();
                break;
            default:
        }
        return true;
    }

    //确认对话框
    private void showNormalDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(AddAnimeActivity.this);
        normalDialog.setMessage("清空所有?");
        normalDialog.setPositiveButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Nothing-To-do
                    }
                });
        normalDialog.setNegativeButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddAnimeActivity.this.finish();
                    }
                });
        normalDialog.show();
    }
}
