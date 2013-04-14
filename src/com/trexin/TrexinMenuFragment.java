package com.trexin;

import android.app.ListFragment;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TrexinMenuFragment extends ListFragment {
    public class MenuAdapter extends ArrayAdapter<MenuItem> {
        public MenuAdapter(List<MenuItem> items) {
            super( TrexinMenuFragment.this.getActivity(), R.layout.main_menu_item, items );
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate( R.layout.main_menu_item, parent, false );
            }

            MenuItem menuItem = getItem(position);
            // 1. set up the menu item text
            TextView textTitle = (TextView) convertView.findViewById(R.id.title);
            textTitle.setText( menuItem.getDescription() );
            // 2. set up the menu thumbnail
            String imagePath = menuItem.getImagePath();
            if ( imagePath != null ){
                ImageView imageView = (ImageView) convertView.findViewById(R.id.thumbnail );
                try {
                    InputStream imageStream = getActivity().getAssets().open( imagePath );
                    imageView.setImageBitmap( BitmapFactory.decodeStream( imageStream ));
                } catch (IOException e ){
                    // TODO: we cannot ignore this exception
                    throw new RuntimeException(e);
                }
            }
            return convertView;
        }
    }

    private List<MenuItem> parseMenuXml(){
        List<MenuItem> result = new ArrayList<MenuItem>();
        XmlPullParser parser = Xml.newPullParser();
        try {
            // auto-detect the encoding from the stream
            parser.setInput( getResources().openRawResource( R.raw.menu_items ), null);
            int eventType = parser.getEventType();

            MenuItem currentItem = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        if ( "item".equalsIgnoreCase(name) ){
                            currentItem = new MenuItem();
                            result.add( currentItem );
                        } else if ( "description".equalsIgnoreCase( name )){
                            currentItem.setDescription( parser.nextText());
                        } else if ( "image".equalsIgnoreCase( name )){
                            currentItem.setImagePath(parser.nextText());
                        } else if ( "url".equalsIgnoreCase( name )){
                            currentItem.setUrl(parser.nextText());
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate( R.layout.main_menu_fragment, container, false );
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MenuAdapter listAdapter = new MenuAdapter( this.parseMenuXml() );
        this.setListAdapter(listAdapter);
    }

    @Override
    public void onListItemClick( ListView listView, View view, int position, long id) {
        MenuItem menuItem = (MenuItem)listView.getItemAtPosition( position );
        TrexinMobile trexinMobile = (TrexinMobile)this.getActivity();
        trexinMobile.downloadAndViewFile( menuItem.getUrl() );
    }
}
