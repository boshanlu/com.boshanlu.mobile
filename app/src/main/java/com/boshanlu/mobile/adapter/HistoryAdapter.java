package com.boshanlu.mobile.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.activity.PostActivity;
import com.boshanlu.mobile.model.ReadHistoryData;

import java.util.List;

/**
 * Created by free2 on 16-12-10.
 * 浏览历史adapter
 */
public class HistoryAdapter extends BaseAdapter {

    private static final int CONTENT = 0;
    private List<ReadHistoryData> datas;
    private Context context;

    public HistoryAdapter(Context context, List<ReadHistoryData> datas) {
        this.datas = datas;
        this.context = context;
    }

    @Override
    protected int getDataCount() {
        return datas.size();
    }

    @Override
    protected int getItemType(int pos) {
        return CONTENT;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new HistoryVivwHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false));
    }


    private class HistoryVivwHolder extends BaseViewHolder {
        protected TextView title, author, time;

        HistoryVivwHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            time = itemView.findViewById(R.id.time);
            itemView.findViewById(R.id.main_item_btn_item).setOnClickListener(v -> item_click());
        }

        @Override
        void setData(int position) {
            title.setText(datas.get(position).title);
            author.setText(datas.get(position).author);
            time.setText(datas.get(position).readTime);
        }

        void item_click() {
            String tid = datas.get(getAdapterPosition()).tid;
            if (!TextUtils.isEmpty(tid))
                PostActivity.open(context, "tid=" + tid, datas.get(getAdapterPosition()).author);
        }
    }
}
