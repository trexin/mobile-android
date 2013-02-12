package com.trexin;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class DevelopmentPlan extends Activity {
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.development_plan);

        WebView webView = (WebView)this.findViewById( R.id.development_plan_webview );
        webView.loadUrl( getString( R.string.url_development_plan ));
    }
}
