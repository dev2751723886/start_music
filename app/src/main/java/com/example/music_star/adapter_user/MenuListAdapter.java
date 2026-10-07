package com.example.music_star.adapter_user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_star.R;

import java.util.List;

// 「我的」页 music_mune 的固定两项菜单（我喜欢的歌曲 / 音乐热榜）
public class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.viewHolder> {

    public static final int TYPE_LOVE = 0; // 我喜欢的歌曲
    public static final int TYPE_HOT = 1;  // 音乐热榜

    public interface OnMenuClickListener {
        void onMenuClick(int type);
    }

    // 菜单项
    public static class Entry {
        public final int type;
        public final String name;
        public final int imgRes;

        public Entry(int type, String name, int imgRes) {
            this.type = type;
            this.name = name;
            this.imgRes = imgRes;
        }
    }

    private final List<Entry> entries;
    private OnMenuClickListener listener;

    public MenuListAdapter(List<Entry> entries) {
        this.entries = entries;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_sheet_item, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Entry e = entries.get(position);
        holder.name.setText(e.name);
        holder.img.setImageResource(e.imgRes);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClick(e.type);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name;

        viewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.sheet_img);
            name = itemView.findViewById(R.id.sheet_name);
        }
    }
}
