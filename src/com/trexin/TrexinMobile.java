package com.trexin;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import com.trexin.download.DownloadResult;
import com.trexin.download.DownloadState;
import com.trexin.download.FileDownloadLoader;

public class TrexinMobile extends Activity implements LoaderManager.LoaderCallbacks<DownloadResult>{
    private Handler handler = new Handler();

    private DownloadState downloadState = new DownloadState();
    private boolean stateAlreadySaved;

    private void initDownloadIfAny(){
        Integer activeLoaderId = this.downloadState.getActiveLoaderId();
        if ( activeLoaderId != null ){
            TrexinUtils.logInfo(String.format( "Initiating the download '%s'...", this.downloadState ));
            Bundle args = new Bundle();
            args.putParcelable(TrexinUtils.KEY_DOWNLOAD_STATE, this.downloadState);
            this.getLoaderManager().initLoader( activeLoaderId, args, this );
        }
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.trexin_mobile );

        this.stateAlreadySaved = false;
        // as this onCreate() may be called as a result of configuration change, I need to re-initialize the loader
        if ( savedInstanceState != null ){
            this.downloadState = savedInstanceState.getParcelable( TrexinUtils.KEY_DOWNLOAD_STATE );
            this.initDownloadIfAny();
        }
//        Office365Token.clearToken( this );
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.stateAlreadySaved = false;
    }

    public void cancelDownload(){
        // destroy the corresponding loader
        Integer currentLoaderId = this.downloadState.getActiveLoaderId();
        if ( currentLoaderId != null ){
            this.getLoaderManager().destroyLoader( currentLoaderId );
        }
    }

    public void downloadAndViewFile( String url ){
        DownloadResult downloadResult = this.downloadState.cachedDownloadResult(url);
        if ( downloadResult != null ){
            // 1. if download result is already cached, simply render it in UI
            this.renderDownloadResult( downloadResult );
        } else {
            // 2. if the download result is not cached, start the downloading process
            ProgressDialogFragment progressDialog = ProgressDialogFragment.newInstance();
            progressDialog.show( this.getFragmentManager(), ProgressDialogFragment.PROGRESS_DIALOG_TAG );
            this.downloadState.markActiveDownload(url);
            this.initDownloadIfAny();
        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch (requestCode) {
            case 0: {
                if (resultCode == RESULT_OK && data != null) {
                    TrexinUtils.logInfo( "Scheduling download after successful login..." );
                    this.stateAlreadySaved = false;
                    this.downloadState = data.getParcelableExtra( TrexinUtils.KEY_DOWNLOAD_STATE );
                    this.initDownloadIfAny();
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void renderDownloadResult(DownloadResult downloadResult){
        TrexinUtils.logInfo( String.format( "Starting renderDownloadResult() for '%s'...", downloadResult) );
        try {
            // 1. remove the Progress dialog
            Fragment dialogFragment = getFragmentManager().findFragmentByTag( ProgressDialogFragment.PROGRESS_DIALOG_TAG );
            if ( dialogFragment != null ){
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.remove( dialogFragment );
                transaction.commit();
            }
            if ( downloadResult.isLoginRequired() ){
                // 2. login is required
                TrexinUtils.logInfo( "Starting the Login screen ." );
                Intent loginIntent = new Intent( this, LoginOffice365.class );
                this.startActivityForResult( loginIntent, 0 );
                return;
            } else if ( !downloadResult.isSuccess() ){
                // 3. the download was a failure
                TrexinUtils.logWarn(String.format( "Download '%s' failed. Error: '%s'", downloadResult.getTargetUrl(),
                                                                                        downloadResult.getErrorMessage()));
                // 3a. show the error dialog box
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(
                                                                                R.string.dialog_download_failed_title,
                                                                                downloadResult.getErrorMessage());
                errorDialogFragment.show( getFragmentManager(), ErrorDialogFragment.ERROR_DIALOG_TAG );
                return;
            }
            // 4. Download is a success
            Uri localFile = downloadResult.getLocalFile();
            Intent viewDocumentIntent = new Intent( Intent.ACTION_VIEW );
            viewDocumentIntent.setDataAndType( localFile, downloadResult.getMimeType() );
            try {
                this.startActivity(viewDocumentIntent);
            } catch ( ActivityNotFoundException e ){
                TrexinUtils.logWarn(String.format(  "Download '%s' succeeded but cannot read the file.",
                        downloadResult.getTargetUrl()), e);
                String filename = localFile.getLastPathSegment();
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(
                        R.string.dialog_cannot_open_title,
                        this.getString(R.string.download_cannot_open_message, filename));
                errorDialogFragment.show( this.getFragmentManager(), ErrorDialogFragment.ERROR_DIALOG_TAG );
            }
        } finally {
            TrexinUtils.logInfo( String.format( "Completed renderDownloadResult() for '%s'...", downloadResult) );
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState( outState );
        // 1. mark the activity with 'state already saved'
        this.stateAlreadySaved = true;
        // 2. carry over the 'current download url' in case of configuration change
        outState.putParcelable( TrexinUtils.KEY_DOWNLOAD_STATE, this.downloadState );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // is activity simply pausing (i.e. configuration change) or completely finishing?
        if ( this.isFinishing() ) {
            // The activity is really finishing
            new Thread( new Runnable(){
                public void run() {
                    // 1. cancel the active download if any
                    cancelDownload();
                    // 2. clear the cached files if any
                    downloadState.clearCachedFiles();
                }
            }, "downloadState-cleanup" ).start();
        }
    }

    public Loader<DownloadResult> onCreateLoader( int id, Bundle args ) {
        TrexinUtils.logInfo("onCreateLoader() called");
        DownloadState downloadState = args.getParcelable(TrexinUtils.KEY_DOWNLOAD_STATE);
        return new FileDownloadLoader( this, downloadState.getActiveDownloadUrl() );
    }

    public void onLoadFinished( Loader<DownloadResult> fileDownloadLoader, final DownloadResult downloadResult) {
        TrexinUtils.logInfo( String.format( "Starting onLoadFinished() for '%s'...", downloadResult) );
        // NOTE: LoaderManager.LoaderCallbacks.onLoadFinished() javadoc says: an application is not allowed to commit
        // fragment transactions while in this call, since it can happen after an activity's state is saved.
        // Because of that I cannot directly call any fragment-operating methods. Instead I have to schedule these
        // operations for the future.
        if ( !this.stateAlreadySaved ){
            this.downloadState.endActiveDownload( ((FileDownloadLoader)fileDownloadLoader).getDownloadUrl(), downloadResult );
            this.handler.post( new Runnable() {
                public void run() {
                    renderDownloadResult( downloadResult );
                }
            });
        } else {
            TrexinUtils.logWarn( String.format( "onLoadFinished() for '%s': onLoadFinished() is called after onSaveInstanceState(). UI updating code is skipped.", downloadResult) );
        }
        TrexinUtils.logInfo( String.format( "Completed onLoadFinished() for '%s'", downloadResult) );
    }

    public void onLoaderReset(Loader<DownloadResult> fileDownloadLoader) {
        TrexinUtils.logInfo("onLoaderReset() called");
    }
}
