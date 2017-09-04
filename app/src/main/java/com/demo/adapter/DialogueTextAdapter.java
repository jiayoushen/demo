package com.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.demo.R;
import com.demo.entity.Grade;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/4.
 */

public class DialogueTextAdapter extends BaseAdapter {
    private List<Grade> grades;
    private Context context;
    private LayoutInflater inflater;

    public DialogueTextAdapter(ArrayList<Grade> grades, Context context) {
        this.grades = (ArrayList<Grade>) grades.clone();
        this.context = context;
        inflater = LayoutInflater.from(context);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        Grade entity = grades.get(position);
        int sort = entity.getSort();
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.dialogue_text_item, null);
            holder.username = (TextView) convertView.findViewById(R.id.dti_username);
            holder.content = (TextView) convertView.findViewById(R.id.dti_content);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        switch (sort) {
            // 用户
            case 1:
                holder.username.setText(context.getString(R.string.newresult_list_item_username_self));
                break;
            // 机器
            case 2:
                holder.username.setText(context.getString(R.string.newresult_list_item_username_robat));
                break;
        }
        holder.content.setText(entity.getContent_text());
        return convertView;
    }

    class ViewHolder {
        TextView username, content;
    }
}
