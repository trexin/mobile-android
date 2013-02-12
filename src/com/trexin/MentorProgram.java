package com.trexin;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class MentorProgram extends Activity {
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.mentor_program);

        WebView webView = (WebView)this.findViewById( R.id.mentor_program_webview );
        webView.loadUrl( getString( R.string.url_mentor_program_asset ) );
    }
}
