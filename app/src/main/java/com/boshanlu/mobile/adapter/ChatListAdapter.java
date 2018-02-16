package com.boshanlu.mobile.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.activity.UserDetailActivity;
import com.boshanlu.mobile.model.ChatListData;
import com.boshanlu.mobile.widget.CircleImageView;
import com.boshanlu.mobile.widget.htmlview.HtmlView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by free2 on 16-3-30.
 * 私人消息 adapter
 */
public class ChatListAdapter extends BaseAdapter {

    private final int LEFT_ITEM = 0;
    private final int RIGHT_ITEM = 1;

    private List<ChatListData> DataSets;
    private Activity context;

    public ChatListAdapter(Activity context, List<ChatListData> datas) {
        DataSets = datas;
        this.context = context;
    }

    @Override
    protected int getDataCount() {
        return DataSets.size();
    }

    @Override
    protected int getItemType(int pos) {
        if (DataSets.get(pos).getType() == 0) {
            return LEFT_ITEM;
        } else {
            return RIGHT_ITEM;
        }
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case LEFT_ITEM:
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_l, parent, false));
            case RIGHT_ITEM:
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_r, parent, false));
        }
        return null;
    }

    private class MyViewHolder extends BaseViewHolder {

        protected CircleImageView avatar;
        protected TextView content, time;

        MyViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
            avatar = itemView.findViewById(R.id.logo);
            time = itemView.findViewById(R.id.post_time);

            avatar.setOnClickListener(v -> {
                String imageUrl = DataSets.get(getAdapterPosition()).getUserImage();
                UserDetailActivity.openWithAnimation(context, "username", avatar, imageUrl);
            });
        }

        void setData(final int position) {
            final ChatListData single = DataSets.get(position);
            Picasso.with(context).load(single.getUserImage()).into(avatar);
            time.setText(single.getTime());

            HtmlView.parseHtml(single.getContent()).into(content);
        }
    }

}
