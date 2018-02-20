package com.boshanlu.mobile.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.model.Category;
import com.boshanlu.mobile.model.Forum;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuisUtils {

    /**
     * 获得板块图标
     */
    public static Drawable getForumlogo(Context contex, int fid) {
        try {
            InputStream ims = contex.getAssets().open("forumlogo/common_" + fid + "_icon.png");
            return Drawable.createFromStream(ims, null);
        } catch (IOException ex) {
            return null;
        }
    }

    //加载我的头像
    //size s m l
    public static void LoadMyAvatar(WeakReference<Context> context, String uid, WeakReference<ImageView> target, String size) {
        File f = new File(context.get().getFilesDir() + uid + size);
        String url;

        url = UrlUtils.getAvatarUrl(uid, size);

        if (f.exists()) {
            Picasso.with(context.get())
                    .load(f)
                    .error(R.drawable.image_placeholder)
                    .into(target.get());
        } else {
            new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... params) {
                    Bitmap b = null;
                    Context c = context.get();
                    if (c == null) return null;
                    try {
                        b = Picasso.with(c).load(params[0]).get();
                        FileOutputStream out = new FileOutputStream(f);
                        b.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return b;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null && target.get() != null && context.get() != null) {
                        Drawable d = new BitmapDrawable(context.get().getResources(), bitmap);
                        target.get().setImageDrawable(d);
                    }
                }
            }.execute(url);
        }
    }

    public static String getLevel(int a) {
        if (a < 0) {
            return "限制会员";
        } else if (a < 600) {
            return " 新手实习";
        } else if (a < 3200) {
            return " 注册会员";
        } else if (a < 8000) {
            return " 中级会员";
        } else if (a < 19000) {
            return " 高级会员";
        } else if (a < 500000) {
            return " 金牌会员";
        } else if (a < 999999999) {
            return " 论坛元老";
        } else {
            return "新手上路";
        }
    }


    //获得到下一等级的积分
    public static int getNextLevel(int a) {
        if (a < 0) {
            return 0;
        } else if (a < 600) {
            return 600;
        } else if (a < 3200) {
            return 3200;
        } else if (a < 8000) {
            return 8000;
        } else if (a < 19000) {
            return 19000;
        } else if (a < 500000) {
            return 500000;
        } else if (a < 999999999) {
            return 999999999;
        } else {
            return 100;
        }
    }

    public static float getLevelProgress(int a) {
        if (a < 0) {
            return 0;
        } else if (a < 600) {
            return a / 100f;
        } else if (a < 3200) {
            return (a - 600) / 2600f;
        } else if (a < 8000) {
            return (a - 3200) / 4800f;
        } else if (a < 19000) {
            return (a - 8000) / 11000f;
        } else if (a < 500000) {
            return (a - 19000) / 481000f;
        } else if (a < 999999999) {
            return (a - 500000) / (999999999 - 500000);
        } else if (a < 3500) {
            return (a - 3000) / 500f;
        } else if (a < 6000) {
            return (a - 3500) / 2500f;
        } else {
            return 0;
        }
    }

    public static Map<String, String> getForms(Document document, String id) {
        Element element = document.getElementById(id);
        Map<String, String> params = new HashMap<>();
        if (element == null) return params;
        Elements inputs = element.select("input");
        for (Element ee : inputs) {
            String key = ee.attr("name");
            String type = ee.attr("type");
            String value = ee.attr("value");
            if (!TextUtils.isEmpty(key) && !"submit".equals(type)) {
                params.put(key, value);
            }
        }

        Elements textareas = element.select("textarea");
        for (Element ee : textareas) {
            String key = ee.attr("name");
            String value = ee.html();
            params.put(key, value);
        }

        return params;
    }

    public static List<Category> getForums(Context context, boolean isLogin) {
        String url = "api/mobile/index.php?module=forumindex&version=4";
        List<Category> cates = new ArrayList<>();
        HttpUtil.SyncGet(context, url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                try {
                    JSONObject resData = new JSONObject(res).getJSONObject("Variables");
                    JSONArray cateData = resData.getJSONArray("catlist");
                    JSONArray forumData = resData.getJSONArray("forumlist");
                    Map<String, Forum> forums = new HashMap<>();

                    for (int j = 0; j < forumData.length(); j++) {
                        JSONObject o = forumData.getJSONObject(j);
                        forums.put(o.getString("fid"), new Forum(o.getString("name").replace("（","\n（"), o.getInt("fid"), true));
                    }

                    for (int i = 0; i < cateData.length(); i++) {
                        JSONObject o = cateData.getJSONObject(i);
                        JSONArray forumList = o.getJSONArray("forums");
                        List<Forum> fs = new ArrayList<>();
                        for (int j = 0; j < forumList.length(); j++) {
                            fs.add(forums.get(forumList.getString(j)));
                        }
                        cates.add(new Category(o.getString("name"), o.getInt("fid"), true, true, fs));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                Toast.makeText(context, "网络错误，无法获取版块列表数据", Toast.LENGTH_SHORT).show();
            }
        });
        return cates;
    }

    public static String toHtml(String s) {
        s = s.replace("[b]", "<b>");
        s = s.replace("[/b]", "</b>");

        s = s.replace("[i]", "<i>");
        s = s.replace("[/i]", "</i>");

        s = s.replace("[quote]", "<blockquote>");
        s = s.replace("[/quote]", "</blockquote>");

        s = s.replace("[size=1]", "<font size=\"1\">");//<font size="6">哈哈</font>
        s = s.replace("[size=2]", "<font size=\"2\">");
        s = s.replace("[size=3]", "<font size=\"3\">");
        s = s.replace("[size=4]", "<font size=\"4\">");
        s = s.replace("[size=5]", "<font size=\"5\">");
        s = s.replace("[size=6]", "<font size=\"6\">");
        s = s.replace("[size=7]", "<font size=\"7\">");
        s = s.replace("[/size]", "</size>");

        return s;
    }
}
