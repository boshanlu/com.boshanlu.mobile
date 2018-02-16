package com.boshanlu.mobile.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.utils.DimmenUtils;
import com.boshanlu.mobile.utils.GetId;

/**
 * Created by free2 on 16-7-19.
 * color picker on edit bar
 * 发帖的时候颜色选择器
 */

public class MyColorPicker extends PopupWindow implements AdapterView.OnItemClickListener {

    private Context mContext;
    private OnItemSelectListener listener;
    private String[][] colorDatas = null;

    public MyColorPicker(Context context) {
        super(context);
        mContext = context;
        init();
    }


    private void init() {
        String[] colors = mContext.getResources().getStringArray(R.array.color_list);
        for (int i = 0; i < colors.length; i++) {
            if (colorDatas == null) {
                colorDatas = new String[colors.length][2];
            }
            colorDatas[i][0] = colors[i].split(",")[0];
            colorDatas[i][1] = colors[i].split(",")[1];
        }

        GridView gridView = new GridView(mContext);
        gridView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        gridView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        gridView.setNumColumns(8);
        gridView.setPadding(DimmenUtils.dip2px(mContext, 8), DimmenUtils.dip2px(mContext, 12), DimmenUtils.dip2px(mContext, 8), DimmenUtils.dip2px(mContext, 12));
        gridView.setGravity(Gravity.CENTER);
        gridView.setHorizontalSpacing(DimmenUtils.dip2px(mContext, 4));
        gridView.setVerticalSpacing(DimmenUtils.dip2px(mContext, 12));
        gridView.setOnItemClickListener(this);

        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.rec_solid_primary_bg));
        setFocusable(true);
        setContentView(gridView);

        MyAdapter adapter = new MyAdapter();
        gridView.setAdapter(adapter);
    }

    public void setListener(OnItemSelectListener listener) {
        this.listener = listener;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (listener != null) {
            listener.itemClick(i, view, colorDatas[i][0]);
        }
        dismiss();
    }

    public interface OnItemSelectListener {
        void itemClick(int pos, View v, String color);
    }


    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return colorDatas.length;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View colorView;
            colorView = new View(mContext);
            colorView.setLayoutParams(new GridView.LayoutParams(DimmenUtils.dip2px(mContext, 20), DimmenUtils.dip2px(mContext, 20)));//设置ImageView对象布局
            colorView.setPadding(4, 4, 4, 4);//设置间距
            int color = GetId.getColor(mContext, colorDatas[i][1]);
            colorView.setBackgroundColor(color);
            return colorView;
        }
    }
}