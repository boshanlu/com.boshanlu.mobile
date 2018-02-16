package com.boshanlu.mobile.activity;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.adapter.BaseAdapter;
import com.boshanlu.mobile.adapter.FriendAdapter;
import com.boshanlu.mobile.listener.ListItemLongClickListener;
import com.boshanlu.mobile.listener.LoadMoreListener;
import com.boshanlu.mobile.model.FriendData;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.myhttp.TextResponseHandler;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.utils.KeyboardUtil;
import com.boshanlu.mobile.utils.UrlUtils;
import com.boshanlu.mobile.widget.AddFriendDialog;
import com.boshanlu.mobile.widget.MyListDivider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendActivity extends BaseActivity implements LoadMoreListener.OnLoadMoreListener,
        ListItemLongClickListener, TextView.OnEditorActionListener,
        View.OnClickListener, TextWatcher, AddFriendDialog.AddFriendListener {
    protected RecyclerView friends;
    private FriendAdapter adapter;
    private List<FriendData> datas, backUpdatas, totalDatas;
    private int CurrentPage = 1;
    private boolean isEnableLoadMore = true;
    private boolean isHaveMore = true;
    private EditText search_input;
    private CardView search_card;
    private Animator animator;
    //是否在搜索模式中
    private boolean searchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        initToolBar(true, "我的好友");
        friends = findViewById(R.id.recycler_view);
        datas = new ArrayList<>();
        backUpdatas = new ArrayList<>();
        totalDatas = new ArrayList<>();
        adapter = new FriendAdapter(this, datas, this);
        friends.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        friends.addItemDecoration(new MyListDivider(this, MyListDivider.VERTICAL));
        friends.setLayoutManager(lm);
        friends.addOnScrollListener(new LoadMoreListener(lm, this, 12));
        friends.setAdapter(adapter);
        search_input = findViewById(R.id.search_input);
        search_input.setHint("查找好友");
        search_card = findViewById(R.id.search_card);
        final String url = "home.php?mod=space&do=friend&mobile=2";
        new GetDataTask().execute(url);
        search_input.setOnEditorActionListener(this);
        addToolbarMenu(R.drawable.ic_search_white_24dp).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.start_search).setOnClickListener(this);
        search_card.setVisibility(View.INVISIBLE);
        search_input.addTextChangedListener(this);
    }

    @Override
    public void onLoadMore() {
        //加载更多被电击
        if (isEnableLoadMore && isHaveMore && !searchMode) {
            isEnableLoadMore = false;
            CurrentPage++;
            String url = "home.php?mod=space&do=friend&mobile=2" + "&page=" + CurrentPage;
            new GetDataTask().execute(url);
        }
    }

    @Override
    public void onItemLongClick(View v, final int position) {
        String name = datas.get(position).userName;
        String imgurl = datas.get(position).imgUrl;
        boolean isfrend = false;
        for (FriendData d : totalDatas) {
            if (d.userName.equals(name)) {
                isfrend = true;
                break;
            }
        }
        if (!isfrend) {
            AddFriendDialog dialogFragment = AddFriendDialog.newInstance(
                    this, name, imgurl);
            dialogFragment.show(getFragmentManager(), "add");
        } else {
            new AlertDialog.Builder(this).
                    setTitle("删除好友").
                    setMessage("你要删除" + datas.get(position).userName + "吗？").
                    setPositiveButton("删除", (dialog, which) -> removeFriend(datas.get(position).uid, position))
                    .setNegativeButton("取消", null)
                    .setCancelable(true)
                    .create()
                    .show();
        }
    }

    @Override
    public void OnAddFriendOkClick(String mes, String uid) {
        final ProgressDialog dialog1 = new ProgressDialog(this);
        dialog1.setTitle("正在发送请求");
        dialog1.setMessage("请等待......");
        Map<String, String> paras = new HashMap<>();
        paras.put("addsubmit", "true");
        paras.put("handlekey", "friend_" + uid);
        paras.put("note", mes);
        paras.put("gid", "1");
        paras.put("addsubmit_btn", "true");
        HttpUtil.post(UrlUtils.getAddFrirndUrl(uid), paras, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                if (res.contains("好友请求已")) {
                    Toast.makeText(getApplicationContext(), "请求已发送成功，正在请等待对方验证", Toast.LENGTH_SHORT).show();
                } else if (res.contains("正在等待验证")) {
                    Toast.makeText(getApplicationContext(), "好友请求已经发送了，正在等待对方验证", Toast.LENGTH_SHORT).show();
                } else if (res.contains("你们已成为好友")) {
                    Toast.makeText(getApplicationContext(), "你们已经是好友了不用添加了...", Toast.LENGTH_SHORT).show();
                }

                dialog1.dismiss();
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                Toast.makeText(getApplicationContext(), "出错了，我也不知道哪儿错了...", Toast.LENGTH_SHORT).show();
                dialog1.dismiss();
            }
        });
    }


    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        boolean handled = false;
        if (i == EditorInfo.IME_ACTION_SEARCH) {
            startSearch();
            handled = true;
        }
        return handled;
    }

    private void startSearch() {
        String str = search_input.getText().toString().trim();
        if (TextUtils.isEmpty(str)) {
            Snackbar.make(friends,
                    "请输入要搜索好友的名称！", Snackbar.LENGTH_SHORT).show();
        } else {
            KeyboardUtil.hideKeyboard(this);
            String url = "home.php?username=" + str
                    + "&searchsubmit=true&op=&mod=spacecp&ac=search&type=all&mobile=2";
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
            new GetDataTask().execute(url);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu:
                enterSearchMode();
                break;
            case R.id.btn_back:
                exitSearchMode();
                break;
            case R.id.start_search:
                startSearch();
                break;
        }
    }

    private void enterSearchMode() {
        ////进入搜索模式
        searchMode = true;
        backUpdatas.clear();
        backUpdatas.addAll(datas);
        adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
        showSearchView();
    }

    private void exitSearchMode() {
        //退出搜索模式
        searchMode = false;
        datas.clear();
        datas.addAll(backUpdatas);
        adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
        adapter.notifyDataSetChanged();
        hideSearchView();
    }

    private void removeFriend(String uid, final int pos) {
        //操作成功
        String url = "home.php?mod=spacecp&ac=friend&op=ignore&uid=" + uid + "&confirm=1";
        if (App.ISLOGIN(this)) {
            url = url + "&mobile=2";
        }
        HashMap<String, String> pa = new HashMap<>();
        pa.put("friendsubmit", "true");
        HttpUtil.post(url, pa, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String s = new String(response);
                if (s.contains("操作成功")) {
                    removeRes(true, pos);
                } else {
                    removeRes(false, pos);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                removeRes(false, pos);
            }
        });
    }

    private void removeRes(boolean b, int pos) {
        if (b) {
            datas.remove(pos);
            adapter.notifyItemRemoved(pos);
            Snackbar.make(friends, "删除好友成功！", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(friends, "删除好友失败！", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showSearchView() {
        search_card.setVisibility(View.VISIBLE);
        animator = ViewAnimationUtils.createCircularReveal(
                search_card,
                search_card.getWidth(),
                0,
                0,
                (float) Math.hypot(search_card.getWidth(), search_card.getHeight()));

        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(260);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                KeyboardUtil.showKeyboard(search_input);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        animator.start();
    }

    private void hideSearchView() {
        animator = ViewAnimationUtils.createCircularReveal(
                search_card,
                search_card.getWidth(),
                0,
                (float) Math.hypot(search_card.getWidth(), search_card.getHeight()),
                0);

        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(260);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                search_card.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    @Override
    public void onBackPressed() {
        if (searchMode) {
            exitSearchMode();
            return;
        }
        super.onBackPressed();
    }

    //监听搜索文字改变
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String s = search_input.getText().toString().trim();
        datas.clear();
        if (TextUtils.isEmpty(s)) {
            datas.addAll(backUpdatas);
            adapter.notifyDataSetChanged();
        } else {
            for (FriendData d : backUpdatas) {
                if (d.userName.contains(s)) {
                    datas.add(d);
                }
            }

            if (datas.size() != backUpdatas.size()) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    private class GetDataTask extends AsyncTask<String, Void, List<FriendData>> {
        @Override
        protected List<FriendData> doInBackground(String... params) {
            final List<FriendData> temp = new ArrayList<>();
            HttpUtil.SyncGet(FriendActivity.this, params[0], new TextResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    Document document = Jsoup.parse(response);
                    Elements lists = document.select("ul.buddy").select("li");
                    if (lists.size() <= 0) {
                        isHaveMore = false;
                    } else {
                        for (Element element : lists) {
                            String imgurl = element.select("img").attr("src");
                            String userName = element.select("h4").select("a[href^=home.php?mod=space&uid=]").text();
                            String uid = GetId.getId("uid=", imgurl);
                            String info = element.select("p.maxh").text();
                            boolean online = element.select("em.gol").text().contains("在线");
                            //userName,imgUrl,info,uid
                            temp.add(new FriendData(userName, imgurl, info, uid, online));
                        }
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    super.onFailure(e);
                }
            });
            return temp;
        }

        @Override
        protected void onPostExecute(List<FriendData> s) {
            super.onPostExecute(s);
            if (!isHaveMore || searchMode) {
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
            } else {
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
            }
            int i = datas.size();
            datas.addAll(s);
            if (!searchMode) {
                totalDatas.addAll(s);
            }
            if (i == 0) {
                if (!searchMode && datas.size() == 0) {
                    adapter.setPlaceHolderText("你还没有好友");
                } else {
                    adapter.setPlaceHolderText("暂无数据");
                }
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(i, s.size());
            }
            isEnableLoadMore = true;
        }
    }
}
