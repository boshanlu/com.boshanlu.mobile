package com.boshanlu.mobile.widget.htmlview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.widget.htmlview.callback.ImageGetter;
import com.boshanlu.mobile.widget.htmlview.callback.ImageGetterCallBack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//rs 表情static/image/smiley/jgz/jgz065.png
//小图 不动
public class DefaultImageGetter implements ImageGetter {

    private static final String TAG = DefaultImageGetter.class.getSimpleName();
    //表情链接
    private static final String SMILEY_PREFIX = "static/image/smiley/";
    private static final String ALBUM_PREFIX = "forum.php?mod=image&aid=";
    private static Set<BitmapWorkerTask> taskCollection;
    private static ExecutorService mPool;

    static {
        taskCollection = new HashSet<>();
        if (mPool == null) {
            int thread = Runtime.getRuntime().availableProcessors();
            mPool = Executors.newFixedThreadPool(thread);
        }
    }

    private final int smileySize;//限制表情最大值
    private Context context;
    private ImageCacher imageCacher;
    private int maxWidth;//最大宽度 图片不要大于这个值


    public DefaultImageGetter(Context context, int maxWidth) {
        this.context = context;
        this.maxWidth = maxWidth;
        imageCacher = ImageCacher.instance(context.getCacheDir() + "/imageCache/");
        smileySize = (int) (HtmlView.FONT_SIZE * 1.6f);
    }

