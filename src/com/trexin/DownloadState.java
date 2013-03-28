package com.trexin;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadState implements Parcelable {
    private String downloadUrl;
    private Integer loaderId;

    public static final Parcelable.Creator<DownloadState> CREATOR = new Parcelable.Creator<DownloadState>() {
        public DownloadState createFromParcel(Parcel in) {
            return new DownloadState( in.readString(), in.readInt() );
        }

        public DownloadState[] newArray(int size) {
            return new DownloadState[size];
        }
    };

    public DownloadState(String downloadUrl, Integer loaderId) {
        this.downloadUrl = downloadUrl;
        this.loaderId = loaderId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.downloadUrl);
        out.writeInt( this.loaderId );
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public Integer getLoaderId() {
        return this.loaderId;
    }

    public String toString(){
        return String.format( "DownloadState[url: '%s'; loaderId: '%s']", this.downloadUrl, this.loaderId );
    }
}
