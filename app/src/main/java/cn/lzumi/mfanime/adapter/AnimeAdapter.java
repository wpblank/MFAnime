package cn.lzumi.mfanime.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.lzumi.mfanime.R;
import cn.lzumi.mfanime.bean.Anime;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.ViewHolder> {
    private List<Anime> mAnimeList;
    private Context mContext;

    //内部类
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView animeImage;
        TextView animeName;
        TextView animeComment;


        ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            animeImage = view.findViewById(R.id.anime_image);
            animeName = view.findViewById(R.id.anime_name);
            animeComment = view.findViewById(R.id.anime_comment);
        }
    }

    public AnimeAdapter(List<Anime> animeList) {
        mAnimeList = animeList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null)
            mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.anime_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Anime anime = mAnimeList.get(position);
        holder.animeName.setText(anime.getName());
        holder.animeComment.setText(anime.getComment());
        //holder.animeImage.setImageResource(anime.getPicId());
        //使用Glide加载图片：https://github.com/bumptech/glide
        Glide.with(mContext).load(anime.getPic()).into(holder.animeImage);
    }

    @Override
    public int getItemCount() {
        return mAnimeList.size();
    }

}
