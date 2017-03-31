package com.pw.qi1siwole.mynotebook;

import com.pw.qi1siwole.mynotebook.Common.ObjectWithCheck;

/**
 * Created by user on 2017/3/17.
 */

public class Tag implements ObjectWithCheck {
    private String mName;
    private boolean mIsChecked;
    private boolean mShowCheckbox;
    private boolean mShowButtons;

    public Tag(String name, boolean isChecked) {
        mName = name;
        mIsChecked = isChecked;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) { mName = name; }
    public boolean isChecked() {
        return mIsChecked;
    }
    public void setChecked(boolean isChecked) { mIsChecked = isChecked; }
}
