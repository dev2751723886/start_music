package com.example.music_star.activity.yonghu.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.music_star.R;
import com.example.music_star.activity.SongListActivity;
import com.example.music_star.activity.yonghu.UserActivity;
import com.example.music_star.adapter_user.PlayMusicAdapter;
import com.example.music_star.adapter_user.RedMusicAdapter;
import com.example.music_star.bean.MusicBean;
import com.example.music_star.bean.PlayMusicBean;
import com.example.music_star.dao.MusicDao;
import com.example.music_star.dao.PalymusicDao;

import java.util.List;

//这个类用于操作首页
public class Homefragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.frame_user_home, container, false);

        // 推荐歌单
        List<PlayMusicBean> list = PalymusicDao.getPlayMusic();
        PlayMusicAdapter de = new PlayMusicAdapter(list);
        de.setOnPlayClickListener(sheetId -> {
            UserActivity activity = (UserActivity) getActivity();
            if (activity != null) {
                activity.playSheet(sheetId);
            }
        });

        RecyclerView listDe = rootview.findViewById(R.id.music_mune);
        listDe.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        if (list == null || list.size() == 0) {
            listDe.setAdapter(null);
        } else {
            listDe.setAdapter(de);
            de.notifyDataSetChanged();
        }

        // 音乐热榜：垂直列表，显示所有歌曲，排名 1、2、3...
        List<MusicBean> redList = MusicDao.getAllMusic();
        RecyclerView redDe = rootview.findViewById(R.id.music_music_chat);
        redDe.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        if (redList == null || redList.size() == 0) {
            redDe.setAdapter(null);
        } else {
            RedMusicAdapter redAdapter = new RedMusicAdapter(redList);
            redAdapter.setOnItemClickListener((bean, position) -> {
                UserActivity activity = (UserActivity) getActivity();
                if (activity != null) {
                    activity.playSongList(redList, position);
                }
            });
            redDe.setAdapter(redAdapter);
        }

        // 搜索：点击搜索 → 另开一个搜索结果列表（不改动热榜）
        SearchView searchView = rootview.findViewById(R.id.man_home_music_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    Intent intent = new Intent(getContext(), SongListActivity.class);
                    intent.putExtra("type", SongListActivity.TYPE_SEARCH);
                    intent.putExtra("query", query.trim());
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false; // 不实时过滤热榜，保持热榜不变
            }
        });

        return rootview;
    }
}
