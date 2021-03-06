package com.trexin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.trexin.download.DownloadState;

public class LoginOffice365 extends Activity {
    class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // when start to load page, show url in activity's title bar
            setTitle(url);
        }

        @Override
        public void onPageFinished( WebView view, String url ) {
            CookieSyncManager.getInstance().sync();
            String cookie = CookieManager.getInstance().getCookie(url);
            Log.i( TrexinUtils.LOG_TAG, String.format( "Login interaction: page '%s'; cookies: '%s'", url,  cookie ));
            try {
                DownloadState downloadState = getIntent().getParcelableExtra( TrexinUtils.KEY_DOWNLOAD_STATE);
                // get the cookie and pass it to the token manager for saving
                Office365Token.saveToken( cookie, LoginOffice365.this );
                // set the resulting intent
                Intent result = new Intent();
                setResult( RESULT_OK,  result );
                result.putExtra( TrexinUtils.KEY_DOWNLOAD_STATE, downloadState );
                // finish the current activity without showing the content of page
                finish();
                Log.i( TrexinUtils.LOG_TAG, "Logged in Office 365." );
            } catch ( Office365Token.TokenNotValidException ignore ){
                // if the tokens are not
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.i( TrexinUtils.LOG_TAG, String.format( "onReceivedError: '%s'", failingUrl ));
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        // allow the title bar to show loading progress.
        this.requestWindowFeature( Window.FEATURE_PROGRESS );

        this.setContentView(R.layout.web_view_screen);
        WebView webview = (WebView) findViewById( R.id.webview_control );

        CookieManager cookieMgr = CookieManager.getInstance();
        cookieMgr.removeAllCookie();

        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient( new MyWebViewClient() );
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // show loading progress in activity's title bar.
                setProgress(progress * 100);
            }
        });

        String urlString = this.getString(R.string.url_login_page);
        webview.loadUrl( urlString );
    }
}
