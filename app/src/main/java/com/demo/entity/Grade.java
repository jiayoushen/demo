package com.demo.entity;

import android.support.annotation.Nullable;

/**
 * Created by Administrator on 2017/8/29.
 */

public class Grade {
    // 类别：1.用户 2.机器
    private int sort;
    // 机器语音带文字
    private String content_text;
    // 语音路径
    private String voice_path;
    // 用户语音带评分
    private float score;
    // 语音时长
    private int time;
    // 语音播放状态
    private int flag;

    public Grade(int sort, @Nullable String content_text, String voice_path, float score, int time) {
        this.sort = sort;
        this.content_text = content_text;
        this.voice_path = voice_path;
        this.score = score;
        this.time = time;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getContent_text() {
        return content_text;
    }

    public void setContent_text(String content_text) {
        this.content_text = content_text;
    }

    public String getVoice_path() {
        return voice_path;
    }

    public void setVoice_path(String voice_path) {
        this.voice_path = voice_path;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "[sort = " + sort + ";content_text = " + content_text + ";voice_path = " + voice_path + ",score = " + score + ",time = " + time + "]";
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