    public static Bitmap decodeBitmapFromStream(InputStream is, boolean needScale, int reqWidth) {
        if (is == null) return null;
        if (needScale) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth);
            options.inJustDecodeBounds = false;
            Bitmap src = BitmapFactory.decodeStream(is, null, options);
            return limitBitmap(src, reqWidth);
        } else {
            Bitmap src = BitmapFactory.decodeStream(is);
            return limitBitmap(src, reqWidth);
        }
    }

    public static Bitmap decodeBitmapFromRes(Resources res, int resId, boolean needScale, int reqWidth) {
        if (needScale) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth);
            options.inJustDecodeBounds = false;
            Bitmap src = BitmapFactory.decodeResource(res, resId, options);
            return limitBitmap(src, reqWidth);
        } else {
            Bitmap src = BitmapFactory.decodeResource(res, resId);
            return limitBitmap(src, reqWidth);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        // 源图片的高度和宽度
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            final int halfWidth = width / 2;
            while ((halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //限制最大图片
    private static Bitmap limitBitmap(Bitmap src, int maxWidth) {
        if (src == null) return null;
        int srcWidth = src.getWidth();
        if (srcWidth <= maxWidth) return src;

        float scale = maxWidth * 1.0f / srcWidth;
        int dstHeight = (int) (scale * src.getHeight());

        Bitmap dst = Bitmap.createScaledBitmap(src, maxWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    @Override
    public void getDrawable(String source, int start, int end, ImageGetterCallBack callBack) {
        if (callBack == null) return;
        boolean isInRam = true; //是否在内存
        String cacheKey = source; //缓存key
        Bitmap b = null;
        if (!TextUtils.isEmpty(source)) {
            if (source.startsWith(SMILEY_PREFIX)) { //表情文件 内存->assets->文件->网络
                cacheKey = source.substring(source.indexOf("smiley/"));
                //内存
                b = imageCacher.getMemCache(cacheKey);

                //assets 表情
                String fileToSave = null;
                if (b == null) {
                    isInRam = false;
                    fileToSave = source.substring(source.indexOf("smiley"));
                    if (source.contains("/tieba") || source.contains("/jgz") || source.contains("/acn") || source.contains("/default")) {
                        if (source.contains("/default")) {
                            fileToSave = fileToSave.replace(".gif", ".png");
                        }
                        try {
                            b = decodeBitmapFromStream(context.getAssets().open(fileToSave), false, smileySize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //不在assets,检查文件
                if (b == null) {
                    File smileyFile = new File(context.getFilesDir() + "/" + fileToSave);
                    if (smileyFile.exists()) {
                        try {
                            b = decodeBitmapFromStream(new FileInputStream(smileyFile), false, smileySize);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                b = scaleSmiley(b, smileySize);
            } else if (source.startsWith(ALBUM_PREFIX)) {//图片文件 可以浏览大图
                cacheKey = GetId.getId("aid=", source); //cacheKey = aid
                if (!TextUtils.isEmpty(cacheKey)) {
                    //内存
                    b = imageCacher.getMemCache(cacheKey);
                    if (b == null) {
                        isInRam = false;

                        //硬盘
                        b = BitmapFactory.decodeStream(imageCacher.getDiskCacheStream(cacheKey));
                        b = limitBitmap(b, maxWidth);
                    }
                }
            } else {//其余图片
                b = imageCacher.getMemCache(cacheKey);
                if (b == null) {
                    isInRam = false;
                    //检查硬盘
                    b = BitmapFactory.decodeStream(imageCacher.getDiskCacheStream(cacheKey));
                    b = limitBitmap(b, maxWidth);
                }
            }

            if (!isInRam && b != null) {//放到内存缓存
                imageCacher.putMemCache(cacheKey, b);
            }

            if (b == null) {
                //没有缓存去下载
                if (!mPool.isShutdown()) {
                    mPool.execute(new BitmapWorkerTask(source, cacheKey, start, end, callBack));
                }
            }
        }

        callBack.onImageReady(source, start, end, bmpToDrawable(source, b));
    }

    public void cancelAllTasks() {
        if (taskCollection != null) {
            for (BitmapWorkerTask t : taskCollection) {
                t.cancel();
            }
        }

        if (mPool != null && !mPool.isShutdown()) {
            synchronized (mPool) {
                mPool.shutdownNow();
            }
        }
    }

    //永远不要返回null
    public Drawable bmpToDrawable(String source, Bitmap b) {
        if (b == null) {
            return getPlaceHolder(source);
        } else {
            Drawable d = new BitmapDrawable(context.getResources(), b);
            d.setBounds(0, 0, b.getWidth(), b.getHeight());
            return d;
        }
    }

    private Drawable getPlaceHolder(String souce) {
        ColorDrawable colorDrawable = new ColorDrawable(0xffcccccc);
        if (souce == null || souce.isEmpty()) {
            colorDrawable.setBounds(0, 0, 120, 120);
        } else if (souce.startsWith(SMILEY_PREFIX)) {
            colorDrawable.setBounds(0, 0, smileySize, smileySize);
        } else {
            colorDrawable.setBounds(0, 0, (int) (maxWidth / 2.0f), (int) (maxWidth / 4.0f));
        }

        return colorDrawable;
    }

    //缩放图片
    private Bitmap scaleSmiley(Bitmap origin, int dstWidth) {
        if (origin == null || origin.isRecycled()) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scale = ((float) dstWidth) / width;

        //一点点误差忽略不计
        if (Math.abs(scale - 1) < 0.15) {
            return origin;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    //图片下载及存储
    private class BitmapWorkerTask implements Runnable {
        private String source;
        private boolean isCancel;
        private int start, end;
        private ImageGetterCallBack callBack;
        private String cacheKey;

        public BitmapWorkerTask(String source, String key, int start, int end, ImageGetterCallBack callBack) {
            this.source = source;
            this.start = start;
            this.end = end;
            this.callBack = callBack;
            this.cacheKey = key;
        }

        public void cancel() {
            isCancel = true;
        }

        @Override
        public void run() {
            boolean isSmiley = false;
            taskCollection.add(this);
            Log.d(TAG, "start download image " + source);
            HttpURLConnection urlConnection = null;
            BufferedOutputStream out = null;
            BufferedInputStream in = null;
            Bitmap bitmap = null;

            isSmiley = source.startsWith(SMILEY_PREFIX); //表情

            try {
                final URL url = new URL(source.startsWith("http") ? source : App.BASE_URL + source);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream(), 4 * 1024);
                bitmap = BitmapFactory.decodeStream(in);
                if (bitmap != null && !isCancel) {
                    Log.d(TAG, "download image compete " + source);
                    Bitmap.CompressFormat f = Bitmap.CompressFormat.PNG;
                    if (source.endsWith(".jpg") || source.endsWith(".jpeg") ||
                            source.endsWith(".JPG") || source.endsWith(".JPEG")) {
                        f = Bitmap.CompressFormat.JPEG;
                    } else if (source.endsWith(".webp")) {
                        f = Bitmap.CompressFormat.WEBP;
                    }

                    if (isSmiley) { //缓存表情
                        String fileDir = source.substring(source.indexOf("/smiley"), source.lastIndexOf("/"));
                        File dir = new File(context.getFilesDir() + fileDir);
                        if (!dir.exists()) {
                            Log.d("image getter", "创建目录" + dir.mkdirs());
                        }
                        String path = source.substring(source.indexOf("/smiley"));
                        File file = new File(context.getFilesDir() + path);
                        Log.d(TAG, "save smiley to file:" + file);

                        out = new BufferedOutputStream(new FileOutputStream(file));
                        bitmap.compress(f, 100, out);
                        out.flush();
                        bitmap = scaleSmiley(bitmap, smileySize);
                    } else { //缓存一般图片
                        out = new BufferedOutputStream(
                                imageCacher.newDiskCacheStream(cacheKey), 4 * 1024);
                        bitmap.compress(f, 90, out);
                        out.flush();
                        //存到内存之前需要压缩
                        bitmap = limitBitmap(bitmap, maxWidth);
                    }

                    //存入内存缓存
                    imageCacher.putMemCache(cacheKey, bitmap);
                } else {
                    Log.d(TAG, "download image error " + source);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            taskCollection.remove(this);

            if (!isCancel && bitmap != null) {
                //如果下载失败就不用返回了 因为之前以前有holder了
                callBack.onImageReady(source, start, end, bmpToDrawable(source, bitmap));
            }
        }
    }
}

/**
 * 笔记 android 分辨率和dpi关系
 * ldpi	    120dpi	0.75
 * mdpi	    160dpi	1
 * hdpi	    240dpi	1.5
 * xhdpi    320dpi	2     1280*720   1dp=2px
 * xxhdpi： 480dpi  3     1920*1080 1dp=3px
 * xxxhdpi  640dpi  4
 */
