package com.pw.qi1siwole.mynotebook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.pw.qi1siwole.mynotebook.Common.Common;
import com.pw.qi1siwole.mynotebook.Common.ObjectWithCheck;

import java.util.List;

/**
 * Created by user on 2017/3/17.
 */

public class TagAdapter extends ArrayAdapter<ObjectWithCheck> {
    private int resourceId;
    private Common.OnListItemCheckedListener mOnListItemCheckedListener;

    void setOnListItemCheckedListener(Common.OnListItemCheckedListener onListItemCheckedListener) {
        mOnListItemCheckedListener = onListItemCheckedListener;
    }

    public TagAdapter(Context context, int resourceId, List<ObjectWithCheck> objects) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ObjectWithCheck tag = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);

        CheckBox tagCheckbox = (CheckBox)view.findViewById(R.id.checkbox);
        tagCheckbox.setChecked(tag.isChecked());
        tagCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tag.setChecked(isChecked);
                if (isChecked && null != mOnListItemCheckedListener) {
                    mOnListItemCheckedListener.onListItemChecked(Common.RADIO_BUTTON.TAG.Value());
                }
            }
        });

        TextView seqTextView = (TextView)view.findViewById(R.id.text_view_seq);
        seqTextView.setVisibility(View.GONE);

        TextView tagTextView = (TextView)view.findViewById(R.id.text_view_name);
        tagTextView.setText(((Tag)tag).getName());

        return view;
    }
}
