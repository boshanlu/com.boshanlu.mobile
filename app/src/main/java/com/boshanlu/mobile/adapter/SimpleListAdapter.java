package com.boshanlu.mobile.adapter;

import android.app.Activity;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.activity.PostActivity;
import com.boshanlu.mobile.model.ListType;
import com.boshanlu.mobile.model.SimpleListData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by free2 on 16-4-7.
 * 简单的adapter 比如用户信息
 * 我的收藏 我的帖子,搜索结果
 * 等都用这个
 */
public class SimpleListAdapter extends BaseAdapter {

    private static final int CONTENT = 0;
    private List<SimpleListData> Datas = new ArrayList<>();
    private Activity activity;
    private ListType type;

    public SimpleListAdapter(ListType type, Activity activity, List<SimpleListData> datas) {
        Datas = datas;
        this.activity = activity;
        this.type = type;
    }

    @Override
    protected int getDataCount() {
        return Datas.size();
    }

    @Override
    protected int getItemType(int pos) {
        return CONTENT;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new SimpleVivwHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sim_list, parent, false));
    }


    private class SimpleVivwHolder extends BaseViewHolder {
        protected TextView key;
        protected TextView value;

        SimpleVivwHolder(View itemView) {
            super(itemView);
            key = itemView.findViewById(R.id.key);
            value = itemView.findViewById(R.id.value);
            itemView.findViewById(R.id.main_item_btn_item).setOnClickListener(v -> item_click());

            System.out.println(type);
        }

        @Override
        void setData(int position) {
            String keystr = Datas.get(position).getKey();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                key.setText(Html.fromHtml(keystr, 0));
            } else {
                key.setText(Html.fromHtml(keystr));
            }
            String values = Datas.get(position).getValue();
            if (values.length() > 0) {
                value.setVisibility(View.VISIBLE);
                value.setText(values);
            } else {
                value.setVisibility(View.GONE);
            }
        }

        void item_click() {
            SimpleListData single_data = Datas.get(getAdapterPosition());
            String url = single_data.getExtradata();
            if (url != null && url.length() > 0) {
                PostActivity.open(activity, url, single_data.getValue());
            }
        }
    }
}
