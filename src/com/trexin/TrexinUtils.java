package com.trexin;

import android.util.Log;

public class TrexinUtils {
    public static final String LOG_TAG = "TrexinMobile";
    public static final String KEY_DOWNLOAD_STATE = "downloadState";

    private static String decorateMessage( String message ){
        return String.format("+++ Thread '%s': %s +++", Thread.currentThread().getName(), message);
    }

    public static void logInfo( String message ){
        Log.i( TrexinUtils.LOG_TAG, decorateMessage( message ));
    }

    public static void logWarn( String message ){
        Log.w( TrexinUtils.LOG_TAG, decorateMessage( message ) );
    }

    public static void logWarn( String message, Throwable error ){
        Log.w( TrexinUtils.LOG_TAG, decorateMessage( message ), error );
    }

    public static void logError( String message ){
        Log.e( TrexinUtils.LOG_TAG, decorateMessage( message ) );
    }
}
