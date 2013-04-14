package com.trexin;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

public class ProgressDialogFragment extends DialogFragment {
    public static String PROGRESS_DIALOG_TAG = "progressDialog";

    public static ProgressDialogFragment newInstance() {
        return new ProgressDialogFragment();
    }

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState ) {
        final ProgressDialog progressDialog = new ProgressDialog( getActivity() );
        progressDialog.setMessage( this.getString( R.string.dialog_progress_message ));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        // Disable the back button
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });

        progressDialog.setButton( DialogInterface.BUTTON_NEGATIVE,
                getString( android.R.string.cancel ),
                new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ) {
                        // 1. close the dialog
                        dialog.dismiss();
                        // 2. destroy the corresponding loader
                        TrexinMobile trexinMobile = (TrexinMobile)getActivity();
                        trexinMobile.cancelDownload();
                    }
                });
        return progressDialog;
    }
}
