package cn.lzumi.mfanime.init;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.lzumi.mfanime.adapter.AnimeAdapter;
import cn.lzumi.mfanime.bean.Anime;
import cn.lzumi.mfanime.tools.Constant;
import cn.lzumi.mfanime.tools.HttpRequest;

public class InitAnime {
    public static void initAnime(final List<Anime> animeList, final Context context, final RecyclerView recyclerView) {
        RequestQueue requestQueue = HttpRequest.getRequestQueue(context);
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        HttpRequest.httpJSONArrayGet(Constant.prefix + "/anime/", requestQueue,
                new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < 6; i++) {
                    Anime anime;
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        anime = new Anime(
                                jsonObject.getString("name"),
                                jsonObject.getString("comment"),
                                jsonObject.getString("pic"));
                        animeList.add(anime);
                        atomicInteger.addAndGet(1);
                        if (atomicInteger.get() == 6) {
                            new Handler(context.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    // 在这里执行你要想的操作 比如直接在这里更新ui或者调用回调在 在回调中更新ui
                                    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                                    recyclerView.setLayoutManager(layoutManager);
                                    AnimeAdapter adapter = new AnimeAdapter(animeList);
                                    recyclerView.setAdapter(adapter);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        System.out.println("首页更新发生异常" + e);
                    }
                }
            }
        });
    }
}
