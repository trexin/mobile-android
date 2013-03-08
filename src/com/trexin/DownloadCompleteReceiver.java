package com.trexin;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class DownloadCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive( Context context, Intent intent ) {
        long downloadId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        DownloadManager downloadMgr = (DownloadManager)context.getSystemService( Context.DOWNLOAD_SERVICE );
        Uri downloadUri = downloadMgr.getUriForDownloadedFile( downloadId );
        if ( downloadUri == null ){
            String errorMsg = this.makeErrorMessage( context, downloadId );
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            return;
        }
        String mimeType = downloadMgr.getMimeTypeForDownloadedFile(downloadId);
        Intent webViewIntent = new Intent( Intent.ACTION_VIEW );
        webViewIntent.setDataAndType( downloadUri, mimeType );
        try {
            context.startActivity( webViewIntent );
        } catch ( ActivityNotFoundException e ){
            Toast.makeText( context, "Cannot open file", Toast.LENGTH_LONG ).show();
        }
    }

    private String makeErrorMessage(  Context context, long downloadId ){
        DownloadManager downloadMgr = (DownloadManager)context.getSystemService( Context.DOWNLOAD_SERVICE );
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById( downloadId );
        Cursor cursor = downloadMgr.query(query);
        String filename = null;
        String errorDetails = context.getString( R.string.download_error_generic );
        if ( cursor.moveToFirst() ) {
            String downloadTarget = cursor.getString( cursor.getColumnIndex( DownloadManager.COLUMN_URI ) );
            filename = Uri.parse( downloadTarget ).getLastPathSegment();
            int downloadStatus = cursor.getInt( cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if ( downloadStatus == DownloadManager.STATUS_FAILED ){
                int reason = cursor.getInt(cursor.getColumnIndex( DownloadManager.COLUMN_REASON ));
                errorDetails = this.errorDetailMessage(context, reason);
                Log.w( Constants.LOG_TAG, "Failed. Reason: " + reason);
            } else {
                errorDetails = context.getString( R.string.download_status_unrecognized, downloadStatus );
                Log.d( Constants.LOG_TAG, "Unrecognized download status: " + downloadStatus );
            }
        }
        return context.getString( R.string.download_error_message, filename, errorDetails );
    }

    private String errorDetailMessage(Context context, int reasonCode){
        switch (reasonCode) {
            case DownloadManager.ERROR_CANNOT_RESUME:{
                return context.getString( R.string.download_error_cannot_resume );
            }
            case DownloadManager.ERROR_DEVICE_NOT_FOUND: {
                return context.getString( R.string.download_error_device_not_found );
            }
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS: {
                return context.getString( R.string.download_error_file_already_exists );
            }
            case DownloadManager.ERROR_FILE_ERROR:{
                return context.getString( R.string.download_error_file_error );
            }
            case DownloadManager.ERROR_HTTP_DATA_ERROR:{
                return context.getString( R.string.download_error_http_data_error );
            }
            case DownloadManager.ERROR_INSUFFICIENT_SPACE :{
                return context.getString( R.string.download_error_insufficient_space );
            }
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS: {
                return context.getString( R.string.download_error_too_many_redirects );
            }
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE: {
                return context.getString( R.string.download_error_unhandled_http_code );
            }
            case DownloadManager.ERROR_UNKNOWN: {
                return context.getString( R.string.download_error_unknown );
            }
            default: {
                return context.getString( R.string.download_error_http_with_code, reasonCode );
            }
        }

    }
}
