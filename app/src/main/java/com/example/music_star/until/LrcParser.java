package com.example.music_star.until;

// 歌词解析：JNI 调用 C++ 的 parseLrc
public class LrcParser {

    static {
        System.loadLibrary("music_star");
    }

    // 解析 LRC 歌词文本，返回 String[][]：每行 {毫秒时间戳, 歌词文本}
    public static native String[][] parseLrc(String lrcText);
}
