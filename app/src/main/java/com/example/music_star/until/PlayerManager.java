package com.example.music_star.until;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.music_star.bean.MusicBean;
import com.example.music_star.dao.MusicDao;
import com.example.music_star.dao.PalymusicDao;
import com.example.music_star.dao.UserDao;

import java.util.List;
import java.util.Random;

/**
 * 统一的播放类（单例）：管 MediaPlayer 播放、播放列表、顺序/随机切歌、播放状态。
 * 所有界面（用户界面、歌单列表、热榜、收藏）都调它，不再各自 new MediaPlayer。
 *
 * 播放状态：未播放(0) → 播放时标记 正在播放(1) → 播放完标记 未播放(0)。
 */
public class PlayerManager {

    // 播放监听：UI 层实现它来更新界面（切歌了 / 播放暂停了）
    public interface OnPlayerListener {
        void onMusicChanged(MusicBean music);      // 开始播放新歌
        void onPlayStateChanged(boolean playing);  // 播放状态变化（true=播放中）
    }

    private static PlayerManager instance;

    private Context appContext;                 // Application Context（避免内存泄漏，跨界面播放）
    private MediaPlayer mediaPlayer;
    private List<MusicBean> currentPlaylist;    // 当前播放列表
    private int currentIndex = 0;               // 当前播到第几首
    private boolean randomMode = false;         // false=顺序 true=随机
    private final Random random = new Random();
    private MusicBean currentMusic;             // 当前正在播放的歌曲
    private OnPlayerListener listener;
    private String uid = "";                    // 当前账号（播放状态要按用户记）
    private long playStartTime = 0;             // 当前这首歌开始播的时间戳（算听歌时长）

    private PlayerManager() {
    }

    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public void setListener(OnPlayerListener listener) {
        this.listener = listener;
    }

    // 播放列表里的第 index 首
    public void play(Context context, List<MusicBean> songs, int index) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
        // 同步当前账号（登录用户变了，播放状态也跟着变）
        uid = tools.getOnAccount(appContext);

        if (songs == null || songs.isEmpty() || index < 0 || index >= songs.size()) {
            return;
        }
        MusicBean target = songs.get(index);
        // 播放前识别：如果正在播的就是这首歌，就不再重复播
        if (currentMusic != null && currentMusic.getId().equals(target.getId()) && isPlaying()) {
            return;
        }
        currentPlaylist = songs;
        currentIndex = index;
        playMusic(target);
    }

    private void playMusic(MusicBean bean) {
        if (appContext == null) {
            return;
        }
        int resId = appContext.getResources().getIdentifier(bean.getPath(), "raw", appContext.getPackageName());
        if (resId == 0) {
            return;
        }
        recordListenedTime();            // 换歌前结算上一首的听歌时长
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(appContext, resId);
        if (mediaPlayer != null) {
            currentMusic = bean;
            markPlaying(bean.getId());   // 播放状态：改成「正在播放」
            mediaPlayer.setOnCompletionListener(mp -> onComplete());
            mediaPlayer.start();
            playStartTime = System.currentTimeMillis();
            if (!uid.isEmpty()) {
                UserDao.addSongCount(uid);              // 听歌曲数 +1
                MusicDao.savePlayHistory(uid, bean.getId()); // 记录最近播放
            }
            MusicDao.incrementPlayCount(bean.getId());  // 播放量 +1
            if (listener != null) {
                listener.onMusicChanged(bean);
                listener.onPlayStateChanged(true);
            }
        }
    }

    // 结算当前这首歌的听歌时长并累加到用户
    private void recordListenedTime() {
        if (playStartTime > 0 && currentMusic != null && !uid.isEmpty()) {
            int elapsed = (int) ((System.currentTimeMillis() - playStartTime) / 1000);
            if (elapsed > 0) {
                UserDao.addListeningTime(uid, elapsed);
            }
        }
        playStartTime = 0;
    }

    // 播放完成：先把状态改成「未播放」，再自动切下一首
    private void onComplete() {
        if (currentMusic != null) {
            markFinished(currentMusic.getId());   // 播放状态：改回「未播放」
            recordListenedTime();                 // 这首播完，结算时长
        }
        playNext();
    }

    private void playIndex(int index) {
        if (currentPlaylist == null || currentPlaylist.isEmpty()) {
            return;
        }
        currentIndex = index;
        playMusic(currentPlaylist.get(index));
    }

    // 下一首：顺序往后排，随机乱挑（避免连播同一首）
    public void playNext() {
        if (currentPlaylist == null || currentPlaylist.isEmpty()) {
            if (listener != null) {
                listener.onPlayStateChanged(false);
            }
            return;
        }
        int size = currentPlaylist.size();
        int next;
        if (randomMode && size > 1) {
            do {
                next = random.nextInt(size);
            } while (next == currentIndex);
        } else {
            next = (currentIndex + 1) % size;
        }
        playIndex(next);
    }

    // 上一首：顺序往前排，随机乱挑
    public void playPrev() {
        if (currentPlaylist == null || currentPlaylist.isEmpty()) {
            return;
        }
        int size = currentPlaylist.size();
        int prev;
        if (randomMode && size > 1) {
            do {
                prev = random.nextInt(size);
            } while (prev == currentIndex);
        } else {
            prev = (currentIndex - 1 + size) % size;
        }
        playIndex(prev);
    }

    // 播放/暂停切换
    public void togglePlayPause() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            recordListenedTime();   // 暂停时结算这一段
            mediaPlayer.pause();
            if (listener != null) {
                listener.onPlayStateChanged(false);
            }
        } else {
            mediaPlayer.start();
            playStartTime = System.currentTimeMillis(); // 继续播，重新计时
            if (listener != null) {
                listener.onPlayStateChanged(true);
            }
        }
    }

    // 切换顺序/随机，返回切换后的模式（true=随机）
    public boolean toggleMode() {
        randomMode = !randomMode;
        return randomMode;
    }

    public boolean isRandomMode() {
        return randomMode;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public MusicBean getCurrentMusic() {
        return currentMusic;
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public void seekTo(int ms) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(ms);
        }
    }

    // 播放状态：标记「正在播放」
    private void markPlaying(String musicId) {
        if (uid.isEmpty()) {
            return;
        }
        PalymusicDao.saveCurrentPlayMusic(uid, musicId);
    }

    // 播放状态：标记「未播放」
    private void markFinished(String musicId) {
        if (uid.isEmpty()) {
            return;
        }
        MusicDao.updateCurrentUserPlayMusicSta(uid, musicId, "0");
    }

    public void release() {
        if (mediaPlayer != null) {
            recordListenedTime();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
