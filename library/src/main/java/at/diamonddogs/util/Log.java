package at.diamonddogs.util;

import at.diamonddogs.http.BuildConfig;

/**
 * Created by Daniel.Hoeggerl on 16.12.2015.
 */
public class Log {

    public static void i(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.i(tag, string);
    }

    public static void e(String tag, String string, Throwable th) {
        if (BuildConfig.DEBUG) android.util.Log.e(tag, string, th);
    }

    public static void e(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.e(tag, string);
    }

    public static void d(String tag, String string, Throwable th) {
        if (BuildConfig.DEBUG) android.util.Log.d(tag, string, th);
    }

    public static void d(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.d(tag, string);
    }

    public static void v(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.v(tag, string);
    }

    public static void w(String tag, String string, Throwable th) {
        if (BuildConfig.DEBUG) android.util.Log.w(tag, string, th);
    }

    public static void w(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.w(tag, string);
    }
}
