package com.boshanlu.mobile.downloadfile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.myhttp.FileResponseHandler;
import com.boshanlu.mobile.myhttp.HttpUtil;

import java.io.File;

/***
 * 下载服务 2016 07 11
 *
 * @author yang
 */
public class DownloadService extends Service {
    public static final int DOWN_OK = 1;
    public static final int DOWNLOADING = 0;
    public static final int DOWN_ERROR = -1;
    private String filename = null;
    private int downloadProgress = 0;

    private Notification.Builder mBuilder;
    private NotificationManager mNotifyManager;
    private Intent intent = new Intent("me.yluo.ruisiapp.download");

    private FileResponseHandler handler;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * 方法描述：onStartCommand方法
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //取消下载按钮被电击
        if (intent.getExtras().containsKey("cancel")) {
            if (handler != null) {
                handler.cancelDownload();
                if (mNotifyManager != null) {
                    mNotifyManager.cancel(0);
                    FileUtil.deleteFile(filename);
                }
            }
            return super.onStartCommand(intent, flags, startId);
        }

        //开始下载
        downloadProgress = 0;
        //判断sd卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "SD卡不存在无法下载...", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }

        String down_url = intent.getStringExtra("download_url");
        filename = FileUtil.getFileName(down_url);
        handler = new FileResponseHandler(filename) {
            @Override
            public void onStartDownLoad(String fileName) {
                if (filename.equals("null") && !fileName.equals("null")) {
                    filename = fileName;
                }
                createNotification(filename);
            }

            @Override
            public void onProgress(int progress, long totalBytes) {
                super.onProgress(progress, totalBytes);
                updateProgress(DOWNLOADING, progress);
            }

            @Override
            public void onSuccess(File file) {
                updateProgress(DOWN_OK, 100);
            }

            @Override
            public void onFailure(Throwable throwable, File file) {
                Log.e("error", throwable.getMessage());
                updateProgress(DOWN_ERROR, 0);
            }
        };
        HttpUtil.get(down_url, handler);
        return super.onStartCommand(intent, flags, startId);
    }


    public void createNotification(final String filename) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> Toast.makeText(getApplicationContext(), "开始下载" + filename, Toast.LENGTH_SHORT).show());
        Intent resultIntent = new Intent(this, DownLoadActivity.class);
        resultIntent.putExtra("fileName", filename);
        resultIntent.putExtra("progress", downloadProgress);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new Notification.Builder(this);
        mBuilder.setContentTitle("下载文件" + filename)
                .setContentIntent(notifyPendingIntent)
                .setSmallIcon(R.mipmap.logo);
        mBuilder.setProgress(100, 0, false);
        mBuilder.setContentText("下载进度：" + 0 + "%");
        mNotifyManager.notify(0, mBuilder.build());
    }

    //type
    private void updateProgress(int type, int progress) {
        // Start a lengthy operation in a background thread
        /**
         * 发送广播给ui activity
         */
        downloadProgress = progress;
        intent.putExtra("progress", progress);
        intent.putExtra("type", type);
        sendBroadcast(intent);
        Log.d("===发送广播===", type + " " + progress);
        switch (type) {
            case DOWN_ERROR:
                mBuilder.setContentText("文件下载失败！")
                        .setContentIntent(null)
                        // Removes the progress bar
                        .setProgress(0, 0, false);

                mNotifyManager.notify(0, mBuilder.build());
                break;
            case DOWN_OK:
                mBuilder.setContentText("文件下载完成！")
                        // Removes the progress bar
                        .setProgress(0, 0, false);
                mNotifyManager.notify(0, mBuilder.build());
                /**
                 * 取消之前的notification 新建
                 */
                mNotifyManager.cancel(0);

                Intent okIntent = new Intent(this, DownLoadActivity.class);
                okIntent.putExtra("fileName", filename);
                okIntent.putExtra("progress", 100);
                // Creates the PendingIntent
                PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 1, okIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                mBuilder = new Notification.Builder(this);
                mBuilder.setContentTitle(filename + "下载完成")
                        .setContentText("文件下载完成，点击打开！！")
                        .setContentIntent(notifyPendingIntent)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.logo);

                mNotifyManager.notify(1, mBuilder.build());
                break;
            case DOWNLOADING:
                mBuilder.setProgress(100, progress, false);
                mBuilder.setContentText("下载进度：" + progress + "%");
                downloadProgress = progress;
                //发送Action为com.example.communication.RECEIVER的广播
                mNotifyManager.notify(0, mBuilder.build());
                break;
        }
    }
}