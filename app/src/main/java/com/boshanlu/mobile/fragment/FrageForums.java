package com.boshanlu.mobile.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.activity.BaseActivity;
import com.boshanlu.mobile.activity.LoginActivity;
import com.boshanlu.mobile.activity.SearchActivity;
import com.boshanlu.mobile.activity.UserDetailActivity;
import com.boshanlu.mobile.adapter.ForumsAdapter;
import com.boshanlu.mobile.model.Category;
import com.boshanlu.mobile.model.WaterData;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.utils.RuisUtils;
import com.boshanlu.mobile.utils.UrlUtils;
import com.boshanlu.mobile.widget.CircleImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by free2 on 16-3-19.
 * 板块列表fragemnt
 */
public class FrageForums extends BaseLazyFragment implements View.OnClickListener {
    private ForumsAdapter adapter = null;
    private CircleImageView userImg;
    private RecyclerView formsList;
    private boolean lastLoginState;
    private List<Category> forumDatas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forumDatas = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        userImg = mRootView.findViewById(R.id.img);
        formsList = mRootView.findViewById(R.id.recycler_view);
        formsList.setClipToPadding(false);
        formsList.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.bottombarHeight));
        mRootView.findViewById(R.id.search).setOnClickListener(this);
        adapter = new ForumsAdapter(getActivity());
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 4);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int type = adapter.getItemViewType(position);
                if (type == ForumsAdapter.TYPE_HEADER || type == ForumsAdapter.TYPE_WATER) {
                    return 4;
                } else {
                    return 1;
                }
            }
        });
        userImg.setOnClickListener(this);
        formsList.setLayoutManager(layoutManager);
        formsList.setAdapter(adapter);
        return mRootView;
    }

    @Override
    public void onFirstUserVisible() {
        lastLoginState = App.ISLOGIN(getActivity());
        initForums(lastLoginState);
        initAvatar();
    }

    @Override
    public void onUserVisible() {
        Log.d("=========", lastLoginState + "");
        Log.d("=========", App.ISLOGIN(getActivity()) + "");

        if (lastLoginState != App.ISLOGIN(getActivity())) {
            lastLoginState = !lastLoginState;
            initForums(App.ISLOGIN(getActivity()));
            initAvatar();
        }
    }

    @Override
    public void ScrollToTop() {
        if (forumDatas != null && forumDatas.size() > 0)
            formsList.scrollToPosition(0);
    }

    private void initAvatar() {
        lastLoginState = App.ISLOGIN(getActivity());
        if (lastLoginState) {
            RuisUtils.LoadMyAvatar(new WeakReference<>(getActivity()),
                    App.getUid(getActivity()),
                    new WeakReference<>(userImg), "s");
        } else {
            userImg.setImageResource(R.drawable.image_placeholder);
        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_forums;
    }

    void initForums(boolean loginstate) {
        new GetForumList().execute(loginstate);
        String url = App.BASE_URL + "forum.php";
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                List<WaterData> temps = new ArrayList<>();
                Document doc = Jsoup.parse(new String(response));
                Elements waters = doc.select("#portal_block_317").select("li");
                for (Element e : waters) {
                    Elements es = e.select("p").select("a[href^=home.php?mod=space]");
                    String uid = GetId.getId("uid=", es.attr("href"));
                    String imgSrc = e.select("img").attr("src");
                    String uname = es.text();
                    int num = 0;
                    if (e.select("p").size() > 1) {
                        if (e.select("p").get(1).text().contains("帖数")) {
                            num = GetId.getNumber(e.select("p").get(1).text());
                        }
                    }
                    temps.add(new WaterData(uname, uid, num, imgSrc));
                    if (temps.size() >= 16) break;
                }

                if (temps.size() > 0)
                    adapter.setWaterData(temps);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                BaseActivity b = (BaseActivity) getActivity();
                if (b.isLogin()) {
                    switchActivity(SearchActivity.class);
                }
                break;
            case R.id.img:
                if (lastLoginState) {
                    String imgurl = UrlUtils.getAvatarUrl(App.getUid(getActivity()), "b");
                    UserDetailActivity.open(getActivity(), App.getName(getActivity()),
                            imgurl, App.getUid(getActivity()));
                } else {
                    switchActivity(LoginActivity.class);
                }
                break;
        }
    }

    //获取首页板块数据 板块列表
    private class GetForumList extends AsyncTask<Boolean, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... params) {
            forumDatas = RuisUtils.getForums(getActivity(), params[0], false);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if (forumDatas == null || forumDatas.size() == 0) {
                Toast.makeText(getActivity(), "获取板块列表失败", Toast.LENGTH_LONG).show();
            }

            adapter.notifyDataSetChanged();
            adapter.setDatas(forumDatas);
        }
    }
}
