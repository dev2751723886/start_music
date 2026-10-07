package com.example.music_star.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_star.R;
import com.example.music_star.adapter_user.LoveAdapter;
import com.example.music_star.adapter_user.MusicAdapter;
import com.example.music_star.adapter_user.RedMusicAdapter;
import com.example.music_star.bean.MusicBean;
import com.example.music_star.dao.MusicDao;
import com.example.music_star.until.PlayerBarController;
import com.example.music_star.until.PlayerManager;
import com.example.music_star.until.tools;

import java.util.List;

// 通用歌曲列表：type=love 我喜欢的歌曲 / hot 音乐热榜 / recent 最近播放
public class SongListActivity extends AppCompatActivity {

    public static final int TYPE_LOVE = 0;
    public static final int TYPE_HOT = 1;
    public static final int TYPE_RECENT = 2;
    public static final int TYPE_SEARCH = 3;

    private List<MusicBean> list;
    private PlayerBarController playerBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        PlayerManager pm = PlayerManager.getInstance();
        playerBarController = new PlayerBarController(findViewById(R.id.player_bar));
        pm.setListener(playerBarController);

        int type = getIntent().getIntExtra("type", TYPE_HOT);
        TextView title = findViewById(R.id.song_list_title);
        RecyclerView recycler = findViewById(R.id.song_list_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());

        if (type == TYPE_LOVE) {
            title.setText("我喜欢的歌曲");
            list = MusicDao.getLoveMusic();
            LoveAdapter adapter = new LoveAdapter(list);
            adapter.setOnItemClickListener((bean, position) -> playAt(position));
            recycler.setAdapter(adapter);
        } else if (type == TYPE_RECENT) {
            title.setText("最近播放");
            list = MusicDao.getRecentMusic(tools.getOnAccount(this));
            MusicAdapter adapter = new MusicAdapter(list, this::playBean);
            recycler.setAdapter(adapter);
        } else if (type == TYPE_SEARCH) {
            String query = getIntent().getStringExtra("query");
            title.setText("搜索：" + (query == null ? "" : query));
            list = MusicDao.searchMusic(query == null ? "" : query);
            MusicAdapter adapter = new MusicAdapter(list, this::playBean);
            recycler.setAdapter(adapter);
        } else {
            title.setText("音乐热榜");
            list = MusicDao.getAllMusic();
            RedMusicAdapter adapter = new RedMusicAdapter(list);
            adapter.setOnItemClickListener((bean, position) -> playAt(position));
            recycler.setAdapter(adapter);
        }
    }

    private void playAt(int position) {
        if (list == null || list.isEmpty() || position < 0 || position >= list.size()) {
            return;
        }
        PlayerManager.getInstance().play(this, list, position);
    }

    private void playBean(MusicBean bean) {
        if (list == null || bean == null) {
            return;
        }
        int i = list.indexOf(bean);
        if (i >= 0) {
            playAt(i);
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
