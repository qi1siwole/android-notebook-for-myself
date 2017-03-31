package com.pw.qi1siwole.mynotebook;

import com.pw.qi1siwole.mynotebook.Common.ObjectWithCheck;

/**
 * Created by user on 2017/3/20.
 */

public class Word implements ObjectWithCheck {
    //private String mStrNo;
    private boolean mIsChecked;
    private String mText;

    public Word(String text, boolean isChecked) {
        mText = text;
        mIsChecked = isChecked;
    }

    public Word(String text) {
        this(text, false);
    }

    public boolean isChecked() { return mIsChecked; }
    public void setChecked(boolean isChecked) { mIsChecked = isChecked; }
    public String getText() { return mText; }
    public void setText(String text) { mText = text; }
}
