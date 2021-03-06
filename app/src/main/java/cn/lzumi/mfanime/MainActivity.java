package cn.lzumi.mfanime;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lzumi.mfanime.bean.Anime;
import cn.lzumi.mfanime.tools.BaseActivity;
import cn.lzumi.mfanime.tools.Constant;
import cn.lzumi.mfanime.tools.HttpRequest;
import cn.lzumi.mfanime.tools.WaitingInterface;

import static cn.lzumi.mfanime.init.InitAnime.initAnime;
import static cn.lzumi.mfanime.tools.loginInfo.cleanLoginStatus;
import static cn.lzumi.mfanime.tools.loginInfo.readLoginUserName;
import static cn.lzumi.mfanime.tools.loginInfo.readToken;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private List<Anime> animeList = new ArrayList<>();
    RecyclerView recyclerView;
    RequestQueue requestQueue;
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestQueue = HttpRequest.getRequestQueue(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //下拉刷新
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFruits();
            }
        });

        //初始化首页
        recyclerView = findViewById(R.id.recycler_view);
        initAnime(animeList, this, recyclerView);
        onLogin();
    }


    //更新头像、用户名，点击头像事件
    public void onLogin() {
        //检查是否登录
        //是--> 个人界面
        //否--> 登陆界面
//        final AtomicBoolean b = new AtomicBoolean(false);
//              b.set(true); 可用于判定登陆状态
        String token = readToken(MainActivity.this);
        //未登录
        if (token.equals("")) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            ImageView imageView = navigationView.getHeaderView(0).findViewById(R.id.imageView);
            TextView textView = navigationView.getHeaderView(0).findViewById(R.id.nav_username);
            Glide.with(MainActivity.this).load(R.mipmap.head).into(imageView);
            textView.setText("未登录...");
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.finish();
                    //跳转登陆界面
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });

            //禁止进入添加喜爱动画界面
            FloatingActionButton floatingActionButton = findViewById(R.id.fab);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNormalDialog();
                }
            });

        } else {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constant.prefix + "/user/login/" + token,
                    new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    String name = null, avatar = null;
                    try {
                        name = response.getString("name");
                        avatar = response.getString("avatar");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println(response);
                    if (readLoginUserName(MainActivity.this).equals(name)) {
                        NavigationView navigationView = findViewById(R.id.nav_view);
                        TextView textView = navigationView.getHeaderView(0).findViewById(R.id.nav_username);
                        ImageView imageView = navigationView.getHeaderView(0).findViewById(R.id.imageView);
                        textView.setText(name);
                        if (!"null".equals(avatar))
                            Glide.with(MainActivity.this).load(avatar).into(imageView);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
                                normalDialog.setMessage("修改头像?");
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
                                                openAlbum(MainActivity.this,MainActivity.this);
                                            }
                                        });
                                normalDialog.show();
                                /*跳转个人资料界面
                                Toast toast = Toast.makeText(MainActivity.this, "已登录", Toast.LENGTH_SHORT);
                                toast.setText("已登录");
                                toast.show();*/
                            }
                        });

                        //进入添加喜爱动画界面
                        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(MainActivity.this,AddAnimeActivity.class));
                            }
                        });
                    } else {
                        NavigationView navigationView = findViewById(R.id.nav_view);
                        ImageView imageView = navigationView.getHeaderView(0).findViewById(R.id.imageView);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.this.finish();
                                //跳转登陆界面
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            }
                        });
                        //禁止进入添加喜爱动画界面
                        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showNormalDialog();
                            }
                        });
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("疑似未登录" + error);
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    ImageView imageView = navigationView.getHeaderView(0).findViewById(R.id.imageView);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity.this.finish();
                            //跳转登陆界面
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        }
                    });
                    //禁止进入添加喜爱动画界面
                    FloatingActionButton floatingActionButton = findViewById(R.id.fab);
                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showNormalDialog();
                        }
                    });

                }
            });
            requestQueue.add(jsonObjectRequest);
        }
    }

    //修改头像
    public void uploadImage(String imagePath) {
        final Dialog dialog = WaitingInterface.createLoadingDialog(MainActivity.this, "上传中");
        if (imagePath != null) {
            final Map<String, String> params = new HashMap<>();
            final Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            params.put("pic", bitmapToBase64(bitmap));

            NavigationView navigationView = findViewById(R.id.nav_view);
            //TextView textView = navigationView.getHeaderView(0).findViewById(R.id.nav_username);
            final ImageView imageView = navigationView.getHeaderView(0).findViewById(R.id.imageView);

            //上传头像获得图像URL
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.prefix_pic + "/wbp4j/", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    final Map<String, String> param = new HashMap<>();
                    param.put("name",readLoginUserName(MainActivity.this));
                    param.put("cookie",readToken(MainActivity.this));
                    param.put("avatar",response);
                    //保存头像URL到数据库
                    StringRequest updateHead = new StringRequest(Request.Method.PUT, Constant.prefix + "/user/", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if("修改成功".equals(response)){
                                Toast.makeText(MainActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                            }
                            else Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error);
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() {
                            return param;
                        }
                    };
                    requestQueue.add(updateHead);
                    Glide.with(MainActivity.this).load(response).into(imageView);
                    WaitingInterface.closeDialog(dialog);
                    System.out.println(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                    WaitingInterface.closeDialog(dialog);
                    System.out.println("上传图片连接错误" + error);
                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            //因为图片过大上传时间不定，等待时间设置长一些
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } else {
            WaitingInterface.closeDialog(dialog);
            Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_exit) {
            //退出登录
            cleanLoginStatus(MainActivity.this);
            onLogin();
            Toast toast = Toast.makeText(MainActivity.this, "mi", Toast.LENGTH_SHORT);
            toast.setText("退出成功");
            toast.show();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //确认对话框
    public void showNormalDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("尚未登陆，不能添加动画");
        normalDialog.setMessage("是否登录?");
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
                        MainActivity.this.finish();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                });
        normalDialog.show();
    }

    //下拉刷新
    private void refreshFruits() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animeList.clear();
                initAnime(animeList, MainActivity.this, recyclerView);
                swipeRefresh.setRefreshing(false);
            }
        },1000);
    }

}
