package com.pw.qi1siwole.mynotebook.Condition.State;

import android.util.Log;

import com.pw.qi1siwole.mynotebook.Condition.Condition;

import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017/3/30.
 */

public class ConditionStateItem extends ConditionState {

    private int mSelectedIndex;

    public ConditionStateItem(Condition condition) {
        super(condition);
        mSelectedIndex = -1;
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    @Override
    public void reset() {
        mSelectedIndex = -1;
    }

    @Override
    public boolean isComplete() {
        if (-1 != mSelectedIndex) {
            List keys = getCondition().getKeys();
            if (null != keys && mSelectedIndex >= 0 && mSelectedIndex < keys.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set getResult() {
        if (!isComplete()) {
            return null;
        }
        return getCondition().getValue(mSelectedIndex);
    }

    @Override
    public int getType() {
        return Condition.TYPE_ITEM;
    }

    @Override
    public String toString() {
        final String FORMAT = "{%s}";
        String str = null;

        if (isComplete()) {
            str = String.valueOf(getCondition().getKeys().get(mSelectedIndex));
        }
        else {
            str = "@";
        }

        return String.format(FORMAT, str);
    }
}
