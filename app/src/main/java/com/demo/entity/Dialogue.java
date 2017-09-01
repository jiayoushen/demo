package com.demo.entity;

/**
 * Created by Administrator on 2017/8/29.
 */

public class Dialogue {
    // 类别：1.用户 2.机器
    private int sort;
    // 对话文本
    private String text;
    // 机器对应的语音文件路径(raw路径所以是int)
    private String path;
    // 人对应的录音长度
    private int time;

    public Dialogue(int sort, String text, String path, int time) {
        this.sort = sort;
        this.text = text;
        this.path = path;
        this.time = time;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path){
        this.path = path;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "[sort = " + sort + ";text = " + text + ";path = " + path + ",time = " + time + "]";
    }
}
