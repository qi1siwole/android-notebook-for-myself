package com.pw.qi1siwole.mynotebook.Condition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017/3/30.
 */

public class ConditionStateGroup extends ConditionState {

    public static final int TYPE_NONE = -1;
    public static final int TYPE_UNION = 0;
    public static final int TYPE_INTERSECTION = 1;
    public static final int TYPE_DIFFERENCE_SET = 2;

    public static final String STRING_NONE = " ";
    public static final String STRING_DIFFERENCE_SET = "-";
    public static final String STRING_INTERSECTION = "∩";
    public static final String STRING_UNION = "∪";

    private static final String NORM_FORMAT = "%s";
    private static final String GROUP_FORMAT = "(%s)";

    private static final int MAX_OPERAND_COUNT = 2;
    private static final int MIN_OPERAND_COUNT = 2;

    private int mOperatorType;

    private List<Condition> mOperands;
    private boolean mHasOperandCountLimit;

    public ConditionStateGroup(Condition condition) {
        super(condition);
        mOperands = new ArrayList();
        reset();
    }

    public String getOperatorString() {
        String str = null;

        switch (mOperatorType) {
            case TYPE_NONE:
                str = STRING_NONE;
                break;
            case TYPE_DIFFERENCE_SET:
                str = STRING_DIFFERENCE_SET;
                break;
            case TYPE_INTERSECTION:
                str = STRING_INTERSECTION;
                break;
            case TYPE_UNION:
                str = STRING_UNION;
                break;
            default:
                break;
        }

        return str;
    }

    public void setOperatorType(int type) {
        mOperatorType = type;
        mHasOperandCountLimit = TYPE_DIFFERENCE_SET == type;
        resetOperands();
    }

    public Condition newOperands() {
        Condition condition = new Condition(getCondition().getConditionData());
        condition.setParentCondion(getCondition());
        return condition;
    }

    public boolean canAddOperand() {
        return !mHasOperandCountLimit || mOperands.size() < MAX_OPERAND_COUNT;
    }

    public int addOperand(Condition condition) {
        if (!canAddOperand()) {
            return -1;
        }
        if (null == condition) {
            condition = newOperands();
        }
        mOperands.add(condition);
        return mOperands.size() - 1;
    }

    public boolean updateOperand(int index, Condition condition) {
        if (index < 0 || index >= mOperands.size()) {
            return false;
        }
        if (null == condition) {
            condition = newOperands();
        }
        mOperands.set(index, condition);
        return true;
    }

    public void resetOperands() {
        mOperands.clear();
        for (int i = 0; i < MIN_OPERAND_COUNT; ++i) {
            addOperand(null);
        }
    }

    public void reset() {
        setOperatorType(TYPE_NONE);
    }

    @Override
    public boolean isComplete() {
        if (TYPE_NONE == mOperatorType
                || mOperands.size() < MIN_OPERAND_COUNT
                || mHasOperandCountLimit && mOperands.size() > MAX_OPERAND_COUNT) {
            return false;
        }

        for (Condition condition: mOperands) {
            if (!condition.isComplete()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Set getResult() {
        if (!isComplete()) {
            return null;
        }

        Set resSet = new HashSet();

        switch (mOperatorType) {
            case TYPE_UNION:

                for (Condition condition: mOperands) {
                    Set newSet = condition.getResult();
                    resSet.addAll(newSet);
                }

                break;
            case TYPE_INTERSECTION:

                boolean isFirst = true;
                for (Condition condition: mOperands) {
                    Set newSet = condition.getResult();
                    if (null == newSet) {
                        return null;
                    }

                    if (isFirst) {
                        resSet = newSet;
                        isFirst = false;
                    }
                    else {
                        Set tmpSet = new HashSet();
                        for (Object obj: resSet) {
                            if (newSet.contains(obj)) {
                                tmpSet.add(obj);
                            }
                        }
                        if (null == tmpSet) {
                            return null;
                        }
                        resSet = tmpSet;
                    }
                }

                break;
            case TYPE_DIFFERENCE_SET:

                Set containSet = mOperands.get(0).getResult();
                Set notContainsSet = mOperands.get(1).getResult();

                for (Object obj: containSet) {
                    if (!notContainsSet.contains(obj)) {
                        resSet.add(obj);
                    }
                }

                break;
            default:
                break;
        }

        return resSet;
    }

    @Override
    public int getType() {
        return Condition.TYPE_GROUP;
    }

    @Override
    public String toString() {

        String str = "";

        for (int i = 0; i < mOperands.size(); ++i) {
            Condition condition = mOperands.get(i);

            if (i > 0) {
                str += getOperatorString();
            }

            String format = condition.getType() == Condition.TYPE_GROUP ? GROUP_FORMAT : NORM_FORMAT;
            str += String.format(format, condition.toString());
        }

        return str;
    }
}
