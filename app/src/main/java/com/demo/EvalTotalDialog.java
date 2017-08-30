package com.demo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.demo.adapter.EvalListAdapter;
import com.demo.entity.Grade;
import com.demo.utils.L;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.demo.base.Constants.EVALTOTAL_VOICE_START;
import static com.demo.base.Constants.EVALTOTAL_VOICE_STOP;

/**
 * Created by Administrator on 2017/8/28.
 */

public class EvalTotalDialog extends Dialog {
    @BindView(R.id.tv_eval_total)
    TextView eval_total;
    @BindView(R.id.lv_eval_list)
    ListView eval_list;

    private ArrayList<Grade> grades;
    private Context ctx;
    private EvalListAdapter adapter;

    public EvalTotalDialog(@NonNull Context context) {
        super(context);
        this.ctx = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newresult);
        ButterKnife.bind(this);
        initWidget();
    }

    private void initWidget() {
        float total = 0.0f;
        int sum = 0;
        for (Grade grade : grades) {
            if (grade.getSort() == 1) {
                total += grade.getScore();
                sum += 1;
            }
        }
        float average_score = (total / (float) sum);
        String testStr = ctx.getResources().getString(R.string.eval_total);
        String result = String.format(testStr, average_score);
        eval_total.setText(result);

        adapter = new EvalListAdapter(grades, ctx, new EvalListAdapter.onChatcontentListener() {
            @Override
            public void onClickListener(int flag, int position) {
                updateView(flag, position);
            }
        });
        eval_list.setAdapter(adapter);
    }

    public void setList(ArrayList<Grade> grades) {
        this.grades = grades;
    }

    private void updateView(int flag, int itemIndex) {
        L.i("updateView : "+flag);
        //得到第一个可显示控件的位置，
        int visiblePosition = eval_list.getFirstVisiblePosition();
        switch (flag) {
            case EVALTOTAL_VOICE_START:
                //只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
                L.i("updateView : " + itemIndex + " - " + visiblePosition);
                if (itemIndex - visiblePosition >= 0) {
                    //得到要更新的item的view
                    View view = eval_list.getChildAt(itemIndex - visiblePosition);
                    //调用adapter更新界面
                    adapter.updateView(view);
                }
                break;
            case EVALTOTAL_VOICE_STOP:
                //得到要更新的item的view
                View view = eval_list.getChildAt(itemIndex);
                //调用adapter更新界面
                adapter.updateView2(view);
                break;
        }
    }
}
