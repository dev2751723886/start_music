package com.example.music_star.dao;

//查询音乐,封装类
import android.database.Cursor;

import com.example.music_star.bean.MusicBean;
import com.example.music_star.bean.PlayMusicBean;
import com.example.music_star.until.DBuntil;
import com.example.music_star.until.SqliteT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PalymusicDao {

    //查询推荐歌单（固定顺序，不随机；只返回 sta=1 的系统推荐歌单）
    public static List<PlayMusicBean> getPlayMusic() {
        String sql = "select * from d_sheet where sta='1' order by id";
        return SqliteT.query(sql, PlayMusicBean.class);
    }
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

    // 记录当前正在播放的音乐（先删掉该用户旧的记录，再插入新的）
    public static void saveCurrentPlayMusic(String uid, String musicId) {
        DBuntil.con.execSQL("delete from d_music_play where user_id=?", new String[]{uid});
        DBuntil.con.execSQL("insert into d_music_play(id, user_id, sta) values(?,?,?)",
                new String[]{musicId, uid, "1"});
    }

    // 查询某个用户创建的歌单（「我的」页用）
    public static List<PlayMusicBean> getSheetsByUser(String account) {
        String sql = "select * from d_sheet where create_user_id=?";
        return SqliteT.query(sql, PlayMusicBean.class, account);
    }

    // 新建歌单（封面暂为空，sta=0 表示不出现在“推荐歌单”里，只属于当前用户）
    public static void createSheet(String id, String name, String account) {
        DBuntil.con.execSQL("insert into d_sheet(id,name,img,create_user_id,sta) values(?,?,?,?,?)",
                new String[]{id, name, "", account, "0"});
    }

    // 把一首歌加入歌单（已存在则忽略，不重复添加）
    public static void addMusicToSheet(String sheetId, String musicId) {
        DBuntil.con.execSQL("insert or ignore into d_sheet_music(sheet_id,music_id) values(?,?)",
                new String[]{sheetId, musicId});
    }

    // 从歌单移除一首歌（只删关系，不删歌曲本身，不影响推荐/热榜）
    public static void removeMusicFromSheet(String sheetId, String musicId) {
        DBuntil.con.execSQL("delete from d_sheet_music where sheet_id=? and music_id=?",
                new String[]{sheetId, musicId});
    }

    // 查询歌单里的歌曲（走关系表，兼容自己创建的歌单）
    public static List<MusicBean> getMusicBySheetJunction(String sheetId) {
        String sql = "select m.* from d_music m join d_sheet_music sm on m.id=sm.music_id where sm.sheet_id=?";
        return SqliteT.query(sql, MusicBean.class, sheetId);
    }

    // 删除歌单：删掉歌单本身 + 歌单-歌曲关系
    public static void deleteSheet(String sheetId) {
        DBuntil.con.execSQL("delete from d_sheet_music where sheet_id=?", new String[]{sheetId});
        DBuntil.con.execSQL("delete from d_sheet where id=?", new String[]{sheetId});
    }

    // 查询歌单里的歌曲数量（歌单卡片上显示几首）
    public static int getSheetMusicCount(String sheetId) {
        Cursor c = DBuntil.con.rawQuery("select count(*) from d_sheet_music where sheet_id=?", new String[]{sheetId});
        int count = 0;
        try {
            if (c.moveToNext()) {
                count = c.getInt(0);
            }
        } finally {
            c.close();
        }
        return count;
    }
}
