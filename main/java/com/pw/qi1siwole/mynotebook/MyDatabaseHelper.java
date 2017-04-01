package com.pw.qi1siwole.mynotebook;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pw.qi1siwole.mynotebook.Common.Common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017/3/17.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_WORD = "create table Word ("
            + "id integer primary key autoincrement, "
            + "word text, "
            + "tag text)";

    public static final String CREATE_TAG = "create table Tag ("
            + "id integer primary key autoincrement, "
            + "tag text)";

    public static final int VERSION_1_0 = 1;
    public static final String DB_NAME = "Word.db";

    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    private static MyDatabaseHelper mDbHelper;

    public static MyDatabaseHelper initDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        if (null == mDbHelper) {
            mDbHelper = new MyDatabaseHelper(context, name, factory, version);
        }
        return mDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WORD);
        db.execSQL(CREATE_TAG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    /* 查询所有Tag */
    public static List<String> queryTagListFromDB() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select distinct tag from Tag order by tag", null);

        List<String> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String tagText = cursor.getString(cursor.getColumnIndex("tag"));
                list.add(tagText);
            } while (cursor.moveToNext());
        }

        return list;
    }

    /* 返回Tag的Word集合 */
    public static Set<String> queryWordSetByTagFromDB(String tagText) {
        Set<String> set = new HashSet<>();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select word from Word where tag = '" + tagText + "'", null);
        if (cursor.moveToFirst()) {
            do {
                String wordText = cursor.getString(cursor.getColumnIndex("word"));
                set.add(wordText);
            } while (cursor.moveToNext());
        }
        return set;
    }

    /* 查询Word表中是否存在word */
    public static boolean isWordExistInWordFromDB(String wordText) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select 1 from Word where word = '" + wordText + "'", null);
        return cursor.moveToFirst();
    }

    /* 返回Word的Tag集合（第二个参数：是否包含空标签） */
    public static Set<String> queryTagSetByWordFromDB(String wordText, boolean containsNullTag) {
        Set<String> set = new HashSet<>();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
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

    /* 添加Tag */
    public static void insertTagIntoDB(String tagText) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("insert into Tag(tag) values(?)", new String[]{tagText});
    }

    private static void doSQLWithCollection(Collection<String> collection, String sql_format) {
        String[] texts = collection.toArray(new String[collection.size()]);
        String str = Common.getStringWithJoin(collection, ", ", "?");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(String.format(sql_format, str), texts);
    }

    /* 删除Tag中的记录*/
    public static void deleteFromTagFromDB(Collection<String> collection) {
        final String SQL_FORMAT = "delete from Tag where tag in (%s)";
        doSQLWithCollection(collection, SQL_FORMAT);
    }

    /* 删除Word中的记录：给定word相关记录完全删除 */
    public static void deleteFromWordUsingWordFromDB(Collection<String> collection) {
        final String SQL_FORMAT = "delete from Word where word in (%s)";
        doSQLWithCollection(collection, SQL_FORMAT);
    }

    /* 删除Word中的记录：给定tag相关记录完全删除 */
    public static void deleteFromWordUsingTagFromDB(Collection<String> collection) {
        final String SQL_FORMAT = "delete from Word where tag in (%s)";
        doSQLWithCollection(collection, SQL_FORMAT);
    }

    /* 更新Word中的tag值 */
    private static void updateTagValInWordFromDB(Collection<String> collection, String newVal) {
        final String SQL_FORMAT = "update Word set tag = '" + newVal + "' where tag in (%s)";
        doSQLWithCollection(collection, SQL_FORMAT);
    }
    public static void updateOneTagValInWordFromDB(String tagText, String newVal) {
        Collection<String> collection = new ArrayList<>();
        collection.add(tagText);
        updateTagValInWordFromDB(collection, newVal);
    }

    /* 从Word表中删除无用的记录（只适用仅删除一个Tag） */
    public static void deleteInValidRecordFromWordFromDB() {
        final String SQL =  "delete from Word where word in "
                + "(select distinct word from Word where word in "
                +"(select word from Word where tag = '') and tag <> '') and tag = ''";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(SQL);
    }

    /* 部分删除Word记录（包括关联所选标签，以及无标签记录） */
    public static Set<String> deletePartFromWordFromDB(List<String> tagTextList, List<String> wordTextList) {
        Set<String> removedWordTextSet = new HashSet<>();

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

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
    public static void insertWordIntoDB(List<String> tagTextList, List<String> wordTextList) {
        final String SQL = "insert into Word (word, tag) values (?, ?)";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
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
    public static void insertIntoWordSelectFromDB(List<String> tagTextList, String newTagText, boolean isReallyNew) {
        final String SQL_FORMAT_1 = "insert into Word (word, tag)"
                + " select distinct word, '%s' from Word where tag in (%s)";
        final String SQL_FORMAT_2 = " and word not in ("
                + " select word from Word where tag = '%s')";

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String strTags = Common.getStringWithJoin(tagTextList, ", ", "'%s'");
        String sql = String.format(SQL_FORMAT_1, newTagText, strTags);
        if (!isReallyNew) {
            sql += String.format(SQL_FORMAT_2, newTagText);
        }
        db.execSQL(sql);
    }

    /* 更改Word中的word值 */
    public static void updateWordWithWordFromDB(String oriWordText, String newWordText, boolean isReallyNew) {
        final String SQL_FORMAT_1 = "update Word set word = '%s' where word = '%s'";
        final String SQL_FORMAT_2 = "insert into Word (word, tag)"
                + " select '%s', tag from Word where word = '%s' and tag not in ("
                + " select tag from Word where word = '%s')";
        String sql_1 = String.format(SQL_FORMAT_1, newWordText, oriWordText);
        String sql_2 = String.format(SQL_FORMAT_2, newWordText, oriWordText, newWordText);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(isReallyNew ? sql_1 : sql_2);
    }
}
