package com.example.music_star.adapter_user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_star.R;
import com.example.music_star.bean.PlayMusicBean;
import com.example.music_star.dao.PalymusicDao;

import java.util.List;

// 「我的」页里我创建的歌单适配器（推荐歌单那种竖版卡片）
public class MySheetAdapter extends RecyclerView.Adapter<MySheetAdapter.viewHolder> {

    // 点击卡片回调（进入歌单详情）
    public interface OnSheetClickListener {
        void onSheetClick(PlayMusicBean bean);
    }

    // 点击播放按钮回调（播放该歌单）
    public interface OnPlayClickListener {
        void onPlayClick(String sheetId);
    }

    private List<PlayMusicBean> list;
    private OnSheetClickListener listener;
    private OnPlayClickListener playListener;

    public MySheetAdapter(List<PlayMusicBean> list) {
        this.list = list;
    }

    public void setOnSheetClickListener(OnSheetClickListener listener) {
        this.listener = listener;
    }

    public void setOnPlayClickListener(OnPlayClickListener listener) {
        this.playListener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_sheet_card, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        PlayMusicBean bean = list.get(position);
        holder.name.setText(bean.getName());
        holder.count.setText(PalymusicDao.getSheetMusicCount(bean.getId()) + "首");
        Glide.with(holder.img.getContext())
                .load(bean.getImg())
                .placeholder(R.drawable.beizi)
                .error(R.drawable.beizi)
                .into(holder.img);

        // 播放按钮：播放该歌单第一首歌
        holder.run.setOnClickListener(v -> {
            if (playListener != null) {
                playListener.onPlayClick(bean.getId());
            }
        });

        // 点击卡片：进入歌单详情
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSheetClick(bean);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView count;
        ImageView run;
        ImageView img;

        viewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            count = itemView.findViewById(R.id.much);
            run = itemView.findViewById(R.id.user_home_play_music_run);
            img = itemView.findViewById(R.id.img);
        }
    }
}
