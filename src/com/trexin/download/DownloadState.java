package com.trexin.download;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class DownloadState implements Parcelable {
    private String activeDownloadUrl;
    private Integer activeLoaderId;
    private Map<String, DownloadResult> cachedDownloadResults = new HashMap<String, DownloadResult>();
    private int nextLoaderId = 0;

    public static final Parcelable.Creator<DownloadState> CREATOR = new Parcelable.Creator<DownloadState>() {
        public DownloadState createFromParcel(Parcel in) {
            return new DownloadState( in );
        }

        public DownloadState[] newArray(int size) {
            return new DownloadState[size];
        }
    };

    public DownloadState() {}

    public DownloadState(Parcel in) {
        this.readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.cachedDownloadResults.size());
        for ( String key: this.cachedDownloadResults.keySet() ) {
            out.writeString( key );
            out.writeParcelable( this.cachedDownloadResults.get(key), flags );
        }
        out.writeString(this.activeDownloadUrl);
        out.writeString(this.activeLoaderId != null ? this.activeLoaderId.toString() : null);
        out.writeInt( this.nextLoaderId);
    }

    private void readFromParcel( Parcel in ){
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            this.cachedDownloadResults.put( in.readString(), in.<DownloadResult>readParcelable(null) );
        }
        this.activeDownloadUrl = in.readString();
        String loaderId = in.readString();
        if ( TextUtils.isEmpty( loaderId ) ){
            this.activeLoaderId = Integer.valueOf( loaderId );
        }
        this.nextLoaderId = in.readInt();
    }

    public void markActiveDownload(String activeDownloadUrl) {
        this.activeDownloadUrl = activeDownloadUrl;
        this.activeLoaderId = this.nextLoaderId++;
    }

    public void endActiveDownload( String url, DownloadResult downloadResult ) {
        this.activeDownloadUrl = null;
        this.activeLoaderId = null;
        this.cachedDownloadResults.put( url, downloadResult );
    }

    public DownloadResult cachedDownloadResult( String url ){
        return this.cachedDownloadResults.get( url );
    }

    public String getActiveDownloadUrl() {
        return this.activeDownloadUrl;
    }

    public Integer getActiveLoaderId() {
        return this.activeLoaderId;
    }

    public String toString(){
        return String.format( "DownloadState[ active loader id: '%s'; active download url: '%s'; cached downloads: '%s']",
                              this.activeLoaderId, this.activeDownloadUrl, this.cachedDownloadResults );
    }
}
