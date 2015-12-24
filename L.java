package com.yuexunit.fingerfinance.util;

import android.util.Log;


/**
 * LogUtils工具类
 *
 * Created by HuangYH on 2015/8/7.
 *
 * <p>
 * 工具说明：
 * 1.
 * 2.v,d,i,w,e均对应两个方法
 *   若不设置TAG或者TAG为空则为设置默认TAG
 * </p>
 */
public class L {

    public static boolean DEBUG = true; // 发布产品时改为false

    public static final String DEFAULT_TAG = "hyh";

    public static void openDebug() {
        DEBUG = true;
    }

    public static void closeDebug() {
        DEBUG = false;
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            e(DEFAULT_TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void w(String msg) {
        if (DEBUG) {
            w(DEFAULT_TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            i(DEFAULT_TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void d(String msg) {
        if (DEBUG) {
            d(DEFAULT_TAG, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void v(String msg) {
        if (DEBUG) {
            v(DEFAULT_TAG, msg);
        }
    }

}
