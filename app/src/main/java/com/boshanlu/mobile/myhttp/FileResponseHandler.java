package com.boshanlu.mobile.myhttp;

import com.boshanlu.mobile.downloadfile.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public abstract class FileResponseHandler extends ResponseHandler {
    private File mFile = null;
    private String fileName = "null";
    private boolean isCancel = false;


    protected FileResponseHandler(String fileName) {
        if (!fileName.equals("null")) {
            this.fileName = fileName;
            mFile = FileUtil.createFile(fileName);
        }
    }

    public void cancelDownload() {
        isCancel = true;
    }

    @Override
    public void onSuccess(byte[] response) {
        onSuccess(getTargetFile());
    }

    @Override
    public void onFailure(Throwable e) {
        onFailure(e, getTargetFile());
    }

    @Override
    public void onStartDownload(String fileName) {
        onStartDownLoad(fileName);
    }

    protected File getTargetFile() {
        assert (mFile != null);
        return mFile;
    }


    public abstract void onStartDownLoad(String fileName);

    public abstract void onSuccess(File file);

    public abstract void onFailure(Throwable throwable, File file);

    public void onProgress(int progress, long totalBytes) {
        // Do nothing by default
    }

    @Override
    protected void processResponse(HttpURLConnection connection) throws IOException {
        int down_step = 2;// 提示step
        long totalSize;// 文件总大小
        long downloadCount = 0;// 已经下载好的大小
        int updateCount = 0;// 下载进度计数

        InputStream instream = connection.getInputStream();
        if (connection.getResponseCode() == 404) {
            onFailure(new Exception("file 404"), getTargetFile());
            return;
        }
        if (mFile == null) {
            if (connection.getHeaderField("Content-Disposition") != null) {
                fileName = connection.getHeaderField("Content-Disposition");
                fileName = fileName.substring(22, fileName.length() - 1);
                mFile = FileUtil.createFile(fileName);
            }
        }
        totalSize = connection.getContentLength();
        FileOutputStream fos = new FileOutputStream(getTargetFile());
        sendStartDownloadMessage(fileName);
        try {
            byte[] tmp = new byte[1024];
            int len;
            while ((len = instream.read(tmp)) != -1 && !Thread.currentThread().isInterrupted() && (!isCancel)) {
                downloadCount += len;// 时时获取下载到的大小
                fos.write(tmp, 0, len);
                if (updateCount == 0 || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
                    updateCount += down_step;
                    // 改变通知栏
                    sendProgressMessage(updateCount, totalSize);
                }
            }
            fos.flush();
            fos.close();
            instream.close();
            onSuccess(getTargetFile());
        } catch (Exception e) {
            e.printStackTrace();
            onFailure(new Exception("received bytes length is not contentLength"), getTargetFile());
        }
    }

}
