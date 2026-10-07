package com.example.music_star.activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.music_star.R;
import com.example.music_star.bean.UserBean;
import com.example.music_star.dao.UserDao;
import com.example.music_star.until.FileUntil;
import com.example.music_star.until.tools;

// 编辑个人资料：昵称、头像、密码
public class EditProfileActivity extends AppCompatActivity {

    private String account;
    private String existingImg = ""; // 当前头像（没换新头像就保留它）
    private Uri newAvatar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        account = tools.getOnAccount(this);
        UserBean user = UserDao.getUserByAccount(account);

        ImageView avatar = findViewById(R.id.edit_avatar);
        EditText nick = findViewById(R.id.edit_nick);
        EditText pwd = findViewById(R.id.edit_pwd);

        if (user != null) {
            nick.setText(user.getNickname());
            existingImg = user.getImg() == null ? "" : user.getImg();
            if (existingImg.isEmpty()) {
                avatar.setImageResource(R.drawable.photo);
            } else {
                Glide.with(this).load(existingImg).placeholder(R.drawable.photo).error(R.drawable.photo).into(avatar);
            }
        }

        ActivityResultLauncher<String> picker = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        avatar.setImageURI(uri);
                        newAvatar = uri;
                    }
                });

        avatar.setOnClickListener(v -> picker.launch("image/*"));

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v ->
                save(nick.getText().toString().trim(), pwd.getText().toString().trim()));
    }

    private void save(String nickname, String pwd) {
        if (nickname.isEmpty()) {
            tools.Toast(this, "昵称不能为空");
            return;
        }
        String img = existingImg;
        if (newAvatar != null) {
            String path = FileUntil.saveUriToFile(this, newAvatar);
            if (!path.isEmpty()) {
                img = path;
            }
        }
        UserDao.updateProfile(account, nickname, img, pwd);
        tools.Toast(this, "保存成功");
        finish();
    }
}
