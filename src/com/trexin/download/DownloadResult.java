package com.trexin.download;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class DownloadResult implements Parcelable {
    private String targetUrl;
    private Uri localFile;
    private String mimeType;
    private String errorMessage;
    private Integer httpErrorCode;

    public static final int HTTP_CODE_FORBIDDEN = 403;
    public static final int HTTP_CODE_INTERNAL_SERVER_ERROR = 500;

    public static final Parcelable.Creator<DownloadResult> CREATOR = new Parcelable.Creator<DownloadResult>() {
        public DownloadResult createFromParcel(Parcel in) {
            return new DownloadResult(in);
        }
        public DownloadResult[] newArray(int size) {
            return new DownloadResult[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString( this.targetUrl );
        out.writeString( this.localFile != null ? this.localFile.toString() : null );
        out.writeString( this.mimeType );
        out.writeString( this.errorMessage );
        out.writeString( this.httpErrorCode != null ? this.httpErrorCode.toString() : null  );
    }

    public DownloadResult( Parcel in ) {
        this.targetUrl = in.readString();
        String fileName = in.readString();
        if ( !TextUtils.isEmpty( fileName )){
            this.localFile = Uri.parse( fileName );
        }
        this.mimeType = in.readString();
        this.errorMessage = in.readString();
        String errorCode = in.readString();
        if ( !TextUtils.isEmpty( errorCode )){
            this.httpErrorCode = Integer.valueOf( errorCode );
        }
    }

    public DownloadResult(String targetUrl, Uri localFile, String mimeType) {
        this.targetUrl = targetUrl;
        this.localFile = localFile;
        this.mimeType = mimeType;
    }

    public DownloadResult(String targetUrl, int httpErrorCode, String errorMessage) {
        this.targetUrl = targetUrl;
        this.httpErrorCode = httpErrorCode;
        this.errorMessage = errorMessage;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public Uri getLocalFile() {
        return this.localFile;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public boolean isSuccess(){
        return this.errorMessage == null;
    }

    public boolean isLoginRequired(){
        return this.httpErrorCode != null &&
               ( this.httpErrorCode == HTTP_CODE_FORBIDDEN || this.httpErrorCode == HTTP_CODE_INTERNAL_SERVER_ERROR );
    }

    @Override
    public String toString(){
        if ( this.isSuccess() ){
            return String.format( "DownloadResult [success; url: '%s'; local file:'%s'; mime: '%s']",
                                  this.targetUrl, this.localFile, this.mimeType );
        } else {
            return String.format( "DownloadResult [failure; url: '%s'; http error code: '%s'; message:'%s']",
                                  this.targetUrl, this.httpErrorCode, this.errorMessage );
        }
    }
}
