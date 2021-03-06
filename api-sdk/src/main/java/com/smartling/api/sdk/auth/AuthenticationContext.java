package com.smartling.api.sdk.auth;

import com.smartling.web.api.v2.ResponseData;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpMessage;

public class AuthenticationContext implements ResponseData
{
    public static final int TIME_TO_REFRESH = 1500;

    private String accessToken;
    private long parsingTime = System.currentTimeMillis();
    private long expiresIn;
    private long refreshExpiresIn;
    private String refreshToken;
    private String tokenType;
    private String sessionState;

    public String getAccessToken()
    {
        return accessToken;
    }

    public void setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
    }

    public long getExpiresIn()
    {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn)
    {
        this.expiresIn = expiresIn;
    }

    public long getRefreshExpiresIn()
    {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(long refreshExpiresIn)
    {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public String getRefreshToken()
    {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken)
    {
        this.refreshToken = refreshToken;
    }

    public String getTokenType()
    {
        return tokenType;
    }

    public void setTokenType(String tokenType)
    {
        this.tokenType = tokenType;
    }

    public String getSessionState()
    {
        return sessionState;
    }

    public void setSessionState(String sessionState)
    {
        this.sessionState = sessionState;
    }

    public String getAuthorizationTokenString()
    {
        return String.format("%s %s", getTokenType(), getAccessToken());
    }

    public void applyTo(final HttpMessage httpMessage)
    {
        httpMessage.addHeader(HttpHeaders.AUTHORIZATION, getAuthorizationTokenString());
    }

    public long getParsingTime()
    {
        return parsingTime;
    }

    public void setParsingTime(final long parsingTime)
    {
        this.parsingTime = parsingTime;
    }

    public long calculateAccessTokenExpireTime()
    {
        return parsingTime + expiresIn * 1000 - TIME_TO_REFRESH;
    }

    public long calculateRefreshTokenExpireTime()
    {
        return parsingTime + refreshExpiresIn * 1000 - TIME_TO_REFRESH;
    }
}
