package com.example.music_star.until;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.music_star.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.database.sqlite.SQLiteDatabase;
public class DBuntil extends SQLiteOpenHelper {
    private static final int version=16;//版本号，每次更改表结构都需要加1，否则不生效
    private static final String databaseName="db_takeaway.db";
    private Context context;
    public static  SQLiteDatabase con;//连接数据库连接，通过他可以操作数据库


    public DBuntil(@Nullable Context context) {
        super(context,databaseName,null,version,null);
        this.context=context;
    }




    @Override
    public void onCreate(SQLiteDatabase db) {
        android.util.Log.d("DBuntil", "onCreate 开始执行");
    db.execSQL("PRAGMA foreign_keys=false");
    //所以的数据库操作在这个位置写
//账号, 昵称, 密码, 头像, 权限, 地址, 性别, 听歌时长, 曲数, 注册时间 （可以修改自己账号，与密码）
        db.execSQL("create table d_user(account  varchar(20) primary key," +
                "nickname VARCHAR(50)," +
                "pwd VARCHAR(50)," +
                "img VARCHAR(50)," +
                "pow VARCHAR(50)," +//0是用户1是管理者
                "address VARCHAR(50)," +
                "sex VARCHAR(50),"+
                "listening_time INTEGER DEFAULT 0," +
                "song_count INTEGER DEFAULT 0," +
                "registration_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");
        android.util.Log.d("DBuntil", "d_user 建好了");
        String data[]={"root","小寒","123456","","0","北京","男"};
        db.execSQL("insert into d_user(account,nickname,pwd,img,pow,address,sex) values(?,?,?,?,?,?,?)",data);
        String data1[]={"xx","小欣","123456","","1","上海","女"};
        db.execSQL("insert into d_user(account,nickname,pwd,img,pow,address,sex) values(?,?,?,?,?,?,?)",data1);
        db.execSQL("PRAGMA foreign_keys=true");
        //创建一个歌单表  歌单的ID,歌单的名字,歌单的封面,歌单的创建时间,歌单用户ID,
        db.execSQL("drop table if exists d_sheet");
        db.execSQL("create table d_sheet(id varchar(50) primary key," +
                "name VARCHAR(50)," +
                "img VARCHAR(50)," +
                "create_user_id VARCHAR(50)," +//0 是用户  1是管理员
                "sta VARCHAR(50)," +//0 是用户  1是管理员
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");
//关系表,这个表代表着,有多个歌单,有同一个用户创建,所以需要一个中间表来关联   id,歌单的ID
        String minjianPath=FileUntil.saveDrawableToFileSaveAs(context,R.drawable.minjian);
        String heipiPath=FileUntil.saveDrawableToFileSaveAs(context,R.drawable.heipi);
        String liuxingPath=FileUntil.saveDrawableToFileSaveAs(context,R.drawable.beizi);
        String data2[]={"1","民谣歌曲",minjianPath,"admin","1"};//0是只能自己可以见
        db.execSQL( "insert into d_sheet (id,name,img,create_user_id,sta) values(?,?,?,?,?)",data2);
        String data3[]={"2","流行曲",liuxingPath,"卓文萱","1"};//0是只能自己可以见
        db.execSQL( "insert into d_sheet (id,name,img,create_user_id,sta) values(?,?,?,?,?)",data3);
        String data4[]={"3","摇滚乐曲",heipiPath,"star","1"};//0是只能自己可以见
        db.execSQL( "insert into d_sheet (id,name,img,create_user_id,sta) values(?,?,?,?,?)",data4);

     //创建一个歌单表  歌单的ID,歌单的名字,歌单的封面,歌单的创建时间,歌单用户ID,          歌单的收藏量
        db.execSQL("drop table if exists d_music");
        db.execSQL("create table d_music(id varchar(50) primary key," +
                "name VARCHAR(50)," +
                "singer VARCHAR(50)," +
                "img VARCHAR(50)," +       //封面 drawable 资源名
                "path VARCHAR(50)," +      //音频 raw 资源名
                "sheet_id VARCHAR(50)," +  //属于哪个歌单(d_sheet.id)
                "love VARCHAR(50) DEFAULT '0'," + //是否喜欢 1=喜欢 0=不喜欢
                "deleted VARCHAR(50) DEFAULT '0'," + //是否删除 1=已删除 0=未删除
                "play_count INTEGER DEFAULT 0," +     //播放量（热榜按它排序）
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");
        //往「流行曲」(id=2) 歌单里插入三首歌
        String music1[]={"m1","到时候说爱我","茜拉","daoshihoushuoaiw","daoshihoushuoaini","2"};
        db.execSQL("insert into d_music(id,name,singer,img,path,sheet_id) values(?,?,?,?,?,?)",music1);
        String music2[]={"m2","那年梨花瘦","七月","namianlihuas","namoianlihuashou","2"};
        db.execSQL("insert into d_music(id,name,singer,img,path,sheet_id) values(?,?,?,?,?,?)",music2);
        String music3[]={"m3","失眠","刘舒舒","sm","shimian","2"};
        db.execSQL("insert into d_music(id,name,singer,img,path,sheet_id) values(?,?,?,?,?,?)",music3);

        //“正在播放”表：记录某个用户当前正在播放的音乐
        db.execSQL("create table d_music_play(id varchar(50)," +   //音乐ID，对应 d_music.id
                "user_id varchar(50)," +                          //用户账号，对应 d_user.account
                "sta varchar(50)" +                               //状态：1=正在播放
                ")");

        //歌单-歌曲关系表：一首歌可以加入多个歌单（多对多）
        db.execSQL("create table d_sheet_music(sheet_id varchar(50)," +
                "music_id varchar(50)," +
                "primary key(sheet_id, music_id)" +
                ")");

        //播放历史表：记录每次播放（最近播放用）
        db.execSQL("create table d_play_history(id integer primary key autoincrement," +
                "user_id varchar(50)," +
                "music_id varchar(50)" +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级时不要 DROP 表，否则老用户（比如注册的“落落”）等数据会被清空。
        // 只有确实新增了字段时，才用 ALTER TABLE 增量加列，老数据会保留。
        // 示例：当从 7 升到 8 时给 d_user 新增一列
        // if (oldVersion < 8) {
        //     db.execSQL("ALTER TABLE d_user ADD COLUMN xxx VARCHAR(50)");
        // }
        if (oldVersion < 8) {
            // 重新生成封面文件，并更新歌单表里的图片路径（保留老数据，不清空用户）
            String minjianPath = FileUntil.saveDrawableToFileSaveAs(context, R.drawable.minjian);
            String liuxingPath = FileUntil.saveDrawableToFileSaveAs(context, R.drawable.beizi);
            String heipiPath = FileUntil.saveDrawableToFileSaveAs(context, R.drawable.heipi);

            db.execSQL("UPDATE d_sheet SET img=? WHERE id=?", new String[]{minjianPath, "1"});
            db.execSQL("UPDATE d_sheet SET img=? WHERE id=?", new String[]{liuxingPath, "2"});
            db.execSQL("UPDATE d_sheet SET img=? WHERE id=?", new String[]{heipiPath, "3"});
        }
        if (oldVersion < 10) {
            // 保证 d_music 表存在且带 sheet_id 列，并插入三首歌（不清空老数据）
            db.execSQL("CREATE TABLE IF NOT EXISTS d_music(id varchar(50) primary key," +
                    "name VARCHAR(50), singer VARCHAR(50), img VARCHAR(50), path VARCHAR(50)," +
                    "sheet_id VARCHAR(50), create_time DATETIME DEFAULT CURRENT_TIMESTAMP)");
            try {
                db.execSQL("ALTER TABLE d_music ADD COLUMN sheet_id VARCHAR(50)");
            } catch (Exception ignored) { }

            db.execSQL("INSERT OR IGNORE INTO d_music(id,name,singer,img,path,sheet_id) VALUES(?,?,?,?,?,?)",
                    new String[]{"m1","到时候说爱我","茜拉","daoshihoushuoaiw","daoshihoushuoaini","2"});
            db.execSQL("INSERT OR IGNORE INTO d_music(id,name,singer,img,path,sheet_id) VALUES(?,?,?,?,?,?)",
                    new String[]{"m2","那年梨花瘦","七月","namianlihuas","namoianlihuashou","2"});
            db.execSQL("INSERT OR IGNORE INTO d_music(id,name,singer,img,path,sheet_id) VALUES(?,?,?,?,?,?)",
                    new String[]{"m3","失眠","刘舒舒","sm","shimian","2"});
        }
        if (oldVersion < 11) {
            // 新增“正在播放”表
            db.execSQL("create table if not exists d_music_play(id varchar(50), user_id varchar(50), sta varchar(50))");
        }
        if (oldVersion < 12) {
            // 给 d_music 加“是否喜欢”字段（1=喜欢 0=不喜欢）
            try {
                db.execSQL("ALTER TABLE d_music ADD COLUMN love VARCHAR(50) DEFAULT '0'");
            } catch (Exception ignored) { }
        }
        if (oldVersion < 13) {
            // 新增歌单-歌曲关系表（一首歌可进多个歌单）
            db.execSQL("create table if not exists d_sheet_music(sheet_id varchar(50)," +
                    "music_id varchar(50), primary key(sheet_id, music_id))");
        }
        if (oldVersion < 14) {
            // 给 d_music 加“是否删除”字段（1=已删除 0=未删除）
            try {
                db.execSQL("ALTER TABLE d_music ADD COLUMN deleted VARCHAR(50) DEFAULT '0'");
            } catch (Exception ignored) { }
        }
        if (oldVersion < 15) {
            // d_music 加播放量，新建播放历史表
            try {
                db.execSQL("ALTER TABLE d_music ADD COLUMN play_count INTEGER DEFAULT 0");
            } catch (Exception ignored) { }
            db.execSQL("create table if not exists d_play_history(id integer primary key autoincrement," +
                    "user_id varchar(50), music_id varchar(50))");
        }
        if (oldVersion < 16) {
            // 把用户自己创建的歌单从“推荐歌单”里排除（sta 改成 0，只留系统推荐）
            db.execSQL("update d_sheet set sta='0' where create_user_id in (select account from d_user)");
        }
    }
}
