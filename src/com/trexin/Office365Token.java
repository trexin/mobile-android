package com.trexin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class Office365Token {
    private static enum TokenKey {
        rtFa,
        FedAuth
    }

    public static class TokenNotValidException extends Exception {
        private List<TokenKey> missingTokens;

        public TokenNotValidException(List<TokenKey> missingTokens) {
            this.missingTokens = missingTokens;
        }

        @Override
        public String getMessage() {
            return String.format( "Office 365 token is invalid. Missing tokens: [%s]",
                                  TextUtils.join( ",", this.missingTokens ));
        }
    }

    public static String asCookie( Context context ) throws TokenNotValidException {
        // this throws if the map does not represent a proper token
        EnumMap<TokenKey, String> tokenMap = new EnumMap<TokenKey, String>( TokenKey.class );
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        for ( TokenKey token : TokenKey.values() ) {
            tokenMap.put( token, settings.getString( token.name(), null ));
        }
        // validate the token; throw a TokenNotValidException if the token is not valid
        validateToken( tokenMap );
        return String.format( "%1$s=%2$s; %3$s=%4$s", TokenKey.rtFa.name(), tokenMap.get( TokenKey.rtFa ),
                                                      TokenKey.FedAuth.name(), tokenMap.get( TokenKey.FedAuth ) );
    }

    public static void clearToken( Context context ){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        for ( TokenKey token : TokenKey.values() ) {
            editor.remove( token.name() );
        }
        editor.commit();
        Log.i( Constants.LOG_TAG, "Office 365 token is removed from Shared Preferences.");
    }

    public static void saveToken( String cookies, Context context ) throws TokenNotValidException {
        // parse the cookies to extract the token
        EnumMap<TokenKey, String> cookieMap = new EnumMap<TokenKey, String>( TokenKey.class );
        if ( cookies != null ) {
            // Cookie is a string like NAME=VALUE [; NAME=VALUE]
            String[] pairs = cookies.split(";");
            for (String pair : pairs) {
                String[] parts = pair.split( "=", 2 );
                if ( parts.length == 2 ){
                    try {
                        cookieMap.put( TokenKey.valueOf( parts[0].trim() ), parts[1].trim() );
                    } catch (IllegalArgumentException ignore){}
                }
            }
        }
        // validate the token; throw a TokenNotValidException if the token is not valid
        validateToken( cookieMap );
        // persist the token into the Shared Preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        for ( TokenKey tokenKey : cookieMap.keySet() ) {
            editor.putString( tokenKey.name(), cookieMap.get( tokenKey ));
        }
        // commit the edits
        editor.commit();
        Log.i( Constants.LOG_TAG, "Office 365 token is written to Shared Preferences.");
    }

    private static void validateToken( EnumMap<TokenKey, String> tokenMap ) throws TokenNotValidException {
        List<TokenKey> missingTokens = new ArrayList<TokenKey>();
        for ( TokenKey token : TokenKey.values() ) {
            if ( tokenMap.get( token )  == null ){
                missingTokens.add( token );
            }
        }
        if ( !missingTokens.isEmpty() ){
            throw new TokenNotValidException( missingTokens );
        }
    }

}
