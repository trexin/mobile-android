package com.trexin.download;

import android.app.DownloadManager;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import com.trexin.Office365Token;
import com.trexin.R;
import com.trexin.TrexinUtils;

public class FileDownloadLoader extends AsyncTaskLoader<DownloadResult> {
    private String downloadUrl;
    private DownloadCompleteReceiver downloadCompleteReceiver;

    private class DownloadCompleteReceiver extends BroadcastReceiver {
        private long downloadId;
        private boolean active;
        private DownloadResult downloadResultResult;

        private DownloadCompleteReceiver(long downloadId) {
            this.downloadId = downloadId;
            this.active = true;
            // NOTE: because 'registerReceiver()' is called on the context which is an Activity, the receiver's
            // 'onReceive' is invoked on the UI thread.
            getContext().registerReceiver( this, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            TrexinUtils.logInfo( String.format( "%s instantiated", this ));
        }

        public DownloadResult getDownloadResultResult() {
            return this.downloadResultResult;
        }

        // NOTE: because DownloadCompleteReceiver was registered on the context which is an Activity, this
        // call is invoked on the main UI thread.
        @Override
        public void onReceive( Context context, Intent intent ) {
            long incomingDownloadId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            if ( this.downloadId == incomingDownloadId ){
                TrexinUtils.logInfo( String.format( "'%s'.onReceive() called", this ));

                DownloadManager downloadMgr = (DownloadManager)context.getSystemService( Context.DOWNLOAD_SERVICE );

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById( this.downloadId );
                Cursor cursor = downloadMgr.query( query );
                if ( !cursor.moveToFirst()){
                    throw new IllegalStateException( String.format( "Unexpected error: missing a record in DB for '%s'", this ));
                }
                String targetUrl = cursor.getString( cursor.getColumnIndex( DownloadManager.COLUMN_URI ) );
                int downloadStatus = cursor.getInt( cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if ( downloadStatus == DownloadManager.STATUS_SUCCESSFUL ) {
                    this.downloadResultResult = new DownloadResult( targetUrl,
                                                            downloadMgr.getUriForDownloadedFile( this.downloadId ),
                                                            downloadMgr.getMimeTypeForDownloadedFile( this.downloadId ));
                    // unregister itself
                    this.unregister( context );
                    // notify the loader about the download completion
                    FileDownloadLoader.this.onContentChanged();
                } else if ( downloadStatus == DownloadManager.STATUS_FAILED ){
                    int reason = cursor.getInt(cursor.getColumnIndex( DownloadManager.COLUMN_REASON ));
                    String errorMsg = this.detailedDownloadMessage( context, reason);
                    this.downloadResultResult = new DownloadResult( targetUrl, reason, errorMsg );
                    // unregister itself
                    this.unregister( context );
                    // notify the loader about the download completion
                    FileDownloadLoader.this.onContentChanged();
                }
            }
        }

        public void unregister( Context context ){
            if ( this.active ){
                TrexinUtils.logInfo( String.format( "'%s'.unregister() called", this ));
                context.unregisterReceiver(this);
                this.active = false;
            }
        }

        private String detailedDownloadMessage( Context context, int reasonCode){
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

        @Override
        public String toString() {
            return String.format( "DownloadCompleteReceiver [downloadId:'%s'; active='%s']", this.downloadId, this.active );
        }
    }

    public FileDownloadLoader( Context context, String downloadUrl ) {
        super( context );
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    @Override
    protected void onStartLoading() {
        // Invoked on main UI thread.
        TrexinUtils.logInfo( String.format( "'%s'.onStartLoading() called", this ));

        if ( this.downloadCompleteReceiver != null ) {
            // deliver any previously loaded data immediately.
            TrexinUtils.logInfo( "Immediately delivering previously loaded data to the client...");
            this.deliverResult( this.downloadCompleteReceiver.getDownloadResultResult() );
        } else {
            // start download listener
            Context context = this.getContext();
            DownloadManager downloadMgr = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri targetUri = Uri.parse( this.downloadUrl );
            // 1. figure the name of the file
            String filename = targetUri.getLastPathSegment();
            DownloadManager.Request request = new DownloadManager.Request( targetUri ).
                    setDestinationInExternalFilesDir( context, null, filename).
                    setNotificationVisibility( DownloadManager.Request.VISIBILITY_HIDDEN ).
                    addRequestHeader( "cookie", Office365Token.asCookie( context ));
            // 2. create and register new download complete receiver
            this.downloadCompleteReceiver = new DownloadCompleteReceiver( downloadMgr.enqueue(request) );
        }
    }

    @Override
    protected void onStopLoading() {
        TrexinUtils.logInfo( String.format( "'%s'.onStopLoading() called", this ));
        this.cancelLoad();
//        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        TrexinUtils.logInfo( String.format( "'%s'.onReset() called", this ));
        super.onReset();
        // Ensure the loader is stopped
        this.onStopLoading();

        if ( this.downloadCompleteReceiver != null ){
            // unregister the receiver
            this.downloadCompleteReceiver.unregister( this.getContext() );
            this.downloadCompleteReceiver = null;
        }
    }

    @Override
    public DownloadResult loadInBackground() {
        // A task that performs the asynchronous load
        TrexinUtils.logInfo( String.format( "'%s'.loadInBackground() started...", this ));
        try {
            return this.downloadCompleteReceiver.getDownloadResultResult();
        } finally {
            TrexinUtils.logInfo( String.format( "'%s'.loadInBackground() completed", this ));
        }
    }

    @Override
    public void deliverResult( DownloadResult newDownloadResult){
        TrexinUtils.logInfo( String.format( "'%s'.deliverResult() started...", this ));
        try {
            if (isReset()) {
                TrexinUtils.logWarn( "Warning! An async query came in while the Loader was reset!" );
                // The Loader has been reset; ignore the result and invalidate the data.
                // This can happen when the Loader is reset while an asynchronous query
                // is working in the background. That is, when the background thread
                // finishes its work and attempts to deliver the results to the client,
                // it will see here that the Loader has been reset and discard any
                // resources associated with the new data as necessary.
                return;
            }

            if ( isStarted() ) {
                // If the Loader is in a started state, have the superclass deliver the results to the client.
                super.deliverResult(newDownloadResult);
            }
        } finally {
            TrexinUtils.logInfo( String.format( "'%s'.deliverResult() completed...", this ));
        }
    }

    @Override
    public String toString() {
        return String.format( "FileDownloadLoader[loaderId='%s'; downloadUrl='%s']", this.getId(), this.downloadUrl );
    }
}
