package com.demo.business;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.demo.R;
import com.demo.result.Result;
import com.demo.result.xml.XmlResultParser;
import com.demo.utils.L;
import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;
import static com.demo.base.Constants.ISE_DELAYTIME;
import static com.demo.base.Constants.ISE_WHAT;
import static com.demo.base.Constants.LOW_SCORE;
import static com.demo.base.Constants.NOT_STANDARD_ANSWER;
import static com.demo.base.Constants.NO_ANSWER;
import static com.demo.base.Constants.VOICE_ORDER;

/**
 * Created by Administrator on 2017/8/16.
 */

public class Ise {
    private final static String PREFER_NAME = "ise_settings";
    private static Ise d = null;
    float score;
    private SpeechEvaluator mIse;
    // 评测语种
    private String language;
    // 评测题型
    private String category;
    // 结果等级
    private String result_level;
    private Toast mToast;
    private Context ctx;
    private String mLastResult;
    private FSMListener fsmListener;
    private Handler mHandler; // 回调回主线程使用
    // 录音储存位置
    private String save_path;
    // 用于记录录音时长
    private long startVoiceT, endVoiceT;
    private RecordBeginListener recordBeginListener;
    private RecordOverListener recordOverListener;
    private TimeChangedListener timeChangedListener;
    private Step5Listener step5Listener;
    // 评测监听接口
    private EvaluatorListener mEvaluatorListener = new EvaluatorListener() {
        @Override
        public void onResult(EvaluatorResult result, boolean isLast) {
            //            L.d("evaluator result :" + isLast);

            if (isLast) {
                StringBuilder builder = new StringBuilder();
                builder.append(result.getResultString());

                if (!TextUtils.isEmpty(builder)) {
                    //                    mResultEditText.setText(builder.toString());
                }
                mLastResult = builder.toString();
                resultParse();
                showTip("评测结束");

            }
        }

        @Override
        public void onError(SpeechError error) {
            if (error != null) {
                showTip("error:" + error.getErrorCode() + "," + error.getErrorDescription());
                //                L.i("error:" + error.getErrorCode() + "," + error.getErrorDescription());
                if (error.getErrorCode() == 11401) {
                    //                    finite_state_machine(NO_ANSWER);
                    if (fsmListener != null)
                        fsmListener.onFiniteStateMachine(NO_ANSWER, 0.0f, 0);
                    // TODO: 关健:录音失败的话,是继续还是停止(目前是继续)
                    mHandler.sendEmptyMessageDelayed(ISE_WHAT, ISE_DELAYTIME);
                }
            } else {
                //                L.d("evaluator over");
            }
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            //            L.d("evaluator begin");
            startVoiceT = System.currentTimeMillis();
            ;
            if (recordBeginListener != null) {
                recordBeginListener.onRecordBegin();
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            //            L.d("evaluator stoped");
            endVoiceT = System.currentTimeMillis();
            if (recordOverListener != null) {
                recordOverListener.onRecordOver();
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前音量：" + volume);
            //            L.d("返回音频数据：" + data.length);
            // 当音量大于5的时候，默认为用户说话了
            if (timeChangedListener != null) {
                timeChangedListener.onTimeChanged((int) (System.currentTimeMillis() - startVoiceT));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    public Ise(Context ctx) {
        this.ctx = ctx;
        mIse = SpeechEvaluator.createEvaluator(ctx, null);
        SharedPreferences pref = ctx.getSharedPreferences(PREFER_NAME, MODE_PRIVATE);
        language = pref.getString(SpeechConstant.LANGUAGE, "en_us");
        category = pref.getString(SpeechConstant.ISE_CATEGORY, "read_sentence");
        mToast = Toast.makeText(ctx, "", Toast.LENGTH_LONG);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                if (msg.what == ISE_WHAT && step5Listener != null) {
                    step5Listener.onAfterStep5();
                }
                super.handleMessage(msg);
            }
        };
    }

    public static Ise createIse(Context var0) {
        synchronized (Ise.class) {
            if (d == null) {
                d = new Ise(var0);
            }
        }
        return d;
    }

    /**
     * 删除文件
     *
     * @param root
     */
    //删除文件
    public static void delFile(String root) {
        File file = new File(root);
        if (file.isFile()) {
            file.delete();
        }
        file.exists();
    }

    public void evaluation(String save_path, String text, int timeout, FSMListener fsmListener, Step5Listener step5Listener, RecordBeginListener recordBeginListener, RecordOverListener recordOverListener
            , TimeChangedListener timeChangedListener) {
        if (null == mIse) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }
        this.save_path = save_path;
        this.fsmListener = fsmListener;
        this.step5Listener = step5Listener;
        this.recordBeginListener = recordBeginListener;
        this.recordOverListener = recordOverListener;
        this.timeChangedListener = timeChangedListener;
        //       String evaText = mEvaTextEditText.getText().toString();
        setParams(timeout);
        mIse.startEvaluating(text, null, mEvaluatorListener);
    }

    private void setParams(int timeout) {
        SharedPreferences pref = ctx.getSharedPreferences(PREFER_NAME, MODE_PRIVATE);
        // 设置评测语言
        language = pref.getString(SpeechConstant.LANGUAGE, "en_us");
        // 设置需要评测的类型
        category = pref.getString(SpeechConstant.ISE_CATEGORY, "read_sentence");
        // 设置结果等级（中文仅支持complete）
        result_level = pref.getString(SpeechConstant.RESULT_LEVEL, "complete");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        String vad_bos = pref.getString(SpeechConstant.VAD_BOS, "5000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        String vad_eos = pref.getString(SpeechConstant.VAD_EOS, "1800");
        // 语音输入超时时间，即用户最多可以连续说多长时间；
        //        String speech_timeout = pref.getString(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1");
        String speech_timeout = pref.getString(SpeechConstant.KEY_SPEECH_TIMEOUT, String.valueOf(timeout));

        mIse.setParameter(SpeechConstant.LANGUAGE, language);
        mIse.setParameter(SpeechConstant.ISE_CATEGORY, category);
        mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mIse.setParameter(SpeechConstant.VAD_BOS, vad_bos);
        mIse.setParameter(SpeechConstant.VAD_EOS, vad_eos);
        mIse.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, speech_timeout);
        mIse.setParameter(SpeechConstant.RESULT_LEVEL, result_level);

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIse.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIse.setParameter(SpeechConstant.ISE_AUDIO_PATH, save_path);
    }

    /**
     * 解析最终结果
     */
    private void resultParse() {
        // 解析最终结果
        if (!TextUtils.isEmpty(mLastResult)) {
            XmlResultParser resultParser = new XmlResultParser();
            Result result = resultParser.parse(mLastResult);

            if (null != result) {
                //                mResultEditText.setText(result.toString());
                L.i("---ljh", result.toString());
                // 根据要求解析result结果
                int result_state = result2Int(result.toString());
                //                finite_state_machine(result_state);
                int time = (int) ((endVoiceT - startVoiceT) / 1000);
                if (time < 1) {
                    time = 1;
                }
                if (fsmListener != null)
                    fsmListener.onFiniteStateMachine(result_state, score, time);
                // 判断在什么情形下才需要保存
                //                if(result_state!=LOW_SCORE||LOW_SCORE!=NOT_STANDARD_ANSWER)
                //                save_or_abandon();

                // 录音完成之后在停顿ISE_DELAYTIME ms,在继续下一步
                mHandler.sendEmptyMessageDelayed(ISE_WHAT, ISE_DELAYTIME);
            } else {
                showTip("解析结果为空");
            }
        }
    }

    /**
     * 将结果解析成状态值
     *
     * @return 状态值
     */
    private int result2Int(String result) {
        //        if(result.contains("检测到乱读"))
        //            return NOT_STANDARD_ANSWER;
        //        else
        score = 0;
        if (result.contains("总分：")) {
            int index = result.indexOf("总分：");
            String substring = result.substring(index + 3, index + 6);
            try {
                score = Float.parseFloat(substring);
            } catch (Exception e) {
                L.i(e.toString());
            }
            if (score < 3.5) {
                return LOW_SCORE;
            }
        }
        return 0;
    }

    /**
     * 解析状态值
     *
     * @param result_state 状态值
     */
    private void finite_state_machine(int result_state) {
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

    private void showTip(String str) {
        if (!TextUtils.isEmpty(str)) {
            mToast.setText(str);
            mToast.show();
        }
    }

    public void destroy() {
        if (null != mIse) {
            mIse.destroy();
            mIse = null;
        }
    }

    /**
     * 由于科大讯飞语音录完就保存，所以放弃保存就是将声音文件从sd卡删除
     */
    private void save_or_abandon() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(ctx);
        normalDialog.setIcon(R.mipmap.ic_launcher);
        normalDialog.setTitle("提示");
        normalDialog.setMessage("保存或放弃?");
        normalDialog.setPositiveButton("保存",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        renameToNewFile("");

                        dialog.dismiss();

                        if (step5Listener != null)
                            step5Listener.onAfterStep5();
                    }
                });
        normalDialog.setNegativeButton("放弃",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delFile(save_path);
                        // android.view.WindowLeaked: Activity  has leaked window com.android.internal.policy.impl.PhoneWindow$DecorView{b3e23088 V.E..... R.....ID 0,0-480,243} that was originally added here
                        // 此处不dismiss的话就会产生上面的问题
                        dialog.dismiss();

                        if (step5Listener != null)
                            step5Listener.onAfterStep5();
                    }
                });
        // 显示
        normalDialog.show();
    }

    /**
     * 确认保存的话，保存的名字？
     *
     * @param dest
     * @return
     */
    private boolean renameToNewFile(String dest) {
        File srcDir = new File(save_path);  //就文件夹路径
        boolean isOk = srcDir.renameTo(new File(dest));  //dest新文件夹路径，通过renameto修改
        L.d("renameToNewFile is OK ? :" + isOk);
        return isOk;
    }

    // FSM回调
    public interface FSMListener {
        void onFiniteStateMachine(int result_state, float score, int time);
    }

    // 开始录音的回调
    public interface RecordBeginListener {
        void onRecordBegin();
    }

    // 结束录音的回调
    public interface RecordOverListener {
        void onRecordOver();
    }

    // 时间改变的回调
    public interface TimeChangedListener {
        void onTimeChanged(int time);
    }

    // 第5步结束的回调
    public interface Step5Listener {
        void onAfterStep5();
    }
}
