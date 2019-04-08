package cn.lzumi.mfanime.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.service.carrier.CarrierMessagingService;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class HttpRequest {

    private static RequestQueue sRequestQueue;

    public static RequestQueue getRequestQueue(Context context) {
        if (sRequestQueue == null) {
            synchronized (RequestQueue.class) {
                if (sRequestQueue == null) {
                    sRequestQueue = Volley.newRequestQueue(context);
                }
            }
        }
        return sRequestQueue;
    }

    /**
     * GET方法
     *
     * @param url          GET方法地址
     * @param requestQueue
     * @param listener
     */
    public static void httpGet(String url, RequestQueue requestQueue, Response.Listener<JSONObject> listener) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("连接错误" + error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    //返回JSON数组的GET方法
    public static void httpJSONArrayGet(String url, RequestQueue requestQueue, Response.Listener<JSONArray> listener) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("连接错误" + error.getMessage());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    public static void httpStringGet(String url, RequestQueue requestQueue, Response.Listener<String> listener) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("连接错误" + error.getMessage());
            }
        });
        requestQueue.add(stringRequest);
    }

    public static void httpStringPost(String url, RequestQueue requestQueue, final Map<String, String> params, Response.Listener<String> listener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("连接错误" + error.getMessage());
                //CarrierMessagingService.SendMultipartSmsResult

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };


        requestQueue.add(stringRequest);
    }

//    public static void picStringPost(final Bitmap bitmap, String url, RequestQueue requestQueue, final Map<String, String> params, Response.Listener<String> listener){
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                listener, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                System.out.println("连接错误" + error.getMessage());
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                params.put("file",bitmapToBase64(bitmap));
//                return params;
//            }
//        };
//
//
//        requestQueue.add(stringRequest);
//    }

}
