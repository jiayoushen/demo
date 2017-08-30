package com.demo.base;

import android.Manifest;

/**
 * Created by Administrator on 2017/8/17.
 */

public interface Constants {
    // FSM机制的常量
    int NO_ANSWER = 1;
    int LOW_SCORE = 2;
    int NOT_STANDARD_ANSWER = 3;
    int VOICE_ORDER = 4;

    // 动画逻辑的handler的what
    int FRAMEANIMATION_WHAT = 100;
    // TTS的handler的what
    int TTS_WHAT = 101;
    // ISE的handler的what
    int ISE_WHAT = 102;
    // ISE的handler的Delayed
    int ISE_DELAYTIME = 1000;

    // 总分界面播放与结束语音的标识
    int EVALTOTAL_VOICE_START = 103;
    int EVALTOTAL_VOICE_STOP = 104;

    /**
     * 动态权限相关
     */
    int REQUEST_CODE = 0; // 请求码
    // 所需的全部权限
    String[] PERMISSIONS = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
}
