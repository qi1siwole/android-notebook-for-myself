package com.pw.qi1siwole.mynotebook;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements Common.OnListItemCheckedListener {

    private RadioGroup mRadioGroup;

    private EditText mWordEditText;
    private EditText mTagEditText;

    private Button mAddWordButton;
    private Button mAddTagButton;

    private ListView mWordListView;
    private ListView mTagListView;

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

        dbHelper = new MyDatabaseHelper(this, "Word.db", null, 1);

        // 单选框组合：名称And标签
        mRadioGroup = (RadioGroup)findViewById(R.id.radio_group);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRadioButtonCheckedChanged();
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
            default:
        }

        return true;
    }

    @Override
    public void onListItemChecked(int id) {
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

        this.insertTagIntoDB(inputText);
        Tag tag = new Tag(inputText, isWordInputValid());
        mTagList.add(tag);
        mTagAdapter.notifyDataSetChanged();
    }

    /* 事件：点击菜单项<显示操作对象的一方映射到另一方的列表> */
    private void onShowMapItemSelected() {
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
        Set<String> set = queryWordSetByTagFromDB("");
        List<String> list = new ArrayList<>();
        list.addAll(set);
        Collections.sort(list);
        for (String wordText: list) {
            Word word = new Word(wordText);
            mWordList.add(word);
        }
        mWordAdapter.notifyDataSetChanged();
    }

    /*******************************************************
     【数据库操作】
     *******************************************************/
    /* 查询Word表中是否存在word */
    private boolean isWordExistInWordFromDB(String wordText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select 1 from Word where word = '" + wordText + "'", null);
        return cursor.moveToFirst();
    }

    /* 返回Tag的Word集合 */
    private Set<String> queryWordSetByTagFromDB(String tagText) {
        Set<String> set = new HashSet<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select word from Word where tag = '" + tagText + "'", null);
        if (cursor.moveToFirst()) {
            do {
                String wordText = cursor.getString(cursor.getColumnIndex("word"));
                set.add(wordText);
            } while (cursor.moveToNext());
        }
        return set;
    }

    /* 返回Word的Tag集合（第二个参数：是否包含空标签） */
    private Set<String> queryTagSetByWordFromDB(String wordText, boolean containsNullTag) {
        Set<String> set = new HashSet<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select tag from Word where word = '" + wordText + "'", null);
        if (cursor.moveToFirst()) {
            do {
                String tagText = cursor.getString(cursor.getColumnIndex("tag"));
                if (!containsNullTag && tagText.isEmpty()) {
                    continue;
                }
                set.add(tagText);
            } while (cursor.moveToNext());
        }
        return set;
    }

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
    
    /* 添加Tag */
    private void insertTagIntoDB(String tagText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("insert into Tag(tag) values(?)", new String[]{tagText});
    }

    private void doSQLWithCollection(Collection<String> collection, String sql_format) {
        String[] texts = collection.toArray(new String[collection.size()]);
        String str = Common.getStringWithJoin(collection, ", ", "?");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(String.format(sql_format, str), texts);
    }

    /* 删除Tag中的记录*/
    private void deleteFromTagFromDB(Collection<String> collection) {
        final String SQL_FORMAT = "delete from Tag where tag in (%s)";
        this.doSQLWithCollection(collection, SQL_FORMAT);
    }

    /* 删除Word中的记录：给定word相关记录完全删除 */
    private void deleteFromWordUsingWordFromDB(Collection<String> collection) {
        final String SQL_FORMAT = "delete from Word where word in (%s)";
        this.doSQLWithCollection(collection, SQL_FORMAT);
    }

    /* 删除Word中的记录：给定tag相关记录完全删除 */
    private void deleteFromWordUsingTagFromDB(Collection<String> collection) {
        final String SQL_FORMAT = "delete from Word where tag in (%s)";
        this.doSQLWithCollection(collection, SQL_FORMAT);
    }

    /* 更新Word中的tag值 */
    private void updateTagValInWordFromDB(Collection<String> collection, String newVal) {
        final String SQL_FORMAT = "update Word set tag = '" + newVal + "' where tag in (%s)";
        this.doSQLWithCollection(collection, SQL_FORMAT);
    }
    private void updateOneTagValInWordFromDB(String tagText, String newVal) {
        Collection<String> collection = new ArrayList<>();
        collection.add(tagText);
        this.updateTagValInWordFromDB(collection, newVal);
    }

    /* 从Word表中删除无用的记录（只适用仅删除一个Tag） */
    private void deleteInValidRecordFromWordFromDB() {
        final String SQL =  "delete from Word where word in "
                            + "(select distinct word from Word where word in "
                            +"(select word from Word where tag = '') and tag <> '') and tag = ''";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(SQL);
    }

    /* 部分删除Word记录（包括关联所选标签，以及无标签记录） */
    private Set<String> deletePartFromWordFromDB(List<String> tagTextList, List<String> wordTextList) {
        Set<String> removedWordTextSet = new HashSet<>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String strWords = Common.getStringWithJoin(wordTextList, ", ", "'%s'");

        Cursor cursor = db.rawQuery(String.format("select word from Word where word in (%s) and tag = ''", strWords), null);
        if (cursor.moveToFirst()) {
            do {
                String wordText = cursor.getString(cursor.getColumnIndex("word"));
                removedWordTextSet.add(wordText);
            } while (cursor.moveToNext());
        }

        db.execSQL(String.format("delete from Word where word in (%s) and tag = ''", strWords));

        if (!tagTextList.isEmpty()) {
            String strTags = Common.getStringWithJoin(tagTextList, ", ", "'%s'");

            cursor = db.rawQuery(String.format("select distinct word from Word where word in (%s) and tag in (%s)", strWords, strTags), null);
            if (cursor.moveToFirst()) {
                do {
                    String wordText = cursor.getString(cursor.getColumnIndex("word"));
                    removedWordTextSet.add(wordText);
                } while (cursor.moveToNext());
            }

            db.execSQL(String.format("delete from Word where word in (%s) and tag in (%s)", strWords, strTags));
        }

        return removedWordTextSet;
    }

    /* 添加所有选中的Tag与Word的关联 */
    private void insertWordIntoDB(List<String> tagTextList, List<String> wordTextList) {
        final String SQL = "insert into Word (word, tag) values (?, ?)";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String tagText: tagTextList) {
                for (String wordText: wordTextList) {
                    db.execSQL(SQL, new String[] { wordText, tagText });
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /* 添加数据到Word：使用表中已有tag的word数据，赋给新tag值 */
    private void insertIntoWordSelectFromDB(List<String> tagTextList, String newTagText, boolean isReallyNew) {
        final String SQL_FORMAT_1 = "insert into Word (word, tag)"
                + " select distinct word, '%s' from Word where tag in (%s)";
        final String SQL_FORMAT_2 = " and word not in ("
                + " select word from Word where tag = '%s')";

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String strTags = Common.getStringWithJoin(tagTextList, ", ", "'%s'");
        String sql = String.format(SQL_FORMAT_1, newTagText, strTags);
        if (!isReallyNew) {
            sql += String.format(SQL_FORMAT_2, newTagText);
        }
        db.execSQL(sql);
    }

    /* 更改Word中的word值 */
    private void updateWordWithWordFromDB(String oriWordText, String newWordText, boolean isReallyNew) {
        final String SQL_FORMAT_1 = "update Word set word = '%s' where word = '%s'";
        final String SQL_FORMAT_2 = "insert into Word (word, tag)"
                + " select '%s', tag from Word where word = '%s' and tag not in ("
                + " select tag from Word where word = '%s')";
        String sql_1 = String.format(SQL_FORMAT_1, newWordText, oriWordText);
        String sql_2 = String.format(SQL_FORMAT_2, newWordText, oriWordText, newWordText);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(isReallyNew ? sql_1 : sql_2);
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
        builder.setNeutralButton(R.string.delete_word_dialog_neutral,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeWordRelyOnTag(wordTextList);
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
        boolean isReallyNew = !this.isWordExistInWordFromDB(newWordText);

        // Word
        this.updateWordWithWordFromDB(oriWordText, newWordText, isReallyNew);
        if (!isReallyNew) {
            this.deleteInValidRecordFromWordFromDB();
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
        this.deleteFromTagFromDB(tagTextList);
        if (isReallyNew) {
            this.insertTagIntoDB(newTagText);
        }

        // Word
        this.insertIntoWordSelectFromDB(tagTextList, newTagText, isReallyNew);

        this.deleteFromWordUsingTagFromDB(tagTextList);

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
        this.deletePartFromWordFromDB(tagTextList, wordTextList);
        this.insertWordIntoDB(tagTextList, wordTextList);
    }

    // 删除Word：完全删除
    private void removeWordCompletely(List<String> wordTextList) {
        this.deleteFromWordUsingWordFromDB(wordTextList);
        this.removeCheckedWordOnUI();
    }

    // 删除Word：部分删除
    private void removeWordRelyOnTag(List<String> wordTextList) {
        Set<String> removedWordTextSet = this.deletePartFromWordFromDB(this.getCheckedTagTextList(), wordTextList);
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
        this.deleteFromTagFromDB(tagTextList);
        this.removeOnlyTagFromWord(tagTextList);
        this.removeTagOnUI();
    }

    // 从Word表中移除给定tag相关的记录（word部分保留）
    private void removeOnlyTagFromWord(Collection<String> collection) {
        for (String tagText: collection) {
            this.updateOneTagValInWordFromDB(tagText, "");
            this.deleteInValidRecordFromWordFromDB();
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
                Set<String> newSet = queryWordSetByTagFromDB(tag.getName());
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
                Set<String> newSet = queryTagSetByWordFromDB(word.getText(), false);
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
}
