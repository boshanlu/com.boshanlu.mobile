package com.boshanlu.mobile.downloadfile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * 下载附件的处理文件处理函数
 */
public class FileUtil {
    /***********
     * 保存升级APK的目录
     ***********/
    public static final String path = "Download/博山庐手机客户端下载";

    /**
     * 方法描述：createFile方法
     */
    public static File createFile(String filename) {
        boolean isCreateFileSucess = true;
        File fileDir, downFile = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            fileDir = new File(Environment.getExternalStorageDirectory() + "/" + path + "/");
            if (!fileDir.exists()) {
                boolean b = fileDir.mkdirs();
            }

            System.out.println("download file dir " + fileDir.exists());
            downFile = new File(fileDir + "/" + filename);
            if (downFile.exists()) {
                if (downFile.delete()) {
                    try {
                        boolean b = downFile.createNewFile();
                        Log.i("create file", b + downFile.getPath() + "");
                    } catch (IOException e) {
                        isCreateFileSucess = false;
                        e.printStackTrace();
                    }
                }
            }
        } else {
            isCreateFileSucess = false;
        }

        if (isCreateFileSucess) {
            return downFile;
        } else {
            return null;
        }
    }


    public static boolean deleteFile(String filename) {
        File fileDir = new File(Environment.getExternalStorageDirectory() + "/" + path + "/");
        File file = new File(fileDir + "/" + filename);
        Log.i("file", "delete file");
        return !file.exists() || file.delete();

    }

    public static void requestHandleFile(Context context, String fileName) {
        File fileDir = new File(Environment.getExternalStorageDirectory() + "/" + path + "/");
        File file = new File(fileDir + "/" + fileName);
        if (fileName.endsWith(".apk")) {
            /*********下载完成，点击安装***********/
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            /**********加这个属性是因为使用Context的startActivity方法的话，就需要开启一个新的task**********/
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        } else {
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/" + path);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setDataAndType(uri, "*/*");
            if (intent.resolveActivityInfo(context.getPackageManager(), 0) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "没有打开目录的合适app,请自行打开目录 " + path, Toast.LENGTH_LONG).show();
            }
        }
    }

    private static String fileExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return "";
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            Log.i("type", ext.toLowerCase());
            return ext.toLowerCase();
        }
    }

    public static String getFileName(String url) {

        String fileName = url;
        if (url.contains("/")) {
            fileName = url.substring(url.lastIndexOf(".") + 1);
        }
        if (fileName.contains(".")) {
            String txt = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (txt.length() > 4) {
                return "null";
            }
            return fileName;
        } else {
            return "null";
        }
    }
}