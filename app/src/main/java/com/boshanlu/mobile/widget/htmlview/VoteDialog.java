package com.boshanlu.mobile.widget.htmlview;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.activity.PostActivity;
import com.boshanlu.mobile.model.VoteData;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.widget.MyListDivider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yang on 2017/5/24.
 * <p>
 * 投票选择页面
 */

public class VoteDialog {
    public VoteDialog(Context context, VoteData data) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setCancelable(true);
        List<SimpleData> datas = new ArrayList<>();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_friend_picker, null);
        view.findViewById(R.id.info_view).setVisibility(View.GONE);
        TextView title = view.findViewById(R.id.friend_sel);
        RecyclerView list = view.findViewById(R.id.friend_list);
        view.findViewById(R.id.btn_close).setOnClickListener(view1 -> dialog.dismiss());


        view.findViewById(R.id.btn_ok).setOnClickListener(view12 -> {
            String choose = "";
            int count = 0;
            for (int i = 0; i < datas.size(); i++) {
                SimpleData d = datas.get(i);
                if (d.isCheck) {
                    if (count == 0) {
                        choose = data.options.get(i).first;
                    } else {
                        choose = choose + "&pollanswers[]=" + data.options.get(i).first;
                    }
                    count++;

                    if (data.maxSelection == 1) {
                        break;
                    }
                }
            }

            if (count == 0) {
                Toast.makeText(context, "你还没有选择", Toast.LENGTH_SHORT).show();
                return;
            } else if (count > data.maxSelection) {
                Toast.makeText(context, "最多只能选择" + data.maxSelection + "项",
                        Toast.LENGTH_SHORT).show();
                return;
            }


            Map<String, String> paras = new HashMap<>();
            if (count == 1) {
                paras.put("pollanswers[]", choose);
            } else {
                data.url = data.url + "&pollanswers[]=" + choose;
                Log.d("==", data.url);
            }


            Toast.makeText(context, "提交中", Toast.LENGTH_SHORT).show();
            HttpUtil.post(data.url, paras, new ResponseHandler() {
                @Override
                public void onSuccess(byte[] response) {
                    String s = new String(response);
                    if (!s.contains("参数错误")) {
                        Toast.makeText(context, "投票成功", Toast.LENGTH_SHORT).show();
                        dialog.setOnDismissListener(dialog1 -> {
                            if (context instanceof PostActivity) {
                                PostActivity p = (PostActivity) context;
                                p.refresh();
                            }
                        });
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "投票失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    super.onFailure(e);
                    dialog.setOnDismissListener(null);
                    Toast.makeText(context, "投票失败", Toast.LENGTH_SHORT).show();
                }
            });
        });

        for (Pair<String, String> p : data.options) {
            datas.add(new SimpleData(p.second));
        }

        list.setVisibility(View.VISIBLE);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.addItemDecoration(new MyListDivider(context, MyListDivider.VERTICAL));
        list.setAdapter(new VoteAdapter(datas, data.maxSelection == 1));
        title.setText("投票(" + (data.maxSelection > 1 ? "多选,最多" + data.maxSelection + "项" :
                "单选") + ")");
        dialog.setContentView(view);
        dialog.show();
    }

    public static void show(Context context, VoteData data) {
        new VoteDialog(context, data);
    }

    private static class VoteAdapter extends RecyclerView.Adapter<VoteAdapter.ViewHolder> {

        private List<SimpleData> datas;
        private boolean isSingle;

        public VoteAdapter(List<SimpleData> datas, boolean isSingle) {
            this.datas = datas;
            this.isSingle = isSingle;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_vote, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.userName.setText(datas.get(position).name);
            if (isSingle) {
                holder.checkBox.setVisibility(View.GONE);
                holder.radioButton.setChecked(datas.get(position).isCheck);
            } else {
                holder.radioButton.setVisibility(View.GONE);
                holder.checkBox.setChecked(datas.get(position).isCheck);
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView userName;
            private CheckBox checkBox;
            private RadioButton radioButton;

            public ViewHolder(View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.name);
                checkBox = itemView.findViewById(R.id.check);
                radioButton = itemView.findViewById(R.id.radio);

                checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                    datas.get(getAdapterPosition()).isCheck = b;
                });

                radioButton.setOnCheckedChangeListener((buttonView, b) -> {
                    datas.get(getAdapterPosition()).isCheck = b;
                    if (b) {
                        for (int i = 0; i < datas.size(); i++) {
                            if (i == getAdapterPosition()) continue;

                            if (datas.get(i).isCheck) {
                                datas.get(i).isCheck = false;
                                notifyItemChanged(i);
                            }
                        }
                    }
                });
            }
        }
    }


    public class SimpleData {
        String name;
        boolean isCheck;

        public SimpleData(String name) {
            this.name = name;
        }
    }
}
