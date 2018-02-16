package com.boshanlu.mobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.listener.ListItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by free2 on 16-5-1.
 * 表情adapter
 */
public class SmileyAdapter extends RecyclerView.Adapter<SmileyAdapter.SmileyViewHolder> {

    private List<Pair<String, String>> smileys = new ArrayList<>();
    private ListItemClickListener itemListener;
    private Context context;

    public SmileyAdapter(Context context, ListItemClickListener itemListener, List<Pair<String, String>> smileys) {
        this.smileys = smileys;
        this.itemListener = itemListener;
        this.context = context;
    }


    @Override
    public SmileyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SmileyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_smiley, parent, false));
    }

    @Override
    public void onBindViewHolder(SmileyViewHolder holder, int position) {
        holder.setSmiley(position);
    }


    @Override
    public int getItemCount() {
        return smileys.size();
    }

    class SmileyViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;

        SmileyViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.smiley);
            image.setOnClickListener(view -> itemListener.onListItemClick(image, getAdapterPosition()));
        }


        private void setSmiley(int position) {
            Picasso.with(context).load(smileys.get(position).first).into(image);
        }


    }


}
