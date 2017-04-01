package com.pw.qi1siwole.mynotebook.Condition;

import com.pw.qi1siwole.mynotebook.Condition.State.ConditionState;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionStateGroup;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionStateItem;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionStateNone;

import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017/3/30.
 */

public class Condition {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_GROUP = 2;

    private Condition mParentCondition;
    private ConditionData mData;
    private ConditionState mState;
    private int mLevel;
    private boolean mIsExpand;

    public Condition(ConditionData data) {
        mParentCondition = null;
        mData = data;
        mState = new ConditionStateNone(this);
        //mLevel = 0;
        mIsExpand = true;
    }

    public ConditionState getState() {
        return mState;
    }

    public void setParentCondition(Condition condition) {
        mParentCondition = condition;
        //mLevel = condition.getLevel() + 1;
    }

    public Condition getParentCondition() {
        return mParentCondition;
    }

    public void setConditionData(ConditionData data) {
        mData = data;
    }

    public ConditionData getConditionData() {
        return mData;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public boolean isExpand() {
        return mIsExpand;
    }

    public void setExpand(boolean isExpand) {
        mIsExpand = isExpand;
    }

    public List getKeys() {
        if (null == mData) {
            return null;
        }
        return mData.requestKeys();
    }

    public Set getValue(int index) {
        List keys = getKeys();
        if (null == keys || index < 0 || index >= keys.size()) {
            return null;
        }
        return mData.requestValue(keys.get(index));
    }

    public boolean setType(int type) {
        if (type == getType()) {
            return false;
        }

        if (TYPE_NONE == type) {
            mState = new ConditionStateNone(this);
        }
        else if (TYPE_ITEM == type) {
            mState = new ConditionStateItem(this);
        }
        else if (TYPE_GROUP == type) {
            mState = new ConditionStateGroup(this);
        }
        else {
            return false;
        }

        return true;
    }

    public void reset() {
        mState.reset();
        mIsExpand = true;
    }

    public boolean isComplete() {
        return mState.isComplete();
    }

    public Set getResult() {
        return mState.getResult();
    }

    public int getType() {
        return mState.getType();
    }

    public String toString() {
        return mState.toString();
    }
}
