package com.boshanlu.mobile.adapter;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.activity.PostsActivity;
import com.boshanlu.mobile.activity.UserDetailActivity;
import com.boshanlu.mobile.model.Category;
import com.boshanlu.mobile.model.Forum;
import com.boshanlu.mobile.model.ForumListData;
import com.boshanlu.mobile.model.WaterData;
import com.boshanlu.mobile.utils.DimmenUtils;
import com.boshanlu.mobile.utils.RuisUtils;
import com.boshanlu.mobile.widget.CircleImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by free2 on 16-3-19.
 * 板块列表
 */
public class ForumsAdapter extends BaseAdapter {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_WATER = 2;
    protected Context context;
    List<ForumListData> datas = new ArrayList<>();
    List<WaterData> ds = new ArrayList<>();

    public ForumsAdapter(Context context) {
        this.context = context;
        disableLoadMore();
        setEnablePlaceHolder(false);
    }

    public void setDatas(List<Category> ds) {
        if (ds == null) {
            ds = new ArrayList<>();
        }

        for (Category c : ds) {
            datas.add(new ForumListData(true, c.name, c.gid));
            for (Forum f : c.forums) {
                datas.add(new ForumListData(false, f.name, f.fid));
            }
        }

        notifyDataSetChanged();
    }

    public void setWaterData(List<WaterData> ds) {
        this.ds = ds;
        int i = datas.size();
        notifyItemRangeInserted(i, ds.size() + 1);
    }


    @Override
    protected int getDataCount() {
        return datas.size() + (ds.size() > 0 ? 2 : 0);
    }

    @Override
    protected int getItemType(int pos) {
        if (pos < datas.size()) {
            return datas.get(pos).isheader ? TYPE_HEADER : TYPE_NORMAL;
        } else if (pos == datas.size()) {
            return TYPE_HEADER;
        } else {
            return TYPE_WATER;
        }
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeadView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forum_h, parent, false));
        } else if (viewType == TYPE_WATER) {
            return new WaterHolder(new GridView(context));
        } else {
            return new ChildViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forum_n, parent, false));
        }
    }

    private class HeadView extends BaseViewHolder {
        TextView head;

        HeadView(View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.header_title);
        }

        @Override
        void setData(int position) {
            if (position < datas.size())
                head.setText(datas.get(position).title);
            else
                head.setText("水神榜");
        }
    }

    private class ChildViewHolder extends BaseViewHolder {
        ImageView img;
        TextView title;
        int size = 42;

        ChildViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            title = itemView.findViewById(R.id.title);
            size = DimmenUtils.dip2px(context, 42);
        }

        @Override
        void setData(final int position) {
            final ForumListData s = datas.get(position);
            title.setText(s.title);
            //todo
            //today_count.setVisibility(View.VISIBLE);
            //today_count.setText(s.todaynew);
            Drawable a = RuisUtils.getForumlogo(context, s.fid);
            if (a != null) {
                img.setImageDrawable(a);
            } else {
                img.setImageResource(R.drawable.image_placeholder);
            }
            itemView.setOnClickListener(view -> PostsActivity.open(context, s.fid, s.title));
        }
    }

    private class WaterHolder extends BaseViewHolder {

        WaterHolder(View itemView) {
            super(itemView);
            GridView g = (GridView) itemView;
            int p12 = DimmenUtils.dip2px(context, 12);
            g.setVerticalSpacing(p12);
            g.setHorizontalSpacing(p12);

            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);

            Point point = new Point();
            wm.getDefaultDisplay().getSize(point);
            int width = point.x;
            int itemWidth = (width - (p12 * 9)) / 8;
            g.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (ds.size() <= 8 ? itemWidth + p12 * 2 : itemWidth * 2 + p12 * 3)));

            g.setPadding(p12, p12, p12, p12);
            g.setNumColumns(8);
            g.setAdapter(new WaterBAdapter(itemWidth));

            g.setOnItemClickListener((parent, view, position, id) -> {
                WaterData d = ds.get(position);
                UserDetailActivity.open(context, d.name, d.imgSrc, d.uid);
            });

            g.setOnItemLongClickListener((parent, view, position, id) -> {
                WaterData d = ds.get(position);
                Toast.makeText(context, d.name + "\n帖子数:" + d.num, Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

    private class WaterBAdapter extends android.widget.BaseAdapter {
        int itemWidth;

        public WaterBAdapter(int itemWidth) {
            this.itemWidth = itemWidth;
        }

        @Override
        public int getCount() {
            return ds.size();
        }

        @Override
        public Object getItem(int position) {
            return ds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CircleImageView imageView; // 声明ImageView的对象
            if (convertView == null) {
                imageView = new CircleImageView(context);
                AbsListView.LayoutParams p = new AbsListView.LayoutParams(itemWidth, itemWidth);
                imageView.setLayoutParams(p);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (CircleImageView) convertView;
            }

            Picasso.with(context)
                    .load(ds.get(position).imgSrc)
                    .placeholder(R.drawable.image_placeholder)
                    .into(imageView);
            return imageView;
        }
    }
}
