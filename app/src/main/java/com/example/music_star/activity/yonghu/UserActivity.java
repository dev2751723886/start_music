package com.example.music_star.activity.yonghu;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.music_star.R;
import com.example.music_star.activity.yonghu.fragment.Homefragment;
import com.example.music_star.activity.yonghu.fragment.LoveFragment;
import com.example.music_star.activity.yonghu.fragment.MyFragment;
import com.example.music_star.bean.MusicBean;
import com.example.music_star.dao.MusicDao;
import com.example.music_star.until.PlayerBarController;
import com.example.music_star.until.PlayerManager;

import java.util.List;

public class UserActivity extends AppCompatActivity {

    private PlayerBarController playerBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);

        // 初始化播放类 + 播放菜单（播放条的所有按钮/进度条/收藏都由它管）
        PlayerManager pm = PlayerManager.getInstance();
        playerBarController = new PlayerBarController(findViewById(R.id.player_bar));
        pm.setListener(playerBarController);

        // 加载首页 Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.man_manage_frame, new Homefragment());
        transaction.commit();

        // 底部导航切换：首页 / 列表(收藏) / 我的
        BottomNavigationView bottomNav = findViewById(R.id.user_bottom_menu);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (item.getItemId() == R.id.user_bottom_list) {
                ft.replace(R.id.man_manage_frame, new LoveFragment());
                ft.commit();
                return true;
            } else if (item.getItemId() == R.id.user_bottom_menu) {
                ft.replace(R.id.man_manage_frame, new Homefragment());
                ft.commit();
                return true;
            } else if (item.getItemId() == R.id.user_bottom_my) {
                ft.replace(R.id.man_manage_frame, new MyFragment());
                ft.commit();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从歌单列表等其他页面播放后回到这里，重新绑定播放条并同步当前播放状态
        PlayerManager pm = PlayerManager.getInstance();
        pm.setListener(playerBarController);
        MusicBean music = pm.getCurrentMusic();
        if (music != null) {
            playerBarController.onMusicChanged(music);
            playerBarController.onPlayStateChanged(pm.isPlaying());
        }
    }

    // 点击歌单的播放按钮：播放该歌单的第一首歌
    public void playSheet(String sheetId) {
        List<MusicBean> songs = MusicDao.getMusicBySheet(sheetId);
        if (songs == null || songs.isEmpty()) {
            Toast.makeText(this, "该歌单暂无歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        PlayerManager.getInstance().play(this, songs, 0);
    }

    // 播放热榜/收藏/任意歌曲列表：从指定下标开始播
    public void playSongList(List<MusicBean> songs, int index) {
        if (songs == null || songs.isEmpty() || index < 0 || index >= songs.size()) {
            Toast.makeText(this, "没有可播放的歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        PlayerManager.getInstance().play(this, songs, index);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerBarController != null) {
            playerBarController.destroy();
        }
        // 注意：不 release PlayerManager，它是单例，跨界面继续播
    }
}
