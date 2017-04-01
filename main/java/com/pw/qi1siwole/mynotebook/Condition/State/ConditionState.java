package com.pw.qi1siwole.mynotebook.Condition.State;

import com.pw.qi1siwole.mynotebook.Condition.Condition;

import java.util.Set;

/**
 * Created by user on 2017/3/30.
 */

public abstract class ConditionState {

    private Condition mCondition;

    protected ConditionState(Condition condition) {
        mCondition = condition;
    }

    protected Condition getCondition() {
        return mCondition;
    }

    public void reset() {

    }

    public boolean isComplete() {
        return false;
    }

    public Set getResult() {
        return null;
    }

    public abstract int getType();

    public abstract String toString();

}
