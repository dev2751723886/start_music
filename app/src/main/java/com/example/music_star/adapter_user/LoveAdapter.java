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

// 收藏（我喜欢）列表的适配器
public class LoveAdapter extends RecyclerView.Adapter<LoveAdapter.viewHolder> {

    // 点击回调接口
    public interface OnItemClickListener {
        void onItemClick(MusicBean bean, int position);
    }

    private List<MusicBean> list;
    private OnItemClickListener listener;

    public LoveAdapter(List<MusicBean> list) {
        this.list = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.love_list, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        MusicBean bean = list.get(position);
        // 排名：第一首显示 1，第二首显示 2，以此类推
        holder.rank.setText(String.valueOf(position + 1));
        holder.name.setText(bean.getName());
        holder.singer.setText(bean.getSinger());

        // 封面存的是 drawable 资源名，转成资源 id 加载
        int resId = holder.img.getContext().getResources()
                .getIdentifier(bean.getImg(), "drawable", holder.img.getContext().getPackageName());
        holder.img.setImageResource(resId != 0 ? resId : R.drawable.beizi);

        // 点击整行：播放这首歌
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(bean, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {
        TextView rank;    // 排名
        ImageView img;    // 封面
        TextView name;    // 歌名
        TextView singer;  // 歌手

        viewHolder(View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.music_much_red);
            img = itemView.findViewById(R.id.music_img_love);
            name = itemView.findViewById(R.id.music_love_name);
            singer = itemView.findViewById(R.id.music_love_singer);
        }
    }
}
