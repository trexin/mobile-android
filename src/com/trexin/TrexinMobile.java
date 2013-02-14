package com.trexin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class TrexinMobile extends Activity {
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trexin_mobile);
    }

    public void openMentorProgram( View view ){
        Intent webViewIntent = new Intent( this, WebViewScreen.class);
        webViewIntent.putExtra( WebViewScreen.URL_PARAM, getString( R.string.url_mentor_program ));
        this.startActivity( webViewIntent );
    }

    public void openDevelopmentPlan( View view ){
        Intent webViewIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( getString( R.string.url_development_plan )));
        startActivity( webViewIntent );
//        Intent webViewIntent = new Intent( this, WebViewScreen.class);
//        webViewIntent.putExtra( WebViewScreen.URL_PARAM, getString( R.string.development_plan ));
//        this.startActivity( webViewIntent );
    }

    public void openSharePoint( View view ) {
        Intent webViewIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( getString( R.string.url_sharepoint )));
        startActivity( webViewIntent );
    }

    public void openDashboard( View view ){
        Intent webViewIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( getString( R.string.url_dashboard )));
        startActivity(webViewIntent);
    }

    public void startPrototype( View view ){
        Intent dashboard = new Intent( this, Dashboard.class);
        this.startActivity(dashboard);
    }
}
