package com.demo;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.demo.base.BaseActivity;
import com.demo.business.FrameAnimation;
import com.demo.business.Ise;
import com.demo.utils.L;
import com.demo.utils.permissions.PermissionsActivity;
import com.demo.utils.permissions.PermissionsChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.demo.base.Constants.LOW_SCORE;
import static com.demo.base.Constants.NOT_STANDARD_ANSWER;
import static com.demo.base.Constants.NO_ANSWER;
import static com.demo.base.Constants.PERMISSIONS;
import static com.demo.base.Constants.REQUEST_CODE;
import static com.demo.base.Constants.TTS_WHAT;
import static com.demo.base.Constants.VOICE_ORDER;

public class MainActivity extends BaseActivity {
    @BindView(R.id.rl_scene)
    RelativeLayout scene;
    @BindView(R.id.iv_gif)
    ImageView gif;
    @BindView(R.id.bt_start)
    Button start;

    private PermissionsChecker mPermissionsChecker; // 权限检测器
    private FrameAnimation fa;
    private Ise ise;
    private MediaPlayer mPlayer;
    private Handler mHandler; // 回调回主线程使用

    int[] resource={R.mipmap.role_travel1_1,R.mipmap.role_travel1_3};
    // 模拟5句对话 机器→人→机器→人
    String[] dialogue_resource={"aaa","bbb","ccc","ddd","eee"};
    String[] dialogue_resource_path={"/sdcard/msc/tts1.wav","/sdcard/msc/tts3.wav", "/sdcard/msc/tts5.wav"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPermissionsChecker = new PermissionsChecker(this);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                if (msg.what == TTS_WHAT) {
                    record_eval((String) msg.obj);
                }
                super.handleMessage(msg);
            }
        };
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
        if(mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        super.onDestroy();
    }

    @OnClick(R.id.bt_start)
    public void OnClick(){
        setScene(R.mipmap.scene_cabin);

        int j=0;
        for (int i=0; i<dialogue_resource.length;) {
//            speak("/sdcard/msc/tts.wav",dialogue_resource[i]);
            speak(dialogue_resource[i],dialogue_resource_path[j]);
            i+=2;
            j+=1;
        }
    }

    private void record_eval(String text) {
        L.i("record_eval = "+text);
        ise = Ise.createIse(this);
//        String text = "hellow world!";
        ise.evaluation(text, new Ise.FSMListener() {
            @Override
            public void onFiniteStateMachine(int result_state) {
                L.i("result_state = " + result_state);
                switch (result_state) {
                    case NO_ANSWER:
                        break;
                    case LOW_SCORE:
                        break;
                    case NOT_STANDARD_ANSWER:
                        break;
                    case VOICE_ORDER:
                        break;
                }
            }
        });
    }

    /**
     * 播放对话文件
     * @param speak         第一个人
     * @param speak_path    对话的路径
     */
    private void speak(final String speak,String speak_path) {
        try {
            if (mPlayer != null) {
                mPlayer.reset();
                mPlayer.release();
                mPlayer = null;
            }
            mPlayer = new MediaPlayer();
            mPlayer.reset();
            L.i("speak = "+speak_path);
            mPlayer.setDataSource(speak_path);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();

                    // 对话资源开始的时候显示动画
                    List<Bitmap> bitmaps = new ArrayList<>();
                    int delayTime = 1000;
                    setGif(bitmaps, delayTime, resource, gif);
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    mPlayer = null;

                    // 对话资源结束的时候停止动画
                    Glide.with(MainActivity.this).load(resource[0]).asBitmap().into(gif);

                    Message msg = Message.obtain();
                    msg.what = TTS_WHAT;
                    msg.obj = speak;
                    L.i("onCompletion = "+speak);
                    mHandler.sendMessageDelayed(msg,100);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置GIF
     * @param bitmaps   用于制作GIF的bitmap集合
     * @param delayTime GIF第一帧到尾帧的总时长
     * @param resource  资源数组
     * @param gif       用于显示GIF的ImageView
     */
    private void setGif(List<Bitmap> bitmaps, int delayTime, int[] resource, ImageView gif) {
        for(int i=0;i<resource.length;i++){
            Bitmap bitmap = decodeResource(getResources(),resource[i]);
            bitmaps.add(bitmap);
        }
        fa = FrameAnimation.createFrameAnimation(new FrameAnimation.OnGifPlayOverListener() {
            @Override
            public void OnGifPlayOver() {
            }
        });
        fa.composeGif(this,bitmaps,delayTime,isFinishing(),gif);
    }

    /**
     * 设置场景
     * @param scene_id  场景对应的资源ID
     */
    private void setScene(int scene_id){
        scene.setBackgroundResource(scene_id);
    }

    /**
     * 因为目前我们只有一套资源文件，全都放在hdpi下面，这样如果是遇到高密度手机， 系统会按照
     * scale = (float) targetDensity / density 把图片放到几倍，这样会使得在高密度手机上经常会发生OOM。
     * 这个方法用来解决在如果密度大于hdpi（240）的手机上，decode资源文件被放大scale，内容浪费的问题。
     * @param resources
     * @param id
     * @return
     */
    public Bitmap decodeResource(Resources resources, int id) {
        int densityDpi = resources.getDisplayMetrics().densityDpi;
        Bitmap bitmap;
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ALPHA_8;
        if (densityDpi > DisplayMetrics.DENSITY_HIGH) {
            opts.inTargetDensity = value.density;
            bitmap = BitmapFactory.decodeResource(resources, id, opts);
        }else{
            bitmap = BitmapFactory.decodeResource(resources, id);
        }
        return bitmap;
    }
}
