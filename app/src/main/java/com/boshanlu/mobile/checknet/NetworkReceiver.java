package com.boshanlu.mobile.checknet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by free2 on 16-4-13.
 * 检测网络变化切换内外网
 */
public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v("NetworkReceiver", "网络发生了变化");
    }
}
