package com.mallto.sdk;

import android.util.Log;

public class MtLog {

    public static final String TAG = "beacon";

    public static void d(String msg) {
        if (Global.debug) {
            Log.d(TAG, msg);
        }
    }


    public static void i(String msg) {
        if (Global.debug) {
            Log.i(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (Global.debug) {
            Log.e(TAG, msg);
        }
    }
}
