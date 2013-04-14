package com.trexin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;


public class ErrorDialogFragment extends DialogFragment {
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
