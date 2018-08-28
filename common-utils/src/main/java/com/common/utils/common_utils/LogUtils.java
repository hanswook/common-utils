package com.common.utils.common_utils;

import com.orhanobut.logger.Logger;

public class LogUtils {

    public static void d(String msg) {
        Logger.i(msg);
    }

    public static void w(String msg) {
        Logger.w(msg);
    }

    public static void d(Throwable throwable) {
        Logger.e(throwable, throwable != null ? throwable.getMessage() : "");
    }

    public static void json(String jsonStr) {
        Logger.json(jsonStr);
    }
}
