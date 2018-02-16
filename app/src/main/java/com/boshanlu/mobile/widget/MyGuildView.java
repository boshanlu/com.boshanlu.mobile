package com.boshanlu.mobile.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.model.GalleryData;
import com.boshanlu.mobile.utils.DimmenUtils;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 图片轮流展示的view
 * 用于热帖/新帖 上面的图片展示
 */
public class MyGuildView extends RelativeLayout implements ViewPager.OnPageChangeListener {

    private static final int AutoPlayDelay = 5000;
    private static final int RMP = LayoutParams.MATCH_PARENT;
    private static final int RWC = LayoutParams.WRAP_CONTENT;
    private static final int LWC = LinearLayout.LayoutParams.WRAP_CONTENT;
    private ViewPager viewPager;
    private Context context;
    private boolean isAutoPlay;
    private OnItemClickListener listener;
    private int dotMargine = 0;
    private int containerPaddingLR = 0;
    private LinearLayout dotContainer;
    private int dotImageResourseId = 0;
    private PageAdapter adapter;
    //自动播放
    private AutoPlayTask mAutoPlayTask;
    private List<GalleryData> datas;

    public MyGuildView(Context context) {
        super(context);
        init(context);
    }

    public MyGuildView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        dotMargine = DimmenUtils.dip2px(context, 2);
        int containerPaddingTB = DimmenUtils.dip2px(context, 8);
        containerPaddingLR = DimmenUtils.dip2px(context, 12);

        dotImageResourseId = R.drawable.dot_bg;
        viewPager = new ViewPager(context);
        viewPager.setLayoutParams(new LayoutParams(RMP, RMP));
        viewPager.addOnPageChangeListener(this);
        adapter = new PageAdapter();
        viewPager.setPageTransformer(true, new ScalePageTransformer());
        viewPager.setAdapter(adapter);
        addView(viewPager);

        dotContainer = new LinearLayout(context);
        LayoutParams lp = new LayoutParams(RMP, RWC);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        /**
         * 在这儿设置圆点位置
         */
        lp.addRule(CENTER_HORIZONTAL);
        dotContainer.setLayoutParams(lp);
        dotContainer.setOrientation(LinearLayout.HORIZONTAL);
        //dotContainer.setGravity(CENTER_HORIZONTAL);
        dotContainer.setGravity(CENTER_VERTICAL);
        dotContainer.setBackgroundColor(0x7f333333);
        dotContainer.setPadding(containerPaddingLR, containerPaddingTB, containerPaddingLR, containerPaddingTB);
        addView(dotContainer);

        mAutoPlayTask = new AutoPlayTask(this);
    }

    public void setData(List<GalleryData> datas) {
        isAutoPlay = !(isAutoPlay && datas.size() < 3);
        this.datas = datas;
        viewPager.setOffscreenPageLimit(datas.size());
        adapter.notifyDataSetChanged();
        initdotsAndImages();
        changeView(0);
        startAutoPlay();
    }

    private void initdotsAndImages() {
        if (dotContainer != null && datas.size() > 1) {
            dotContainer.removeAllViews();
            LinearLayout.LayoutParams lpt = new LinearLayout.LayoutParams(LWC, LWC, 1);
            lpt.setMargins(0, 0, containerPaddingLR, 0);
            TextView title = new TextView(context);
            title.setGravity(Gravity.CENTER_VERTICAL);
            title.setSingleLine(true);
            title.setEllipsize(TextUtils.TruncateAt.END);
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            dotContainer.addView(title, lpt);

            LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(LWC, LWC);
            lpp.setMargins(dotMargine, 0, dotMargine, 0);
            lpp.gravity = Gravity.CENTER_VERTICAL;

            ImageView dotImageView;
            for (int i = 0; i < datas.size(); i++) {
                dotImageView = new ImageView(getContext());
                dotImageView.setImageResource(dotImageResourseId);
                dotImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                dotImageView.setEnabled(false);
                dotContainer.addView(dotImageView, lpp);
            }
        }
    }

    private void switchToNextPage() {
        int page = (viewPager.getCurrentItem() + 1) % datas.size();
        viewPager.setCurrentItem(page);
        startAutoPlay();
    }

    private void changeView(int pos) {
        if (dotContainer != null && datas != null) {
            ((TextView) dotContainer.getChildAt(0)).setText(datas.get(pos).getTitle());
            for (int i = 1; i < dotContainer.getChildCount(); i++) {
                if (i == pos + 1) {
                    dotContainer.getChildAt(i).setEnabled(true);
                    dotContainer.getChildAt(i).setScaleX(1.4f);
                    dotContainer.getChildAt(i).setScaleY(1.4f);
                } else {
                    dotContainer.getChildAt(i).setEnabled(false);
                    dotContainer.getChildAt(i).setScaleX(1.0f);
                    dotContainer.getChildAt(i).setScaleY(1.0f);
                }
            }
        }
    }


    //启用和关闭自动播放
    public void startAutoPlay() {
        if (isAutoPlay) {
            stopAutoPlay();
            postDelayed(mAutoPlayTask, AutoPlayDelay);
        }
    }

    public void stopAutoPlay() {
        if (isAutoPlay) {
            removeCallbacks(mAutoPlayTask);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //current_position = position;
    }


    @Override
    public void onPageSelected(int position) {
        position = position % datas.size();
        changeView(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isAutoPlay) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoPlay();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    startAutoPlay();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 点击回掉
     */
    public interface OnItemClickListener {
        void onBannerItemClick(View view, int position);
    }

    private static class AutoPlayTask implements Runnable {
        private final WeakReference<MyGuildView> mBanner;

        private AutoPlayTask(MyGuildView banner) {
            mBanner = new WeakReference<>(banner);
        }

        @Override
        public void run() {
            MyGuildView banner = mBanner.get();
            if (banner != null) {
                banner.switchToNextPage();
            }
        }
    }

    private class PageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return datas == null ? 0 : datas.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ImageView v = container.findViewWithTag(position);
            if (v == null) {
                v = new ImageView(context);
                v.setLayoutParams(new LayoutParams(RMP, RMP));
                v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Picasso.with(context).load(datas.get(position).getImgurl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(v);
                if (listener != null) {
                    v.setOnClickListener(view -> listener.onBannerItemClick(view, position));
                }
                v.setTag(position);
                container.addView(v);
            }
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}