/**
 * Adapted from code from foursquare.
 * 
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.http;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import com.geozen.smarttrail.error.SmartTrailException;
import com.geozen.smarttrail.error.CredentialsException;

public class HttpApiWithBasicAuth extends AbstractHttpApi {

    private HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {

        @Override
        public void process(final HttpRequest request, final HttpContext context)
                throws HttpException, IOException {

            AuthState authState = (AuthState)context.getAttribute(ClientContext.TARGET_AUTH_STATE);
            CredentialsProvider credsProvider = (CredentialsProvider)context
                    .getAttribute(ClientContext.CREDS_PROVIDER);

            // If  auth scheme has not been initialized yet
            if (authState.getAuthScheme() == null) {
            	//we don't specify host and port to make it easier to debug. We really don't care.
                AuthScope authScope = new AuthScope(null,-1 );

                // Obtain credentials matching the target host
                org.apache.http.auth.Credentials creds = credsProvider.getCredentials(authScope);
                
                // If found, generate BasicScheme preemptively
                if (creds != null) {
                    authState.setAuthScheme(new BasicScheme());
                    authState.setCredentials(creds);
                }
            }
        }

    };

    public HttpApiWithBasicAuth(DefaultHttpClient httpClient, String clientVersion) {
        super(httpClient, clientVersion);
        httpClient.addRequestInterceptor(preemptiveAuth, 0);
    }

    public String doHttpRequest(HttpRequestBase httpRequest) throws CredentialsException,
             SmartTrailException, IOException {
        return executeRequest(httpRequest);
    }
}
