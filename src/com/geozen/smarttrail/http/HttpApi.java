/**
 * Adapted from code from foursquare.
 *  
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.http;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import com.geozen.smarttrail.error.SmartTrailException;
import com.geozen.smarttrail.error.CredentialsException;

import java.io.IOException;


public interface HttpApi {

	abstract public String doHttpRequest(HttpRequestBase httpRequest)
			throws CredentialsException, IOException, SmartTrailException;

	abstract public String doHttpPost(String url,
			NameValuePair... nameValuePairs) throws CredentialsException,
			IOException, SmartTrailException;

	abstract public HttpGet createHttpGet(String url,
			NameValuePair... nameValuePairs);
	
	abstract public HttpDelete createHttpDelete(String url,
			NameValuePair... nameValuePairs);

	abstract public HttpPost createHttpPost(String url,
			NameValuePair... nameValuePairs);
	
	abstract public HttpPost createHttpJsonPost(String url,
			String data);
}
