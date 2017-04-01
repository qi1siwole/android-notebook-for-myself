package com.pw.qi1siwole.mynotebook.Condition.View;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.widget.ListView;
import android.widget.Toast;

import com.pw.qi1siwole.mynotebook.Common.Common;
import com.pw.qi1siwole.mynotebook.Condition.Condition;
import com.pw.qi1siwole.mynotebook.Condition.ConditionData;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionState;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionStateGroup;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionStateItem;
import com.pw.qi1siwole.mynotebook.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by qi1siwole on 2017/3/30.
 */

public class ConditionView extends ListView implements ConditionAdapter.OnConditionClickListener {

    private Context mContext;
    private ConditionHelper mConditionHelper;
    private ConditionAdapter mConditionAdapter;
    private List<Condition> mShowList;

    public ConditionView(Context context) {
        this(context, null);
    }

    public ConditionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConditionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        mConditionHelper = new ConditionHelper();
    }


    /**
     * 初始设置
     * @param data
     */
    public void initView(ConditionData data) {
        initView(new Condition(data));
    }
    public void initView(Condition condition) {
        mConditionHelper.setBaseCondition(condition);

        mShowList = mConditionHelper.getShowList();

        mConditionAdapter = new ConditionAdapter(mContext, R.layout.condition_view_item, mShowList);
        mConditionAdapter.setOnConditionClickListener(this);
        setAdapter(mConditionAdapter);
    }

    public Set getResult() {
        return mConditionHelper.getResult();
    }

    @Override
    public void onClickExpandFlag(Condition condition) {
        condition.setExpand(!condition.isExpand());
        _updateView();
    }

    private void _updateView() {
        mConditionHelper.assignShowList(mShowList);
        mConditionAdapter.notifyDataSetChanged();
    }

    private static final int CHOOSE_ITEM             = R.string.choose_as_item;
    private static final int CHOOSE_GROUP            = R.string.choose_as_set_operation;
    private static final int CHOOSE_RESET            = R.string.reset;
    private static final int CHOOSE_ADD_OPERAND      = R.string.add_a_operand;
    private static final int CHOOSE_DELETE_OPERAND   = R.string.remove_this_operand;
    private static final int CHOOSE_RESET_OPERATOR   = R.string.reset_operator;

    @Override
    public void onClickCondition(final Condition codition) {
        final List<Integer> flagList = new ArrayList<>();
        final List<String> textList = new ArrayList<>();

        _pushItemsToList(codition, flagList, textList);

        final String[] texts = textList.toArray(new String[textList.size()]);

        Common.showItemDialog(mContext, texts, new Common.OnDialogItemClickListener() {
            @Override
            public void onClick(int which) {
                int flag = flagList.get(which);
                _updateCondition(codition, flag);
            }
        });
    }

    /**
     * 更新条件
     * @param condition
     * @param flag
     */
    private void _updateCondition(Condition condition, int flag) {

        switch (flag) {
            /**
             * 重置
             */
            case CHOOSE_RESET:
                condition.reset();
                _updateView();

                switch ( condition.getType() ) {
                    case Condition.TYPE_ITEM:

                        _showDialogChooseItem(condition);

                        break;
                    case Condition.TYPE_GROUP:

                        _showDialogChooseOperator(condition, true);

                        break;
                    default:
                        break;
                }

                break;

            /**
             * 选择作为单项
             */
            case CHOOSE_ITEM:

                if (condition.getType() == Condition.TYPE_ITEM) {
                    break;
                }

                condition.setType(Condition.TYPE_ITEM);
                _updateView();

                _showDialogChooseItem(condition);

                break;

            /**
             * 选择作为集合
             */
            case CHOOSE_GROUP:

                if (condition.getType() == Condition.TYPE_GROUP) {
                    break;
                }

                condition.setType(Condition.TYPE_GROUP);
                _updateView();

                _showDialogChooseOperator(condition, true);

                break;

            /**
             * 更改操作符
             */
            case CHOOSE_RESET_OPERATOR:

                if (condition.getType() != Condition.TYPE_GROUP) {
                    break;
                }

                _showDialogChooseOperator(condition, false);

                break;

            /**
             * 添加条件
             */
            case CHOOSE_ADD_OPERAND:

                if (condition.getType() != Condition.TYPE_GROUP) {
                    break;
                }

                ConditionStateGroup state = (ConditionStateGroup) condition.getState();

                if (!state.canAddOperand()) {
                    break;
                }

                state.addOperand(null);
                _updateView();

                break;

            /**
             * 移除该条件
             */
            case CHOOSE_DELETE_OPERAND:

                Condition parent = condition.getParentCondition();
                if (null == parent) {
                    break;
                }

                ConditionStateGroup parentState = (ConditionStateGroup) parent.getState();

                if (!parentState.canRemoveOperand()) {
                    break;
                }

                parentState.removeOperand(condition);
                _updateView();

                break;

            default:
                break;
        }
    }

    /**
     * 弹出对话框：选择操作符（交集、并集、差集）
     * @param condition
     */
    private void _showDialogChooseOperator(Condition condition, final boolean isReset) {
        final ConditionStateGroup state = (ConditionStateGroup) condition.getState();
        final List<Integer> typeList = ConditionStateGroup.getOperatorTypeList();
        String[] texts = new String[typeList.size()];
        for (int i = 0; i < texts.length; ++i) {
            texts[i] = String.format("(%d) [ %s ]", i + 1, ConditionStateGroup.getOperatorString(typeList.get(i)));
        }
        Common.showItemDialog(mContext, texts, new Common.OnDialogItemClickListener() {
            @Override
            public void onClick(int which) {
                int type = typeList.get(which);
                if (isReset) {
                    state.resetOperatorType(type);
                    _updateView();
                }
                else {
                    if (state.modifyOperatorType(type)) {
                        _updateView();
                    }
                    else {
                        _toast(R.string.condition_toast_cant_change_operator);
                    }
                }
            }
        });
    }

    /**
     * 弹出对话框：选择Item
     * @param condition
     */
    private void _showDialogChooseItem(final Condition condition) {
        List<String> list = condition.getKeys();
        String[] texts = new String[list.size()];
        for (int i = 0; i < texts.length; ++i) {
            texts[i] = String.format("(%d) %s", i + 1, list.get(i));
        }
        Common.showItemDialog(mContext, texts, new Common.OnDialogItemClickListener() {
            @Override
            public void onClick(int which) {
                ((ConditionStateItem) condition.getState()).setSelectedIndex(which);
                _updateView();
            }
        });
    }

    /**
     * 填充Item对话框
     * @param condition
     * @param flagList
     * @param textList
     */
    private void _pushItemsToList(Condition condition, List<Integer> flagList, List<String> textList) {
        if (condition.getType() != Condition.TYPE_NONE) {
            _putIntoList(CHOOSE_RESET, flagList, textList);
        }

        if (condition.getType() != Condition.TYPE_ITEM) {
            _putIntoList(CHOOSE_ITEM, flagList, textList);
        }

        if (condition.getType() != Condition.TYPE_GROUP) {
            _putIntoList(CHOOSE_GROUP, flagList, textList);
        }
        else {
            _putIntoList(CHOOSE_RESET_OPERATOR, flagList, textList);

            ConditionStateGroup state = (ConditionStateGroup) condition.getState();
            if (state.canAddOperand()) {
                _putIntoList(CHOOSE_ADD_OPERAND, flagList, textList);
            }
        }

        Condition parent = condition.getParentCondition();
        if (null != parent && parent.getType() == Condition.TYPE_GROUP) {
            ConditionStateGroup state = (ConditionStateGroup) parent.getState();
            if (state.canRemoveOperand()) {
                _putIntoList(CHOOSE_DELETE_OPERAND, flagList, textList);
            }
        }
    }

    /**
     * 插入单个Item
     * @param flag
     * @param flagList
     * @param textList
     */
    private void _putIntoList(int flag, List<Integer> flagList, List<String> textList) {
        flagList.add(Integer.valueOf(flag));
        textList.add("=> " + mContext.getResources().getString(flag));
    }

    private void _toast(int param) {
        Toast.makeText(mContext, param, Toast.LENGTH_SHORT).show();
    }
}
