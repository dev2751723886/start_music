package com.example.music_star.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_star.R;
import com.example.music_star.adapter_user.MusicAdapter;
import com.example.music_star.bean.MusicBean;
import com.example.music_star.dao.MusicDao;
import com.example.music_star.until.PlayerBarController;
import com.example.music_star.until.PlayerManager;
import com.example.music_star.until.tools;

import java.util.List;

public class MusicListActivity extends AppCompatActivity {

    private List<MusicBean> list; // 当前歌单的歌曲，播放用
    private MusicAdapter adapter;
    private PlayerBarController playerBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        // 初始化播放类 + 播放菜单（播放条复用）
        PlayerManager pm = PlayerManager.getInstance();
        playerBarController = new PlayerBarController(findViewById(R.id.player_bar));
        pm.setListener(playerBarController);

        // 首页传过来的歌单 id 和名字
        String sheetId = getIntent().getStringExtra("sheet_id");
        String sheetName = getIntent().getStringExtra("sheet_name");

        TextView title = findViewById(R.id.music_list_title);
        if (sheetName != null && !sheetName.isEmpty()) {
            title.setText(sheetName);
        }

        // 返回按钮：点击返回用户界面
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());

        list = MusicDao.getMusicBySheet(sheetId);

        RecyclerView recycler = findViewById(R.id.music_list_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MusicAdapter(list, this::playMusic);
        adapter.setShowLove(true);
        adapter.setOnLoveClickListener(this::toggleLove);
        recycler.setAdapter(adapter);
    }

    // 点击某首歌后播放（走统一的 PlayerManager，和用户界面共享播放状态）
    private void playMusic(MusicBean bean) {
        if (list == null || list.isEmpty()) {
            return;
        }
        int index = list.indexOf(bean);
        if (index < 0) {
            index = 0;
        }
        PlayerManager.getInstance().play(this, list, index);
    }

    // 收藏 / 取消收藏
    private void toggleLove(MusicBean bean) {
        String love = MusicDao.toggleLove(bean);
        tools.Toast(this, "1".equals(love) ? "已收藏" : "已取消收藏");
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从全屏播放页返回时，重新绑定播放条并同步状态
        PlayerManager pm = PlayerManager.getInstance();
        pm.setListener(playerBarController);
        MusicBean music = pm.getCurrentMusic();
        if (music != null) {
            playerBarController.onMusicChanged(music);
            playerBarController.onPlayStateChanged(pm.isPlaying());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerBarController != null) {
            playerBarController.destroy();
        }
    }
}
