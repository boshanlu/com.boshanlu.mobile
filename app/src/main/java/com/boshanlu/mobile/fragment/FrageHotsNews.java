package com.boshanlu.mobile.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.adapter.BaseAdapter;
import com.boshanlu.mobile.adapter.HotNewListAdapter;
import com.boshanlu.mobile.database.MyDB;
import com.boshanlu.mobile.listener.LoadMoreListener;
import com.boshanlu.mobile.model.ArticleListData;
import com.boshanlu.mobile.model.GalleryData;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.myhttp.SyncHttpClient;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.widget.MyListDivider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by free2 on 16-3-19.
 * 简单的fragment 首页第二页 展示最新的帖子等
 */
public class FrageHotsNews extends BaseLazyFragment implements LoadMoreListener.OnLoadMoreListener {

    private static final int TYPE_HOT = 0;
    private static final int TYPE_NEW = 1;
    protected RecyclerView postList;
    protected SwipeRefreshLayout refreshLayout;
    private int currentType = 1;
    private List<GalleryData> galleryDatas = new ArrayList<>();
    private List<ArticleListData> mydataset = new ArrayList<>();
    private HotNewListAdapter adapter;
    private boolean isEnableLoadMore = false;
    private int CurrentPage = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        ((RadioButton) mRootView.findViewById(R.id.btn_1)).setText("新帖");
        mRootView.findViewById(R.id.btn_2).setVisibility(View.GONE);
        ((RadioButton) mRootView.findViewById(R.id.btn_3)).setText("热贴");

        postList = mRootView.findViewById(R.id.recycler_view);
        refreshLayout = mRootView.findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.red_light, R.color.green_light, R.color.blue_light, R.color.orange_light);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        postList.setLayoutManager(mLayoutManager);
        postList.addItemDecoration(new MyListDivider(getActivity(), MyListDivider.VERTICAL));
        //设置可以滑出底栏
        postList.setClipToPadding(false);
        postList.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.bottombarHeight));
        adapter = new HotNewListAdapter(getActivity(), mydataset, galleryDatas);
        postList.setAdapter(adapter);
        postList.addOnScrollListener(new LoadMoreListener((LinearLayoutManager) mLayoutManager, this, 10));
        refreshLayout.setOnRefreshListener(this::refresh);

        RadioGroup swictchMes = mRootView.findViewById(R.id.btn_change);
        swictchMes.setOnCheckedChangeListener((radioGroup, id) -> {
            int pos = -1;
            if (id == R.id.btn_1) {
                pos = TYPE_NEW;
            } else {
                pos = TYPE_HOT;
            }

            if (pos != currentType) {
                currentType = pos;
                refreshLayout.setRefreshing(true);
                refresh();
            }
        });
        return mRootView;
    }

    @Override
    public void onFirstUserVisible() {
        getData();
    }

    @Override
    public void ScrollToTop() {
        if (mydataset.size() > 0)
            postList.scrollToPosition(0);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_msg_hot;
    }

    private void refresh() {
        CurrentPage = 1;
        getData();
    }

    @Override
    public void onLoadMore() {
        if (isEnableLoadMore) {
            CurrentPage++;
            getData();
        }
    }

    private void getData() {
        isEnableLoadMore = false;
        adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);

        new GetGalleryTask().execute();

        String type = (currentType == TYPE_HOT) ? "hot" : "new";
        String url = "forum.php?mod=guide&view=" + type + "&page=" + CurrentPage + "&mobile=2";
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                new GetNewArticleListTaskMe().execute(new String(response));
            }

            @Override
            public void onFailure(Throwable e) {
                refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 300);

                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
            }
        });
    }

    private class GetGalleryTask extends AsyncTask<Void, Void, List<GalleryData>> {
        @Override
        protected List<GalleryData> doInBackground(Void... voids) {
            List<GalleryData> temps = new ArrayList<>();
            String url = App.BASE_URL + "forum.php";
            Document doc;
            try {
                doc = Jsoup.connect(url).userAgent(SyncHttpClient.DEFAULT_USER_AGENT).get();
            } catch (IOException e) {
                e.printStackTrace();
                return temps;
            }

            Elements listgallerys = doc.select("#wp").select("ul.slideshow");
            for (Element e : listgallerys.select("li")) {
                String title = e.text();
                String titleurl = e.select("a").attr("href");
                String imgurl = e.select("img").attr("src");
                temps.add(new GalleryData(imgurl, title, titleurl));
            }
            return temps;
        }

        @Override
        protected void onPostExecute(List<GalleryData> data) {
            super.onPostExecute(data);
            if (data.size() == 0) {
                return;
            }
            if (galleryDatas.size() == 0) {
                galleryDatas.addAll(data);
            } else if (galleryDatas.size() != data.size()) {//进行了一下优化 只有不相同时才刷行
                galleryDatas.clear();
                galleryDatas.addAll(data);
            } else {
                return;
            }
            adapter.notifyItemChanged(0);
        }
    }

    private class GetNewArticleListTaskMe extends AsyncTask<String, Void, List<ArticleListData>> {
        @Override
        protected List<ArticleListData> doInBackground(String... params) {
            List<ArticleListData> dataset = new ArrayList<>();
            Document doc = Jsoup.parse(params[0]);
            Elements body = doc.select("div[class=threadlist]"); // 具有 href 属性的链接
            Elements links = body.select("li");
            for (Element src : links) {
                String url = src.select("a").attr("href");
                int titleColor = GetId.getColor(getActivity(), src.select("a").attr("style"));
                String author = src.select(".by").text();
                src.select("span.by").remove();
                String replyCount = src.select("span.num").text();
                src.select("span.num").remove();
                String title = src.select("a").text();
                String img = src.select("img").attr("src");
                boolean hasImage = img.contains("icon_tu.png");
                dataset.add(new ArticleListData(hasImage, title, url, author, replyCount, titleColor));
            }

            MyDB myDB = new MyDB(getActivity());
            return myDB.handReadHistoryList(dataset);
        }

        @Override
        protected void onPostExecute(List<ArticleListData> datas) {
            refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 300);
            if (CurrentPage == 1) {
                mydataset.clear();
                mydataset.addAll(datas);
                adapter.notifyDataSetChanged();
            } else {
                if (datas.size() == 0) {
                    adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
                    return;
                } else {
                    int size = mydataset.size();
                    mydataset.addAll(datas);
                    adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_OK);
                    if (galleryDatas.size() > 0) {
                        size++;
                    }
                    adapter.notifyItemRangeInserted(size, datas.size());
                }
            }
            isEnableLoadMore = true;
        }
    }

}
