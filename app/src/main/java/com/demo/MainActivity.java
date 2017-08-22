package com.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.demo.base.BaseActivity;
import com.demo.business.FrameAnimation;
import com.demo.utils.permissions.PermissionsActivity;
import com.demo.utils.permissions.PermissionsChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.demo.base.Constants.PERMISSIONS;
import static com.demo.base.Constants.REQUEST_CODE;

public class MainActivity extends BaseActivity {
    @BindView(R.id.rl_scene)
    RelativeLayout scene;
    @BindView(R.id.iv_gif)
    ImageView gif;
    @BindView(R.id.bt_start)
    Button start;

    private PermissionsChecker mPermissionsChecker; // 权限检测器
    private FrameAnimation fa;

    private MediaPlayer mPlayer = new MediaPlayer();;

    int[] resource={R.mipmap.role_travel1_1,R.mipmap.role_travel1_2,R.mipmap.role_travel1_3,R.mipmap.role_travel1_4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPermissionsChecker = new PermissionsChecker(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if(mPlayer != null)
            mPlayer.release();
        super.onDestroy();
    }

    @OnClick(R.id.bt_start)
    public void OnClick(){
        try {
            mPlayer.setDataSource("/sdcard/msc/tts.wav");
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        scene.setBackgroundResource(R.mipmap.scene_cabin);
        List<Bitmap> bitmaps = new ArrayList<>();
        int delayTime = 1000;
        for(int i=0;i<resource.length;i++){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),resource[i]);
            bitmaps.add(bitmap);
        }
        fa = FrameAnimation.createFrameAnimation(new FrameAnimation.OnGifPlayOverListener() {
            @Override
            public void OnGifPlayOver() {
            }
        });
        fa.composeGif(this,bitmaps,delayTime,isFinishing(),gif);
    }
}
