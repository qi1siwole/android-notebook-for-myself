package com.pw.qi1siwole.mynotebook;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pw.qi1siwole.mynotebook.Common.CheckCondition;
import com.pw.qi1siwole.mynotebook.Common.CheckConditionWithParam;
import com.pw.qi1siwole.mynotebook.Common.Common;
import com.pw.qi1siwole.mynotebook.Common.ObjectWithCheck;
import com.pw.qi1siwole.mynotebook.Condition.Condition;
import com.pw.qi1siwole.mynotebook.Condition.ConditionData;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionState;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionStateGroup;
import com.pw.qi1siwole.mynotebook.Condition.State.ConditionStateItem;
import com.pw.qi1siwole.mynotebook.Condition.View.ConditionView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements Common.OnListItemCheckedListener {

    private TextView mTvRadioGroup;
    private RadioGroup mRadioGroup;

    private LinearLayout mAddWordLayout;
    private LinearLayout mAddTagLayout;

    private EditText mWordEditText;
    private EditText mTagEditText;

    private Button mAddWordButton;
    private Button mAddTagButton;

    private ListView mWordListView;
    private ListView mTagListView;

    private ConditionView mConditionView;

    private MenuItem mMenuItemAddMap;
    private MenuItem mMenuItemDefault;
    private MenuItem mMenuItemSetOperation;


    private List<ObjectWithCheck> mWordList = new ArrayList<>();
    private List<ObjectWithCheck> mTagList = new ArrayList<>();

    private WordAdapter mWordAdapter;
    private TagAdapter mTagAdapter;

    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getSupportActionBar().hide(); // 隐藏标题栏
        setContentView(R.layout.activity_main);

        dbHelper = MyDatabaseHelper.initDatabaseHelper(this, MyDatabaseHelper.DB_NAME, null, MyDatabaseHelper.VERSION_1_0);

        // 单选框组合：名称And标签
        mRadioGroup = (RadioGroup)findViewById(R.id.radio_group);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRadioButtonCheckedChanged();
            }
        });

        mTvRadioGroup = (TextView)findViewById(R.id.tv_radio_group);

        mAddTagLayout = (LinearLayout)findViewById(R.id.layout_add_tag);
        mAddWordLayout = (LinearLayout)findViewById(R.id.layout_add_word);

        // 输入框：名称
        mWordEditText = (EditText)findViewById(R.id.edit_text_word);
        mWordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onWordInputTextChanged(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
        // 按钮：添加名称
        mAddWordButton = (Button)findViewById(R.id.button_add_word);
        mAddWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddWordButtonClicked();
            }
        });
        // 输入框：标签
        mTagEditText = (EditText)findViewById(R.id.edit_text_tag);
        // 按钮：添加标签
        mAddTagButton = (Button)findViewById(R.id.button_add_tag);
        mAddTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddTagButtonClicked();
            }
        });

        // 自定义列表：条件
        _dealWithConditionView();

        // 列表：标签
        this.queryTagListFromDB();
        mTagListView = (ListView)findViewById(R.id.list_view_tag);
        mTagAdapter = new TagAdapter(MainActivity.this, R.layout.list_view_item_1, mTagList);
        mTagListView.setAdapter(mTagAdapter);

        // 列表：名称
        this.queryWordListFromDB("");
        mWordListView = (ListView)findViewById(R.id.list_view_word);
        mWordAdapter = new WordAdapter(MainActivity.this, R.layout.list_view_item_1, mWordList);
        mWordListView.setAdapter(mWordAdapter);

        // 监听列表项Checked事件
        mWordAdapter.setOnListItemCheckedListener(this);
        mTagAdapter.setOnListItemCheckedListener(this);
    }

    /**
     * 处理条件控件相关事务
     */
    private void _dealWithConditionView() {
        mConditionView = (ConditionView)findViewById(R.id.condition_view);

        ConditionData<String, String> conditionData = new ConditionData<>(new ConditionData.RequestDataMethod<String, String>() {
            @Override
            public List<String> requestKeys() {
                return MyDatabaseHelper.queryTagListFromDB();
            }

            @Override
            public Set<String> requestValue(String key) {
                return MyDatabaseHelper.queryWordSetByTagFromDB(key);
            }
        });

        mConditionView.initView(conditionData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        mMenuItemAddMap = menu.findItem(R.id.item_add_map);
        mMenuItemDefault = menu.findItem(R.id.item_tag_use_default_intersection_operation);
        mMenuItemSetOperation = menu.findItem(R.id.item_tag_use_set_operation);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_show_map:
                this.onShowMapItemSelected();
                break;
            case R.id.item_add_map:
                List<String> tagTextList = this.getCheckedTagTextList();
                List<String> wordTextList = this.getCheckedWordTextList();
                if (isWordListViewShow() && !tagTextList.isEmpty() && !wordTextList.isEmpty()) {
                    this.addMapBetweenTagAndWord(tagTextList, wordTextList);
                }
                else {
                    this.showToast(R.string.add_map_toast_check_both);
                }
                break;
            case R.id.item_check_all:
                this.onCheckAllItemSelected();
                break;
            case R.id.item_check_inverse:
                this.onCheckInverseItemSelected();
                break;
            case R.id.item_edit:
                this.onEditItemSelected();
                break;
            case R.id.item_remove:
                this.onRemoveItemSelected();
                break;
            case R.id.item_show_word_no_tag:
                this.onShowWordNoTagItemSelected();
                break;
            case R.id.item_tag_use_default_intersection_operation:
                this.onTagUseDefaultIntersectionOperation();
                break;
            case R.id.item_tag_use_set_operation:
                this.onTagUseSetOperation();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onListItemChecked(int id) {
        if (isWordInputValid()) {
            return;
        }
        mRadioGroup.check(id);
        onRadioButtonCheckedChanged();
    }

    /*******************************************************
     【事件响应处理】
     *******************************************************/
    /* 事件：当单选按钮选项改变 */
    private void onRadioButtonCheckedChanged() {
        int nText = isOperatingOnWord() ? R.string.button_name_add_word : R.string.button_name_add_tag;
        mAddWordButton.setText(nText);
        mAddWordButton.setEnabled(isOperatingOnWord());
    }

    /* 事件：当名称的输入框文字改变 */
    private void onWordInputTextChanged(String inputText) {
        mTagEditText.setText("");

        this.queryWordListFromDB(inputText);
        mWordAdapter.notifyDataSetChanged();

        this.showWordListView(!mWordList.isEmpty());
    }

    /* 事件：当添加名称的按钮被点击 */
    private void onAddWordButtonClicked() {
        String inputText = mWordEditText.getText().toString().trim();
        mWordEditText.setText("");
        if (inputText.isEmpty()) {
            return;
        }

        Set<String> newTagSet = new HashSet<>();
        for (ObjectWithCheck tag: mTagList) {
            if (tag.isChecked()) {
                newTagSet.add(((Tag)tag).getName());
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select tag from Word where word = '" + inputText + "'", null);
        if (newTagSet.isEmpty()) {
            if (!cursor.moveToFirst()) {
                db.execSQL("insert into Word(word, tag) values(?, ?)", new String[]{inputText, ""});
            }
            return;
        }

        Set<String> oriTagSet = new HashSet<>();
        if (cursor.moveToFirst()) {
            do {
                String tagText = cursor.getString(cursor.getColumnIndex("tag"));
                oriTagSet.add(tagText);
            } while (cursor.moveToNext());
        }

        if (oriTagSet.contains("")) {
            db.execSQL("delete from Word where word = ? and tag = ''", new String[]{inputText});
        }

        for (String tagText: newTagSet) {
            if (!oriTagSet.contains(tagText)) {
                db.execSQL("insert into Word(word, tag) values(?, ?)", new String[]{inputText, tagText});
            }
        }
    }

    /* 事件：当添加标签的按钮被点击 */
    private void onAddTagButtonClicked() {
        String inputText = mTagEditText.getText().toString().trim();
        mTagEditText.setText("");
        if (inputText.isEmpty()) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select tag from Tag where tag = '" + inputText + "'", null);
        if (cursor.moveToFirst()) {
            if (isWordInputValid()) {
                for (ObjectWithCheck obj : mTagList) {
                    Tag tag = (Tag)obj;
                    if (tag.getName().equals(inputText)) {
                        tag.setChecked(true);
                        mTagAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }
            return;
        }

        MyDatabaseHelper.insertTagIntoDB(inputText);
        Tag tag = new Tag(inputText, isWordInputValid());
        mTagList.add(tag);
        mTagAdapter.notifyDataSetChanged();
    }

    /* 事件：点击菜单项<显示操作对象的一方映射到另一方的列表> */
    private void onShowMapItemSelected() {
        if (!isTagUsingDefaultOperation()) {
            showSetOperationResult();
            return;
        }

        if (isOperatingOnWord()) {
            if (this.isWordListViewShow()) {
                showMapFromWordToTag();
            }
        }
        else {
            showMapFromTagToWord();
        }
    }

    /* 事件：点击菜单项<全选> */
    private void onCheckAllItemSelected() {
        if (isOperatingOnWord()) {
            if (!this.isWordListViewShow()) {
                return;
            }
            Common.setChecked(mWordList, true);
        }
        else {
            Common.setChecked(mTagList, true);
        }
        this.notifyCurrentChanged();
    }

    /* 事件：点击菜单项<反选> */
    private void onCheckInverseItemSelected() {
        if (isOperatingOnWord() && !isWordListViewShow()) {
            return;
        }
        List<ObjectWithCheck> list = mTagList;
        if (isOperatingOnWord()) {
            list = mWordList;
        }
        Common.setChecked(list, new CheckCondition() {
            @Override
            public boolean isChecked(ObjectWithCheck obj) {
                return !obj.isChecked();
            }
        });
        this.notifyCurrentChanged();
    }

    /* 事件：点击菜单项<修改> */
    private void onEditItemSelected() {
        // 编辑Word
        if (this.isOperatingOnWord()) {
            if (!isWordListViewShow() || mWordList.isEmpty()) {
                return;
            }
            List<String> wordTextList = this.getCheckedWordTextList();
            if (wordTextList.isEmpty()) {
                return;
            }
            if (wordTextList.size() > 1) {
                this.showToast(R.string.rename_word_toast_only_support_check_one);
                return;
            }
            this.showDialogRenameWord(wordTextList.get(0));
        }
        // 编辑Tag
        else {
            if (mTagList.isEmpty()) {
                return;
            }
            List<String> tagTextList = this.getCheckedTagTextList();
            if (tagTextList.isEmpty()) {
                return;
            }
            this.showDialogRenameTag(tagTextList);
        }
    }

    /* 事件：点击菜单项<删除> */
    private void onRemoveItemSelected() {
        // 删除Word
        if (this.isOperatingOnWord()) {
            if (!isWordListViewShow() || mWordList.isEmpty()) {
                return;
            }
            List<String> wordTextList = this.getCheckedWordTextList();
            if (wordTextList.isEmpty()) {
                return;
            }
            this.showDialogRemoveWord(wordTextList);
        }
        // 删除Tag
        else {
            if (mTagList.isEmpty()) {
                return;
            }
            List<String> tagTextList = this.getCheckedTagTextList();
            if (tagTextList.isEmpty()) {
                return;
            }
            this.showDialogRemoveTag(tagTextList);
        }
    }

    /* 事件：点击菜单项<显示无标签名称> */
    private void onShowWordNoTagItemSelected() {
        this.showWordListView(true);
        mWordList.clear();
        Set<String> set = MyDatabaseHelper.queryWordSetByTagFromDB("");
        List<String> list = new ArrayList<>();
        list.addAll(set);
        Collections.sort(list);
        for (String wordText: list) {
            Word word = new Word(wordText);
            mWordList.add(word);
        }
        mWordAdapter.notifyDataSetChanged();
    }

    /* 事件：点击菜单项<标签使用复杂集合运算> */
    private void onTagUseSetOperation() {
        mMenuItemAddMap.setVisible(false);
        mMenuItemDefault.setVisible(true);
        mMenuItemSetOperation.setVisible(false);

        mConditionView.setVisibility(View.VISIBLE);
        mTagListView.setVisibility(View.GONE);

        mRadioGroup.check(Common.RADIO_BUTTON.WORD.Value());
        mRadioGroup.setVisibility(View.GONE);
        mTvRadioGroup.setVisibility(View.GONE);

        mWordList.clear();
        showWordListView(false);

        mAddTagLayout.setVisibility(View.GONE);
        mAddWordLayout.setVisibility(View.GONE);
    }

    /* 事件：点击菜单项<标签使用简单交集运算> */
    private void onTagUseDefaultIntersectionOperation() {
        mMenuItemAddMap.setVisible(true);
        mMenuItemDefault.setVisible(false);
        mMenuItemSetOperation.setVisible(true);

        mConditionView.setVisibility(View.GONE);
        mTagListView.setVisibility(View.VISIBLE);

        mRadioGroup.setVisibility(View.VISIBLE);
        mTvRadioGroup.setVisibility(View.VISIBLE);

        mAddTagLayout.setVisibility(View.VISIBLE);
        mAddWordLayout.setVisibility(View.VISIBLE);
    }

    /*******************************************************
     【数据库操作】
     *******************************************************/
    /* 查询Word：  " "（空格）起到SQL中"%"通配符的作用 */
    private void queryWordListFromDB(String part) {
        mWordList.clear();

        if (part.isEmpty()) {
            return;
        }

        part = part.replace("/", "//").replace("_", "/_").replace("%", "/%").replace(" ", "%");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select distinct word from Word where word like '" + part + "' escape '/' order by word", null);
        if (cursor.moveToFirst()) {
            do {
                String wordText = cursor.getString(cursor.getColumnIndex("word"));
                Word word = new Word(wordText);
                mWordList.add(word);
            } while (cursor.moveToNext());
        }
    }

    /* 查询所有Tag */
    private void queryTagListFromDB() {
        mTagList.clear();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select distinct tag from Tag order by tag", null);
        if (cursor.moveToFirst()) {
            do {
                String tagText = cursor.getString(cursor.getColumnIndex("tag"));
                Tag tag = new Tag(tagText, false);
                mTagList.add(tag);
            } while (cursor.moveToNext());
        }
    }

    /*******************************************************
    【其它私有方法】
     *******************************************************/

    // 弹出提示文字
    private void showToast(int param) {
        Toast.makeText(MainActivity.this, param, Toast.LENGTH_SHORT)
                .show();
    }
    private void showToast(String param) {
        Toast.makeText(MainActivity.this, param, Toast.LENGTH_SHORT)
                .show();
    }

    // 标签在使用默认交集运算
    private boolean isTagUsingDefaultOperation() {
        return !mMenuItemDefault.isVisible();
    }

    // 返回：输入名称是否有效（有效：不能是空或只是空格）
    private boolean isWordInputValid() {
        return !mWordEditText.getText().toString().trim().isEmpty();
    }

    // 显示/隐藏：名称列表
    private void showWordListView(boolean isShow) {
        mWordListView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    // 名称列表是否正在显示
    private boolean isWordListViewShow() {
        return mWordListView.getVisibility() == View.VISIBLE;
    }

    // 当前操作对象是否是Word
    private boolean isOperatingOnWord() {
        int radioButtonId = mRadioGroup.getCheckedRadioButtonId();
        return radioButtonId == Common.RADIO_BUTTON.WORD.Value();
    }

    // 返回选中的Tag的Text的List
    private List<String> getCheckedTagTextList() {
        List<String> tagTextList = new ArrayList<>();
        for (ObjectWithCheck obj: mTagList) {
            Tag tag = (Tag)obj;
            if (tag.isChecked()) {
                tagTextList.add(tag.getName());
            }
        }
        return tagTextList;
    }

    // 返回选中的Word的Text的List
    private List<String> getCheckedWordTextList() {
        List<String> wordTextList = new ArrayList<>();
        for (ObjectWithCheck obj: mWordList) {
            Word word = (Word)obj;
            if (word.isChecked()) {
                wordTextList.add(word.getText());
            }
        }
        return wordTextList;
    }

    // 当前操作对象，通知列表更新
    private void notifyCurrentChanged() {
        if (isOperatingOnWord()) {
            mWordAdapter.notifyDataSetChanged();
        }
        else {
            mTagAdapter.notifyDataSetChanged();
        }
    }

    // 弹出对话框（带输入框）：重命名所选的那个Word
    private void showDialogRenameWord(final String wordText) {
        LinearLayout view = (LinearLayout)getLayoutInflater()
                .inflate(R.layout.rename_tag, null);

        TextView oriWordText = (TextView)view.findViewById(R.id.ori_tag_name);
        TextView newWordText = (TextView)view.findViewById(R.id.new_tag_name);
        oriWordText.setText(R.string.ori_word_name);
        newWordText.setText(R.string.new_word_name);

        TextView oriWordTextView = (TextView)view.findViewById(R.id.text_view_ori_tag);
        final EditText renameWordEditText = (EditText)view.findViewById(R.id.edit_text_rename_tag);

        oriWordTextView.setText(String.format("【%s】", wordText));
        renameWordEditText.setText(wordText);
        renameWordEditText.selectAll();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.rename_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.dialog_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputText = renameWordEditText.getText().toString().trim();
                        if (inputText.isEmpty()) {
                            showToast(R.string.rename_word_toast_invalid_text);
                            renameWordEditText.setText("");
                            return;
                        }
                        if (wordText.equals(inputText)) {
                            showToast(R.string.rename_word_toast_same_text);
                            renameWordEditText.setText("");
                            return;
                        }
                        renameWord(wordText, inputText);
                    }
                });
        builder.setNegativeButton(R.string.dialog_negative,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog =  builder.create();
        alertDialog.show();
    }

    // 弹出对话框（带输入框）：重命名Tag
    private void showDialogRenameTag(final List<String> tagTextList) {
        LinearLayout view = (LinearLayout)getLayoutInflater()
                .inflate(R.layout.rename_tag, null);
        TextView oriTagTextView = (TextView)view.findViewById(R.id.text_view_ori_tag);
        final EditText renameTagEditText = (EditText)view.findViewById(R.id.edit_text_rename_tag);

        oriTagTextView.setText(Common.getStringWithJoin(tagTextList, " | ", "【%s】"));

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.rename_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.dialog_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputText = renameTagEditText.getText().toString().trim();
                        if (inputText.isEmpty()) {
                            showToast(R.string.rename_tag_toast_invalid_text);
                            renameTagEditText.setText("");
                            return;
                        }
                        if (tagTextList.size() == 1 && tagTextList.get(0).equals(inputText)) {
                            showToast(R.string.rename_tag_toast_same_text);
                            renameTagEditText.setText("");
                            return;
                        }
                        renameTagName(tagTextList, inputText);
                    }
                });
        builder.setNegativeButton(R.string.dialog_negative,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog =  builder.create();
        /*alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //同上positive_button中的onclick代码，但是有错误
                alertDialog.dismiss();
            }
        });*/
        alertDialog.show();
    }

    // 弹出对话框：删除Word
    private void showDialogRemoveWord(final List<String> wordTextList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.delete_word_dialog_message);
        builder.setPositiveButton(R.string.delete_word_dialog_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeWordCompletely(wordTextList);
                    }
                });
        if (isTagUsingDefaultOperation()) {
            builder.setNeutralButton(R.string.delete_word_dialog_neutral,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeWordRelyOnTag(wordTextList);
                        }
                    });
        }
        builder.setNegativeButton(R.string.dialog_negative,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    // 弹出对话框：删除Tag
    private void showDialogRemoveTag(final List<String> tagTextList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.delete_tag_dialog_message);
        builder.setPositiveButton(R.string.delete_tag_dialog_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeTagOnly(tagTextList);
                    }
                });
        builder.setNegativeButton(R.string.dialog_negative,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    // 修改Word：data + UI
    private void renameWord(String oriWordText, String newWordText) {
        boolean isReallyNew = !MyDatabaseHelper.isWordExistInWordFromDB(newWordText);

        // Word
        MyDatabaseHelper.updateWordWithWordFromDB(oriWordText, newWordText, isReallyNew);
        if (!isReallyNew) {
            MyDatabaseHelper.deleteInValidRecordFromWordFromDB();
        }

        // UI
        for (int i = mWordList.size() - 1; i >= 0; --i) {
            Word word = (Word)mWordList.get(i);

            if (word.isChecked()) {
                word.setText(newWordText);
            }
            else if (!isReallyNew && word.getText().equals(newWordText)) {
                mWordList.remove(i);
            }
        }
        mWordAdapter.notifyDataSetChanged();
    }

    // 修改Tag名：data + UI
    private void renameTagName(List<String> tagTextList, String newTagText) {
        int index = tagTextList.indexOf(newTagText);
        if (index != -1) {
            tagTextList.remove(index);
        }

        index = 0;
        for (ObjectWithCheck obj: mTagList) {
            Tag tag = (Tag)obj;
            if (tag.getName().equals(newTagText)) {
                break;
            }
            ++index;
        }
        boolean isReallyNew = index == mTagList.size();
        
        // Tag
        MyDatabaseHelper.deleteFromTagFromDB(tagTextList);
        if (isReallyNew) {
            MyDatabaseHelper.insertTagIntoDB(newTagText);
        }

        // Word
        MyDatabaseHelper.insertIntoWordSelectFromDB(tagTextList, newTagText, isReallyNew);

        MyDatabaseHelper.deleteFromWordUsingTagFromDB(tagTextList);

        // UI
        index = tagTextList.size();
        for (int i = mTagList.size() - 1; i >= 0; --i) {
            Tag tag = (Tag)mTagList.get(i);
            if (!isReallyNew && !tag.isChecked() && tag.getName().equals(newTagText)) {
                tag.setChecked(true);
            }
            if (tag.isChecked() && !tag.getName().equals(newTagText)) {
                --index;
                if (isReallyNew && 0 == index) {
                    tag.setName(newTagText);
                }
                else {
                    mTagList.remove(i);
                }
            }
        }
        mTagAdapter.notifyDataSetChanged();
    }

    // 添加映射
    private void addMapBetweenTagAndWord(List<String> tagTextList, List<String> wordTextList) {
        MyDatabaseHelper.deletePartFromWordFromDB(tagTextList, wordTextList);
        MyDatabaseHelper.insertWordIntoDB(tagTextList, wordTextList);
    }

    // 删除Word：完全删除
    private void removeWordCompletely(List<String> wordTextList) {
        MyDatabaseHelper.deleteFromWordUsingWordFromDB(wordTextList);
        this.removeCheckedWordOnUI();
    }

    // 删除Word：部分删除
    private void removeWordRelyOnTag(List<String> wordTextList) {
        Set<String> removedWordTextSet = MyDatabaseHelper.deletePartFromWordFromDB(this.getCheckedTagTextList(), wordTextList);
        this.removeWordOnUI(removedWordTextSet);
    }

    // 完全删除Word时的UI更新：移除所有选中状态的Item
    private void removeCheckedWordOnUI() {
        for (int i = mWordList.size() - 1; i >= 0; --i) {
            Word word = (Word)mWordList.get(i);
            if (word.isChecked()) {
                mWordList.remove(i);
            }
        }
        mWordAdapter.notifyDataSetChanged();
    }

    // 部分删除Word时的UI更新
    private void removeWordOnUI(Set<String> removedWordTextSet) {
        for (int i = mWordList.size() - 1; i >= 0; --i) {
            Word word = (Word)mWordList.get(i);
            if (word.isChecked() && removedWordTextSet.contains(word.getText())) {
                mWordList.remove(i);
            }
        }
        mWordAdapter.notifyDataSetChanged();
    }

    // 删除Tag数据：只删除标签
    private void removeTagOnly(List<String> tagTextList) {
        MyDatabaseHelper.deleteFromTagFromDB(tagTextList);
        this.removeOnlyTagFromWord(tagTextList);
        this.removeTagOnUI();
    }

    // 从Word表中移除给定tag相关的记录（word部分保留）
    private void removeOnlyTagFromWord(Collection<String> collection) {
        for (String tagText: collection) {
            MyDatabaseHelper.updateOneTagValInWordFromDB(tagText, "");
            MyDatabaseHelper.deleteInValidRecordFromWordFromDB();
        }
    }

    // 删除Tag的UI
    private void removeTagOnUI() {
        mWordList.clear();
        mWordAdapter.notifyDataSetChanged();

        for (int i = mTagList.size() - 1; i >= 0; --i) {
            Tag tag = (Tag)mTagList.get(i);
            if (tag.isChecked()) {
                mTagList.remove(i);
            }
        }
        mTagAdapter.notifyDataSetChanged();
    }

    // 显示Tag映射到Word的列表
    private void showMapFromTagToWord() {
        mWordList.clear();
        Set<String> curSet = new HashSet<>();
        boolean isFirst = true;
        for (ObjectWithCheck obj: mTagList) {
            Tag tag = (Tag)obj;
            if (tag.isChecked()) {
                Set<String> newSet = MyDatabaseHelper.queryWordSetByTagFromDB(tag.getName());
                if (isFirst) {
                    isFirst = false;
                    if (newSet.isEmpty()) {
                        break;
                    }
                    curSet = newSet;
                }
                else {
                    if (newSet.isEmpty()) {
                        curSet.clear();
                        break;
                    }
                    Set<String> tmpSet = new HashSet<>();
                    for (String s: curSet) {
                        if (newSet.contains(s)) {
                            tmpSet.add(s);
                        }
                    }
                    curSet = tmpSet;
                    if (curSet.isEmpty()) {
                        break;
                    }
                }
            }
        }

        if (!isFirst && !curSet.isEmpty()) {
            List<String> wordTextList = new ArrayList<>();
            wordTextList.addAll(curSet);
            Collections.sort(wordTextList);
            for (String wordText: wordTextList) {
                Word word = new Word(wordText);
                mWordList.add(word);
            }
        }
        mWordAdapter.notifyDataSetChanged();

        //this.showWordListView(!isFirst);
        this.showWordListView(true);
    }

    // 显示Word映射到Tag的列表
    private void showMapFromWordToTag() {
        //mWordList.clear();
        Set<String> curSet = new HashSet<>();
        boolean isFirst = true;
        for (ObjectWithCheck obj: mWordList) {
            Word word = (Word)obj;
            if (word.isChecked()) {
                Set<String> newSet = MyDatabaseHelper.queryTagSetByWordFromDB(word.getText(), false);
                if (isFirst) {
                    isFirst = false;
                    if (newSet.isEmpty()) {
                        break;
                    }
                    curSet = newSet;
                }
                else {
                    if (newSet.isEmpty()) {
                        curSet.clear();
                        break;
                    }
                    Set<String> tmpSet = new HashSet<>();
                    for (String s: curSet) {
                        if (newSet.contains(s)) {
                            tmpSet.add(s);
                        }
                    }
                    curSet = tmpSet;
                    if (curSet.isEmpty()) {
                        break;
                    }
                }
            }
        }

        if (!isFirst && !curSet.isEmpty()) {
            Common.setChecked(mTagList, new CheckConditionWithParam(curSet) {
                @Override
                public boolean isChecked(ObjectWithCheck obj) {
                    Tag tag = (Tag)obj;
                    Set<String> set = (Set)getParam();
                    return set.contains(tag.getName());
                }
            });
        }
        else {
            Common.setChecked(mTagList, false);
        }
        mTagAdapter.notifyDataSetChanged();
    }

    /**
     * 显示集合运算结果
     */
    private void showSetOperationResult() {
        mWordList.clear();
        Set<String> set = mConditionView.getResult();

        if (null != set && !set.isEmpty()) {
            List<String> wordTextList = new ArrayList<>();
            wordTextList.addAll(set);
            Collections.sort(wordTextList);
            for (String wordText: wordTextList) {
                Word word = new Word(wordText);
                mWordList.add(word);
            }
        }

        mWordAdapter.notifyDataSetChanged();

        this.showWordListView(null != set && !set.isEmpty());
    }

    private Condition _test_init_base_condition(ConditionData conditionData) {
        Condition condition_2 = new Condition(conditionData);
        condition_2.setType(Condition.TYPE_ITEM);
        ConditionStateItem state_2 = (ConditionStateItem)condition_2.getState();
        state_2.setSelectedIndex(0);

        Condition condition_3 = new Condition(conditionData);
        condition_3.setType(Condition.TYPE_ITEM);
        ConditionStateItem state_3 = (ConditionStateItem)condition_3.getState();
        state_3.setSelectedIndex(1);

        Condition condition_1 = new Condition(conditionData);
        condition_1.setType(Condition.TYPE_GROUP);
        ConditionStateGroup state_1 = (ConditionStateGroup)condition_1.getState();
        state_1.resetOperatorType(ConditionStateGroup.TYPE_DIFFERENCE_SET);
        state_1.updateOperand(0, condition_2);
        state_1.updateOperand(1, condition_3);

        Condition condition_4 = new Condition(conditionData);
        condition_4.setType(Condition.TYPE_ITEM);
        ConditionStateItem state_4 = (ConditionStateItem)condition_4.getState();
        state_4.setSelectedIndex(0);

        Condition condition_5 = new Condition(conditionData);
        condition_5.setType(Condition.TYPE_ITEM);
        ConditionStateItem state_5 = (ConditionStateItem)condition_5.getState();
        state_5.setSelectedIndex(1);

        Condition condition_6 = new Condition(conditionData);
        condition_6.setType(Condition.TYPE_GROUP);
        ConditionStateGroup state_6 = (ConditionStateGroup)condition_6.getState();
        state_6.resetOperatorType(ConditionStateGroup.TYPE_DIFFERENCE_SET);
        state_6.updateOperand(1, condition_4);
        state_6.updateOperand(0, condition_5);

        Condition baseCondition = new Condition(conditionData);

        baseCondition.setType(Condition.TYPE_GROUP);
        ConditionStateGroup state = (ConditionStateGroup)baseCondition.getState();
        state.resetOperatorType(ConditionStateGroup.TYPE_UNION);
        state.updateOperand(0, condition_1);
        state.updateOperand(1, condition_6);

        return baseCondition;
    }
}
