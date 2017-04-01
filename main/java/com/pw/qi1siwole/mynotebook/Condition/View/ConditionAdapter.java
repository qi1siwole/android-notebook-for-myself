package com.pw.qi1siwole.mynotebook.Condition.View;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pw.qi1siwole.mynotebook.Condition.Condition;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionStateGroup;
import com.pw.qi1siwole.mynotebook.R;

import java.util.List;

/**
 * Created by qi1siwole on 2017/3/31.
 */

public class ConditionAdapter extends ArrayAdapter<Condition> {
    private int mResourceId;
    private OnConditionClickListener mOnConditionClickListener;

    public ConditionAdapter(Context context, int resourceId, List<Condition> objects) {
        super(context, resourceId, objects);
        mResourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Condition condition = getItem(position);

        convertView = LayoutInflater.from(getContext()).inflate(mResourceId, null);
        TextView tvFlag = (TextView) convertView.findViewById(R.id.tv_flag);
        TextView tvDesc = (TextView) convertView.findViewById(R.id.tv_desc);

        convertView.setPadding(condition.getLevel() * 100, 0, 0, 0);
        _dealWithFlag(tvFlag, condition);
        _dealWithDesc(tvDesc, condition);

        return convertView;
    }

    /**
     * 判断是否折叠并显示
     * @param view
     * @param condition
     */
    private void _dealWithFlag(TextView view, final Condition condition) {
        if (condition.getType() == Condition.TYPE_GROUP) {
            String flag = condition.isExpand() ? "[-] " : "[+] ";
            view.setText(flag);
            int color = condition.isExpand() ? Color.DKGRAY : Color.BLACK;
            view.setTextColor(color);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnConditionClickListener) {
                        mOnConditionClickListener.onClickExpandFlag(condition);
                    }
                }
            });
        }
        else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 条件的显示
     * @param view
     * @param condition
     */
    private void _dealWithDesc(TextView view, final Condition condition) {
        String text = null;

        if (condition.getType() == Condition.TYPE_GROUP && condition.isExpand()) {
            text = ((ConditionStateGroup)condition.getState()).getOperatorString();
        }
        else {
            text = condition.toString();
        }

        view.setText(text);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnConditionClickListener) {
                    mOnConditionClickListener.onClickCondition(condition);
                }
            }
        });
    }

    public void setOnConditionClickListener(OnConditionClickListener onConditionClickListener) {
        mOnConditionClickListener = onConditionClickListener;
    }

    public interface OnConditionClickListener {
        void onClickExpandFlag(Condition condition);
        void onClickCondition(Condition codition);
    }
}
