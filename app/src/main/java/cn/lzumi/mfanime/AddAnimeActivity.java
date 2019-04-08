package cn.lzumi.mfanime;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.lzumi.mfanime.tools.BaseActivity;
import cn.lzumi.mfanime.tools.Constant;
import cn.lzumi.mfanime.tools.HttpRequest;
import cn.lzumi.mfanime.tools.WaitingInterface;

import static cn.lzumi.mfanime.tools.loginInfo.readLoginUserName;

public class AddAnimeActivity extends BaseActivity {

    RequestQueue requestQueue;
    ImageView imageView;

    @Override
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

        imageView = findViewById(R.id.test_img);
        Button button = findViewById(R.id.btn_add_anime_pic);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**FileInputStream fis = null;
                 try {
                 fis = new FileInputStream("图片的路径");
                 Bitmap bitmap = BitmapFactory.decodeStream(fis);
                 } catch (FileNotFoundException e) {
                 e.printStackTrace();
                 }*/
                //尝试获取权限
                if (ContextCompat.checkSelfPermission(AddAnimeActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddAnimeActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
            }
        });
        AddAnime();
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 2); // 打开相册
    }


    //ActivityCompat.requestPermissions() 执行后执行
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast toast = Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT);
                    toast.setText("打开失败");
                    toast.show();
                }
                break;
            default:
        }
    }


    //从相册选择完图片回到 onActivityResult()方法 ，进行图片处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK)
                    handleImageOnKitKat(data);
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath); // 根据图片路径显示图片
        uploadImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(String imagePath) {
        if (imagePath != null) {
            final Map<String, String> params = new HashMap<>();
            final Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            params.put("pic", bitmapToBase64(bitmap));

            CardView cv_add_anime_pic = findViewById(R.id.cv_add_anime_pic);
            final EditText add_anime_pic = cv_add_anime_pic.findViewById(R.id.add_anime_pic);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.prefix_pic + "/wbp4j/", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    add_anime_pic.setText(response);
                    System.out.println(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(AddAnimeActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                    add_anime_pic.setText("图片过大&网络不好!!");
                    System.out.println("上传图片连接错误" + error);
                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            /**
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constant.prefix + "/wbp4j/", new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println(response);
                    try {
                        if (response.getString("result").equals("SUCCESS")) {
                            add_anime_pic.setText(response.getJSONObject("imageInfo").getString("large"));
                        } else add_anime_pic.setText("上传失败，请重试");
                    } catch (JSONException e) {
                        add_anime_pic.setText("上传失败，请重试");
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    add_anime_pic.setText("上传失败，请重试");
                    System.out.println("图片上传失败" + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };*/
            requestQueue.add(stringRequest);
        } else {
            Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
        }
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
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
                String name = add_anime_name.getText().toString().trim();
                params.put("name", name);

                params.put("userName", readLoginUserName(AddAnimeActivity.this));

                CardView cv_add_anime_comment = findViewById(R.id.cv_add_anime_comment);
                EditText add_anime_comment = cv_add_anime_comment.findViewById(R.id.add_anime_comment);
                String comment = add_anime_comment.getText().toString().trim();
                params.put("comment", comment);

                CardView cv_add_anime_pic = findViewById(R.id.cv_add_anime_pic);
                EditText add_anime_pic = cv_add_anime_pic.findViewById(R.id.add_anime_pic);
                params.put("pic", add_anime_pic.getText().toString().trim());

                CardView cv_add_anime_bilibili = findViewById(R.id.cv_add_anime_bilibili);
                EditText add_anime_bilibili = cv_add_anime_bilibili.findViewById(R.id.add_anime_bilibili);
                params.put("bilibili", add_anime_bilibili.getText().toString().trim());

                if (!name.equals("") && !comment.equals(""))
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
                else {
                    WaitingInterface.closeDialog(dialog);
                    AlertDialog.Builder normalDialog = new AlertDialog.Builder(AddAnimeActivity.this);
                    normalDialog.setMessage("名称和简介不能为空");
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
                                    //Nothing-To-do
                                }
                            });
                    normalDialog.show();
                }

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
