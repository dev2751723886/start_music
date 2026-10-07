package com.example.music_star.adapter_user;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_star.R;
import com.example.music_star.activity.MusicListActivity;
import com.example.music_star.bean.PlayMusicBean;

import java.util.List;

//音乐的适配器
public class PlayMusicAdapter extends RecyclerView.Adapter<PlayMusicAdapter.viewHolder> {

    // 点击播放按钮的回调
    public interface OnPlayClickListener {
        void onPlayClick(String sheetId);
    }

    private List<PlayMusicBean> list;
    private OnPlayClickListener playListener;

    public PlayMusicAdapter(List<PlayMusicBean> list) {
        this.list = list;
    }

    public void setOnPlayClickListener(OnPlayClickListener listener) {
        this.playListener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View convertView = inflater.inflate(R.layout.list_user, parent, false);
        return new viewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        PlayMusicBean bean = list.get(position);
        holder.name.setText(bean.getName());
        holder.count.setText("36首");
        Glide.with(holder.img.getContext())
                .load(bean.getImg())
                .placeholder(R.drawable.beizi)
                .error(R.drawable.beizi)
                .into(holder.img);

        // 点击播放按钮：直接播放该歌单的第一首歌
        holder.run.setOnClickListener(v -> {
            if (playListener != null) {
                playListener.onPlayClick(bean.getId());
            }
        });

        // 点击歌单进入音乐列表
        holder.itemView.setOnClickListener(v -> {
            //打包（把参数放进 Intent）
            Intent intent = new Intent(holder.itemView.getContext(), MusicListActivity.class);
            intent.putExtra("sheet_id", bean.getId());
            intent.putExtra("sheet_name", bean.getName());
            //寄出（把 Intent 发送给系统MusicListActivity）
            holder.itemView.getContext().startActivity(intent);
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
        View view;
        viewHolder(View itemView) {
            super(itemView);
            view=itemView;
            name = itemView.findViewById(R.id.name);
            count=itemView.findViewById(R.id.much);
            run=itemView.findViewById(R.id.user_home_play_music_run);
            img=itemView.findViewById(R.id.img);
        }
    }
}
