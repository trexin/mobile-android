package com.trexin.download;

import android.net.Uri;

public class FileDownload {
    private String targetUrl;
    private Uri localFile;
    private String mimeType;
    private String errorMessage;
    private Integer httpErrorCode;

    public static final int HTTP_CODE_FORBIDDEN = 403;

    public FileDownload( String targetUrl, Uri localFile, String mimeType) {
        this.targetUrl = targetUrl;
        this.localFile = localFile;
        this.mimeType = mimeType;
    }

    public FileDownload( String targetUrl, int httpErrorCode, String errorMessage ) {
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
        return this.httpErrorCode == HTTP_CODE_FORBIDDEN;
    }

    @Override
    public String toString(){
        if ( this.isSuccess() ){
            return String.format( "FileDownload [success; url: '%s'; local file:'%s'; mime: '%s']",
                                  this.targetUrl, this.localFile, this.mimeType );
        } else {
            return String.format( "FileDownload [failure; url: '%s'; http error code: '%s'; message:'%s']",
                                  this.targetUrl, this.httpErrorCode, this.errorMessage );
        }
    }
}
