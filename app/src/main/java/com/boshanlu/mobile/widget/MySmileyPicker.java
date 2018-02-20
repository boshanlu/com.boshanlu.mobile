package com.boshanlu.mobile.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.adapter.SmileyAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by free2 on 16-7-19.
 * smiley picker
 * 表情选择器
 */

public class MySmileyPicker extends PopupWindow {
    private static final int SMILEY_DF = 0;
    private static final int SMILEY_TB = 1;
    private static final int SMILEY_JGZ = 2;
    private static final int SMILEY_ACN = 3;
    private Context mContext;
    private OnItemClickListener listener;
    private SmileyAdapter adapter;
    private List<Pair<String, String>> smileys = new ArrayList<>();
    private int smiley_type = SMILEY_DF;


    public MySmileyPicker(Context context) {
        super(context);
        mContext = context;
        init();
    }


    private void init() {
        View v = LayoutInflater.from(mContext).inflate(R.layout.my_smiley_view, null);
        TabLayout tab = v.findViewById(R.id.mytab);
        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);

        getSmileys();
        tab.addTab(tab.newTab().setText("默认"));
        tab.addTab(tab.newTab().setText("贴吧"));
        tab.addTab(tab.newTab().setText("金馆长"));
        tab.addTab(tab.newTab().setText("AC娘"));
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 7, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SmileyAdapter(mContext, (v1, position) -> {
            ImageView img = (ImageView) v1;
            smileyClick(img.getDrawable(), position);
            dismiss();
        }, smileys);

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                changeSmiley(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        recyclerView.setAdapter(adapter);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.rec_solid_primary_bg));
        setFocusable(true);
        setContentView(v);
    }


    private void changeSmiley(int position) {
        switch (position) {
            case 0:
                smiley_type = SMILEY_DF;
                break;
            case 1:
                smiley_type = SMILEY_TB;
                break;
            case 2:
                smiley_type = SMILEY_JGZ;
                break;
            case 3:
                smiley_type = SMILEY_ACN;
                break;
        }
        getSmileys();
        adapter.notifyDataSetChanged();
    }


    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private void getSmileys() {
        smileys.clear();
        String smiley_dir = "file:///android_asset/smiley/";
        int stringId = R.array.smiley_default;
        if (smiley_type == SMILEY_TB) {
            stringId = R.array.smiley_tieba;
        } else if (smiley_type == SMILEY_JGZ) {
            stringId = R.array.smiley_jgz;
        } else if (smiley_type == SMILEY_ACN) {
            stringId = R.array.smiley_acn;
        }
        String[] smileyArray = mContext.getResources().getStringArray(stringId);
        for (String aSmileyArray : smileyArray) {
            String path = smiley_dir + aSmileyArray.split(",")[0];
            String name = aSmileyArray.split(",")[1];
            smileys.add(new Pair<>(path, name));
        }
    }

    private void smileyClick(Drawable d, int position) {
        if (position > smileys.size()) {
            return;
        }

        String name = smileys.get(position).second;

        if (listener != null) {
            listener.itemClick(name, d);
        }
    }

    public interface OnItemClickListener {
        void itemClick(String str, Drawable a);
    }
}