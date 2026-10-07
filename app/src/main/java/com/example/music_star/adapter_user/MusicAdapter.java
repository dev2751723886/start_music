package com.example.music_star.adapter_user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_star.R;
import com.example.music_star.bean.MusicBean;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.viewHolder> {

    // 点击回调接口，方便在 Activity 里处理播放
    public interface OnItemClickListener {
        void onItemClick(MusicBean bean);
    }

    // 删除按钮回调
    public interface OnDeleteClickListener {
        void onDeleteClick(MusicBean bean);
    }

    // 收藏按钮回调
    public interface OnLoveClickListener {
        void onLoveClick(MusicBean bean);
    }

    private List<MusicBean> list;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;
    private OnLoveClickListener loveListener;
    private boolean showDelete = false; // 是否显示删除按钮
    private boolean showLove = false;   // 是否显示收藏按钮

    public MusicAdapter(List<MusicBean> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setShowDelete(boolean showDelete) {
        this.showDelete = showDelete;
    }

    public void setOnLoveClickListener(OnLoveClickListener loveListener) {
        this.loveListener = loveListener;
    }

    public void setShowLove(boolean showLove) {
        this.showLove = showLove;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_music, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        MusicBean bean = list.get(position);
        holder.name.setText(bean.getName());
        holder.singer.setText(bean.getSinger());

        // 封面存的是 drawable 资源名，转成资源 id 加载（改图片会立即生效，不用复制文件）
        int resId = holder.img.getContext().getResources()
                .getIdentifier(bean.getImg(), "drawable", holder.img.getContext().getPackageName());
        holder.img.setImageResource(resId != 0 ? resId : R.drawable.beizi);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(bean);
            }
        });

        // 收藏按钮：默认隐藏，歌曲列表里才显示
        if (showLove) {
            holder.love.setVisibility(View.VISIBLE);
            holder.love.setImageResource("1".equals(bean.getLove()) ? R.drawable.music_red : R.drawable.music_star);
            holder.love.setOnClickListener(v -> {
                if (loveListener != null) {
                    loveListener.onLoveClick(bean);
                }
            });
        } else {
            holder.love.setVisibility(View.GONE);
        }

        // 删除按钮：默认隐藏，歌单详情里才显示
        if (showDelete) {
            holder.del.setVisibility(View.VISIBLE);
            holder.del.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(bean);
                }
            });
        } else {
            holder.del.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name;
        TextView singer;
        ImageView love;
        ImageView del;

        viewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.music_img);
            name = itemView.findViewById(R.id.music_name);
            singer = itemView.findViewById(R.id.music_singer);
            love = itemView.findViewById(R.id.music_love);
            del = itemView.findViewById(R.id.music_del);
        }
    }
}
