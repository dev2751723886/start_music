package com.example.music_star.until;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUntil {

    /**
     * 把 drawable 资源保存成图片文件，返回文件绝对路径。
     *
     * @param context    上下文
     * @param drawableId drawable 资源 id，例如 R.drawable.track
     * @return 保存成功返回文件绝对路径；失败返回空字符串 ""
     */
    public static String saveDrawableToFileSaveAs(Context context, int drawableId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, drawableId);
            if (drawable == null) {
                Log.e("FileUntil", "drawable 为空, id=" + drawableId);
                return "";
            }

            Bitmap bitmap = drawableToBitmap(drawable);

            // 用资源名（如 "track"）作为文件名
            String name = context.getResources().getResourceEntryName(drawableId);

            // 存到 app 私有目录，不需要申请存储权限
            File dir = context.getFilesDir();
            File file = new File(dir, name + ".png");

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("FileUntil", "保存 drawable 失败", e);
            return "";
        }
    }

    /**
     * 把 content:// 图片 URI 复制到应用私有目录，返回本地文件绝对路径。
     * 注册时头像用它存成文件，避免 App 重启后 content URI 权限失效导致头像加载不出来。
     *
     * @return 成功返回文件绝对路径；失败返回空字符串 ""
     */
    public static String saveUriToFile(Context context, Uri uri) {
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in == null) {
                Log.e("FileUntil", "打开图片流失败");
                return "";
            }
            File dir = context.getFilesDir();
            File file = new File(dir, "avatar_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            fos.close();
            in.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("FileUntil", "保存头像失败", e);
            return "";
        }
    }

    /**
     * Drawable 转 Bitmap。
     */
    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        // 其他类型（如 VectorDrawable）手动绘制到 Bitmap 上
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0) width = 1;
        if (height <= 0) height = 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
