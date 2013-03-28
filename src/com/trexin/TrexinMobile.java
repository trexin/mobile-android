package com.trexin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import com.trexin.download.FileDownload;
import com.trexin.download.FileDownloadLoader;

public class TrexinMobile extends Activity implements LoaderManager.LoaderCallbacks<FileDownload>{
    private Handler handler = new Handler();

    private DownloadState downloadState;
    private boolean stateAlreadySaved;

    public static class ProgressDialogFragment extends DialogFragment {
        public static String PROGRESS_DIALOG_TAG = "progressDialog";

        public static ProgressDialogFragment newInstance() {
            return new ProgressDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ProgressDialog progressDialog = new ProgressDialog( getActivity() );
//            progressDialog.setTitle("Indeterminate");
            progressDialog.setMessage("Please wait while loading...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);

            // Disable the back button
            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            });
            return progressDialog;
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        public static String ERROR_DIALOG_TAG = "errorDialog";

        public static ErrorDialogFragment newInstance( int title, String text ){
            ErrorDialogFragment frag = new ErrorDialogFragment();
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

    private void initDownloadIfAny(){
        if ( this.downloadState != null ){
            TrexinUtils.logInfo(String.format( "Initiating the download '%s'...", this.downloadState ));
            Bundle args = new Bundle();
            args.putParcelable(TrexinUtils.KEY_DOWNLOAD_STATE, this.downloadState);
            this.getLoaderManager().initLoader( this.downloadState.getLoaderId(), args, this );
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

    private void downloadAndViewFile( String downloadUrl, Integer loaderId ){
        ProgressDialogFragment progressDialog = ProgressDialogFragment.newInstance();
        progressDialog.show( this.getFragmentManager(), ProgressDialogFragment.PROGRESS_DIALOG_TAG );
        this.downloadState = new DownloadState( downloadUrl, loaderId );
        this.initDownloadIfAny();
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

    private void changeUIOnDownloadComplete(FileDownload fileDownload){
        TrexinUtils.logInfo( String.format( "Starting changeUIOnDownloadComplete() for '%s'...", fileDownload ) );
        try {
            // 1. remove the Progress dialog
            Fragment dialogFragment = getFragmentManager().findFragmentByTag( ProgressDialogFragment.PROGRESS_DIALOG_TAG );
            if ( dialogFragment != null ){
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.remove( dialogFragment );
                transaction.commit();
            }
            if ( fileDownload.isLoginRequired() ){
                // 2. login is required
                TrexinUtils.logInfo( "Starting the Login screen ." );
                Intent loginIntent = new Intent( this, LoginOffice365.class );
                this.startActivityForResult( loginIntent, 0 );
                return;
            } else if ( !fileDownload.isSuccess() ){
                // 3. the download was a failure
                TrexinUtils.logWarn(String.format( "Download '%s' failed. Error: '%s'", fileDownload.getTargetUrl(),
                                                                                        fileDownload.getErrorMessage()));
                // 3a. show the error dialog box
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(
                                                                                R.string.dialog_download_failed_title,
                                                                                fileDownload.getErrorMessage());
                errorDialogFragment.show( getFragmentManager(), ErrorDialogFragment.ERROR_DIALOG_TAG );
                return;
            }
            // 4. Download is a success
            Uri localFile = fileDownload.getLocalFile();
            Intent viewDocumentIntent = new Intent( Intent.ACTION_VIEW );
            viewDocumentIntent.setDataAndType( localFile, fileDownload.getMimeType() );
            try {
                this.startActivity(viewDocumentIntent);
            } catch ( ActivityNotFoundException e ){
                TrexinUtils.logWarn(String.format(  "Download '%s' succeeded but cannot read the file.",
                        fileDownload.getTargetUrl()), e);
                String filename = localFile.getLastPathSegment();
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(
                        R.string.dialog_cannot_open_title,
                        this.getString(R.string.download_cannot_open_message, filename));
                errorDialogFragment.show( this.getFragmentManager(), ErrorDialogFragment.ERROR_DIALOG_TAG );
            }
        } finally {
            TrexinUtils.logInfo( String.format( "Completed changeUIOnDownloadComplete() for '%s'...", fileDownload ) );
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

    public Loader<FileDownload> onCreateLoader( int id, Bundle args ) {
        TrexinUtils.logInfo("onCreateLoader() called");
        DownloadState downloadState = args.getParcelable(TrexinUtils.KEY_DOWNLOAD_STATE);
        return new FileDownloadLoader( this, downloadState );
    }

    public void onLoadFinished( Loader<FileDownload> fileDownloadLoader, final FileDownload fileDownload ) {
        TrexinUtils.logInfo( String.format( "Starting onLoadFinished() for '%s'...", fileDownload ) );
        // NOTE: LoaderManager.LoaderCallbacks.onLoadFinished() javadoc says: an application is not allowed to commit
        // fragment transactions while in this call, since it can happen after an activity's state is saved.
        // Because of that I cannot directly call any fragment-operating methods. Instead I have to schedule these
        // operations for the future.
        if ( !this.stateAlreadySaved ){
            this.downloadState = null;
            this.handler.post( new Runnable() {
                public void run() {
                    changeUIOnDownloadComplete(fileDownload);
                }
            });
        } else {
            TrexinUtils.logWarn( String.format( "onLoadFinished() for '%s': onLoadFinished() is called after onSaveInstanceState(). UI updating code is skipped.", fileDownload ) );
        }
        TrexinUtils.logInfo( String.format( "Completed onLoadFinished() for '%s'", fileDownload ) );
    }

    public void onLoaderReset(Loader<FileDownload> fileDownloadLoader) {
        TrexinUtils.logInfo("onLoaderReset() called");
    }

    public void openMentorProgram( View view ){
        this.downloadAndViewFile( this.getString( R.string.url_mentor_program ), 0 );
    }

    public void openDevelopmentCalendar( View view ){
        this.downloadAndViewFile( this.getString( R.string.url_development_calendar ), 1 );
    }

    public void openDevelopmentTracking( View view ) {
        this.downloadAndViewFile( this.getString( R.string.url_development_tracking ), 2 );
    }

    public void openDevelopmentProgram( View view ) {
        this.downloadAndViewFile( this.getString( R.string.url_development_program ), 3 );
    }

    public void openPayrollSchedule( View view ) {
        this.downloadAndViewFile( this.getString( R.string.url_payroll_schedule ), 4 );
    }
}
