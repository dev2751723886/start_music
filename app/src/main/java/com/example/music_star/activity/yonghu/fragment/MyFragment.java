package com.example.music_star.activity.yonghu.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_star.R;
import com.example.music_star.activity.EditProfileActivity;
import com.example.music_star.activity.SheetDetailActivity;
import com.example.music_star.activity.SongListActivity;
import com.example.music_star.adapter_user.MenuListAdapter;
import com.example.music_star.adapter_user.MySheetAdapter;
import com.example.music_star.bean.MusicBean;
import com.example.music_star.bean.PlayMusicBean;
import com.example.music_star.bean.UserBean;
import com.example.music_star.dao.PalymusicDao;
import com.example.music_star.dao.UserDao;
import com.example.music_star.until.PlayerManager;
import com.example.music_star.until.tools;

import java.util.ArrayList;
import java.util.List;

// 「我的」页面：头像、昵称、我创建的歌单、我喜欢的歌曲 / 音乐热榜
public class MyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.my_home, container, false);

        String account = tools.getOnAccount(getContext());

        // 1. 头像 + 昵称
        ImageView toux = root.findViewById(R.id.toux);
        TextView nic = root.findViewById(R.id.nic);
        UserBean user = UserDao.getUserByAccount(account);
        if (user != null) {
            if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                nic.setText(user.getNickname());
            }
            String img = user.getImg();
            if (img == null || img.isEmpty()) {
                toux.setImageResource(R.drawable.photo);
            } else {
                Glide.with(getContext())
                        .load(img)   // 本地文件路径（content:// 也能加载）
                        .placeholder(R.drawable.photo)
                        .error(R.drawable.photo)
                        .into(toux);
            }
        }

        // 点击头像进入编辑资料
        toux.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));

        // 播放时长：显示累计听歌总时间（登录进入时读一次）
        TextView timeText = root.findViewById(R.id.music_time_text);
        timeText.setText(tools.formatTime(UserDao.getListeningTime(account)));

        // 最近播放：打开最近播放列表
        root.findViewById(R.id.music_kaujie).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SongListActivity.class);
            intent.putExtra("type", SongListActivity.TYPE_RECENT);
            startActivity(intent);
        });

        // 2. 我创建的歌单
        RecyclerView ged = root.findViewById(R.id.music_ged);
        ged.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        loadSheets(ged, account);

        root.findViewById(R.id.btn_create_sheet).setOnClickListener(v -> showCreateSheetDialog(account, ged));

        // 3. 我喜欢的歌曲 + 音乐热榜
        RecyclerView mune = root.findViewById(R.id.music_mune);
        mune.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        List<MenuListAdapter.Entry> entries = new ArrayList<>();
        entries.add(new MenuListAdapter.Entry(MenuListAdapter.TYPE_LOVE, "我喜欢的歌曲", R.drawable.f));
        entries.add(new MenuListAdapter.Entry(MenuListAdapter.TYPE_HOT, "音乐热榜", R.drawable.music_red));
        MenuListAdapter menuAdapter = new MenuListAdapter(entries);
        menuAdapter.setOnMenuClickListener(type -> {
            Intent intent = new Intent(getContext(), SongListActivity.class);
            intent.putExtra("type", type);
            startActivity(intent);
        });
        mune.setAdapter(menuAdapter);

        return root;
    }

    private void loadSheets(RecyclerView ged, String account) {
        List<PlayMusicBean> list = PalymusicDao.getSheetsByUser(account);
        if (list == null || list.isEmpty()) {
            ged.setAdapter(null);
            return;
        }
        MySheetAdapter adapter = new MySheetAdapter(list);
        adapter.setOnSheetClickListener(bean -> {
            Intent intent = new Intent(getContext(), SheetDetailActivity.class);
            intent.putExtra("sheet_id", bean.getId());
            intent.putExtra("sheet_name", bean.getName());
            startActivity(intent);
        });
        adapter.setOnPlayClickListener(sheetId -> {
            List<MusicBean> songs = PalymusicDao.getMusicBySheetJunction(sheetId);
            if (songs == null || songs.isEmpty()) {
                tools.Toast(getContext(), "该歌单暂无歌曲");
            } else {
                PlayerManager.getInstance().play(getContext(), songs, 0);
            }
        });
        ged.setAdapter(adapter);
    }

    private void showCreateSheetDialog(String account, RecyclerView ged) {
        EditText input = new EditText(getContext());
        input.setHint("请输入歌单名字");
        new AlertDialog.Builder(getContext())
                .setTitle("创建歌单")
                .setView(input)
                .setPositiveButton("创建", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        tools.Toast(getContext(), "歌单名不能为空");
                        return;
                    }
                    PalymusicDao.createSheet("s" + System.currentTimeMillis(), name, account);
                    tools.Toast(getContext(), "创建成功");
                    loadSheets(ged, account);
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
