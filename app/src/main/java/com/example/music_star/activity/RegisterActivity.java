package com.example.music_star.activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music_star.R;
import com.example.music_star.dao.UserDao;
import com.example.music_star.until.FileUntil;
import com.example.music_star.until.tools;

public class RegisterActivity extends AppCompatActivity {
    Uri result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageView tx = findViewById(R.id.touxiang);
        EditText nc = findViewById(R.id.nicheng);
        EditText zh = findViewById(R.id.zhanghao);
        EditText dz = findViewById(R.id.dizhi);
        EditText pd = findViewById(R.id.pwd);
        RadioButton sexMan = findViewById(R.id.boy);    // 男
        RadioButton sexWoman = findViewById(R.id.girl); // 女
        Button reg = findViewById(R.id.RegisterT);

        // 必须在 onCreate 里（Activity 启动之前）注册头像选择器，不能在点击时才注册
        ActivityResultLauncher<String> getContentLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        tx.setImageURI(uri);
                        result = uri;
                    } else {
                        tools.Toast(RegisterActivity.this, "未选择头像");
                    }
                });

        // 点击头像 → 打开系统图片选择器
        tx.setOnClickListener(v -> getContentLauncher.launch("image/*"));

        // 注册
        reg.setOnClickListener(v -> {
            String nick    = nc.getText().toString().trim();
            String account = zh.getText().toString().trim();
            String pwd     = pd.getText().toString().trim();
            String address = dz.getText().toString().trim();
            // 取性别（必须选一个，否则提示并返回）
            String sexStr;
            if (sexMan.isChecked()) {
                sexStr = "男";
            } else if (sexWoman.isChecked()) {
                sexStr = "女";
            } else {
                tools.Toast(RegisterActivity.this, "请选择性别");
                return;
            }

            // 再校验
            if (nick.isEmpty()) {
                tools.Toast(RegisterActivity.this, "请输入昵称");
                return;
            } else if (account.isEmpty()) {
                tools.Toast(RegisterActivity.this, "请输入账号");
                return;
            } else if (pwd.isEmpty()) {
                tools.Toast(RegisterActivity.this, "请输入密码");
                return;
            } else {
                // 1. 检查账号/昵称是否已存在
                int exist = UserDao.checkExist(account, nick);
                if (exist == 1) {
                    tools.Toast(RegisterActivity.this, "账号 " + account + " 已存在");
                    return;
                } else if (exist == 2) {
                    tools.Toast(RegisterActivity.this, "昵称 " + nick + " 已存在");
                    return;
                }

                // 2. 头像：没选就是空字符串，选了就把图片复制到应用私有目录，存本地路径（重启后仍可读）
                String imgStr = (result != null) ? FileUntil.saveUriToFile(RegisterActivity.this, result) : "";
                int sta = UserDao.isRegist(nick, account, pwd, address, sexStr, imgStr);
                if (sta == 1) {
                    tools.Toast(RegisterActivity.this, "注册成功");
                    finish(); // 注册成功后关闭注册页，返回登录页
                } else {
                    tools.Toast(RegisterActivity.this, "注册失败");
                }
            }
        });
    }
}
