package com.boshanlu.mobile.checknet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.utils.RuisUtils;

/**
 * Created by free2 on 16-4-13.
 * 检测网络变化切换内外网
 */
public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v("NetworkReceiver", "网络发生了变化");
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            new CheckNet(context).startCheck(new CheckNetResponse() {
                @Override
                public void onFinish(int type, String response) {
                    RuisUtils.getForums(context, App.ISLOGIN(context), false);
                }
            });
        }
    }
}
