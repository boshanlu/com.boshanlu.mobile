package com.boshanlu.mobile.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.boshanlu.mobile.model.ArticleListData;
import com.boshanlu.mobile.model.ForumListData;
import com.boshanlu.mobile.model.ReadHistoryData;
import com.boshanlu.mobile.utils.GetId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MyDB {
    /**
     * 浏览历史表
     */
    static final String TABLE_READ_HISTORY = "rs_article_list";
    /**
     * 板块列表 表
     */
    static final String TABLE_FORUM_LIST = "rs_forum_list";
    private Context context;
    private SQLiteDatabase db = null;    //数据库操作


    //构造函数
    public MyDB(Context context) {
        this.context = context;
        this.db = new SQLiteHelper(context).getWritableDatabase();
    }

    private SQLiteDatabase getDb() {
        if (this.db == null || !this.db.isOpen()) {
            this.db = new SQLiteHelper(context).getWritableDatabase();
        }
        return this.db;
    }

    private String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());

        return format.format(curDate);
    }

    private Date getDate(String str) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        return format.parse(str);
    }

    //处理单个点击事件 判断是更新还是插入
    public void handSingleReadHistory(String tid, String title, String author) {
        if (null == title) {
            title = "null";
        }
        if (null == author) {
            author = "null";
        }
        if (isArticleRead(tid)) {
            updateReadHistory(tid, title, author);
        } else {
            insertReadHistory(tid, title, author);
        }
    }

    //判断list<> 是否为已读并修改返回
    public List<ArticleListData> handReadHistoryList(List<ArticleListData> datas) {
        getDb();
        String sql = "SELECT tid from " + TABLE_READ_HISTORY + " where tid = ?";
        for (ArticleListData data : datas) {
            String tid = GetId.getId("tid=", data.titleUrl);
            String args[] = new String[]{String.valueOf(tid)};
            Cursor result = db.rawQuery(sql, args);
            int count = result.getCount();
            result.close();
            if (count != 0)//判断得到的返回数据是否为空
            {
                data.isRead = true;
            }
        }
        this.db.close();
        return datas;
    }

    //判断插入数据的ID是否已经存在数据库中。
    private boolean isArticleRead(String tid) {
        getDb();
        String sql = "SELECT tid from " + TABLE_READ_HISTORY + " where tid = ?";
        String args[] = new String[]{String.valueOf(tid)};
        Cursor result = db.rawQuery(sql, args);
        int count = result.getCount();
        result.close();
        this.db.close();
        return count != 0;
    }

    //	//插入操作
    private void insertReadHistory(String tid, String title, String author) {
        getDb();
        String sql = "INSERT INTO " + TABLE_READ_HISTORY + " (tid,title,author,read_time)"
                + " VALUES(?,?,?,?)";
        String read_time_str = getTime();
        Object args[] = new Object[]{tid, title, author, read_time_str};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    //更新操作
    private void updateReadHistory(String tid, String title, String author) {
        getDb();
        String read_time_str = getTime();
        String sql = "UPDATE " + TABLE_READ_HISTORY + " SET title=?,read_time=? WHERE tid=?";
        Object args[] = new Object[]{title, read_time_str, tid};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void clearHistory() {
        getDb();
        String sql = "DELETE FROM " + TABLE_READ_HISTORY;
        this.db.execSQL(sql);
        this.db.close();
    }

    public void deleteOldHistory(int num) {
        //最长缓存2000条数据 num 2000
        getDb();
        Cursor cursor = this.db.rawQuery("SELECT COUNT(*) FROM " + TABLE_READ_HISTORY, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        int a = count - num;
        if (a > 0) {
            //大于就一次性删除1/5
            a = num / 5;
            //DELETE FROM XXX WHERE tid IN (SELECT TOP 100 PurchaseOrderDetailID FROM Purchasing.PurchaseOrderDetail
            //ORDER BY DueDate DESC);
            String sql = "DELETE FROM " + TABLE_READ_HISTORY + " WHERE tid IN (SELECT tid FROM " + TABLE_READ_HISTORY
                    + "  ORDER BY read_time ASC limit " + a + ")";
            this.db.rawQuery(sql, null);

            Log.e("阅读历史", "删除了最后" + a + "条记录");
        }

        this.db.close();
    }

    public List<ReadHistoryData> getHistory(int num) {
        getDb();
        List<ReadHistoryData> datas = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_READ_HISTORY + " order by read_time desc limit " + num;
        Cursor result = this.db.rawQuery(sql, null);    //执行查询语句
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            datas.add(new ReadHistoryData(result.getString(0), result.getString(1), result.getString(3), result.getString(2)));
        }
        result.close();
        this.db.close();
        return datas;
    }

    /**
     * 获得板块列表
     */
    public List<ForumListData> getForums() {
        getDb();
        List<ForumListData> datas = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_FORUM_LIST;
        Cursor result = this.db.rawQuery(sql, null);    //执行查询语句
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            boolean isHeader = (result.getInt(3) == 1);
            int fid = result.getInt(1);
            String name = result.getString(0);
            String todayNew = result.getString(2);
            //String todayNew, String titleUrl
            datas.add(new ForumListData(isHeader, name, fid));
        }
        result.close();
        this.db.close();
        return datas;
    }

    /**
     * 设置板块列表到数据库
     */
    public void setForums(List<ForumListData> datas) {
        if (datas != null && datas.size() > 0) {
            clearForums();
            getDb();
            for (ForumListData d : datas) {
                String sql = "INSERT INTO " + TABLE_FORUM_LIST + " (name,fid,todayNew,isHeader)"
                        + " VALUES(?,?,?,?)";
                int isHeader = d.isheader ? 1 : 0;
                Object args[] = new Object[]{d.title, d.fid, d.todayNew, isHeader};
                try {
                    this.db.execSQL(sql, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.db.close();
        }
    }

    /**
     * 清空板块数据库
     */
    public void clearForums() {
        getDb();
        String sql = "DELETE FROM " + TABLE_FORUM_LIST;
        this.db.execSQL(sql);
        this.db.close();
    }

}