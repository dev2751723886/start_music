package com.example.music_star.bean;

// 用户信息（只放「我的」页需要的字段，供 SqliteT 反射映射）
public class UserBean {
    private String account;
    private String nickname;
    private String img;

    public UserBean() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
