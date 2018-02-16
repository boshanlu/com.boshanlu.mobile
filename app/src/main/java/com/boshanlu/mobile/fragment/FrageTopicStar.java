package com.boshanlu.mobile.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.adapter.BaseAdapter;
import com.boshanlu.mobile.adapter.SimpleListAdapter;
import com.boshanlu.mobile.listener.LoadMoreListener;
import com.boshanlu.mobile.model.FrageType;
import com.boshanlu.mobile.model.ListType;
import com.boshanlu.mobile.model.SimpleListData;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.widget.MyListDivider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by free2 on 16-7-14.
 * 收藏/主题/历史纪录
 * //todo 删除浏览历史
 */
public class FrageTopicStar extends BaseFragment implements LoadMoreListener.OnLoadMoreListener {

    private List<SimpleListData> datas;
    private SimpleListAdapter adapter;
    private int CurrentPage = 1;
    private boolean isEnableLoadMore = true;
    private boolean isHaveMore = true;
    private int currentIndex = 0;
    private String title = "";

    private String url;

    public static FrageTopicStar newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt("type", type);
        FrageTopicStar fragment = new FrageTopicStar();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle bundle = getArguments();//从activity传过来的Bundle
        if (bundle != null) {
            int type = bundle.getInt("type", -1);
            switch (type) {
                case FrageType.TOPIC:
                    currentIndex = 0;
                    title = "我的帖子";
                    break;
                case FrageType.START:
                    currentIndex = 1;
                    title = "我的收藏";
                    break;
            }
        }
        initToolbar(true, title);
        RecyclerView recyclerView = mRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        SwipeRefreshLayout refreshLayout = mRootView.findViewById(R.id.refresh_layout);
        refreshLayout.setEnabled(false);
        String uid = App.getUid(getActivity());
        switch (currentIndex) {
            case 0:
                //主题
                url = "home.php?mod=space&uid=" + uid + "&do=thread&view=me&mobile=2";
                break;
            case 1:
                //我的收藏
                url = "home.php?mod=space&uid=" + uid + "&do=favorite&view=me&type=thread&mobile=2";
                break;
        }

        datas = new ArrayList<>();
        adapter = new SimpleListAdapter(ListType.ARTICLE, getActivity(), datas);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new MyListDivider(getActivity(), MyListDivider.VERTICAL));
        recyclerView.addOnScrollListener(new LoadMoreListener((LinearLayoutManager) layoutManager, this, 10));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        refresh();
        return mRootView;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.list_toolbar;
    }


    @Override
    public void onLoadMore() {
        if (isEnableLoadMore && isHaveMore) {
            CurrentPage++;
            getWebDatas();
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
            isEnableLoadMore = false;
        }
    }

    private void refresh() {
        datas.clear();
        adapter.notifyDataSetChanged();
        getWebDatas();
    }


    private void getWebDatas() {
        String newurl = url + "&page=" + CurrentPage;
        HttpUtil.get(newurl, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                if (currentIndex == 0) {
                    new GetUserArticles().execute(res);
                } else if (currentIndex == 1) {
                    new GetUserStarTask().execute(res);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
            }
        });
    }

    //加载完成
    private void onLoadCompete(List<SimpleListData> d) {
        if (isHaveMore && d.size() > 0) {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
        } else {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
        }

        if (d.size() > 0) {
            int i = datas.size();
            datas.addAll(d);
            if (i == 0) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(i, d.size());
            }
        } else if (datas.size() == 0) {
            adapter.notifyDataSetChanged();
        }
        isEnableLoadMore = true;
    }

    //获得主题
    private class GetUserArticles extends AsyncTask<String, Void, List<SimpleListData>> {
        @Override
        protected List<SimpleListData> doInBackground(String... strings) {
            String res = strings[0];
            List<SimpleListData> temp = new ArrayList<>();
            Elements lists = Jsoup.parse(res).select(".threadlist").select("ul").select("li");
            for (Element tmp : lists) {
                String title = tmp.select("a").text();
                if (title.isEmpty()) {
                    isHaveMore = false;
                    break;
                }
                String titleUrl = tmp.select("a").attr("href");
                String num = tmp.select(".num").text();
                temp.add(new SimpleListData(title, num, titleUrl));
            }

            if (temp.size() % 10 != 0) {
                isHaveMore = false;
            }
            return temp;
        }

        @Override
        protected void onPostExecute(List<SimpleListData> aVoid) {
            if (datas.size() == 0 && aVoid.size() == 0) {
                adapter.setPlaceHolderText("你还没有发过帖子");
            }
            onLoadCompete(aVoid);
        }

    }

    //获得用户收藏
    private class GetUserStarTask extends AsyncTask<String, Void, List<SimpleListData>> {
        @Override
        protected List<SimpleListData> doInBackground(String... params) {
            String res = params[0];
            List<SimpleListData> temp = new ArrayList<>();
            Elements lists = Jsoup.parse(res).select(".threadlist").select("ul").select("li");
            for (Element tmp : lists) {
                String key = tmp.select("a").text();
                if (key.isEmpty()) {
                    isHaveMore = false;
                    break;
                }
                String link = tmp.select("a").attr("href");
                temp.add(new SimpleListData(key, "", link));
            }
            if (temp.size() % 10 != 0) {
                isHaveMore = false;
            }
            return temp;
        }

        @Override
        protected void onPostExecute(List<SimpleListData> data) {
            super.onPostExecute(data);
            if (datas.size() == 0 && data.size() == 0) {
                adapter.setPlaceHolderText("你还没有收藏");
            }
            onLoadCompete(data);
        }
    }
}
