package com.example.music_star.until;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class tools {

    // SharedPreferences 文件名和账号的 key
    private static final String SP_NAME = "music_star";
    private static final String KEY_ACCOUNT = "account";

    //这是一个弹窗的方法
    public static void Toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    //保存当前登录账号
    public static void saveAccount(Context context, String account) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_ACCOUNT, account).apply();
    }

    //获取当前登录账号（没登录过则返回空字符串）
    public static String getOnAccount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_ACCOUNT, "");
    }

    // 把秒数格式化成「X小时Y分 / Y分Z秒 / Z秒」
    public static String formatTime(int totalSeconds) {
        if (totalSeconds <= 0) return "0秒";
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        if (h > 0) return h + "小时" + m + "分";
        if (m > 0) return m + "分" + s + "秒";
        return s + "秒";
    }
}
