package com.example.music_star.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.music_star.R;
import com.example.music_star.bean.MusicBean;
import com.example.music_star.dao.MusicDao;
import com.example.music_star.until.LrcParser;
import com.example.music_star.until.LyricProvider;
import com.example.music_star.until.PlayerManager;

// 全屏播放页：大封面 + 进度条 + 控制按钮
public class PlayerActivity extends AppCompatActivity implements PlayerManager.OnPlayerListener {

    private ImageView coverIv, runBtn, modeBtn, prevBtn, nextBtn, loveBtn;
    private TextView nameTv, singerTv, curTimeTv, totalTimeTv;
    private TextView lyricTv;      // 歌词框（唱一句显示一句）
    private String[][] lyrics;     // 解析后的歌词 {毫秒, 文本}
    private SeekBar seekBar;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isUserSeeking = false;

    // 每 0.5 秒刷新进度
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            PlayerManager pm = PlayerManager.getInstance();
            if (pm.isPlaying() && !isUserSeeking) {
                int duration = pm.getDuration();
                if (duration > 0) {
                    seekBar.setMax(duration);
                    seekBar.setProgress(pm.getCurrentPosition());
                    curTimeTv.setText(formatMs(pm.getCurrentPosition()));
                }
                updateLyric();
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        coverIv = findViewById(R.id.player_cover);
        nameTv = findViewById(R.id.player_name);
        singerTv = findViewById(R.id.player_singer);
        curTimeTv = findViewById(R.id.player_cur_time);
        totalTimeTv = findViewById(R.id.player_total_time);
        seekBar = findViewById(R.id.player_seekbar);
        runBtn = findViewById(R.id.player_play);
        modeBtn = findViewById(R.id.player_mode);
        prevBtn = findViewById(R.id.player_prev);
        nextBtn = findViewById(R.id.player_next);
        loveBtn = findViewById(R.id.player_love);
        lyricTv = findViewById(R.id.player_singersi);

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());

        PlayerManager pm = PlayerManager.getInstance();
        pm.setListener(this);

        runBtn.setOnClickListener(v -> pm.togglePlayPause());
        prevBtn.setOnClickListener(v -> pm.playPrev());
        nextBtn.setOnClickListener(v -> pm.playNext());
        modeBtn.setOnClickListener(v -> toggleMode());
        loveBtn.setOnClickListener(v -> toggleLove());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                isUserSeeking = false;
                PlayerManager.getInstance().seekTo(sb.getProgress());
                updateLyric(); // 拖动进度条，歌词也跟着变
            }
        });

        // 进入时同步当前播放状态
        MusicBean music = pm.getCurrentMusic();
        if (music != null) {
            onMusicChanged(music);
            onPlayStateChanged(pm.isPlaying());
        }
    }

    @Override
    public void onMusicChanged(MusicBean music) {
        int resId = getResources().getIdentifier(music.getImg(), "drawable", getPackageName());
        Glide.with(this)
                .load(resId != 0 ? resId : R.drawable.beizi)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(60)))
                .into(coverIv);
        nameTv.setText(music.getName());
        singerTv.setText(music.getSinger());
        updateLoveIcon();

        // 加载并解析歌词（C++ 解析 LRC）
        String lrc = LyricProvider.getLrc(music.getId());
        lyrics = (lrc == null || lrc.isEmpty()) ? null : LrcParser.parseLrc(lrc);
        updateLyric();
    }

    @Override
    public void onPlayStateChanged(boolean playing) {
        runBtn.setImageResource(playing ? R.drawable.music_br : R.drawable.br);
        handler.removeCallbacks(progressRunnable);
        if (playing) {
            int duration = PlayerManager.getInstance().getDuration();
            if (duration > 0) {
                totalTimeTv.setText(formatMs(duration));
            }
            handler.post(progressRunnable);
        }
    }

    private void toggleMode() {
        boolean random = PlayerManager.getInstance().toggleMode();
        modeBtn.setImageResource(random ? R.drawable.rand_br : R.drawable.sun_br);
        Toast.makeText(this, random ? "已切换：随机播放" : "已切换：顺序播放", Toast.LENGTH_SHORT).show();
    }

    private void toggleLove() {
        MusicBean music = PlayerManager.getInstance().getCurrentMusic();
        if (music == null) {
            Toast.makeText(this, "还没在播放歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        String love = MusicDao.toggleLove(music);
        updateLoveIcon();
        Toast.makeText(this, "1".equals(love) ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
    }

    private void updateLoveIcon() {
        MusicBean music = PlayerManager.getInstance().getCurrentMusic();
        if (music != null && "1".equals(music.getLove())) {
            loveBtn.setImageResource(R.drawable.music_red);
        } else {
            loveBtn.setImageResource(R.drawable.music_star);
        }
    }

    // 根据当前播放位置，把歌词显示成「唱到哪一句」
    private void updateLyric() {
        if (lyricTv == null) {
            return;
        }
        if (lyrics == null || lyrics.length == 0) {
            lyricTv.setText("");
            return;
        }
        int pos = PlayerManager.getInstance().getCurrentPosition();
        int idx = -1;
        for (int i = 0; i < lyrics.length; i++) {
            int t = 0;
            try {
                t = Integer.parseInt(lyrics[i][0]);
            } catch (Exception e) {
                continue;
            }
            if (t <= pos) {
                idx = i;
            } else {
                break;
            }
        }
        lyricTv.setText(idx >= 0 ? lyrics[idx][1] : "");
    }

    private String formatMs(int ms) {
        int total = ms / 1000;
        int m = total / 60;
        int s = total % 60;
        return m + ":" + (s < 10 ? "0" + s : s);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressRunnable);
        // 注意：不 release PlayerManager（单例跨界面继续播）
    }
}
