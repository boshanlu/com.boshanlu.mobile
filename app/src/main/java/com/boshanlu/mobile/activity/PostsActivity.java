package com.boshanlu.mobile.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.adapter.BaseAdapter;
import com.boshanlu.mobile.adapter.PostListAdapter;
import com.boshanlu.mobile.database.MyDB;
import com.boshanlu.mobile.listener.HidingScrollListener;
import com.boshanlu.mobile.listener.LoadMoreListener;
import com.boshanlu.mobile.model.ArticleListData;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.DimmenUtils;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.utils.UrlUtils;
import com.boshanlu.mobile.widget.MyListDivider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 一般文章列表
 * 链接到校园网时 getPostsRs
 * 外网时 getPostsMe
 * 2个是不同的
 */
public class PostsActivity extends BaseActivity implements
        LoadMoreListener.OnLoadMoreListener, View.OnClickListener {

    private final String[] orders = new String[]{
            "&filter=lastpost&orderby=lastpost",
            "&filter=heat&orderby=heats",
            "&filter=hot",
            "&filter=digest&digest=1"
    };
    protected SwipeRefreshLayout refreshLayout;
    FloatingActionButton btnRefresh;
    RecyclerView mRecyclerView;
    //当前页数
    int currentPage = 1;
    int maxPage = 1;
    boolean isEnableLoadMore = false;
    RecyclerView.LayoutManager mLayoutManager;
    boolean isHideZhiding = false;
    private int FID = 72;
    private String TITLE;
    private View myToolbar;
    private TabLayout tab;
    //一般板块/图片板块/手机板块数据列表
    private List<ArticleListData> datas;
    private PostListAdapter adapter;
    private MyDB myDB = null;
    private int currentTabindex = 0;


    public static void open(Context context, int fid, String title) {
        Intent intent = new Intent(context, PostsActivity.class);
        intent.putExtra("FID", fid);
        intent.putExtra("TITLE", title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datas = new ArrayList<>();
        setContentView(R.layout.activity_posts);
        if (getIntent().getExtras() != null) {
            FID = getIntent().getExtras().getInt("FID");
            TITLE = getIntent().getExtras().getString("TITLE");
        }
        initToolBar(true, TITLE);
        myToolbar = findViewById(R.id.myToolBar);
        btnRefresh = findViewById(R.id.btn);
        mRecyclerView = findViewById(R.id.recycler_view);
        tab = findViewById(R.id.tab);

        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.red_light, R.color.green_light, R.color.blue_light, R.color.orange_light);
        int top = DimmenUtils.dip2px(this, 60);
        refreshLayout.setProgressViewOffset(true, top, top + 60);

        isHideZhiding = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("setting_hide_zhidin", true);
        if (getType() == PostListAdapter.TYPE_IMAGE) {
            isEnableLoadMore = false;
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            mRecyclerView.setHasFixedSize(false);
            addToolbarMenu(R.drawable.ic_column_change_24dp).setOnClickListener(this);
        } else {
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new MyListDivider(this, MyListDivider.VERTICAL));
            mRecyclerView.addOnScrollListener(new LoadMoreListener((LinearLayoutManager) mLayoutManager, this, 8));
            addToolbarMenu(R.drawable.ic_edit).setOnClickListener(this);
        }

        tab.addTab(tab.newTab().setText("最新"));
        tab.addTab(tab.newTab().setText("热门"));
        tab.addTab(tab.newTab().setText("热帖"));
        tab.addTab(tab.newTab().setText("精华"));

        adapter = new PostListAdapter(this, datas, getType());
        if (getType() == PostListAdapter.TYPE_IMAGE) {
            adapter.setEnablePlaceHolder(false);
        }
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(adapter);
        myDB = new MyDB(this);
        datas.clear();
        btnRefresh.setOnClickListener(v -> refresh());
        init();
        //子类实现获取数据
        getData();
    }

    private int getType() {
        return PostListAdapter.TYPE_NORMAL_MOBILE;
        /*
        if (App.IS_SCHOOL_NET && (FID == 561 || FID == 157 || FID == 13)) {
            return PostListAdapter.TYPE_IMAGE;
        } else if (App.IS_SCHOOL_NET) {
            return PostListAdapter.TYPE_NORMAL;
        } else {
            return PostListAdapter.TYPE_NORMAL_MOBILE;
        }*/
    }

    @Override
    public void onLoadMore() {
        if (isEnableLoadMore) {
            if (currentPage < maxPage) {
                currentPage++;
            }
            isEnableLoadMore = false;
            getData();
        }
    }

    private void init() {
        btnRefresh.hide();
        refreshLayout.setRefreshing(true);
        refreshLayout.setOnRefreshListener(this::refresh);

        //隐藏按钮
        mRecyclerView.addOnScrollListener(new HidingScrollListener(getResources().getDimensionPixelSize(R.dimen.toolbarHeight)) {
            @Override
            public void onHide() {
                int distanceToScroll = btnRefresh.getHeight() + DimmenUtils.dip2px(PostsActivity.this, 16);
                btnRefresh.animate().translationY(distanceToScroll).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200);

                //隐藏toolbar
                myToolbar.animate().translationY(-myToolbar.getHeight()).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200);
                tab.animate().translationY(-myToolbar.getHeight()).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200);
            }

            @Override
            public void onShow() {
                btnRefresh.animate().translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200);

                //显示toolbar
                myToolbar.animate().translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200);
                tab.animate().translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200);
            }
        });


        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabindex = tab.getPosition();
                refresh();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mRecyclerView.scrollToPosition(0);
            }
        });
    }

    private void refresh() {
        btnRefresh.hide();
        refreshLayout.setRefreshing(true);
        currentPage = 1;
        maxPage = 1;
        getData();
    }


    private void getData() {
        adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
        String url = UrlUtils.getArticleListUrl(FID, currentPage);
        url = url + UrlUtils.getArticleListUrl(FID, currentPage);
        url = url + orders[currentTabindex];
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String s = new String(response);
                switch (getType()) {
                    case PostListAdapter.TYPE_IMAGE:
                        new getImagePosts().execute(s);
                        break;
                    case PostListAdapter.TYPE_NORMAL:
                        new getPostsRs().execute(s);
                        break;
                    case PostListAdapter.TYPE_NORMAL_MOBILE:
                        //外网
                        new getPostsMe().execute(s);
                        break;
                }
            }

            @Override
            public void onFailure(Throwable e) {
                //Toast.makeText(getActivity(), "网络错误！！", Toast.LENGTH_SHORT).show();
                refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 500);

                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu:
                if (getType() == PostListAdapter.TYPE_IMAGE) {
                    StaggeredGridLayoutManager m = (StaggeredGridLayoutManager) mLayoutManager;
                    int span = m.getSpanCount();
                    if (span == 1) {
                        m.setSpanCount(2);
                    } else {
                        m.setSpanCount(1);
                    }
                } else {
                    if (isLogin()) {
                        Intent i = new Intent(this, NewPostActivity.class);
                        i.putExtra("FID", FID);
                        i.putExtra("TITLE", TITLE);
                        startActivityForResult(i, 0);
                    }
                }
        }
    }


    //接受发帖是否成功
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //发帖成功 刷新
            refresh();
        }
    }

    private void getDataCompete(List<ArticleListData> dataset) {
        btnRefresh.show();
        if (currentPage == 1) {
            datas.clear();
            adapter.notifyDataSetChanged();
        }
        int start = datas.size();
        datas.addAll(dataset);

        adapter.notifyItemRangeInserted(start, dataset.size());

        if (currentPage < maxPage) {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
        } else {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
        }
        isEnableLoadMore = true;
        refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 500);
    }

    //校园网状态下获得一个普通板块文章列表数据 根据html获得数据
    private class getPostsRs extends AsyncTask<String, Void, List<ArticleListData>> {
        @Override
        protected List<ArticleListData> doInBackground(String... params) {
            String res = params[0];
            List<ArticleListData> tempDatas = new ArrayList<>();
            Document document = Jsoup.parse(res);
            Elements list = document.select("#threadlist tbody");
            ArticleListData temp;
            for (Element src : list) {
                if (src.getElementsByAttributeValue("class", "by").first() != null) {
                    String type;
                    if (src.attr("id").contains("stickthread")) {
                        type = "置顶";
                    } else if (src.select("th").attr("class").contains("lock")) {
                        type = "关闭";
                    } else if (src.select(".icn").select("a").attr("title").contains("投票")) {
                        type = "投票";
                    } else if (src.select("th").select("strong").text().length() > 0) {
                        type = "金币:" + src.select("th").select("strong").text().trim();
                    } else {
                        type = "normal";
                    }

                    if (isHideZhiding && type.equals("置顶")) {
                        continue;
                        //Log.i("article list", "ignore zhidin");
                    }

                    Elements tempEles = src.select("th").select("a[href^=forum.php?mod=viewthread][class=s xst]");
                    String title = tempEles.text();
                    String titleUrl = tempEles.attr("href");
                    int titleColor = GetId.getColor(PostsActivity.this, tempEles.attr("style"));
                    String author = src.getElementsByAttributeValue("class", "by").first().select("a").text();
                    String authorUrl = src.getElementsByAttributeValue("class", "by").first().select("a").attr("href");
                    String time = src.getElementsByAttributeValue("class", "by").first().select("em").text().trim();
                    String viewcount = src.getElementsByAttributeValue("class", "num").select("em").text();
                    String replaycount = src.getElementsByAttributeValue("class", "num").select("a").text();
                    String tag = src.select("em a[href^=forum.php?mod=forumdisplay]").text();

                    if (title.length() > 0 && author.length() > 0) {
                        temp = new ArticleListData(type, title, titleUrl, author, authorUrl, time, viewcount, replaycount, titleColor);
                        if (!TextUtils.isEmpty(tag)) temp.tag = tag;
                        tempDatas.add(temp);
                    }
                }
            }

            Element page = document.select("#fd_page_bottom .pg").first();
            if (page == null) {
                maxPage = currentPage;
            } else {
                maxPage = GetId.getNumber(page.select("label span").text());
            }

            return myDB.handReadHistoryList(tempDatas);
        }

        @Override
        protected void onPostExecute(List<ArticleListData> dataset) {
            getDataCompete(dataset);
        }
    }

    //非校园网状态下获得一个板块文章列表数据
    //根据html获得数据
    //调用的手机版
    private class getPostsMe extends AsyncTask<String, Void, List<ArticleListData>> {
        @Override
        protected List<ArticleListData> doInBackground(String... params) {
            //chiphell
            String res = params[0];
            List<ArticleListData> dataset = new ArrayList<>();
            Document doc = Jsoup.parse(res);
            Elements body = doc.select("div[class=threadlist]"); // 具有 href 属性的链接
            ArticleListData temp;
            Elements links = body.select("li");
            for (Element src : links) {
                String url = src.select("a").attr("href");
                int titleColor = GetId.getColor(PostsActivity.this, src.select("a").attr("style"));
                String author = src.select(".by").text();
                src.select("span.by").remove();
                String title = src.select("a").text();
                String replyCount = src.select("span.num").text();
                String img = src.select("img").attr("src");
                boolean hasImage = img.contains("icon_tu.png");
                temp = new ArticleListData(hasImage, title, url, author, replyCount, titleColor);
                dataset.add(temp);
            }

            Element page = doc.select(".pg").first();
            if (page == null) {
                maxPage = currentPage;
            } else {
                maxPage = GetId.getNumber(page.select("label span").text());
            }

            return myDB.handReadHistoryList(dataset);
        }

        @Override
        protected void onPostExecute(List<ArticleListData> dataset) {
            getDataCompete(dataset);
        }
    }

    //校园网状态下获得图片板块数据 图片链接、标题等  根据html获得数据
    private class getImagePosts extends AsyncTask<String, Void, List<ArticleListData>> {

        @Override
        protected List<ArticleListData> doInBackground(String... params) {
            String response = params[0];
            Document document = Jsoup.parse(response);
            Elements list = document.select("ul[id=waterfall]");
            Elements imagelist = list.select("li");
            List<ArticleListData> temps = new ArrayList<>();
            for (Element tmp : imagelist) {
                //链接不带前缀
                //http://rs.xidian.edu.cn/
                String img = tmp.select("img").attr("src");
                String url = tmp.select("h3.xw0").select("a[href^=forum.php]").attr("href");
                String title = tmp.select("h3.xw0").select("a[href^=forum.php]").text();
                String author = tmp.select("a[href^=home.php]").text();
                String replyCount = tmp.select(".xg1.y").select("a[href^=forum.php]").text();
                tmp.select(".xg1.y").select("a[href^=forum.php]").remove();
                temps.add(new ArticleListData(title, url, img, author, replyCount));
            }

            Element page = document.select("#fd_page_bottom .pg").first();
            if (page == null) {
                maxPage = currentPage;
            } else {
                maxPage = GetId.getNumber(page.select("label span").text());
            }

            return temps;
        }

        @Override
        protected void onPostExecute(List<ArticleListData> dada) {
            getDataCompete(dada);
        }
    }
}
