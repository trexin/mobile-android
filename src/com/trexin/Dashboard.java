package com.trexin;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.trexin.model.DashboardItem;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends ListActivity {
    private List<DashboardItem> dashboardItems;
    private DashboardListAdapter dashboardListAdapter;

    private class DashboardListAdapter extends ArrayAdapter<DashboardItem> {
        public DashboardListAdapter( List<DashboardItem> items ) {
            super( Dashboard.this, R.layout.dashboard_item, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                convertView = inflater.inflate( R.layout.dashboard_item, parent, false );
            }

            // use ViewHolder here to prevent multiple calls to findViewById (if you have a large collection)

            DashboardItem dashboardItem = getItem(position);
            if ( dashboardItem != null ) {
                TextView textName = (TextView) convertView.findViewById(R.id.person_name);
                textName.setText( dashboardItem.getName());

                TextView textLocation = (TextView) convertView.findViewById(R.id.person_location);
                textLocation.setText( dashboardItem.getLocation());

                CheckBox checkBoxSubmitted = (CheckBox)convertView.findViewById( R.id.checkbox_submitted );
                checkBoxSubmitted.setChecked( dashboardItem.isFormSubmitted() );

                CheckBox checkBoxApproved = (CheckBox)convertView.findViewById( R.id.checkbox_approved );
                checkBoxApproved.setChecked( dashboardItem.isFormApproved() );
            }

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView( R.layout.dashboard );

        this.dashboardItems = parseDashboardXml();
        this.dashboardListAdapter = new DashboardListAdapter( this.dashboardItems );
        // ListView adapter (this class extends ListActivity)
        this.setListAdapter(this.dashboardListAdapter);
    }

    private List<DashboardItem> parseDashboardXml(){
        ///Log.d(Constants.LOG_TAG, "parse invoked");
        List<DashboardItem> result = new ArrayList<DashboardItem>();
        XmlPullParser parser = Xml.newPullParser();
        try {
            // auto-detect the encoding from the stream
            parser.setInput( getResources().openRawResource( R.raw.dashboard_items ), null);
            int eventType = parser.getEventType();

            DashboardItem currentItem = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
//                        Log.d(Constants.LOG_TAG, "  start tag: " + name);
                        if ( "item".equalsIgnoreCase(name) ){
                            currentItem = new DashboardItem();
                            result.add( currentItem );
                        } else if ( "name".equalsIgnoreCase( name )){
                            currentItem.setName( parser.nextText());
                        } else if ( "location".equalsIgnoreCase(name) ){
                            currentItem.setLocation( parser.nextText() );
                        } else if ( "form-submitted".equalsIgnoreCase(name) ){
                            currentItem.setFormSubmitted(Boolean.valueOf(parser.nextText()));
                        } else if ( "form-approved".equalsIgnoreCase(name) ){
                            currentItem.setFormApproved(Boolean.valueOf(parser.nextText()));
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e( Constants.LOG_TAG, "Exception parsing XML", e);
            throw new RuntimeException(e);
        }
        return result;
    }
}
