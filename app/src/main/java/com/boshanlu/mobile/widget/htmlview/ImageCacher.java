package com.boshanlu.mobile.widget.htmlview;


import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 磁盘缓存文件名有一个计数XXXX_99
 * 用来表示使用次数 越多使用越不被清除
 * 硬盘cache一般是下载过后立即存入
 * 内存cache一般是使用时从硬盘读入
 */
public class ImageCacher {
    private static final String TAG = ImageCacher.class.getSimpleName();
    private static final long CACHE_SIZE = 10 * 1024 * 1024;//10m
    private static ImageCacher imageCacher;
    private static LruCache<String, Bitmap> mMemoryCache;

    static {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 6;
        Log.d(TAG, "image lrucache size:" + cacheSize);
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    private String cacheDir;

    private ImageCacher(String path) {
        this.cacheDir = path;
    }

    public static ImageCacher instance(String path) {
        if (imageCacher == null) {
            File f = new File(path);
            if (!f.exists() || !f.isDirectory()) {
                f.mkdirs();
            }
            imageCacher = new ImageCacher(path);
        }
        return imageCacher;
    }

    //存到内存
    public void putMemCache(String key, Bitmap bitmap) {
        if (bitmap == null) return;
        mMemoryCache.put(key, bitmap);
    }

    //从内存获取
    public Bitmap getMemCache(String key) {
        return mMemoryCache.get(key);
    }

    //新建一个硬盘缓存
    public OutputStream newDiskCacheStream(String key) throws IOException {
        long size = ensureCacheSize();
        Log.d(TAG, "cache size is :" + size);
        key = hashKeyForDisk(key) + "_0";
        File f = new File(cacheDir, key);
        f.createNewFile();
        Log.d(TAG, "create new disk cache " + key);
        return new FileOutputStream(f);
    }

    //获得一个硬盘缓存流
    public InputStream getDiskCacheStream(String key) {
        key = hashKeyForDisk(key);
        File f = new File(cacheDir);
        File[] fileList = f.listFiles();
        for (File oldFile : fileList) {
            int position = oldFile.getName().lastIndexOf("_");
            if (position <= 0) continue;
            String name = oldFile.getName().substring(0, position);
            if (name.equals(key)) {
                int count = Integer.parseInt(oldFile.getName().substring(position + 1)) + 1;
                File newFile = new File(cacheDir, name + "_" + count);
                Log.d(TAG, "rename file to " + newFile + " old file is " + oldFile);
                oldFile.renameTo(newFile);
                try {
                    return new FileInputStream(newFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public long getCacheSize() {
        File f = new File(cacheDir);
        File[] fileList = f.listFiles();
        long size = 0;
        for (File aFileList : fileList) {
            if (!aFileList.isDirectory()) {
                size = size + aFileList.length();
            }
        }
        return size;
    }

    private String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private long ensureCacheSize() {
        File f = new File(cacheDir);
        File[] fileList = f.listFiles();
        long size = 0;

        for (File aFileList : fileList) {
            if (!aFileList.isDirectory()) {
                size = size + aFileList.length();
            }
        }

        if (size > CACHE_SIZE) {
            Arrays.sort(fileList, (o1, o2) -> {
                int i1 = Integer.parseInt(o1.getName().substring(o1.getName().lastIndexOf("_") + 1));
                int i2 = Integer.parseInt(o2.getName().substring(o2.getName().lastIndexOf("_") + 1));
                return i1 - i2;
            });

            for (File aFileList : fileList) {
                size -= aFileList.length();
                aFileList.delete();
                if (size < CACHE_SIZE) {
                    break;
                }

            }
        }
        return size;
    }
}
