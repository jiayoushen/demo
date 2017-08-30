package com.demo.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.demo.R;
import com.demo.entity.Grade;

import java.util.ArrayList;
import java.util.List;

import static com.demo.base.Constants.EVALTOTAL_VOICE_START;
import static com.demo.base.Constants.EVALTOTAL_VOICE_STOP;

/**
 * Created by Administrator on 2017/8/29.
 */

public class EvalListAdapter extends BaseAdapter {
    private List<Grade> grades;
    private Context context;
    private LayoutInflater inflater;
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private int mMinItemWidth; //最小的item宽度
    private int mMaxItemWidth; //最大的item宽度

    //    ViewHolder holder = null;
    // 正在播放的路径的位置
    private int playing_position;

    private int[] resource = {R.mipmap.chatto_voice_playing_f1, R.mipmap.chatto_voice_playing_f2, R.mipmap.chatto_voice_playing_f3};

    public EvalListAdapter(ArrayList<Grade> grades, Context context, onChatcontentListener chatcontentListener) {
        this.grades = (ArrayList<Grade>) grades.clone();
        this.context = context;
        this.chatcontentListener = chatcontentListener;
        inflater = LayoutInflater.from(context);

        //获取屏幕的宽度
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mMaxItemWidth = (int) (outMetrics.widthPixels * 0.7f);
        mMinItemWidth = (int) (outMetrics.widthPixels * 0.15f);
    }

    public void setList(ArrayList<Grade> grades) {
        this.grades = (ArrayList<Grade>) grades.clone();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return grades.size();
    }

    @Override
    public Object getItem(int position) {
        return grades.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Grade entity = grades.get(position);
        final int sort = entity.getSort();
        //        ViewHolder holder = null;
        //        if (convertView == null) {
        convertView = inflater.inflate(R.layout.newresult_list_item, null);
        //            holder = new ViewHolder();
        //        ImageView userhead = (ImageView) convertView.findViewById(R.id.iv_userhead);
        TextView content_text = (TextView) convertView.findViewById(R.id.tv_chatcontent_text);
        RelativeLayout chatcontent = (RelativeLayout) convertView.findViewById(R.id.tv_chatcontent);
        TextView voice_time = (TextView) convertView.findViewById(R.id.tv_time);
        TextView score = (TextView) convertView.findViewById(R.id.tv_score);
        TextView username = (TextView) convertView.findViewById(R.id.tv_username);
        ImageView chatcontent_view = (ImageView) convertView.findViewById(R.id.tv_chatcontent_view);
        //            convertView.setTag(holder);
        //        } else {
        //            holder = (ViewHolder) convertView.getTag();
        //        }
        switch (sort) {
            // 用户
            case 1:
                score.setVisibility(View.VISIBLE);
                content_text.setVisibility(View.GONE);
                username.setText(context.getString(R.string.newresult_list_item_username_self));
                score.setText(String.format(context.getResources().getString(R.string.eval_score), entity.getScore()));
                break;
            // 机器
            case 2:
                score.setVisibility(View.GONE);
                content_text.setVisibility(View.VISIBLE);
                username.setText(context.getString(R.string.newresult_list_item_username_robat));
                content_text.setText(entity.getContent_text());
                break;
        }
        //        holder.chatcontent.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.chatto_voice_playing, 0, 0, 0);
        ViewGroup.LayoutParams lp = chatcontent.getLayoutParams();
        lp.width = (int) (mMinItemWidth + (mMaxItemWidth / 60f) * entity.getTime());
        voice_time.setText(Math.round(entity.getTime()) + "\"");
        if (entity.getFlag() == EVALTOTAL_VOICE_STOP) {
            chatcontent_view.setImageResource(R.mipmap.chatto_voice_playing);
        } else if (entity.getFlag() == EVALTOTAL_VOICE_START) {
            chatcontent_view.setImageResource(R.drawable.play_voice);
            AnimationDrawable animationDrawable = (AnimationDrawable) chatcontent_view.getDrawable();
            animationDrawable.start();
        }
        chatcontent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                entity.setFlag(EVALTOTAL_VOICE_START);
                playMusic(entity, entity.getVoice_path(), sort, position);
            }
        });
        return convertView;
    }

    //    class ViewHolder {
    //        RelativeLayout chatcontent;
    //        ImageView userhead, chatcontent_view;
    //        TextView content_text, voice_time, score, username;//chatcontent
    //    }

    /**
     * @param name 语音路径
     * @param sort 类别：1.用户 2.机器
     * @Description
     */
    private void playMusic(final Grade entity, String name, int sort, final int position) {
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                if (playing_position != position) {
                    grades.get(playing_position).setFlag(EVALTOTAL_VOICE_STOP);
                    if (chatcontentListener != null) {
                        chatcontentListener.onClickListener(EVALTOTAL_VOICE_STOP, playing_position);
                    }
                }
            }
            if (chatcontentListener != null) {
                chatcontentListener.onClickListener(EVALTOTAL_VOICE_START, position);
            }
            mMediaPlayer.reset();
            if (sort == 2) {
                Uri mUri = Uri.parse(name);
                mMediaPlayer.setDataSource(context, mUri);
            } else {
                mMediaPlayer.setDataSource(name);
            }
            playing_position = position;
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    entity.setFlag(EVALTOTAL_VOICE_STOP);
                    if (chatcontentListener != null) {
                        chatcontentListener.onClickListener(EVALTOTAL_VOICE_STOP, position);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * * 局部刷新
     * * @param view
     */
    public void updateView(View view) {
        if (view == null) {
            return;
        }
        //从view中取得holder
        //        ViewHolder holder = (ViewHolder) view.getTag();
        ImageView chatcontent_view = (ImageView) view.findViewById(R.id.tv_chatcontent_view);
        chatcontent_view.setImageResource(R.drawable.play_voice);
        AnimationDrawable animationDrawable = (AnimationDrawable) chatcontent_view.getDrawable();
        animationDrawable.start();
    }

    /**
     * * 局部刷新
     */
    public void updateView2(View view) {
        if (view == null) {
            return;
        }
        //从view中取得holder
        //        ViewHolder holder = (ViewHolder) view.getTag();
        ImageView chatcontent_view = (ImageView) view.findViewById(R.id.tv_chatcontent_view);
        //        AnimationDrawable animationDrawable = (AnimationDrawable) chatcontent_view.getDrawable();
        //        animationDrawable.stop();
        chatcontent_view.setImageResource(R.mipmap.chatto_voice_playing);
    }

    public interface onChatcontentListener {
        void onClickListener(int flag, int position);
    }

    private onChatcontentListener chatcontentListener;
}
