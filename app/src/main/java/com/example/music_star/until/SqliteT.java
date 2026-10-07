package com.example.music_star.until;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//当前类是封装查询重复利用的东西
public class SqliteT {
    public static SQLiteDatabase db=DBuntil.con;

    /**
     *
     * 查询多个
     * @param sql
     * @param clazz
     * @param data
     * @return
     * @param <T>
     */
    public static <T> List<T> query(String sql, Class<T> clazz, String ...data) {
        //拿到结果集
        Cursor result = db.rawQuery(sql, data);
        //新建结果列表
        List<T> list=new ArrayList<>();
        //遍历结果集
        while(result.moveToNext()){

            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);//创建构造方法
                T t = constructor.newInstance();
                Field[] z=clazz.getDeclaredFields();//获取类里面的属性
                for(Field field:z){
                field.setAccessible(true);//设置公开
                 String filedName= field.getName();
                 int powIndex = result.getColumnIndex(filedName);
                 String value = result.getString(powIndex);//查询到这个数据库
                 field.set(t,value);
                }
                list.add(t);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }

        return list;
    }



    //查询单个

        /**
         *
         * @param sql
         * @param clazz
         * @param data
         * @return
         * @param <T>
         */
        public static <T> T queryOne(String sql, Class<T> clazz, String ...data) {

            Cursor result = db.rawQuery(sql, data);
            if (result.moveToNext()){

                try {
                    Constructor<T> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);//创建构造方法
                    T t = constructor.newInstance();
                    Field[] z=clazz.getDeclaredFields();//获取类里面的属性
                    for(Field field:z){
                        field.setAccessible(true);//设置公开
                        String filedName= field.getName();
                        int powIndex = result.getColumnIndex(filedName);
                        String value = result.getString(powIndex);//查询到这个数据库
                        field.set(t,value);
                    }
                    return t;
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }

            return null;
        }


        //更改查询方法
        public static int updata(String sql,  String ...data) {
            try{
                db.execSQL(sql,data);
                return 1;
            }catch (Exception e){
                return 0;
            }

        }




    /**
     *
     * 查询多个
     * @param sql
     * @param clazz
     * @param data
     * @return
     * @param <T>
     */
    public static <T> List<Map<String,String>>queryMapList(String sql, Class<T> clazz, String ...data) {

        Cursor result = db.rawQuery(sql, data);
        List<Map<String,String>> list=new ArrayList<>();

        while(result.moveToNext()){

                Map<String, String> t = new HashMap<>();
                Field[] z = clazz.getDeclaredFields();//获取类里面的属性
                for (Field field : z) {
                    field.setAccessible(true);//设置公开
                    String filedName = field.getName();

                    int powIndex = result.getColumnIndex(filedName);
                    String value = result.getString(powIndex);//查询到这个数据库
                    t.put(filedName, value);
                }
                list.add(t);
        }

        return list;
    }



    /**
     *
     * 查询多个
     * @param sql
     * @param clazz
     * @param data
     * @return
     * @param <T>
     */
    public static <T> Map<String,String>queryOneMap(String sql, Class<T> clazz, String ...data) {

        Cursor result = db.rawQuery(sql, data);

        while(result.moveToNext()){

            Map<String, String> t = new HashMap<>();
            Field[] z = clazz.getDeclaredFields();//获取类里面的属性
            for (Field field : z) {
                field.setAccessible(true);//设置公开
                String filedName = field.getName();

                int powIndex = result.getColumnIndex(filedName);
                String value = result.getString(powIndex);//查询到这个数据库
                t.put(filedName, value);
            }
         return t;
        }

        return null;
    }

}

