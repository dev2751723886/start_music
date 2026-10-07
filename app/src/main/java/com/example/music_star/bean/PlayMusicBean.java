package com.example.music_star.bean;

public class PlayMusicBean {
    private String id;

    private String name;

    private String img;

    private String create_user_id;

    private String sta;

    private String create_time;

    public PlayMusicBean() {
    }

    public PlayMusicBean(String id) {
        this.id = id;
    }

    public PlayMusicBean(String id, String name, String img, String create_user_id, String sta, String create_time) {
        this.id = id;
        this.name = name;
        this.img = img;
        this.create_user_id = create_user_id;
        this.sta = sta;
        this.create_time = create_time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getCreate_user_id() {
        return create_user_id;
    }

    public void setCreate_user_id(String create_user_id) {
        this.create_user_id = create_user_id;
    }

    public String getSta() {
        return sta;
    }

    public void setSta(String sta) {
        this.sta = sta;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
}
