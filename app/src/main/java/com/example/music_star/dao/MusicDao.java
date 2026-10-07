package com.example.music_star.dao;

import android.database.Cursor;

import com.example.music_star.bean.MusicBean;
import com.example.music_star.until.DBuntil;
import com.example.music_star.until.SqliteT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MusicDao {

    // 根据歌单 id 查询这个歌单下的所有歌曲
    public static List<MusicBean> getMusicBySheet(String sheetId) {
        String sql = "select * from d_music where sheet_id=?";
        return SqliteT.query(sql, MusicBean.class, sheetId);
    }

    // 查询所有歌曲（音乐热榜用，按播放量从高到低）
    public static List<MusicBean> getAllMusic() {
        String sql = "select * from d_music order by play_count desc";
        return SqliteT.query(sql, MusicBean.class);
    }

    // 按歌名/歌手搜索歌曲
    public static List<MusicBean> searchMusic(String query) {
        String like = "%" + query + "%";
        String sql = "select * from d_music where name like ? or singer like ?";
        return SqliteT.query(sql, MusicBean.class, like, like);
    }

    // 更新歌曲的“喜欢”状态（1=喜欢 0=不喜欢）
    public static void updateLove(String musicId, String love) {
        String sql = "update d_music set love=? where id=?";
        SqliteT.updata(sql, love, musicId);
    }

    // 切换喜欢状态（同时更新内存里的对象），返回新状态 1=已收藏 0=未收藏
    public static String toggleLove(MusicBean bean) {
        String newLove = "1".equals(bean.getLove()) ? "0" : "1";
        updateLove(bean.getId(), newLove);
        bean.setLove(newLove);
        return newLove;
    }

    // 查询所有“喜欢”的歌曲（收藏列表用）
    public static List<MusicBean> getLoveMusic() {
        String sql = "select * from d_music where love='1'";
        return SqliteT.query(sql, MusicBean.class);
    }

    // 播放量 +1（热榜按播放量排序）
    public static void incrementPlayCount(String musicId) {
        DBuntil.con.execSQL("update d_music set play_count = play_count + 1 where id=?",
                new String[]{musicId});
    }

    // 记录一次播放历史（最近播放用）
    public static void savePlayHistory(String uid, String musicId) {
        DBuntil.con.execSQL("insert into d_play_history(user_id, music_id) values(?,?)",
                new String[]{uid, musicId});
    }

    // 查询最近播放（按时间倒序，去重）
    public static List<MusicBean> getRecentMusic(String uid) {
        String sql = "select m.* from d_music m join d_play_history h on m.id=h.music_id where h.user_id=? order by h.id desc";
        List<MusicBean> all = SqliteT.query(sql, MusicBean.class, uid);
        List<MusicBean> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (all != null) {
            for (MusicBean m : all) {
                if (seen.add(m.getId())) {
                    result.add(m);
                }
            }
        }
        return result;
    }
    public static void updateCurrentUserPlayMusicSta(String uid,String mid,String sta){
        String sql="update d_music_play set sta=? where user_id=? and id=?";
        SqliteT.updata(sql, sta,uid,mid);
    }

    /**
     * 获取状态值，当前用户播放的状态
     * @param uid
     * @param sta
     */
    public static MusicBean getCurrentUserPlayMusic(String uid, String sta){
        // 先查“正在播放”表，拿到当前用户正在播放的音乐 id
        String sql = "select id from d_music_play where user_id=? and sta=?";
        Cursor cursor = DBuntil.con.rawQuery(sql, new String[]{uid, sta});
        String musicId = null;
        try {
            if (cursor.moveToNext()) {
                musicId = cursor.getString(cursor.getColumnIndex("id"));
            }
        } finally {
            cursor.close();
        }
        if (musicId == null) {
            return null;
        }
        // 再根据音乐 id 查完整音乐信息
        List<MusicBean> list = SqliteT.query("select * from d_music where id=?", MusicBean.class, musicId);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
