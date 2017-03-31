package com.pw.qi1siwole.mynotebook.Common;

/**
 * Created by user on 2017/3/20.
 */

public class CheckConditionWithParam implements CheckCondition {
    private Object mParam;

    public CheckConditionWithParam(Object param) {
        mParam = param;
    }

    public Object getParam() { return mParam; }

    @Override
    public boolean isChecked(ObjectWithCheck obj) {
        return false;
    }
}
