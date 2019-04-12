package com.shoyu666.log;

import android.util.Log;

public class MqttLogger {
    public static final String TAG = "MQTT_SHOYU666";

    public static void d(String tag, String msg) {
        Log.d(TAG, msg + " ## " + tag);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, msg + " ##" + tag);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        Log.e(TAG, msg + " ##  " + tag, throwable);
    }
}
