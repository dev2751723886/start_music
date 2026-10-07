package com.example.music_star.activity.yonghu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_star.R;
import com.example.music_star.activity.yonghu.UserActivity;
import com.example.music_star.adapter_user.LoveAdapter;
import com.example.music_star.bean.MusicBean;
import com.example.music_star.dao.MusicDao;

import java.util.List;

// 收藏（我喜欢）列表页面
public class LoveFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.love_page, container, false);

        // 查询所有"喜欢"的歌曲
        List<MusicBean> loveList = MusicDao.getLoveMusic();
        RecyclerView recycler = root.findViewById(R.id.love_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        if (loveList == null || loveList.isEmpty()) {
            recycler.setAdapter(null);
        } else {
            LoveAdapter adapter = new LoveAdapter(loveList);
            // 点击收藏列表里的歌：跟歌单一样用播放条播放
            adapter.setOnItemClickListener((bean, position) -> {
                UserActivity activity = (UserActivity) getActivity();
                if (activity != null) {
                    activity.playSongList(loveList, position);
                }
            });
            recycler.setAdapter(adapter);
        }

        return root;
    }
}
