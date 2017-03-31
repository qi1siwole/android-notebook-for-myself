package com.pw.qi1siwole.mynotebook.Common;

import com.pw.qi1siwole.mynotebook.R;

import java.util.Collection;
import java.util.List;

/**
 * Created by user on 2017/3/20.
 */

public class Common {

    public enum RADIO_BUTTON {
        WORD(R.id.radio_button_word),
        TAG(R.id.radio_button_tag);

        private int value = 0;

        private RADIO_BUTTON(int value) {
            this.value = value;
        }

        public int Value() {
            return this.value;
        }
    }

    // 列表项被选中复选框的监听器
    public interface OnListItemCheckedListener {
        void onListItemChecked(int id);
    }

    // 设置列表中所有Item的选中状态
    public static void setChecked(List<ObjectWithCheck> list, boolean isChecked) {
        for (ObjectWithCheck obj : list) {
            obj.setChecked(isChecked);
        }
    }

    // 设置列表中所有Item的选中状态，满足给定条件的设为选中，否则为取消选中
    public static void setChecked(List<ObjectWithCheck> list, CheckCondition condition) {
        for (ObjectWithCheck obj : list) {
            obj.setChecked(condition.isChecked(obj));
        }
    }

    public static void setChecked(List<ObjectWithCheck> list, CheckConditionWithParam condition) {
        for (ObjectWithCheck obj : list) {
            obj.setChecked(condition.isChecked(obj));
        }
    }

    public static String getStringWithJoin(Collection<String> collection, String sep, String format) {
        String res = "";
        boolean isFirst = true;
        for (String s : collection) {
            if (isFirst) {
                isFirst = false;
            } else {
                res += sep;
            }

            res += String.format(format, s);
        }
        return res;
    }

    public static String getStringWithJoin(Collection<String> collection, String sep) {
        return getStringWithJoin(collection, sep, "%s");
    }
}
