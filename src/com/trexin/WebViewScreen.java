package com.trexin;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewScreen extends Activity {
    public static final String URL_PARAM = "url_param";
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.web_view_screen);

        WebView webview = (WebView) findViewById( R.id.webview_control);
        webview.getSettings().setJavaScriptEnabled(true);
        String urlString = this.getIntent().getStringExtra( URL_PARAM );
        webview.loadUrl( urlString );
    }
}
