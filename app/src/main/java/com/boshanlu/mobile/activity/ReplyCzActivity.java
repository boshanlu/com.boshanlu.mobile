package com.boshanlu.mobile.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.model.SingleArticleData;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.KeyboardUtil;
import com.boshanlu.mobile.utils.UrlUtils;
import com.boshanlu.mobile.widget.MyFriendPicker;
import com.boshanlu.mobile.widget.emotioninput.SmileyInputRoot;
import com.boshanlu.mobile.widget.htmlview.HtmlView;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class ReplyCzActivity extends BaseActivity {

    Map<String, String> params = new HashMap<>();
    SingleArticleData data = null;
    private SmileyInputRoot rootView;
    private EditText input;
    private String postUrl = "";
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_cz);

        boolean isLz = false;
        String title = getString(R.string.app_name);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            data = b.getParcelable("data");
            isLz = b.getBoolean("islz", false);
            title = "回复" + data.index + ": " + data.username;
        }
        initToolBar(true, title);
        input = findViewById(R.id.ed_comment);
        MyFriendPicker.attach(this, input);
        findViewById(R.id.tv_edit).setVisibility(View.GONE);
        findViewById(R.id.tv_remove).setVisibility(View.GONE);
        findViewById(R.id.bt_lable_lz).setVisibility(isLz ? View.VISIBLE : View.GONE);

        rootView = findViewById(R.id.root);

        View btnSend = findViewById(R.id.btn_send);
        View smileyBtn = findViewById(R.id.btn_emotion);
        rootView.initSmiley(input, smileyBtn, btnSend);

        findViewById(R.id.content).setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                KeyboardUtil.hideKeyboard(input);
                rootView.hideSmileyContainer();
            }
            return false;
        });

        findViewById(R.id.btn_reply_cz).setOnClickListener(view -> {
            KeyboardUtil.showKeyboard(input);
        });

        if (data == null) {
            showToast("加载失败......");
            return;
        } else {
            ImageView imageView = findViewById(R.id.article_user_image);
            String img_url = UrlUtils.getAvatarUrl(data.getImg(), "m");
            Picasso.with(this).load(img_url).placeholder(R.drawable.image_placeholder).into(imageView);
            ((TextView) findViewById(R.id.replay_author)).setText(data.username);
            ((TextView) findViewById(R.id.replay_index)).setText(data.index);
            ((TextView) findViewById(R.id.replay_time)).setText(data.postTime);

            TextView t = findViewById(R.id.html_text);
            HtmlView.parseHtml(data.content).into(t);
            input.setHint("回复: " + data.username);

            imageView.setOnClickListener(v -> UserDetailActivity.openWithAnimation(
                    this, data.username,
                    imageView, data.uid));

        }

        HttpUtil.get(data.replyUrlTitle, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                Document document = Jsoup.parse(new String(response));
                Elements els = document.select("#postform");
                params.put("formhash", els.select("input[name=formhash]").attr("value"));
                params.put("posttime", els.select("input[name=posttime]").attr("value"));
                params.put("noticeauthor", els.select("input[name=noticeauthor]").attr("value"));
                params.put("noticetrimstr", els.select("input[name=noticetrimstr]").attr("value"));
                params.put("reppid", els.select("input[name=reppid]").attr("value"));
                params.put("reppost", els.select("input[name=reppost]").attr("value"));
                params.put("noticeauthormsg", els.select("input[name=noticeauthormsg]").attr("value"));
                params.put("replysubmit", "yes");
                postUrl = els.attr("action");
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
            }
        });

        btnSend.setOnClickListener(view -> {
            if (isLogin() && !TextUtils.isEmpty(input.getText())) {
                dialog = new ProgressDialog(ReplyCzActivity.this);
                dialog.setTitle("回复中");
                dialog.setMessage("请稍后......");
                dialog.show();
                replyCz(postUrl);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        KeyboardUtil.showKeyboard(input);
    }

    //回复层主
    private void replyCz(String url) {
        String inputStr = PostActivity.getPreparedReply(this, input.getText().toString());
        params.put("message", inputStr);
        HttpUtil.post(url, params, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                handleReply(true, res + "层主");
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                handleReply(false, e.getMessage());
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dialog.dismiss();
            }
        });
    }

    private void handleReply(boolean isok, String res) {
        if (isok) {
            if (res.contains("成功") || res.contains("层主")) {
                Toast.makeText(this, "回复发表成功", Toast.LENGTH_SHORT).show();
                input.setText(null);
                KeyboardUtil.hideKeyboard(input);
                rootView.hideSmileyContainer();
                setResult(RESULT_OK);
                finish();
            } else if (res.contains("您两次发表间隔")) {
                Toast.makeText(this, "您两次发表间隔太短了......", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "由于未知原因发表失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (!rootView.hideSmileyContainer()) {
            super.onBackPressed();
        }
    }
}
