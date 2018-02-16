package com.boshanlu.mobile.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.widget.ScaleImageView;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ViewImgActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private static boolean needAnimate = false;
    private ViewPager pager;
    private List<String> datas;
    private String aid = "0";
    private TextView index;
    private MyAdapter adapter;
    private int position = 0;

    public static void open(Context context, String url) {
        Intent intent = new Intent(context, ViewImgActivity.class);
        intent.putExtra("url", url);
        needAnimate = true;
        ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out);
        ActivityCompat.startActivity(context, intent, compat.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_img);
        datas = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.rgb(138, 145, 151));
        }

        pager = findViewById(R.id.pager);
        index = findViewById(R.id.index);
        findViewById(R.id.nav_back).setOnClickListener(v -> finish());

        pager.addOnPageChangeListener(this);
        adapter = new MyAdapter();
        pager.setAdapter(adapter);

        Bundle b = getIntent().getExtras();
        String url = b.getString("url");

        final String tid = GetId.getId("tid=", url);
        aid = GetId.getId("aid=", url);
        String urll = "forum.php?mod=viewthread&tid="
                + tid + "&aid=" + aid + "&from=album&mobile=2";
        HttpUtil.get(urll, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                Document doc = Jsoup.parse(res);
                int ih = doc.head().html().indexOf("keywords");
                if (ih > 0) {
                    int h_start = doc.head().html().indexOf('\"', ih + 15);
                    int h_end = doc.head().html().indexOf('\"', h_start + 1);
                    String title = doc.head().html().substring(h_start + 1, h_end);
                    TextView v = findViewById(R.id.title);
                    v.setText(title);
                }
                Elements elements = doc.select("ul.postalbum_c").select("li");
                int i = 0;
                for (Element e : elements) {
                    String zsrc = e.select("img").attr("zsrc");
                    if (zsrc.contains(aid)) {
                        position = i;
                    }
                    String src = e.select("img").attr("orig");
                    if (TextUtils.isEmpty("src")) {
                        continue;
                    }
                    if (src.startsWith("./")) {
                        src = src.substring(2);
                    }
                    if (!src.startsWith("http")) {
                        src = App.BASE_URL + src;
                    }

                    i++;
                    datas.add(src);
                }
                adapter.notifyDataSetChanged();
                changeIndex(position);
                pager.setCurrentItem(position);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        // 去掉自带的转场动画
        if (needAnimate) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void changeIndex(int pos) {
        index.setText((pos + 1) + "/" + datas.size());
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    @Override
    public void onPageSelected(int position) {
        position = position % datas.size();
        changeIndex(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return datas == null ? 0 : datas.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ScaleImageView v = container.findViewWithTag(position);
            if (v == null) {
                v = new ScaleImageView(ViewImgActivity.this);
                v.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                v.setLayoutParams(params);
                Picasso.with(ViewImgActivity.this).load(datas.get(position))
                        .placeholder(R.drawable.image_placeholder)
                        .into(v);
                v.setTag(position);
                container.addView(v);
            }
            return v;
        }


        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }
}
