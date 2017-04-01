package com.pw.qi1siwole.mynotebook.Condition.View;

import com.pw.qi1siwole.mynotebook.Condition.Condition;
import com.pw.qi1siwole.mynotebook.Condition.ConditionData;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionStateGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017/3/31.
 */

public class ConditionHelper {

    private ConditionData mData;
    private Condition mBaseCondition;

    public ConditionHelper() {
        mBaseCondition = null;
        mData = null;
    }

    /**
     * 设置初始条件
     * @param data/condition
     */
    public void setBaseCondition(ConditionData data) {
        mData = data;
        mBaseCondition = new Condition(data);
    }

    public void setBaseCondition(Condition condition) {
        mBaseCondition = condition;
        mData = condition.getConditionData();
    }

    /**
     * 获得显示列表
     * @return
     */
    public List<Condition> getShowList() {
        if (null == mBaseCondition) {
            return null;
        }

        mBaseCondition.setLevel(0);

        List<Condition> list = new ArrayList<>();
        _addToShowList(mBaseCondition, list);

        return list;
    }

    /** 将条件添加到列表中，并重新设置Level值（在【获得显示列表】方法 中使用）
     * @param condition
     * @param outList
     */
    private void _addToShowList(Condition condition, List<Condition> outList) {
        if (condition.getType() == Condition.TYPE_GROUP && condition.isExpand()) {
            List<Condition> operands = ((ConditionStateGroup)condition.getState()).getOperands();
            for (int i = 0; i < operands.size(); ++i) {
                if (i > 0) {
                    outList.add(condition);
                }
                
                Condition operand = operands.get(i);
                operand.setLevel(condition.getLevel() + 1);
                _addToShowList(operand, outList);
            }
        }
        else {
            outList.add(condition);
        }
    }

    public void assignShowList(List<Condition> outList) {
        if (null != outList) {
            outList.clear();
            outList.addAll(getShowList());
        }
    }

    public Set getResult() {
        return mBaseCondition.getResult();
    }
}
