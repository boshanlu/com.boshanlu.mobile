package com.boshanlu.mobile.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.adapter.BaseAdapter;
import com.boshanlu.mobile.adapter.PostAdapter;
import com.boshanlu.mobile.database.MyDB;
import com.boshanlu.mobile.listener.ListItemClickListener;
import com.boshanlu.mobile.listener.LoadMoreListener;
import com.boshanlu.mobile.model.SingleArticleData;
import com.boshanlu.mobile.model.SingleType;
import com.boshanlu.mobile.model.VoteData;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.myhttp.SyncHttpClient;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.utils.KeyboardUtil;
import com.boshanlu.mobile.utils.LinkClickHandler;
import com.boshanlu.mobile.utils.UrlUtils;
import com.boshanlu.mobile.widget.MyFriendPicker;
import com.boshanlu.mobile.widget.MyListDivider;
import com.boshanlu.mobile.widget.emotioninput.SmileyInputRoot;
import com.boshanlu.mobile.widget.htmlview.VoteDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by free2 on 16-3-6.
 * 单篇文章activity
 * 一楼是楼主
 * 其余是评论
 */
public class PostActivity extends BaseActivity
        implements ListItemClickListener, LoadMoreListener.OnLoadMoreListener, View.OnClickListener {

    private RecyclerView topicList;
    //上一次回复时间
    private long replyTime = 0;
    private int currentPage = 1;
    private int sumPage = 1;
    private int edit_pos = -1;
    private boolean isGetTitle = false;
    private boolean enableLoadMore = false;
    //回复楼主的链接
    private String replyUrl = "";
    private PostAdapter adapter;
    private List<SingleArticleData> datas = new ArrayList<>();
    private boolean isSaveToDataBase = false;
    private String Title, AuthorName, Tid, RedirectPid = "";
    private boolean showPlainText = false;
    private EditText input;
    private SmileyInputRoot rootView;
    private ArrayAdapter<String> spinnerAdapter;
    private Spinner spinner;
    private List<String> pageSpinnerDatas = new ArrayList<>();

    public static void open(Context context, String url, @Nullable String author) {
        Intent intent = new Intent(context, PostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("url", url);
        intent.putExtra("author", TextUtils.isEmpty(author) ? "null" : author);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        initToolBar(true, "加载中......");
        input = findViewById(R.id.ed_comment);
        showPlainText = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("setting_show_plain", false);
        initCommentList();
        initEmotionInput();
        Bundle b = getIntent().getExtras();
        String url = b.getString("url");
        AuthorName = b.getString("author");
        Tid = GetId.getId("tid=", url);
        if (url != null && url.contains("redirect")) {
            RedirectPid = GetId.getId("pid=", url);
            url = url + "&mobile=2";
            HttpUtil.head(url, null, new ResponseHandler() {
                @Override
                public void onSuccess(byte[] response) {
                    int page = GetId.getPage(new String(response));
                    firstGetData(page);
                }
            });
        } else {
            firstGetData(1);
        }

        initSpinner();
    }

    private void initCommentList() {
        topicList = findViewById(R.id.topic_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        topicList.setLayoutManager(mLayoutManager);
        adapter = new PostAdapter(this, this, datas);
        topicList.addItemDecoration(new MyListDivider(this, MyListDivider.VERTICAL));
        topicList.addOnScrollListener(new LoadMoreListener(mLayoutManager, this, 8));
        topicList.setAdapter(adapter);
    }

    private void initEmotionInput() {
        View smileyBtn = findViewById(R.id.btn_emotion);
        View btnMore = findViewById(R.id.btn_more);
        View btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);
        rootView = findViewById(R.id.root);
        rootView.initSmiley(input, smileyBtn, btnSend);
        rootView.setMoreView(LayoutInflater.from(this).inflate(R.layout.my_smiley_menu, null), btnMore);

        topicList.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                KeyboardUtil.hideKeyboard(input);
                rootView.hideSmileyContainer();
            }
            return false;
        });

        MyFriendPicker.attach(this, input);
        findViewById(R.id.btn_star).setOnClickListener(this);
        findViewById(R.id.btn_link).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
    }

    private void initSpinner() {
        spinner = new Spinner(this);
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.my_post_spinner_item, pageSpinnerDatas);
        pageSpinnerDatas.add("第1页");
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (pos + 1 != currentPage) {
                    jumpPage(pos + 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        addToolbarView(spinner);
    }

    private void firstGetData(int page) {
        getArticleData(page);
    }

    @Override
    public void onLoadMore() {
        //加载更多被电击
        if (enableLoadMore) {
            enableLoadMore = false;
            if (currentPage < sumPage) {
                currentPage++;
            }
            getArticleData(currentPage);
        }
    }

    public void refresh() {
        adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
        //数据填充
        datas.clear();
        adapter.notifyDataSetChanged();
        getArticleData(1);
    }

    //文章一页的html 根据页数 Tid
    private void getArticleData(final int page) {
        String url = UrlUtils.getSingleArticleUrl(Tid, page);
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                new DealWithArticleData().execute(res);
            }

            @Override
            public void onFailure(Throwable e) {
                if (e != null && e == SyncHttpClient.NeedLoginError) {
                    isLogin();
                    Toast.makeText(getApplicationContext(), "此贴需要登录", Toast.LENGTH_SHORT).show();
                    return;
                }
                enableLoadMore = true;
                e.printStackTrace();
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
                Toast.makeText(getApplicationContext(), "加载失败(Error -1)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onListItemClick(View v, final int position) {
        switch (v.getId()) {
            case R.id.btn_reply_cz:
                if (isLogin()) {
                    SingleArticleData single = datas.get(position);
                    Intent i = new Intent(PostActivity.this, ReplyCzActivity.class);
                    i.putExtra("islz", single.uid.equals(datas.get(0).uid));
                    i.putExtra("data", single);
                    startActivityForResult(i, 20);
                }
                break;
            case R.id.need_loading_item:
                refresh();
                break;
            case R.id.tv_edit:
                edit_pos = position;
                Intent i = new Intent(this, EditActivity.class);
                i.putExtra("PID", datas.get(position).pid);
                i.putExtra("TID", Tid);
                startActivityForResult(i, 10);
                break;
            case R.id.tv_remove:
                edit_pos = position;
                new AlertDialog.Builder(this).
                        setTitle("删除帖子!").
                        setMessage("你要删除本贴/回复吗？").
                        setPositiveButton("删除", (dialog, which) -> removeItem(position))
                        .setNegativeButton("取消", null)
                        .setCancelable(true)
                        .create()
                        .show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                //编辑Activity返回
                Bundle b = data.getExtras();
                String title = b.getString("TITLE", "");
                String content = b.getString("CONTENT", "");
                if (edit_pos == 0 && !TextUtils.isEmpty(title)) {
                    datas.get(0).title = title;
                }
                datas.get(edit_pos).content = content;
                adapter.notifyItemChanged(edit_pos);
            } else if (requestCode == 20) {
                //回复层主返回
                replyTime = System.currentTimeMillis();
                if (currentPage == sumPage) {
                    onLoadMore();
                }
            }

        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                replyLz(replyUrl);
                break;
            case R.id.btn_star:
                if (isLogin()) {
                    showToast("正在收藏帖子...");
                    starTask(view);
                }
                break;
            case R.id.btn_link:
                String url = UrlUtils.getSingleArticleUrl(Tid, currentPage);
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText(null, url));
                Toast.makeText(this, "已复制链接到剪切板", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, Title + UrlUtils.getSingleArticleUrl(Tid, currentPage));
                shareIntent.setType("text/plain");
                //设置分享列表的标题，并且每次都显示分享列表
                startActivity(Intent.createChooser(shareIntent, "分享到文章到:"));
                break;
        }
    }

    /**
     * 处理数据类 后台进程
     */
    private class DealWithArticleData extends AsyncTask<String, Void, List<SingleArticleData>> {

        private String errorText = "";
        private int pageLoad = 1;

        @Override
        protected List<SingleArticleData> doInBackground(String... params) {
            errorText = "";
            List<SingleArticleData> tepdata = new ArrayList<>();
            String htmlData = params[0];
            if (!isGetTitle) {
                int ih = htmlData.indexOf("keywords");
                if (ih > 0) {
                    int h_start = htmlData.indexOf('\"', ih + 15);
                    int h_end = htmlData.indexOf('\"', h_start + 1);
                    Title = htmlData.substring(h_start + 1, h_end);
                    isGetTitle = true;
                }
            }

            Document doc = Jsoup.parse(htmlData.substring(
                    htmlData.indexOf("<body"),
                    htmlData.lastIndexOf("</body>") + 7));

            Elements as = doc.select(".footer a");
            if (as.size() > 1) {
                String hash = GetId.getHash(as.get(1).attr("href"));
                Log.v("hash", "hash is " + hash);
                App.setHash(PostActivity.this, hash);
            }

            //判断错误
            Elements elements = doc.select(".postlist");
            if (elements.size() <= 0) {
                //有可能没有列表处理错误
                errorText = doc.select(".jump_c").text();
                if (TextUtils.isEmpty(errorText)) {
                    errorText = "network error  !!!";
                }
                return tepdata;
            }

            //获得回复楼主的url
            if (TextUtils.isEmpty(replyUrl)) {
                String s = elements.select("form#fastpostform").attr("action");
                if (!TextUtils.isEmpty(s))
                    replyUrl = s;
            }

            //获取总页数 和当前页数
            if (doc.select(".pg").text().length() > 0) {
                if (doc.select(".pg").text().length() > 0) {
                    pageLoad = GetId.getNumber(doc.select(".pg").select("strong").text());
                    int n = GetId.getNumber(doc.select(".pg").select("span").attr("title"));
                    if (n > 0 && n > sumPage) {
                        sumPage = n;
                    }
                }
            }

            Elements postlist = elements.select("div[id^=pid]");
            int size = postlist.size();
            for (int i = 0; i < size; i++) {
                Element temp = postlist.get(i);
                String pid = temp.attr("id").substring(3);
                String uid = GetId.getId("uid=", temp.select("span[class=avatar]").select("img").attr("src"));
                Elements userInfo = temp.select("ul.authi");
                String commentIndex = userInfo.select("li.grey").select("em").text();
                String username = userInfo.select("a[href^=home.php?mod=space&uid=]").text();
                String postTime = userInfo.select("li.grey.rela").text().replace("收藏", "");
                String replyUrl = temp.select(".replybtn").select("input").attr("href");
                Elements contentels = temp.select(".message");

                //去除script
                contentels.select("script").remove();

                //是否移除所有样式
                if (showPlainText) {
                    //移除所有style
                    contentels.select("[style]").removeAttr("style");
                    contentels.select("font").removeAttr("color").removeAttr("size").removeAttr("face");
                }

                //处理代码
                for (Element codee : contentels.select(".blockcode")) {
                    codee.html("<code>" + codee.html().trim() + "</code>");
                }

                //处理引用
                for (Element codee : contentels.select("blockquote")) {
                    int start = codee.html().indexOf("发表于");
                    if (start > 0) {
                        Elements es = codee.select("a");
                        if (es.size() > 0 && es.get(0).text().contains("发表于")) {
                            String user = es.get(0).text().substring(0, es.get(0).text().indexOf(" "));
                            int sstart = codee.html().indexOf("<br>", start) + 4;
                            codee.html(user + ":" + codee.html().substring(sstart).replaceAll("<br>", " "));
                            break;
                        }
                    }
                }


                SingleArticleData data;
                if (pageLoad == 1 && i == 0) {//内容
                    //处理投票
                    VoteData d = null;
                    int maxSelection = 1;
                    Elements vote = contentels.select("form[action^=forum.php?mod=misc&action=votepoll]");
                    if (vote.size() > 0 && vote.select("input[type=submit]").size() > 0) {// 有且有投票权
                        if (vote.text().contains("单选投票")) {
                            maxSelection = 1;
                        } else if (vote.text().contains("多选投票")) {
                            int start = vote.text().indexOf("多选投票");
                            maxSelection = GetId.getNumber(vote.text().substring(start, start + 20));
                        }

                        Elements ps = vote.select("p");
                        List<Pair<String, String>> options = new ArrayList<>();
                        for (Element p : ps) {
                            if (p.select("input").size() > 0)
                                options.add(new Pair<>(p.select("input").attr("value"),
                                        p.select("label").text()));
                        }

                        if (ps.select("input").get(0).attr("type").equals("radio")) {
                            maxSelection = 1;
                        }

                        vote.select("input[type=submit]").get(0).html("<a href=\"" +
                                LinkClickHandler.VOTE_URL + "\">点此投票</a><br>");
                        d = new VoteData(vote.attr("action"), options, maxSelection);
                    }
                    data = new SingleArticleData(SingleType.CONTENT, Title, uid,
                            username, postTime,
                            commentIndex, replyUrl, contentels.html().trim(), pid);
                    data.vote = d;
                    AuthorName = username;
                    if (!isSaveToDataBase) {
                        //插入数据库
                        MyDB myDB = new MyDB(PostActivity.this);
                        myDB.handSingleReadHistory(Tid, Title, AuthorName);
                        isSaveToDataBase = true;
                    }
                } else {//评论
                    data = new SingleArticleData(SingleType.COMMENT, Title, uid,
                            username, postTime, commentIndex, replyUrl, contentels.html().trim(), pid);
                }
                tepdata.add(data);
            }
            return tepdata;
        }

        @Override
        protected void onPostExecute(List<SingleArticleData> tepdata) {
            enableLoadMore = true;
            if (isGetTitle) {
                setTitle("帖子正文");
            }

            if (pageLoad != currentPage) {
                currentPage = pageLoad;
                spinner.setSelection(currentPage - 1);
            }

            if (!TextUtils.isEmpty(errorText)) {
                Toast.makeText(PostActivity.this, errorText, Toast.LENGTH_SHORT).show();
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
                return;
            }
            if (tepdata.size() == 0) {
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
                return;
            }

            int startsize = datas.size();
            if (datas.size() == 0) {
                datas.addAll(tepdata);
            } else {
                String strindex = datas.get(datas.size() - 1).index;
                if (TextUtils.isEmpty(strindex)) {
                    strindex = "-1";
                } else if (strindex.equals("沙发")) {
                    strindex = "1";
                } else if (strindex.equals("板凳")) {
                    strindex = "2";
                } else if (strindex.equals("地板")) {
                    strindex = "3";
                }
                int index = GetId.getNumber(strindex);
                for (int i = 0; i < tepdata.size(); i++) {
                    String strindexp = tepdata.get(i).index;
                    if (strindexp.equals("沙发")) {
                        strindexp = "1";
                    } else if (strindex.equals("板凳")) {
                        strindexp = "2";
                    } else if (strindex.equals("地板")) {
                        strindexp = "3";
                    }
                    int indexp = GetId.getNumber(strindexp);
                    if (indexp > index) {
                        datas.add(tepdata.get(i));
                    }
                }
            }

            if (currentPage < sumPage) {
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
            } else {
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
            }

            if (datas.size() > 0 && (datas.get(0).type != SingleType.CONTENT) &&
                    (datas.get(0).type != SingleType.HEADER)) {
                datas.add(0, new SingleArticleData(SingleType.HEADER, Title,
                        null, null, null, null, null, null, null));
            }
            int add = datas.size() - startsize;
            if (startsize == 0) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(startsize, add);
            }

            //打开的时候移动到指定楼层
            if (!TextUtils.isEmpty(RedirectPid)) {
                for (int i = 0; i < datas.size(); i++) {
                    if (!TextUtils.isEmpty(datas.get(i).pid)
                            && datas.get(i).pid.equals(RedirectPid)) {
                        topicList.scrollToPosition(i);
                        break;
                    }
                }
                RedirectPid = "";
            }

            int size = pageSpinnerDatas.size();
            if (sumPage != size) {
                pageSpinnerDatas.clear();
                for (int i = 1; i <= sumPage; i++) {
                    pageSpinnerDatas.add("第" + i + "页");
                }
                spinnerAdapter.notifyDataSetChanged();
                spinner.setSelection(currentPage - 1);
            }
        }

    }

    /**
     * 收藏帖子
     */
    private void starTask(final View v) {
        final String url = UrlUtils.getStarUrl(Tid);
        Map<String, String> params = new HashMap<>();
        params.put("favoritesubmit", "true");
        HttpUtil.post(url, params, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                if (res.contains("成功") || res.contains("您已收藏")) {
                    showToast("收藏成功");
                    if (v != null) {
                        final ImageView mv = (ImageView) v;
                        mv.postDelayed(() -> mv.setImageResource(R.drawable.ic_star_32dp_yes), 300);
                    }
                }
            }
        });
    }

    //删除帖子或者回复
    private void removeItem(final int pos) {
        Map<String, String> params = new HashMap<>();
        params.put("editsubmit", "yes");
        //params.put("fid",);
        params.put("tid", Tid);
        params.put("pid", datas.get(pos).pid);
        params.put("delete", "1");
        HttpUtil.post(UrlUtils.getDeleteReplyUrl(), params, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                Log.e("resoult", res);
                if (res.contains("主题删除成功")) {
                    if (datas.get(pos).type == SingleType.CONTENT) {
                        showToast("主题删除成功");
                        finish();
                    } else {
                        showToast("回复删除成功");
                        datas.remove(pos);
                        adapter.notifyItemRemoved(pos);
                    }
                } else {
                    int start = res.indexOf("<p>");
                    int end = res.indexOf("<", start + 5);
                    String ss = res.substring(start + 3, end);
                    showToast(ss);
                }

            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                showToast("网络错误,删除失败！");
            }
        });
    }

    //回复楼主
    private void replyLz(String url) {
        if (!(isLogin() && checkTime() && checkInput())) {
            return;
        }
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("回复中");
        dialog.setMessage("请稍后......");
        dialog.show();

        String s = getPreparedReply(this, input.getText().toString());
        Map<String, String> params = new HashMap<>();
        params.put("message", s);
        HttpUtil.post(url + "&handlekey=fastpost&loc=1&inajax=1", params, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                handleReply(true, res);
            }

            @Override
            public void onFailure(Throwable e) {
                handleReply(false, "");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dialog.dismiss();
            }
        });
    }


    public static String getPreparedReply(Context context, String text) {
        int len = 0;
        try {
            len = text.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(context);
        if (shp.getBoolean("setting_show_tail", false)) {
            String texttail = shp.getString("setting_user_tail", "无尾巴").trim();
            if (!texttail.equals("无尾巴")) {
                texttail = "\r\n" + texttail;
                text += texttail;
            }
        }

        //字数补齐补丁
        if (len < 13) {
            int need = 14 - len;
            for (int i = 0; i < need; i++) {
                text += " ";
            }
        }

        return text;
    }

    private void handleReply(boolean isok, String res) {
        if (isok) {
            if (res.contains("成功") || res.contains("层主")) {
                Toast.makeText(this, "回复发表成功", Toast.LENGTH_SHORT).show();
                input.setText(null);
                replyTime = System.currentTimeMillis();
                KeyboardUtil.hideKeyboard(input);
                rootView.hideSmileyContainer();
                if (sumPage == 1) {
                    refresh();
                } else if (currentPage == sumPage) {
                    onLoadMore();
                }
            } else if (res.contains("您两次发表间隔")) {
                showToast("您两次发表间隔太短了......");
            } else if (res.contains("主题自动关闭")) {
                showLongToast("此主题已关闭回复,无法回复");
            } else {
                showToast("由于未知原因发表失败");
            }
        } else {
            Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
        }
    }

    //跳页
    private void jumpPage(int to) {
        datas.clear();
        adapter.notifyDataSetChanged();
        getArticleData(to);
    }

    private boolean checkInput() {
        String s = input.getText().toString();
        if (TextUtils.isEmpty(s)) {
            showToast("你还没写内容呢!");
            return false;
        } else {
            return true;
        }
    }

    private boolean checkTime() {
        if (System.currentTimeMillis() - replyTime > 15000) {
            return true;
        } else {
            showToast("还没到15s呢，再等等吧!");
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (!rootView.hideSmileyContainer()) {
            super.onBackPressed();
        }
    }

    //显示投票dialog
    public void showVoteView() {
        if (datas.get(0).type == SingleType.CONTENT) {
            VoteData d = datas.get(0).vote;
            if (d != null) {
                VoteDialog.show(this, d);
                return;
            }

        }
        showToast("投票数据异常无法投票");
    }

    //用户点击了回复链接
    //显示软键盘
    public void showReplyKeyboard() {
        KeyboardUtil.showKeyboard(input);
    }
}
