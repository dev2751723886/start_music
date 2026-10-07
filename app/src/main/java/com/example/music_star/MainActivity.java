package com.example.music_star;
import com.example.music_star.activity.yonghu.UserActivity;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.example.music_star.activity.RegisterActivity;
import com.example.music_star.dao.UserDao;
import com.example.music_star.until.DBuntil;
import com.example.music_star.until.tools;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceStat){
        //调用父类初始化
        super.onCreate(savedInstanceStat);
        //开启边到边显示
        EdgeToEdge.enable(this);
        //把登录页面加载在界面上
        setContentView(R.layout.activity_main);

        // 初始化数据库
        DBuntil dbUtil = new DBuntil(this);              // 1. 创建 DBuntil 对象
        DBuntil.con = dbUtil.getWritableDatabase();      // 2. 通过它拿到数据库，赋值给静态变量 con
        //界面调试
        Intent intent=new Intent(MainActivity.this,UserActivity.class);
        EditText accountT=findViewById(R.id.zhanghao);
        EditText pwdT=findViewById(R.id.pwd);
        Button btnlogin=findViewById(R.id.btnLogin);
        Button register=findViewById(R.id.btnRegister);
        //点击登录按钮
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            //给登录按钮设置点击监听器。当用户点击登录时，执行 onClick 方法。
            public void onClick(View v){
                String account=accountT.getText().toString();
                String pwd=pwdT.getText().toString();
                if(account.equals("")){
                    tools.Toast(MainActivity.this,"请输入账号");
                }
                else if(pwd.equals("")){
                    tools.Toast(MainActivity.this,"请输入密码");
                }
                else{
                    int sta= UserDao.isLogin(account,pwd);
                    if(sta==-1){
                        tools.Toast(MainActivity.this,"'账号密码错误");
                    }
                    else if(sta==0){
                        tools.Toast(MainActivity.this,"登录用户");
                        tools.saveAccount(MainActivity.this, account);
                        // 登录成功，跳转到用户界面（底部有“我的”菜单）
                        startActivity(new Intent(MainActivity.this, UserActivity.class));
                    }
                    else if(sta==1){
                        tools.Toast(MainActivity.this,"登录管理员");
                        tools.saveAccount(MainActivity.this, account);
                        // 管理员登录成功，同样跳转到用户界面
                        startActivity(new Intent(MainActivity.this, UserActivity.class));
                    }
                }

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开注册界面
                Intent intent =new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

}
