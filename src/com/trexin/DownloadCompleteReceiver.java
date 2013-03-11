package com.trexin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class DownloadCompleteReceiver extends BroadcastReceiver {

    public static class ConfirmationDialog extends DialogFragment {
        public static ConfirmationDialog newInstance( int title, String text ){
            ConfirmationDialog frag = new ConfirmationDialog();
            Bundle args = new Bundle();
            args.putInt( "title", title );
            args.putString("text", text);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog( Bundle savedInstanceState ) {
            int title = this.getArguments().getInt( "title" );
            String text = this.getArguments().getString( "text" );

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle( title ).
                    setIcon( android.R.drawable.ic_dialog_alert ).
                    setMessage( text ).
                    setPositiveButton( android.R.string.ok, null );
            return builder.create();
        }
    }

    @Override
    public void onReceive( Context context, Intent intent ) {
        long downloadId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        DownloadManager downloadMgr = (DownloadManager)context.getSystemService( Context.DOWNLOAD_SERVICE );
        Uri downloadUri = downloadMgr.getUriForDownloadedFile( downloadId );
        if ( downloadUri == null ){
            ConfirmationDialog confirmationDialog = ConfirmationDialog.newInstance(
                                                                R.string.dialog_download_failed_title,
                                                                this.interpretDownloadError(context, downloadId));
            confirmationDialog.show( ((Activity)context).getFragmentManager(), "errorMessage" );
            return;
        }
        String mimeType = downloadMgr.getMimeTypeForDownloadedFile(downloadId);
        Intent webViewIntent = new Intent( Intent.ACTION_VIEW );
        webViewIntent.setDataAndType( downloadUri, mimeType );
        try {
            context.startActivity( webViewIntent );
        } catch ( ActivityNotFoundException e ){
            ConfirmationDialog confirmationDialog = ConfirmationDialog.newInstance(
                                                                R.string.dialog_cannot_open_title,
                                                                this.interpretReadingError(context, downloadId, e));
            confirmationDialog.show( ((Activity)context).getFragmentManager(), "errorMessage" );
        }
    }

    private String interpretDownloadError(Context context, long downloadId){
        DownloadManager downloadMgr = (DownloadManager)context.getSystemService( Context.DOWNLOAD_SERVICE );
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById( downloadId );
        Cursor cursor = downloadMgr.query(query);
        String filename = null;
        String downloadTarget = null;
        String errorDetails = context.getString( R.string.download_error_message_unknown );
        if ( cursor.moveToFirst() ) {
            downloadTarget = cursor.getString( cursor.getColumnIndex( DownloadManager.COLUMN_URI ) );
            filename = Uri.parse( downloadTarget ).getLastPathSegment();
            int downloadStatus = cursor.getInt( cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if ( downloadStatus == DownloadManager.STATUS_FAILED ){
                int reason = cursor.getInt(cursor.getColumnIndex( DownloadManager.COLUMN_REASON ));
                errorDetails = this.detailedDownloadMessage(context, reason);
            } else {
                errorDetails = context.getString( R.string.download_status_unrecognized, downloadStatus );
            }
        }
        Log.e( Constants.LOG_TAG, String.format( "Download '%s' failed. Error: '%s'", downloadTarget, errorDetails ));
        return context.getString( R.string.download_error_message, filename, errorDetails );
    }

    private String interpretReadingError( Context context, long downloadId, ActivityNotFoundException e  ){
        DownloadManager downloadMgr = (DownloadManager)context.getSystemService( Context.DOWNLOAD_SERVICE );
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById( downloadId );
        Cursor cursor = downloadMgr.query(query);
        String downloadTarget = null;
        String errorDetails = context.getString( R.string.download_error_message_unknown );
        if ( cursor.moveToFirst() ) {
            downloadTarget = cursor.getString( cursor.getColumnIndex( DownloadManager.COLUMN_URI ) );
            String filename = Uri.parse( downloadTarget ).getLastPathSegment();
            errorDetails = context.getString( R.string.download_cannot_open_message, filename );
        }
        Log.e(  Constants.LOG_TAG,
                String.format( "Download '%s' succeeded but cannot read the file.", downloadTarget ), e );
        return errorDetails;
    }

    private String detailedDownloadMessage(Context context, int reasonCode){
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
