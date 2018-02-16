package com.boshanlu.mobile.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.activity.PostActivity;
import com.boshanlu.mobile.model.ArticleListData;
import com.boshanlu.mobile.model.GalleryData;
import com.boshanlu.mobile.widget.MyGuildView;

import java.util.List;

/**
 * Created by free2 on 16-3-31.
 * 支持 gallery
 */
public class HotNewListAdapter extends BaseAdapter {
    private static final int TYPE_ARTICLE_LIST = 3;
    private static final int TYPE_ARTICLE_HEADER = 2;

    private List<ArticleListData> DataSet;
    private List<GalleryData> galleryDatas;
    private Activity activity;
    private int readcolor;

    public HotNewListAdapter(Activity activity, List<ArticleListData> DataSet, @Nullable List<GalleryData> galleryDatas) {
        this.DataSet = DataSet;
        this.activity = activity;
        this.galleryDatas = galleryDatas;
        readcolor = ContextCompat.getColor(activity, R.color.text_color_sec);
    }


    @Override
    protected int getDataCount() {
        int count = DataSet.size();
        if (galleryDatas.size() > 0) {
            count++;
        }
        return count;
    }

    @Override
    protected int getItemType(int position) {
        if (position == 0 && galleryDatas.size() > 0) {
            return TYPE_ARTICLE_HEADER;
        } else {
            return TYPE_ARTICLE_LIST;
        }
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ARTICLE_LIST:
                return new NormalViewHolderMe(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_me, parent, false));
            default:
                return new HeadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false));
        }
    }

    //手机版文章列表
    private class NormalViewHolderMe extends BaseViewHolder {
        TextView article_title;
        TextView author_name;
        TextView is_image;
        TextView reply_count;

        //构造
        NormalViewHolderMe(View v) {
            super(v);
            article_title = v.findViewById(R.id.article_title);
            author_name = v.findViewById(R.id.author_name);
            is_image = v.findViewById(R.id.is_image);
            reply_count = v.findViewById(R.id.reply_count);
            v.findViewById(R.id.main_item_btn_item).setOnClickListener(v1 -> onBtnItemClick());
        }

        //设置listItem的数据
        @Override
        void setData(int position) {
            if (galleryDatas.size() > 0 && position > 0) {
                position--;
            }
            ArticleListData single = DataSet.get(position);
            int color = single.titleColor;
            article_title.setTextColor(single.isRead ? readcolor : color);
            article_title.setText(single.title);
            author_name.setText("\uf2c0 " + single.author);
            reply_count.setText("\uf0e6 " + single.replayCount);
            is_image.setVisibility(single.ishaveImage ? View.VISIBLE : View.GONE);
        }

        void onBtnItemClick() {
            int pos = getAdapterPosition();
            if (pos > 0 && galleryDatas.size() > 0) {
                pos--;
            }
            ArticleListData single_data = DataSet.get(pos);
            if (!single_data.isRead) {
                single_data.isRead = true;
                notifyItemChanged(getAdapterPosition());
            }
            PostActivity.open(activity, single_data.titleUrl, single_data.author);
        }
    }

    //图片切换view
    private class HeadViewHolder extends BaseViewHolder {
        private MyGuildView guildView;

        HeadViewHolder(View itemView) {
            super(itemView);
            guildView = itemView.findViewById(R.id.myGuideView);

        }

        @Override
        void setData(int position) {
            guildView.setData(galleryDatas);
            guildView.setListener((view, position1) -> {
                String titleUrl = galleryDatas.get(position1).getTitleUrl();
                if (!TextUtils.isEmpty(titleUrl)) {
                    PostActivity.open(activity, titleUrl, null);
                }
            });
        }
    }
}
