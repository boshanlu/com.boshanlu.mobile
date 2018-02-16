package com.boshanlu.mobile.widget;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.KeyboardUtil;
import com.boshanlu.mobile.widget.emotioninput.ColorTextSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yang on 2016/12/11.
 *
 * @ 好友选择页面
 */

public class MyFriendPicker {

    public MyFriendPicker(Context context, EditText editText) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setCancelable(true);
        List<SimpleData> datas = new ArrayList<>();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_friend_picker, null);
        ProgressBar progressBar = view.findViewById(R.id.progress_view);
        View infoView = view.findViewById(R.id.info_view);
        TextView infoText = view.findViewById(R.id.info_text);
        RecyclerView recyclerView = view.findViewById(R.id.friend_list);
        view.findViewById(R.id.btn_close).setOnClickListener(view1 -> dialog.dismiss());
        view.findViewById(R.id.btn_ok).setOnClickListener(view12 -> {
            int j = 0;
            for (int i = 0; i < datas.size(); i++) {
                if (datas.get(i).isCheck) {
                    int start = editText.getSelectionStart();
                    if (j == 0) start--;
                    int end = editText.getSelectionEnd();
                    Editable editableText = editText.getEditableText();
                    String s = "@" + datas.get(i).name + " ";
                    editableText.replace(start, end, s);
                    editableText.setSpan(new ColorTextSpan(), start, start + s.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    j++;
                }
            }

            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.setOnDismissListener(dialogInterface -> KeyboardUtil.showKeyboard(editText));
        dialog.show();

        HttpUtil.get("misc.php?mod=getatuser&inajax=1&mobile=2", new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                if (!TextUtils.isEmpty(res) && res.contains("CDATA") && res.contains(",")) {
                    int start = res.indexOf("CDATA") + 6;
                    int end = res.indexOf("]]>");
                    String[] result = res.substring(start, end).split(",");
                    for (String i : result) {
                        datas.add(new SimpleData(i));
                    }
                    infoView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    recyclerView.addItemDecoration(new MyListDivider(context, MyListDivider.VERTICAL));
                    recyclerView.setAdapter(new FriendPickerAdapter(datas));
                } else {
                    infoText.setText("你还没有好友");
                }
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                infoText.setText("获取好友列表失败!!");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressBar.postDelayed(() -> progressBar.setVisibility(View.GONE), 300);
            }
        });
    }

    public static void attach(Context context, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (count == 1) {
                    char s = charSequence.charAt(start);
                    if (s == '@' && !TextUtils.isEmpty(App.getUid(context))) {
                        KeyboardUtil.hideKeyboard(editText);
                        show(context, editText);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public static void show(Context context, EditText editText) {
        new MyFriendPicker(context, editText);
    }

    private static class FriendPickerAdapter extends RecyclerView.Adapter<FriendPickerAdapter.ViewHolder> {

        private List<SimpleData> datas;
        private int count = 0;

        public FriendPickerAdapter(List<SimpleData> datas) {
            this.datas = datas;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_friend_picker, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.userName.setText(datas.get(position).name);
            holder.checkBox.setChecked(datas.get(position).isCheck);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView userName;
            private CheckBox checkBox;

            public ViewHolder(View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.name);
                checkBox = itemView.findViewById(R.id.check);
                checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                    datas.get(getAdapterPosition()).isCheck = b;
                    if (b) {
                        count++;
                    } else {
                        count--;
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
