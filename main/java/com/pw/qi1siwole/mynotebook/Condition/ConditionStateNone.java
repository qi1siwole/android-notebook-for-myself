package com.pw.qi1siwole.mynotebook.Condition;

/**
 * Created by user on 2017/3/30.
 */

public class ConditionStateNone extends ConditionState {

    public ConditionStateNone(Condition condition) {
        super(condition);
    }

    @Override
    public int getType() {
        return Condition.TYPE_NONE;
    }

    @Override
    public String toString() {
        return "[@]";
    }
}
