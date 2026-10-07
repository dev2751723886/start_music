package com.example.music_star.dao;

import android.database.Cursor;
import android.util.Log;

import com.example.music_star.MainActivity;
import com.example.music_star.bean.UserBean;
import com.example.music_star.until.tools;
import com.example.music_star.until.DBuntil;
import com.example.music_star.until.SqliteT;

public class UserDao {
    // 输入账号和密码来判断登录，区分账号和密码
    public static int isLogin(String account, String pwd) {
        String sql = "select * from d_user where account=? and pwd=?";
        // 两个 ? 需要传两个参数，顺序为 account、pwd
        Cursor result = DBuntil.con.rawQuery(sql, new String[]{account, pwd});
        try {
            if (result.moveToNext()) {//判断那没拿到如果有账号和密码就拿到pow表里面的列号
                int powIndex = result.getColumnIndex("pow");
                int pow = result.getInt(powIndex);//读取该列的值，作为 int 返回
                Log.d("登录权限:", "pow" + pow);
                return pow; // 0是用户 1是管理员
            }
        } finally {
            result.close();
        }
        return -1; // 账号或密码错误
    }
    public static int isRegist(String nc, String account, String pwd, String dizhi, String sex, String img){
        // 插入数据必须用 execSQL，不能用 rawQuery（rawQuery 只能查）
        String sql = "insert into d_user(account,nickname,pwd,img,pow,address,sex) values(?,?,?,?,?,?,?)";
        try {
            DBuntil.con.execSQL(sql, new String[]{account, nc, pwd, img, "0", dizhi, sex});
            return 1;
        } catch (Exception e) {
            Log.e("注册", "注册失败", e);
            return -1;
        }
    }

    // 检查账号或昵称是否已存在：0=都不存在，1=账号已存在，2=昵称已存在
    public static int checkExist(String account, String nickname){
        String sql = "select account,nickname from d_user where account=? or nickname=?";
        Cursor result = DBuntil.con.rawQuery(sql, new String[]{account, nickname});
        try {
            while (result.moveToNext()) {
                String acc  = result.getString(result.getColumnIndex("account"));
                String nick = result.getString(result.getColumnIndex("nickname"));
                if (account.equals(acc))  return 1; // 账号已存在
                if (nickname.equals(nick)) return 2; // 昵称已存在
            }
        } finally {
            result.close();
        }
        return 0; // 都不存在
    }

    // 根据账号查询用户信息（昵称、头像），「我的」页用
    public static UserBean getUserByAccount(String account){
        String sql = "select account,nickname,img from d_user where account=?";
        return SqliteT.queryOne(sql, UserBean.class, account);
    }

    // 累计听歌时长（秒）
    public static void addListeningTime(String account, int seconds) {
        DBuntil.con.execSQL("update d_user set listening_time = listening_time + ? where account=?",
                new String[]{String.valueOf(seconds), account});
    }

    // 听歌曲数 +1
    public static void addSongCount(String account) {
        DBuntil.con.execSQL("update d_user set song_count = song_count + 1 where account=?",
                new String[]{account});
    }

    // 查询累计听歌时长（秒）
    public static int getListeningTime(String account) {
        Cursor c = DBuntil.con.rawQuery("select listening_time from d_user where account=?",
                new String[]{account});
        int t = 0;
        try {
            if (c.moveToNext()) {
                t = c.getInt(0);
            }
        } finally {
            c.close();
        }
        return t;
    }

    // 更新个人资料（昵称、头像；密码为空则不修改）
    public static void updateProfile(String account, String nickname, String img, String pwd) {
        if (pwd == null || pwd.isEmpty()) {
            DBuntil.con.execSQL("update d_user set nickname=?, img=? where account=?",
                    new String[]{nickname, img, account});
        } else {
            DBuntil.con.execSQL("update d_user set nickname=?, img=?, pwd=? where account=?",
                    new String[]{nickname, img, pwd, account});
        }
    }

}
