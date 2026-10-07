package com.example.music_star.until;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.music_star.R;
import com.example.music_star.activity.PlayerActivity;
import com.example.music_star.bean.MusicBean;
import com.example.music_star.dao.MusicDao;

/**
 * 播放菜单（播放条）控制器：管播放条的显示、封面、歌名、进度条、各按钮。
 * 任何界面只要 include 了 player_bar，就能 new 一个它来复用，不用再写一遍播放条逻辑。
 * 它实现了 PlayerManager 的监听，播放器一有动静就自动更新界面。
 */
public class PlayerBarController implements PlayerManager.OnPlayerListener {

    private final View playerBar;
    private final ImageView coverIv;    // 封面
    private final TextView nameTv;      // 歌名
    private final SeekBar seekBar;      // 进度条
    private final ImageView runBtn;     // 播放/暂停
    private final ImageView modeBtn;    // 顺序/随机
    private final ImageView prevBtn;    // 上一首
    private final ImageView nextBtn;    // 下一首
    private final ImageView loveBtn;    // 收藏
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isUserSeeking = false; // 用户是否正在拖进度条

    // 每 0.5 秒刷新一次进度条
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            PlayerManager pm = PlayerManager.getInstance();
            if (pm.isPlaying() && !isUserSeeking) {
                int duration = pm.getDuration();
                if (duration > 0) {
                    seekBar.setMax(duration);
                    seekBar.setProgress(pm.getCurrentPosition());
                }
            }
            handler.postDelayed(this, 500);
        }
    };

    public PlayerBarController(View playerBar) {
        this.playerBar = playerBar;
        this.coverIv = playerBar.findViewById(R.id.user_home_photo);
        this.nameTv = playerBar.findViewById(R.id.user_home_name);
        this.seekBar = playerBar.findViewById(R.id.user_seekbar);
        this.runBtn = playerBar.findViewById(R.id.play_qidong_run);
        this.modeBtn = playerBar.findViewById(R.id.bianhua);
        this.prevBtn = playerBar.findViewById(R.id.music_s);
        this.nextBtn = playerBar.findViewById(R.id.music_x);
        this.loveBtn = playerBar.findViewById(R.id.music_star);

        // 默认顺序图标
        modeBtn.setImageResource(R.drawable.sun_br);

        // 各按钮点击：都交给 PlayerManager
        //播放暂停
        runBtn.setOnClickListener(v -> PlayerManager.getInstance().togglePlayPause());
        //切换
        modeBtn.setOnClickListener(v -> toggleMode());
        //上首
        prevBtn.setOnClickListener(v -> PlayerManager.getInstance().playPrev());
        //下首
        nextBtn.setOnClickListener(v -> PlayerManager.getInstance().playNext());
        //收藏
        loveBtn.setOnClickListener(v -> toggleLove());

        // 点击封面/歌名 → 打开全屏播放页
        View.OnClickListener openPlayer = v -> playerBar.getContext()
                .startActivity(new Intent(playerBar.getContext(), PlayerActivity.class));
        playerBar.findViewById(R.id.user_bar).setOnClickListener(openPlayer);
        playerBar.findViewById(R.id.user_home_photo).setOnClickListener(openPlayer);
        playerBar.findViewById(R.id.user_home_name).setOnClickListener(openPlayer);

        // 拖动进度条跳进度
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
            }
        });
    }

    // 切歌：弹出播放条，更新封面、歌名、收藏图标
    @Override
    public void onMusicChanged(MusicBean music) {
        //.显示底部播放条
        playerBar.setVisibility(View.VISIBLE);
        //获取封面图片的资源 ID
        int resId = playerBar.getContext().getResources()
                .getIdentifier(music.getImg(), "drawable", playerBar.getContext().getPackageName());
        //用 Glide 加载图片
        Glide.with(playerBar.getContext())
                .load(resId != 0 ? resId : R.drawable.beizi)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(50)))
                .into(coverIv);
        //更新歌名和歌手
        nameTv.setText(music.getName() + "-" + music.getSinger());
        updateLoveIcon();
    }

    // 播放/暂停状态变化：换图标，并启动/停止进度条刷新
    @Override
    public void onPlayStateChanged(boolean playing) {
        runBtn.setImageResource(playing ? R.drawable.music_br : R.drawable.br);
        handler.removeCallbacks(progressRunnable);
        if (playing) {
            handler.post(progressRunnable);
        }
    }

    // 顺序/随机切换
    private void toggleMode() {
        boolean random = PlayerManager.getInstance().toggleMode();
        modeBtn.setImageResource(random ? R.drawable.rand_br : R.drawable.sun_br);
        Toast.makeText(playerBar.getContext(), random ? "已切换：随机播放" : "已切换：顺序播放", Toast.LENGTH_SHORT).show();
    }

    // 收藏 / 取消收藏
    private void toggleLove() {
        MusicBean music = PlayerManager.getInstance().getCurrentMusic();
        if (music == null) {
            Toast.makeText(playerBar.getContext(), "还没在播放歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        String newLove = "1".equals(music.getLove()) ? "0" : "1";
        MusicDao.updateLove(music.getId(), newLove);
        music.setLove(newLove);
        updateLoveIcon();
        Toast.makeText(playerBar.getContext(), "1".equals(newLove) ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
    }

    // 根据是否喜欢更新收藏图标
    private void updateLoveIcon() {
        MusicBean music = PlayerManager.getInstance().getCurrentMusic();
        if (music != null && "1".equals(music.getLove())) {
            loveBtn.setImageResource(R.drawable.music_red);
        } else {
            loveBtn.setImageResource(R.drawable.music_star);
        }
    }

    // 隐藏播放条
    public void hide() {
        playerBar.setVisibility(View.GONE);
    }

    // 释放：停止进度条刷新（Activity onDestroy 时调）
    public void destroy() {
        handler.removeCallbacks(progressRunnable);
    }
}
