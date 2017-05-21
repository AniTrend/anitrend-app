package com.mxt.anitrend.api.core;

import java.io.Serializable;

/**
 * Created by Maxwell on 10/2/2016.
 *
 * POST: auth/access_token
 * Url Parms:
 * grant_type    : "client_credentials"
 * client_id     :  Client id
 * client_secret :  Client secret
 * You can now access the majority of the resource server’s GET end points by including this access token as a “access_token” header or url parameter.
 * For security this access token will expire in 1 hour, to receive a new one simply repeat this step.
*/
public class Token implements Serializable{

    private String access_token;
    private String token_type;
    private long expires_in;
    private long expires;
    private String refresh_token;

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public long getExpires() {
        return expires;
    }

    public String getHeaderValuePresets(){
        return String.format("%s %s",token_type,access_token);
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public Token(String access_token, String token_type, long expires_in, long expires, String refresh_token) {
        this.access_token = access_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
        this.expires = expires;
        this.refresh_token = refresh_token;
    }
}
