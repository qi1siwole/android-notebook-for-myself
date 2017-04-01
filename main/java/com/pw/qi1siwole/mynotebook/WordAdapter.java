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
 * Created by user on 2017/3/20.
 */

public class WordAdapter extends ArrayAdapter<ObjectWithCheck> {
    private int resourceId;
    private Common.OnListItemCheckedListener mOnListItemCheckedListener;

    void setOnListItemCheckedListener(Common.OnListItemCheckedListener onListItemCheckedListener) {
        mOnListItemCheckedListener = onListItemCheckedListener;
    }

    private static final String SEQ_FORMAT = "%2d. ";

    public WordAdapter(Context context, int resourceId, List<ObjectWithCheck> objects) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ObjectWithCheck word = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);

        CheckBox checkbox = (CheckBox)view.findViewById(R.id.checkbox);
        checkbox.setChecked(word.isChecked());
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                word.setChecked(isChecked);
                if (null != mOnListItemCheckedListener) {
                    mOnListItemCheckedListener.onListItemChecked(Common.RADIO_BUTTON.WORD.Value());
                }
            }
        });

        TextView seqTextView = (TextView)view.findViewById(R.id.text_view_seq);
        seqTextView.setText(String.format(SEQ_FORMAT, position + 1));

        TextView wordTextView = (TextView)view.findViewById(R.id.text_view_name);
        wordTextView.setText(((Word)word).getText());

        return view;
    }
}
