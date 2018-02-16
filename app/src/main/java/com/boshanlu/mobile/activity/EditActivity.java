package com.boshanlu.mobile.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.model.Forum;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.RuisUtils;
import com.boshanlu.mobile.widget.MyColorPicker;
import com.boshanlu.mobile.widget.MySmileyPicker;
import com.boshanlu.mobile.widget.MySpinner;
import com.boshanlu.mobile.widget.emotioninput.EmotionInputHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by free2 on 16-8-4.
 * 编辑activity
 */
public class EditActivity extends BaseActivity implements View.OnClickListener {

    private EditText edTitle, edContent;
    private ProgressDialog dialog;
    private MySpinner typeidSpinner;
    private MyColorPicker myColorPicker;
    private MySmileyPicker smileyPicker;
    private TextView tvSelectType;
    private List<Forum> typeiddatas = new ArrayList<>();
    private View typeIdContainer;
    private int typeId;
    private String pid, tid;
    private Map<String, String> params;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_topic);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            pid = b.getString("PID");
            tid = b.getString("TID");
        } else {
            showToast("参数异常无法编辑");
        }

        myColorPicker = new MyColorPicker(this);
        smileyPicker = new MySmileyPicker(this);
        initToolBar(true, "编辑帖子");
        typeidSpinner = new MySpinner(this);


        View btnDone = addToolbarMenu(R.drawable.ic_done_black_24dp);
        btnDone.setOnClickListener(view -> {
            if (checkPostInput()) {
                dialog = new ProgressDialog(EditActivity.this);
                dialog.setMessage("提交中,请稍后......");
                dialog.show();
                startPost();
            }
        });

        findViewById(R.id.forum_container).setVisibility(View.GONE);
        typeIdContainer = findViewById(R.id.type_id_container);
        typeIdContainer.setVisibility(View.GONE);
        tvSelectType = findViewById(R.id.tv_select_type);
        tvSelectType.setOnClickListener(this);
        edTitle = findViewById(R.id.ed_title);
        edContent = findViewById(R.id.ed_content);
        typeidSpinner.setListener((pos, v) -> {
            typeId = typeiddatas.get(pos).fid;
            tvSelectType.setText(typeiddatas.get(pos).name);
        });
        final LinearLayout edit_bar = findViewById(R.id.edit_bar);
        for (int i = 0; i < edit_bar.getChildCount(); i++) {
            View c = edit_bar.getChildAt(i);
            if (c instanceof ImageView) {
                c.setOnClickListener(this);
            }
        }

        Spinner setSize = findViewById(R.id.action_text_size);
        setSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //[size=7][/size]
                if (edContent == null || (edContent.getText().length() <= 0 && i == 0)) {
                    return;
                }
                handleInsert("[size=" + (i + 1) + "][/size]");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        myColorPicker.setListener((pos, v, color) -> handleInsert("[color=" + color + "][/color]"));

        EmotionInputHandler handler = new EmotionInputHandler(edContent, (enable, s) -> {
            if (enable) {
                btnDone.setVisibility(View.VISIBLE);
            } else {
                btnDone.setVisibility(View.INVISIBLE);
            }
        });

        smileyPicker.setListener(handler::insertSmiley);
        start_edit();
    }

    private void start_edit() {
        String url = "forum.php?mod=post&action=edit&tid=" + tid + "&pid=" + pid + "&mobile=2";
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                Document document = Jsoup.parse(new String(response));
                params = RuisUtils.getForms(document, "postform");
                String title = params.get("subject");
                if (TextUtils.isEmpty(title)) {
                    edTitle.setVisibility(View.GONE);
                } else {
                    edTitle.setText(title);
                }

                if (TextUtils.isEmpty(params.get("message"))) {
                    showToast("本贴不支持编辑！");
                    finish();
                }
                edContent.setText(params.get("message"));

                Elements types = document.select("#typeid").select("option");
                for (Element e : types) {
                    typeiddatas.add(new Forum(Integer.parseInt(e.attr("value")), e.text()));
                }

                if (typeiddatas.size() > 0) {
                    typeIdContainer.setVisibility(View.VISIBLE);
                    tvSelectType.setText(typeiddatas.get(0).name);
                    typeId = typeiddatas.get(0).fid;
                } else {
                    typeIdContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                showToast("网络错误");
            }
        });
    }

    private void startPost() {
        String url = "forum.php?mod=post&action=edit&extra=&editsubmit=yes&mobile=2&handlekey=postform&inajax=1";
        params.put("editsubmit", "yes");
        if (typeId > 0) {
            params.put("typeid", String.valueOf(typeId));
        }

        params.put("subject", edTitle.getText().toString());
        params.put("message", edContent.getText().toString());

        HttpUtil.post(url, params, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                Log.e("resoult", res);
                if (res.contains("帖子编辑成功")) {
                    dialog.dismiss();
                    showToast("帖子编辑成功");
                    Intent i = new Intent();
                    if (edTitle.getVisibility() == View.VISIBLE) {
                        i.putExtra("TITLE", edTitle.getText().toString());
                    }
                    i.putExtra("CONTENT", edContent.getText().toString());
                    i.putExtra("PID", pid);
                    setResult(RESULT_OK, i);
                    EditActivity.this.finish();
                } else if (res.contains("class=\"jump_c\"")) {
                    int start = res.indexOf("<p>", res.indexOf("class=\"jump_c\"")) + 3;
                    int end = res.indexOf("</p>", start);
                    String reason = res.substring(start, end);
                    postFail(reason);
                } else {
                    postFail("编辑失败:我也不知道哪儿错了");
                }
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                dialog.dismiss();
                showToast("网络错误");
            }
        });
    }

    private void handleInsert(String s) {
        int start = edContent.getSelectionStart();
        Editable edit = edContent.getEditableText();//获取EditText的文字
        if (start < 0 || start >= edit.length()) {
            edit.append(s);
        } else {
            edit.insert(start, s);//光标所在位置插入文字
        }
        //[size=7][/size]
        int a = s.indexOf("[/");
        if (a > 0) {
            edContent.setSelection(start + a);
        }
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.action_bold:
                handleInsert("[b][/b]");
                break;
            case R.id.action_italic:
                handleInsert("[i][/i]");
                break;
            case R.id.action_quote:
                handleInsert("[quote][/quote]");
                break;
            case R.id.action_color_text:
                myColorPicker.showAsDropDown(view, 0, 10);
                break;
            case R.id.action_emotion:
                ((ImageView) view).setImageResource(R.drawable.ic_edit_emoticon_accent_24dp);
                smileyPicker.showAsDropDown(view, 0, 10);
                smileyPicker.setOnDismissListener(() -> ((ImageView) view).setImageResource(R.drawable.ic_edit_emoticon_24dp));
                break;
            case R.id.action_backspace:
                int start = edContent.getSelectionStart();
                int end = edContent.getSelectionEnd();
                if (start == 0) {
                    return;
                }
                if ((start == end) && start > 0) {
                    start = start - 1;
                }
                edContent.getText().delete(start, end);
                break;
            case R.id.tv_select_type:
                typeidSpinner.setData(typeiddatas);
                typeidSpinner.setWidth(view.getWidth());
                typeidSpinner.showAsDropDown(view, 0, 15);
        }
    }

    private boolean checkPostInput() {
        if (typeiddatas.size() > 0 && typeId <= 0) {
            Toast.makeText(this, "请选择主题分类", Toast.LENGTH_SHORT).show();
            return false;
        } else if ((edTitle.getVisibility() == View.VISIBLE) && TextUtils.isEmpty(edTitle.getText().toString().trim())) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(edContent.getText().toString().trim())) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //发帖失败执行
    private void postFail(String str) {
        dialog.dismiss();
        showToast(str);
    }
}
