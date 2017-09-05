package com.demo.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.demo.R;

/**
 * Created by Administrator on 2017/9/4.
 */

public class DialogUtils {
    private static DialogUtils d = null;
    TextView tipTextView;
    private int recLen = 3;

    private onTimerFinishListener timerFinishListener;
    private Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            recLen--;
            tipTextView.setText("" + recLen);
            if (recLen > 0) {
                handler.postDelayed(this, 1000);
            } else {
                recLen = 3;
                if(timerFinishListener!=null){
                    timerFinishListener.OnTimerFinish();
                }
            }
        }
    };

    private DialogUtils(){
    }

    public static DialogUtils create() {
        synchronized (DialogUtils.class) {
            if (d == null) {
                d = new DialogUtils();
            }
        }
        return d;
    }

    public Dialog createLoadingDialog(Context context,onTimerFinishListener Listener) {
        this.timerFinishListener = Listener;
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_loading, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v
                .findViewById(R.id.dialog_loading_view);// 加载布局
        tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        tipTextView.setText(String.valueOf(recLen));// 设置加载信息

        handler.postDelayed(runnable, 1000);

        Dialog loadingDialog = new Dialog(context, R.style.MyDialogStyle);// 创建自定义样式dialog
        loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
        loadingDialog.setCanceledOnTouchOutside(false); // 点击加载框以外的区域
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        /**
         *将显示Dialog的方法封装在这里面
         */
        Window window = loadingDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.CENTER);
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.PopWindowAnimStyle);
        loadingDialog.show();
        return loadingDialog;
    }

    /**
     * 关闭dialog
     * @param mDialogUtils
     */
    public void closeDialog(Dialog mDialogUtils) {
        if (mDialogUtils != null && mDialogUtils.isShowing()) {
            mDialogUtils.dismiss();
        }
    }

    // GIF播放完毕的接口回调
    public interface onTimerFinishListener {
        void OnTimerFinish();
    }
}
