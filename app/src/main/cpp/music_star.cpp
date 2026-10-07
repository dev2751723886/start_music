#include <jni.h>
#include <string>
#include <vector>
#include <algorithm>

// 解析 LRC 歌词文本，返回 String[][]：每行 {毫秒时间戳, 歌词文本}
// 输入示例： "[00:12.34]第一句\n[00:30.00]第二句\n"
extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_music_1star_until_LrcParser_parseLrc(JNIEnv *env, jclass, jstring lrcText) {
    //把 Java 字符串转成 C++ 字符串
    const char *raw = env->GetStringUTFChars(lrcText, nullptr);
    std::string content = raw == nullptr ? "" : raw;
    if (raw != nullptr) {
        env->ReleaseStringUTFChars(lrcText, raw);
        //ReleaseStringUTFChars：非常重要！ Java 字符串在 JNI 里用完必须释放，否则会造成内存泄漏。这里立刻转成了 C++ 的 std::string，所以可以马上释放。
    }
    //定义一个动态数组，每个元素是 {毫秒时间戳, 歌词文本} 的键值对。
    std::vector<std::pair<int, std::string>> lines;

    size_t pos = 0;
    while (pos < content.size()) {
        size_t nl = content.find('\n', pos);
        size_t end = (nl == std::string::npos) ? content.size() : nl;
        std::string line = content.substr(pos, end - pos);
        // 去掉行尾的 \r
        if (!line.empty() && line.back() == '\r') {
            line.pop_back();
        }

        // 解析 [mm:ss.xx]歌词 这一行
        if (line.size() > 1 && line[0] == '[') {
            size_t rb = line.find(']');
            if (rb != std::string::npos && rb > 1) {
                std::string ts = line.substr(1, rb - 1);   // mm:ss.xx
                size_t colon = ts.find(':');
                if (colon != std::string::npos) {
                    try {
                        int mm = std::stoi(ts.substr(0, colon));
                        size_t dot = ts.find('.', colon + 1);
                        int ss = std::stoi(ts.substr(colon + 1, dot - colon - 1));
                        int xx = 0;
                        if (dot != std::string::npos) {
                            std::string frac = ts.substr(dot + 1);
                            if (frac.size() >= 2) {
                                frac = frac.substr(0, 2);   // 只取两位百分秒
                            }
                            if (!frac.empty()) {
                                xx = std::stoi(frac);
                            }
                        }
                        int ms = mm * 60000 + ss * 1000 + xx * 10;
                        lines.push_back({ms, line.substr(rb + 1)});
                    } catch (...) {
                        // 时间戳解析失败，忽略这一行
                    }
                }
            }
        }

        if (nl == std::string::npos) {
            break;
        }
        pos = nl + 1;
    }

    // 按时间排序（通常 LRC 已经有序，这里保险起见）
    std::sort(lines.begin(), lines.end());

    jclass stringClass = env->FindClass("java/lang/String");
    jclass stringArrayClass = env->FindClass("[Ljava/lang/String;");
    jobjectArray outer = env->NewObjectArray((jsize) lines.size(), stringArrayClass, nullptr);
    for (size_t i = 0; i < lines.size(); ++i) {
        jobjectArray inner = env->NewObjectArray(2, stringClass, nullptr);
        jstring timeStr = env->NewStringUTF(std::to_string(lines[i].first).c_str());
        jstring textStr = env->NewStringUTF(lines[i].second.c_str());
        env->SetObjectArrayElement(inner, 0, timeStr);
        env->SetObjectArrayElement(inner, 1, textStr);
        env->DeleteLocalRef(timeStr);
        env->DeleteLocalRef(textStr);
        env->SetObjectArrayElement(outer, (jsize) i, inner);
        env->DeleteLocalRef(inner);
    }
    return outer;
}
