package com.example.music_star.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_star.R;
import com.example.music_star.adapter_user.MusicAdapter;
import com.example.music_star.bean.MusicBean;
import com.example.music_star.dao.MusicDao;
import com.example.music_star.dao.PalymusicDao;
import com.example.music_star.until.PlayerBarController;
import com.example.music_star.until.PlayerManager;
import com.example.music_star.until.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 自己创建的歌单详情页：查看歌单里的歌 + 添加歌曲
public class SheetDetailActivity extends AppCompatActivity {

    private String sheetId;
    private List<MusicBean> list;
    private PlayerBarController playerBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_detail);

        PlayerManager pm = PlayerManager.getInstance();
        playerBarController = new PlayerBarController(findViewById(R.id.player_bar));
        pm.setListener(playerBarController);

        sheetId = getIntent().getStringExtra("sheet_id");
        String sheetName = getIntent().getStringExtra("sheet_name");
        TextView title = findViewById(R.id.sheet_detail_title);
        if (sheetName != null && !sheetName.isEmpty()) {
            title.setText(sheetName);
        }

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_music).setOnClickListener(v -> showAddMusicDialog());
        findViewById(R.id.btn_delete_sheet).setOnClickListener(v -> confirmDeleteSheet());

        loadSongs();
    }

    private void loadSongs() {
        list = PalymusicDao.getMusicBySheetJunction(sheetId);
        RecyclerView recycler = findViewById(R.id.sheet_detail_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        if (list == null || list.isEmpty()) {
            recycler.setAdapter(null);
            return;
        }
        MusicAdapter adapter = new MusicAdapter(list, this::playMusic);
        adapter.setShowDelete(true);
        adapter.setOnDeleteClickListener(this::confirmDeleteMusic);
        adapter.setShowLove(true);
        adapter.setOnLoveClickListener(this::toggleLove);
        recycler.setAdapter(adapter);
    }

    // 点击移除歌曲：只从当前歌单移除（不影响推荐/热榜），刷新列表
    private void confirmDeleteMusic(MusicBean bean) {
        new AlertDialog.Builder(this)
                .setTitle("移除歌曲")
                .setMessage("确定把「" + bean.getName() + "」从这个歌单移除吗？（不会影响推荐和热榜）")
                .setPositiveButton("移除", (d, w) -> {
                    PalymusicDao.removeMusicFromSheet(sheetId, bean.getId());
                    tools.Toast(this, "已移除");
                    loadSongs();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 收藏 / 取消收藏
    private void toggleLove(MusicBean bean) {
        String love = MusicDao.toggleLove(bean);
        tools.Toast(this, "1".equals(love) ? "已收藏" : "已取消收藏");
        loadSongs();
    }

    // 点击删除歌单：确认后删掉整个歌单，返回上一页
    private void confirmDeleteSheet() {
        new AlertDialog.Builder(this)
                .setTitle("删除歌单")
                .setMessage("确定删除这个歌单吗？")
                .setPositiveButton("删除", (d, w) -> {
                    PalymusicDao.deleteSheet(sheetId);
                    tools.Toast(this, "歌单已删除");
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }

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

    // 弹出歌曲选择框：显示所有歌曲（去掉已加入的），点一下加入歌单
    private void showAddMusicDialog() {
        List<MusicBean> all = MusicDao.getAllMusic();
        Set<String> inSheet = new HashSet<>();
        if (list != null) {
            for (MusicBean b : list) {
                inSheet.add(b.getId());
            }
        }
        List<MusicBean> available = new ArrayList<>();
        if (all != null) {
            for (MusicBean b : all) {
                if (!inSheet.contains(b.getId())) {
                    available.add(b);
                }
            }
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_song_picker, null);
        RecyclerView recycler = dialogView.findViewById(R.id.dialog_song_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new MusicAdapter(available, bean -> {
            PalymusicDao.addMusicToSheet(sheetId, bean.getId());
            tools.Toast(this, "已添加：" + bean.getName());
            loadSongs();
        }));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择要添加的歌曲");
        builder.setView(dialogView);
        builder.setNegativeButton("关闭", null);
        builder.show();
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
