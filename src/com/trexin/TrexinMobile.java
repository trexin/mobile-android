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

    public void startMentorProgram( View view ){
      Intent mentorProgram = new Intent( this, MentorProgram.class);
      this.startActivity(mentorProgram);
    }

    public void startDevelopmentPlan( View view ){
      Intent developmentPlan = new Intent( this, DevelopmentPlan.class);
      this.startActivity(developmentPlan);
    }

    public void openSharePoint( View view ) {
      Intent openSharepoint = new Intent( Intent.ACTION_VIEW, Uri.parse( getString( R.string.url_sharepoint )));
      startActivity( openSharepoint );
    }

    public void startDashboard( View view ){
      Intent dashboard = new Intent( this, Dashboard.class);
      this.startActivity(dashboard);
    }
}
