package com.boshanlu.mobile.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.adapter.BaseAdapter;
import com.boshanlu.mobile.adapter.MessageAdapter;
import com.boshanlu.mobile.listener.LoadMoreListener;
import com.boshanlu.mobile.model.ListType;
import com.boshanlu.mobile.model.MessageData;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.widget.BatchRadioButton;
import com.boshanlu.mobile.widget.MyListDivider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

//消息页面 回复/提到/AT
//TODO 翻页

public class FrageMessage extends BaseLazyFragment implements LoadMoreListener.OnLoadMoreListener {
    protected RecyclerView messageList;
    protected SwipeRefreshLayout refreshLayout;
    int lastReplyId = 0, lastAtId = 0;
    int currReplyId = 1, currAtId = 1;
    private MessageAdapter adapter;
    private List<MessageData> datas = new ArrayList<>();
    private int index = 0;
    private boolean lastLoginState = false;
    private boolean enableLoadMore = false;
    private int currentPage = 1, totalPage = 1;
    private boolean haveReply, isHavePm, isHaveAt;
    private BatchRadioButton tab1, tab2, tab3;

    public static FrageMessage newInstance() {
        return new FrageMessage();
    }

    public void updateNotifiCations(boolean haveReply, boolean havePm, boolean haveAt) {
        this.haveReply = haveReply;
        this.isHavePm = havePm;
        this.isHaveAt = haveAt;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        messageList = mRootView.findViewById(R.id.recycler_view);
        tab1 = mRootView.findViewById(R.id.btn_1);
        tab2 = mRootView.findViewById(R.id.btn_2);
        tab3 = mRootView.findViewById(R.id.btn_3);
        //设置可以滑出底栏
        messageList.setClipToPadding(false);
        messageList.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.bottombarHeight));
        refreshLayout = mRootView.findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.red_light, R.color.green_light, R.color.blue_light, R.color.orange_light);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        messageList.setLayoutManager(layoutManager);
        messageList.addItemDecoration(new MyListDivider(getActivity(), MyListDivider.VERTICAL));
        messageList.addOnScrollListener(new LoadMoreListener(layoutManager, this, 8));
        adapter = new MessageAdapter(getActivity(), datas);
        if (!App.ISLOGIN(getActivity())) {
            adapter.changeLoadMoreState(BaseAdapter.STATE_NEED_LOGIN);
        }
        messageList.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(this::pullRefresh);
        RadioGroup swictchMes = mRootView.findViewById(R.id.btn_change);

        swictchMes.setOnCheckedChangeListener((radioGroup, id) -> {
            int pos = 2;
            if (id == R.id.btn_1) {
                pos = 0;
            } else if (id == R.id.btn_2) {
                pos = 1;
            }

            if (pos != index) {
                index = pos;
                getData(true);
            }
        });
        return mRootView;
    }

    private void pullRefresh() {
        getData(true);
    }

    @Override
    public void onFirstUserVisible() {
        updateBatch();
        getData(false);
    }

    @Override
    public void onUserVisible() {
        if (lastLoginState != App.ISLOGIN(getActivity())) {
            getData(true);
            lastLoginState = !lastLoginState;
            if (lastLoginState) {
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
            }
        }
        updateBatch();
    }

    private void updateBatch() {
        tab1.setState(haveReply);
        tab2.setState(isHavePm);
        tab3.setState(isHaveAt);
    }

    @Override
    public void ScrollToTop() {
        if (datas.size() > 0)
            messageList.scrollToPosition(0);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_msg_hot;
    }

    private void getData(boolean needRefresh) {
        lastLoginState = App.ISLOGIN(getActivity());
        //记录上次已读消息游标
        if (!App.ISLOGIN(getActivity())) {
            adapter.changeLoadMoreState(BaseAdapter.STATE_NEED_LOGIN);
            refreshLayout.setRefreshing(false);
            return;
        }

        if (needRefresh) {
            datas.clear();
            adapter.notifyDataSetChanged();
            totalPage = 1;
            currentPage = 1;
            refreshLayout.setRefreshing(true);
        }

        lastReplyId = getContext().getSharedPreferences(App.MY_SHP_NAME, Activity.MODE_PRIVATE)
                .getInt(App.NOTICE_MESSAGE_REPLY_KEY, 0);
        currReplyId = lastReplyId;

        lastAtId = getContext().getSharedPreferences(App.MY_SHP_NAME, Activity.MODE_PRIVATE)
                .getInt(App.NOTICE_MESSAGE_AT_KEY, 0);
        currAtId = lastAtId;

        //reply
        String url;
        if (index == 0) { //reply
            url = "home.php?mod=space&do=notice&mobile=2&page=" + currentPage;
        } else if (index == 1) { //pm
            url = "home.php?mod=space&do=pm&mobile=2&page=" + currentPage;
        } else { //@wo
            url = "home.php?mod=space&do=notice&view=mypost&type=at&mobile=2&page=" + currentPage;
        }

        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                if (index == 1) {
                    new GetUserPmTask().execute(res);
                } else {
                    new GetMessageTask(index).execute(res);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 500);
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
            }
        });
    }

    private void finishGetData(List<MessageData> temdatas) {
        //datas.clear();
        int start = datas.size();
        datas.addAll(temdatas);

        if (currentPage < totalPage) {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
        } else {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
        }

        if (currentPage == 1) {
            adapter.notifyDataSetChanged();
        } else {
            adapter.notifyItemRangeInserted(start, temdatas.size());
        }
        refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 500);
        enableLoadMore = true;

        boolean haveUnRead = false;
        for (MessageData d : datas) {
            if (!d.isRead()) {
                haveUnRead = true;
                break;
            }
        }

        if (index == 0) haveReply = haveUnRead;
        if (index == 1) isHavePm = haveUnRead;
        if (index == 2) isHaveAt = haveUnRead;
        updateBatch();
    }

    @Override
    public void onLoadMore() {
        if (enableLoadMore) {
            enableLoadMore = false;
            if (currentPage < totalPage) {
                currentPage++;
                getData(false);
            } else {
                return;
            }
        }
    }

    //获得回复我的
    //获得@我的
    private class GetMessageTask extends AsyncTask<String, Void, List<MessageData>> {

        private static final int TYPE_REPLY = 0;
        private static final int TYPE_AT = 2;
        private int type; //0 reply 2-@

        public GetMessageTask(int type) {
            this.type = type;
        }

        @Override
        protected List<MessageData> doInBackground(String... params) {
            Document document = Jsoup.parse(params[0]);
            Element d1 = document.select(".pg strong").first();
            if (d1 == null || TextUtils.isEmpty(d1.text())) {
                currentPage = 1;
            } else {
                currentPage = Integer.parseInt(d1.text());
            }

            Element d2 = document.select(".pg label span").first();
            if (d2 == null || TextUtils.isEmpty(d2.text())) {
                totalPage = 1;
            } else {
                totalPage = GetId.getNumber(d2.text());
                if (totalPage == 0) {
                    totalPage = 1;
                }
            }

            List<MessageData> tempdatas = new ArrayList<>();
            Elements lists = document.select(".nts").select("dl.cl");
            for (Element tmp : lists) {
                int noticeId = Integer.parseInt(tmp.attr("notice"));
                String authorImage = tmp.select(".avt").select("img").attr("src");
                String time = tmp.select(".xg1.xw0").text();
                String authorTitle;
                String titleUrl;
                String content;

                if (type == TYPE_REPLY) {
                    content = tmp.select(".ntc_body").select("a[href^=forum.php?mod=redirect]").text().replace("查看", "");
                    if (content.isEmpty()) {
                        //这是系统消息
                        authorTitle = "系统消息";
                        titleUrl = tmp.select(".ntc_body").select("a").attr("href");
                        authorImage = App.BASE_URL + authorImage;
                        content = tmp.select(".ntc_body").text();
                    } else {
                        //这是回复消息
                        authorTitle = tmp.select(".ntc_body").select("a[href^=home.php]").text() + " 回复了我";
                        titleUrl = tmp.select(".ntc_body").select("a[href^=forum.php?mod=redirect]").attr("href");
                    }
                } else { //@消息
                    authorTitle = tmp.select(".ntc_body").select("a[href^=home.php]").text() + " 提到了我";
                    titleUrl = tmp.select(".ntc_body").select("a[href^=forum.php?mod=redirect]").attr("href");
                    content = "在主题[" + tmp.select(".ntc_body").select("a[href^=forum.php?mod=redirect]").text() + "]\n" +
                            tmp.select(".ntc_body").select(".quote").text();
                }


                boolean isRead;
                SharedPreferences prf = getActivity().getSharedPreferences(App.MY_SHP_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prf.edit();
                if (type == TYPE_REPLY) {
                    isRead = (noticeId <= lastReplyId);
                    if (noticeId > currReplyId) {
                        currReplyId = noticeId;
                    }

                    if (lastReplyId < currReplyId) {
                        editor.putInt(App.NOTICE_MESSAGE_REPLY_KEY, currReplyId);
                        editor.apply();
                    }
                } else {
                    isRead = (noticeId <= lastAtId);
                    if (noticeId > currAtId) {
                        currAtId = noticeId;
                    }

                    if (lastAtId < currAtId) {
                        editor.putInt(App.NOTICE_MESSAGE_AT_KEY, currReplyId);
                        editor.apply();
                    }
                }
                tempdatas.add(new MessageData(ListType.REPLAYME, authorTitle, titleUrl, authorImage, time, isRead, content));
            }

            return tempdatas;
        }

        @Override
        protected void onPostExecute(List<MessageData> tempdatas) {
            finishGetData(tempdatas);
        }
    }

    //获得pm消息
    private class GetUserPmTask extends AsyncTask<String, Void, List<MessageData>> {
        @Override
        protected List<MessageData> doInBackground(String... params) {
            currentPage = 1;
            totalPage = 1;

            Document document = Jsoup.parse(params[0]);
            List<MessageData> temdatas = new ArrayList<>();
            Elements lists = document.select(".pmbox").select("ul").select("li");
            for (Element tmp : lists) {
                boolean isRead = true;
                if (tmp.select(".num").text().length() > 0) {
                    isRead = false;
                }
                String title = tmp.select(".cl").select(".name").text();
                String time = tmp.select(".cl.grey").select(".time").text();
                tmp.select(".cl.grey").select(".time").remove();
                String content = tmp.select(".cl.grey").text();
                String authorImage = tmp.select("img").attr("src");
                String titleUrl = tmp.select("a").attr("href");
                temdatas.add(new MessageData(ListType.MYMESSAGE, title, titleUrl, authorImage, time, isRead, content));
            }
            return temdatas;
        }

        @Override
        protected void onPostExecute(List<MessageData> tempdatas) {
            finishGetData(tempdatas);
        }
    }
}
