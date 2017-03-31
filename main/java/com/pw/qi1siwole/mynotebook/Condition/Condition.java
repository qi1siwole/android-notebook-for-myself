package com.pw.qi1siwole.mynotebook.Condition;

import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017/3/30.
 */

public class Condition {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_GROUP = 2;

    private ConditionData mData;
    private ConditionState mState;
    private Condition mParentCondition;

    public Condition(ConditionData data) {
        mParentCondition = null;
        mData = data;
        setType(TYPE_NONE);
    }

    public void setParentCondion(Condition condition) {
        mParentCondition = condition;
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

    public List getKeys() {
        if (null == mData) {
            return null;
        }
        return mData.getKeys();
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
