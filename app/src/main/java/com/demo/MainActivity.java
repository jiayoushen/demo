package com.demo;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.demo.adapter.DialogueTextAdapter;
import com.demo.base.BaseActivity;
import com.demo.business.FrameAnimation;
import com.demo.business.Ise;
import com.demo.entity.Grade;
import com.demo.utils.DialogUtils;
import com.demo.utils.L;
import com.demo.utils.permissions.PermissionsActivity;
import com.demo.utils.permissions.PermissionsChecker;
import com.github.lzyzsd.circleprogress.DonutProgress;

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
    @BindView(R.id.r_content)
    TextView r_content;
    @BindView(R.id.l_content)
    TextView l_content;
    @BindView(R.id.bt_start)
    Button start;
    @BindView(R.id.cb_sub_title)
    CheckBox subtitle;
    @BindView(R.id.donut_progress)
    DonutProgress donutProgress;

    // 权限检测器
    private PermissionsChecker mPermissionsChecker;
    private FrameAnimation fa;
    private Ise ise;
    // 用于播放录音文件
    private MediaPlayer mPlayer;
    // 回调回主线程使用
    private Handler mHandler;

    private int[] resource = {R.mipmap.role_travel1_1, R.mipmap.role_travel1_3};
    // 模拟5句对话 机器→人→机器→人
    private String[] dialogue_resource = {"Is this where I check in for flight number 117?", "Yes,this is.Would you like to check in now?",
            "Yes,of course.", "May I see you ticket and passport,please?", "Sure!Here they are."};
    private int[] dialogue_resource_path = {R.raw.dialogue_2, R.raw.dialogue_4};
    private int[] dialogue_resource_time = {5, 1, 3};
    // 循环次数的标志
    private int dr = 0;
    /**
     * 播放顺序
     * true：人→机器→人
     * false：机器→人→机器
     */
    private Boolean order = false;
    // 总分界面
    private EvalTotalDialog dialog;
    // 加载界面
    private Dialog mWeiboDialog;
    // 显示全段文本界面
    private PopupWindow popup;
    // 存储总分界面所需的对象
    private List<Grade> grades;
    // 录音储存位置
    private String save_path;
    /**
     * 字幕显示状态（Subtitle display status）
     */
    private boolean sds;
    /**
     * 对话的状态（State of dialogue）
     **/
    private boolean sod;
    // 用于判断双击
    private long[] mHits = new long[2];
    private DialogueTextAdapter dtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPermissionsChecker = new PermissionsChecker(this);

        grades = new ArrayList<Grade>();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                if (msg.what == TTS_WHAT) {
                    record_eval((String) msg.obj);
                }
                super.handleMessage(msg);
            }
        };

        subtitle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showSubtitle(isChecked);
                sds = isChecked;
            }
        });

        setDate();
    }

    private void setDate() {
        int j = 0;
        int k = 0;
        for (int i = 0; i < dialogue_resource.length; i++) {
            //            if (i % 2 == 1) {
            //                // 人
            //                grades.add(new Grade(1, dialogue_resource[i], null, 0.0f, 3));
            //            } else {
            //                // 机器
            //                grades.add(new Grade(2, dialogue_resource[i], ("android.resource://" + getPackageName() + "/" + dialogue_resource_path[j]), 0.0f, 0));
            //                j++;
            //            }
            if (i % 2 == 1) {
                // 机器
                grades.add(new Grade(2, dialogue_resource[i], ("android.resource://" + getPackageName() + "/" + dialogue_resource_path[j]), 0.0f, 0));
                j++;
            } else {
                // 人
                grades.add(new Grade(1, dialogue_resource[i], null, 0.0f, dialogue_resource_time[k]));
                k++;
            }
        }
        L.i("setDate() = " + grades.toString());
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
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        if (ise != null) {
            ise.destroy();
            ise = null;
        }
        super.onDestroy();
    }

    @OnClick({R.id.bt_start, R.id.rl_scene})
    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.bt_start:
                if (sod == false) {
                    sod = true;
                    mWeiboDialog = DialogUtils.create().createLoadingDialog(this, new DialogUtils.onTimerFinishListener() {
                        @Override
                        public void OnTimerFinish() {
                            DialogUtils.create().closeDialog(mWeiboDialog);
                            setScene(R.mipmap.scene_check_in);
                            if (grades.get(0).getSort() == 1) {
                                oSpeak(true, grades.get(dr).getContent_text());
                            } else {
                                oSpeak(false, grades.get(dr).getContent_text());
                            }
                        }
                    });
                }
                break;
            case R.id.rl_scene:
                // 对话开始才让弹出对话文本？
                if (sod) {
                    // 专业的双击算法
                    System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                    mHits[mHits.length - 1] = SystemClock.uptimeMillis();//获取手机开机时间
                    if (mHits[mHits.length - 1] - mHits[0] < 500) {
                        /**双击的业务逻辑*/
                        showDialogueText();
                    }
                }
                break;
        }
    }

    /**
     * 播放对话文件
     * 人→机器→人
     *
     * @param o1
     * @param speak 用户读的文本
     */
    private void oSpeak(Boolean o1, String speak) {
        /**
         * o1为true时，人→机器→人
         * 将第一个人分离出来，dr+=1，之后 机器→人 作为一组对话
         */
        if (o1) {
            dr += 1;
            order = o1;
            record_eval(speak);
            Glide.with(getApplicationContext()).load(resource[0]).asBitmap().override(600, 600).into(gif);
        } else {
            order = o1;
            speak(grades.get(dr).getContent_text(), grades.get(dr).getVoice_path());
        }
    }

    /**
     * 播放对话文件
     * 机器→人→机器→人
     *
     * @param speak 用户读的文本
     * @param uri   对话的路径
     */
    private void speak(final String speak, final String uri) {
        try {
            if (mPlayer != null) {
                mPlayer.reset();
                mPlayer.release();
                mPlayer = null;
            }
            mPlayer = new MediaPlayer();
            mPlayer.reset();
            Uri mUri = Uri.parse(uri);
            mPlayer.setDataSource(MainActivity.this, mUri);
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();

                    int time = (int) (mp.getDuration() / 1000);
                    grades.get(dr).setTime(time);

                    // 对话资源开始的时候显示动画
                    setContent(r_content, speak);
                    List<Bitmap> bitmaps = new ArrayList<>();
                    int delayTime = 500;
                    setGif(bitmaps, delayTime, resource, gif);
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    mPlayer = null;

                    // 对话资源结束的时候停止动画
                    Glide.with(getApplicationContext()).load(resource[0]).asBitmap().override(600, 600).into(gif);

                    Message msg = Message.obtain();
                    msg.what = TTS_WHAT;
                    if ((dr + 1) < grades.size()) {
                        msg.obj = grades.get(dr + 1).getContent_text();
                    }
                    mHandler.sendMessageDelayed(msg, 100);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示或隐藏对话文本
     *
     * @param show
     */
    private void showSubtitle(boolean show) {
        if (show) {
            if (sod) {
                if (mPlayer != null && mPlayer.isPlaying()) {
                    r_content.setVisibility(View.VISIBLE);
                } else {
                    l_content.setVisibility(View.VISIBLE);
                }
            }
        } else {
            l_content.setVisibility(View.GONE);
            r_content.setVisibility(View.GONE);
        }
    }

    /**
     * 显示状态下对话文本的展示流程
     *
     * @param content
     * @param text
     */
    private void setContent(@Nullable TextView content, @Nullable String text) {
        r_content.setVisibility(View.GONE);
        l_content.setVisibility(View.GONE);
        if (content != null) {
            if (sds) {
                if (content.getVisibility() == View.GONE) {
                    content.setVisibility(View.VISIBLE);
                }
            }
            content.setText(text);
        }
    }

    /**
     * 调用科大讯飞录音测评
     *
     * @param text
     */
    private void record_eval(String text) {
        L.i("record_eval = " + text);
        // 对话为单数的时候
        if (TextUtils.isEmpty(text)) {
            dr = 0;
            setContent(null, text);

            showEvalTotal();

            sod = false;
            return;
        }
        setContent(l_content, text);
        ise = Ise.createIse(this);
        save_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/" + SystemClock.currentThreadTimeMillis() + ".wav";
        // 由sec转换成ms
        int temp_time = 0;
        if (order) {
            temp_time = (grades.get(0).getTime()) * 1000;
        } else {
            temp_time = (grades.get(dr + 1).getTime()) * 1000;
        }
        final int timeout = temp_time;
        L.i("timeout = " + timeout);
        ise.evaluation(save_path, text, timeout, new Ise.FSMListener() {
            @Override
            public void onFiniteStateMachine(int result_state, float score, int time) {
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
                if (order) {
                    grades.get(0).setVoice_path(save_path);
                    grades.get(0).setScore(score);
                } else {
                    grades.get(dr + 1).setVoice_path(save_path);
                    grades.get(dr + 1).setScore(score);
                }
                setContent(null, null);
            }
        }, new Ise.Step5Listener() {
            @Override
            public void onAfterStep5() {
                // order 为true时 是人→机器→人的顺序
                if (order) {
                    order = false;
                    speak(grades.get(dr).getContent_text(), grades.get(dr).getVoice_path());
                } else {
                    dr += 2;
                    if (dr >= grades.size()) {
                        dr = 0;

                        showEvalTotal();
                        sod = false;
                        return;
                    }
                    speak(grades.get(dr).getContent_text(), grades.get(dr).getVoice_path());
                }
            }
        }, new Ise.RecordBeginListener() {
            @Override
            public void onRecordBegin() {
                // 听的表情
                donutProgress.setMax(timeout);
                donutProgress.setVisibility(View.VISIBLE);
            }
        }, new Ise.RecordOverListener() {
            @Override
            public void onRecordOver() {
                // 结束听的表情0
                donutProgress.setVisibility(View.GONE);
                donutProgress.setProgress(0);
            }
        }, new Ise.TimeChangedListener() {
            @Override
            public void onTimeChanged(int time) {
                donutProgress.setProgress(time);
            }
        });
    }

    /**
     * 打开总分界面的方法
     */
    private void showEvalTotal() {
        if (dialog == null) {
            dialog = new EvalTotalDialog(this);
            dialog.setList((ArrayList) grades);
            L.i("showEvalTotal = " + grades.toString());
        }
        if (!dialog.isShowing()) {
            dialog.show();
            WindowManager windowManager = getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.width = (int) (display.getWidth() * 0.8); //设置宽度
            dialog.getWindow().setAttributes(lp);
        }
        //        grades.clear();
    }

    /**
     * 设置GIF
     *
     * @param bitmaps   用于制作GIF的bitmap集合
     * @param delayTime GIF第一帧到尾帧的总时长
     * @param resource  资源数组
     * @param gif       用于显示GIF的ImageView
     */
    private void setGif(List<Bitmap> bitmaps, int delayTime, int[] resource, ImageView gif) {
        for (int i = 0; i < resource.length; i++) {
            Bitmap bitmap = decodeResource(getResources(), resource[i]);
            bitmaps.add(bitmap);
        }
        fa = FrameAnimation.createFrameAnimation(new FrameAnimation.OnGifPlayOverListener() {
            @Override
            public void OnGifPlayOver() {
            }
        });
        fa.composeGif(getApplicationContext(), bitmaps, delayTime, isFinishing(), gif);
    }

    /**
     * 设置场景
     *
     * @param scene_id 场景对应的资源ID
     */
    private void setScene(int scene_id) {
        scene.setBackgroundResource(scene_id);
    }

    /**
     * 因为目前我们只有一套资源文件，全都放在hdpi下面，这样如果是遇到高密度手机， 系统会按照
     * scale = (float) targetDensity / density 把图片放到几倍，这样会使得在高密度手机上经常会发生OOM。
     * 这个方法用来解决在如果密度大于hdpi（240）的手机上，decode资源文件被放大scale，内容浪费的问题。
     *
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
        } else {
            bitmap = BitmapFactory.decodeResource(resources, id);
        }
        return bitmap;
    }

    private void showDialogueText() {
        // 构建一个popupwindow的布局
        View popupView = MainActivity.this.getLayoutInflater().inflate(R.layout.dialogue_text, null);

        ListView lsvMore = (ListView) popupView.findViewById(R.id.lsvMore);

        dtAdapter = new DialogueTextAdapter((ArrayList<Grade>) grades, this);
        lsvMore.setAdapter(dtAdapter);

        // 创建PopupWindow对象，指定宽度和高度
        PopupWindow window = new PopupWindow(popupView, 600, 600);
        // 设置动画
        window.setAnimationStyle(R.style.popup_window_anim);
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F8F8F8")));
        // 设置可以获取焦点
        window.setFocusable(true);
        // 设置可以触摸弹出框以外的区域
        window.setOutsideTouchable(true);
        // 更新popupwindow的状态
        window.update();
        // 以下拉的方式显示，并且可以设置显示的位置
        window.showAtLocation(scene, Gravity.TOP, 0, 0);
    }
}
