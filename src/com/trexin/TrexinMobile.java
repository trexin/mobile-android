package com.trexin;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class TrexinMobile extends Activity {
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trexin_mobile);

        DownloadCompleteReceiver downloadCompleteReceiver = new DownloadCompleteReceiver();
        this.registerReceiver( downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

//        Office365Token.clearToken( this );
    }

    private void downloadAndViewFile( String downloadUrl ){
        try {
            Log.i( Constants.LOG_TAG, String.format( "Scheduling download '%s'...", downloadUrl ));
            startDownload(downloadUrl, Office365Token.asCookie(this));
            Log.i( Constants.LOG_TAG, String.format( "Download '%s' successfully scheduled.", downloadUrl ));
        } catch ( Office365Token.TokenNotValidException e ) {
            Log.i( Constants.LOG_TAG, String.format( "Starting the Login screen for download '%s'. Initial attempt failed: '%s'.",
                                                      downloadUrl, e.getMessage() ));
            Intent loginIntent = new Intent( this, LoginOffice365.class );
            loginIntent.putExtra( Constants.INTENT_KEY_DOWNLOAD_URI, downloadUrl );
            this.startActivityForResult( loginIntent, 0 );
        }
    }

    private void startDownload( String downloadUrl, String cookieString ){
        Log.i( Constants.LOG_TAG, String.format( "Starting download '%s' with cookie '%s'.", downloadUrl, cookieString ));
        DownloadManager downloadMgr = (DownloadManager) this.getSystemService( DOWNLOAD_SERVICE );
        Uri targetUri = Uri.parse( downloadUrl );
        // 1. figure the name of the file
        String filename = targetUri.getLastPathSegment();
        DownloadManager.Request request = new DownloadManager.Request( targetUri ).
                                        setDestinationInExternalFilesDir(this, null, filename).
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).
                                        addRequestHeader( "cookie", cookieString );
        downloadMgr.enqueue(request);
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch (requestCode) {
            case 0: {
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        String downloadUrl = data.getStringExtra( Constants.INTENT_KEY_DOWNLOAD_URI );
                        Log.i( Constants.LOG_TAG, String.format( "Scheduling download '%s' after successful login...",
                                                                 downloadUrl ));
                        this.startDownload(downloadUrl, Office365Token.asCookie(this));
                        Log.i( Constants.LOG_TAG, String.format(
                                                    "Download '%s' successfully scheduled  after successful login.",
                                                    downloadUrl ));
                    } catch ( Office365Token.TokenNotValidException ignore ){}
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void openMentorProgram( View view ){
        this.downloadAndViewFile( this.getString( R.string.url_mentor_program ) );
    }

    public void openDevelopmentCalendar( View view ){
        this.downloadAndViewFile( this.getString( R.string.url_development_calendar ) );
    }

    public void openDevelopmentTracking( View view ) {
        this.downloadAndViewFile( this.getString( R.string.url_development_tracking ) );
    }

    public void openDevelopmentProgram( View view ) {
        this.downloadAndViewFile( this.getString( R.string.url_development_program ) );
    }

    public void openPayrollSchedule( View view ) {
        this.downloadAndViewFile( this.getString( R.string.url_payroll_schedule ) );
    }
}
