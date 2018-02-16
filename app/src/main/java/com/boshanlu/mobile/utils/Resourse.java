package com.boshanlu.mobile.utils;

import android.content.Context;
import android.os.Build;

public class Resourse {
    public static int getColor(Context context, int resId) {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.getColor(resId);
        } else {
            return context.getResources().getColor(resId, context.getTheme());
        }
    }
}
