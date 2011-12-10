/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import android.net.Uri;

import com.geozen.smarttrail.error.SmartTrailException;
import com.geozen.smarttrail.error.CredentialsException;
import com.geozen.smarttrail.model.Condition;
import com.geozen.smarttrail.model.Review;
import com.geozen.smarttrail.model.User;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.Util;

class SmartTrailApiV1 {

	// Uri path construction constants.
	public static final String PATH_API = "api";
	public static final String PATH_V1 = "v1";
	private static final String PATH_SIGNIN = "signin";
	private static final String PATH_SIGNUP = "signup";
	private static final String PATH_REGIONS = "regions";
	private static final String PATH_AREAS = "areas";
	private static final String PATH_TRAILS = "trails";
	private static final String PATH_CONDITIONS = "conditions";
	private static final String PATH_REVIEWS = "reviews";
	private static final String PATH_ADD = "add";
	private static final String PATH_STATUS = "status";
	private static final String PATH_STATUSES = "statuses";
	private static final String PATH_EVENTS = "events";
	private static final String PATH_USERS = "users";
	private static final String PATH_INFO = "info";
	private static final String PATH_MAPS = "maps";

	public static final String META = "meta";
	public static final String ERROR_TYPE = "errorType";
	public static final String ERROR_DETAIL = "errorDetail";
	public static final String RESPONSE = "response";

	// HTTP response constants
	public static final String HTTP_CODE = "code";
	public static final int HTTP_CODE_SUCCESS = 200;
	public static final int HTTP_CODE_BAD_REQUEST = 400;
	public static final int HTTP_CODE_UNAUTHORIZED = 401;
	public static final int HTTP_CODE_FORBIDDEN = 403;
	public static final int HTTP_CODE_NOT_FOUND = 404;
	public static final int HTTP_CODE_METHOD_NOT_ALLOWED = 405;
	public static final int HTTP_CODE_INTERNALSERVER_ERROR = 500;

	// Versioning constants
	private static final String VERSION = "1"; // API version
	private static final String API_VERSION_DATE = "20110509"; // Client API
																// version date
	//API parameters
	public static final String AFTER_TIMESTAMP = "afterTimestamp";
	public static final String LIMIT = "limit";
	public static final String API_VERSION = "v";

	// Response field constants.
	public static final String CONDITION = "condition";
	public static final String CONDITIONS = "conditions";
	public static final String REGIONS = "regions";
	public static final String AREA = "area";
	public static final String AREAS = "areas";
	public static final String TRAIL = "trail";
	public static final String TRAILS = "trails";
	public static final String STATUS = "status";
	public static final String STATUSES = "statuses";
	public static final String EVENTS = "events";
	public static final String REVIEWS = "reviews";
	public static final String INFO = "info";
	public static final String PATROLLER = "isPatroller";

	
	private final DefaultHttpClient mHttpClient = AbstractHttpApi
			.createHttpClient();
	private HttpApi mHttpApi;

	private final String mApiBaseUrl;
	private final AuthScope mAuthScope;

	public SmartTrailApiV1(String apiDomain, String clientVersion,
			boolean useOAuth, boolean ssl) {

		if (ssl) {
			mApiBaseUrl = "https://" + apiDomain;
			mAuthScope = new AuthScope(apiDomain, AbstractHttpApi.SSL_PORT);
		} else {
			mApiBaseUrl = "http://" + apiDomain;
			mAuthScope = new AuthScope(apiDomain, AbstractHttpApi.HTTP_PORT);
		}

		mHttpApi = new HttpApiWithBasicAuth(mHttpClient, clientVersion);
	}

	/**
	 * 
	 * @param username
	 * @param password
	 */
	void setCredentials(String username, String password) {
		if (username == null || username.length() == 0 || password == null
				|| password.length() == 0) {
			AppLog.d("Clearing Credentials");
			mHttpClient.getCredentialsProvider().clear();
		} else {
			AppLog.d("Setting username/password: " + username + "/******");
			mHttpClient.getCredentialsProvider().setCredentials(mAuthScope,
					new UsernamePasswordCredentials(username, password));
		}
	}

	/**
	 * 
	 */
	public void clearCredentials() {
		mHttpClient.getCredentialsProvider().clear();
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasCredentials() {
		return mHttpClient.getCredentialsProvider().getCredentials(mAuthScope) != null;
	}

	/**
	 * Get the version of the API this client adheres to.
	 * 
	 * @return Returns the API version number as a String. E.g. "1"
	 */
	public String getVersion() {
		return VERSION;
	}

	/**
	 * Signup adds a new user to the smarttrail system. Since username and
	 * password info are being passed as a POST, the connection must be over
	 * SSL.
	 * 
	 * @param username
	 *            The publicly visible handle name of the user. Must be unique.
	 * @param email
	 *            The email of the user. Must be unique.
	 * @param password
	 *            The password of the user. 6+ chars
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 */
	String signup(String username, String email, String password)
			throws CredentialsException, IOException, SmartTrailException {

		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_SIGNUP).build();
		String url = uri.toString();

		HttpPost httpPost = mHttpApi.createHttpPost(url //
				, new BasicNameValuePair(User.USERNAME, username) //
				, new BasicNameValuePair(User.EMAIL, email) //
				, new BasicNameValuePair(User.PASSWORD, password) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);

		String json = mHttpApi.doHttpRequest(httpPost);
		return json;
	}

	/**
	 * Signin logs the user in.
	 * 
	 * @return
	 * @throws SmartTrailException
	 * @throws CredentialsException
	 * @throws IOException
	 */
	String signin() throws SmartTrailException, CredentialsException,
			IOException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_SIGNIN).build();
		String url = uri.toString();
		HttpGet httpGet = mHttpApi.createHttpGet(url //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);

		String json = mHttpApi.doHttpRequest(httpGet);

		return json;

	}

	/**
	 * 
	 * @param type
	 * @param trailId
	 * @param status
	 * @param userType
	 * @param comment
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 */
	public String pushCondition(int type, String trailId, String status,
			String userType, String comment) throws CredentialsException,
			IOException, SmartTrailException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_TRAILS)
				.appendPath(trailId).appendPath(PATH_CONDITIONS)
				.appendPath(PATH_ADD).build();

		String url = uri.toString();
		HttpPost httpPost = mHttpApi.createHttpPost(url,
				new BasicNameValuePair(Condition.TYPE, Integer.toString(type)) //
				, new BasicNameValuePair(Condition.STATUS, status) //
				, new BasicNameValuePair(Condition.USER_TYPE, userType) //
				, new BasicNameValuePair(Condition.COMMENT, comment) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String response = mHttpApi.doHttpRequest(httpPost);
		return response;
	}

	/**
	 * 
	 * @param trailId
	 * @param afterTimestamp
	 * @param limit
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	public String pullConditionsByTrail(String trailId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_TRAILS)
				.appendPath(trailId).appendPath(PATH_CONDITIONS).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(
				url //
				,
				new BasicNameValuePair(AFTER_TIMESTAMP, String
						.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(LIMIT, String.valueOf(limit)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param regionId
	 * @param afterTimestamp
	 * @param limit
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	public String pullConditionsByRegion(String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_REGIONS)
				.appendPath(regionId).appendPath(PATH_CONDITIONS).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(url, new BasicNameValuePair(
				AFTER_TIMESTAMP, String.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(LIMIT, String.valueOf(limit)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param regionId
	 * @param afterTimestamp
	 * @param limit
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 */
	public String pullEventsByRegion(String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_REGIONS)
				.appendPath(regionId).appendPath(PATH_EVENTS).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(url,

		new BasicNameValuePair(AFTER_TIMESTAMP, String.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(LIMIT, String.valueOf(limit)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param regionId
	 * @param afterTimestamp
	 * @param limit
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 */
	public String pullAreasByRegion(String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_REGIONS)
				.appendPath(regionId).appendPath(PATH_AREAS).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(url, new BasicNameValuePair(
				AFTER_TIMESTAMP, String.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(LIMIT, String.valueOf(limit)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param areaId
	 * @param afterTimestamp
	 * @param limit
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 */
	public String pullTrailsByArea(String areaId, long afterTimestamp, int limit)
			throws CredentialsException, IOException, SmartTrailException {

		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_AREAS).appendPath(areaId)
				.appendPath(PATH_TRAILS).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(url, new BasicNameValuePair(
				AFTER_TIMESTAMP, String.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(LIMIT, String.valueOf(limit)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param regionId
	 * @param afterTimestamp
	 * @param limit
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 */
	public String pullTrailsByRegion(String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException {

		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_REGIONS)
				.appendPath(regionId).appendPath(PATH_TRAILS).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(url, new BasicNameValuePair(
				AFTER_TIMESTAMP, String.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(LIMIT, String.valueOf(limit)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param regionId
	 * @param afterTimestamp
	 * @param limit
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 */
	public String pullStatusesByRegion(String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException {

		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_REGIONS)
				.appendPath(regionId).appendPath(PATH_STATUSES).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(url, new BasicNameValuePair(
				AFTER_TIMESTAMP, String.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(LIMIT, String.valueOf(limit)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	public String pullStatus(String trailId, long afterTimestamp)
			throws CredentialsException, IOException, SmartTrailException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_TRAILS)
				.appendPath(trailId).appendPath(PATH_STATUS).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(url, new BasicNameValuePair(
				AFTER_TIMESTAMP, String.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param afterTimestamp
	 * @param limit
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	public String pullRegions(long afterTimestamp, int limit)
			throws CredentialsException, IOException, SmartTrailException,
			JSONException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_REGIONS).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(
				url //
				,
				new BasicNameValuePair(AFTER_TIMESTAMP, String
						.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(LIMIT, String.valueOf(limit)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param areaId
	 * @param afterTimestamp
	 * @param limit
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	public String pullReviewsByArea(String areaId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_AREAS).appendPath(areaId)
				.appendPath(PATH_REVIEWS).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(
				url //
				,
				new BasicNameValuePair(AFTER_TIMESTAMP, String
						.valueOf(afterTimestamp)) //
				, new BasicNameValuePair(LIMIT, String.valueOf(limit)) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param areaId
	 * @param rating
	 * @param review
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 */
	public String pushReview(String areaId, float rating, String review)
			throws CredentialsException, IOException, SmartTrailException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_AREAS).appendPath(areaId)
				.appendPath(PATH_REVIEWS).appendPath(PATH_ADD).build();

		String url = uri.toString();
		HttpPost httpPost = mHttpApi.createHttpPost(url //
				, new BasicNameValuePair(Review.RATING, Float.toString(rating)) //
				, new BasicNameValuePair(Review.REVIEW, review) //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String response = mHttpApi.doHttpRequest(httpPost);
		return response;
	}

	/**
	 * 
	 * @return
	 */
	public Uri getApiUri() {
		return Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).build();

	}

	/**
	 * 
	 * @return
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 */
	public String getUserInfo() throws CredentialsException, IOException,
			SmartTrailException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_USERS)
				.appendPath(PATH_INFO).build();
		String url = uri.toString();

		HttpGet httpGet = mHttpApi.createHttpGet(url //
				, new BasicNameValuePair(API_VERSION, API_VERSION_DATE) //
				);
		String json = mHttpApi.doHttpRequest(httpGet);

		return json;
	}

	/**
	 * 
	 * @param mapId
	 * @param mapFile
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public File pullMap(String mapId, File mapFile)
			throws MalformedURLException, IOException {
		Uri uri = Uri.parse(mApiBaseUrl).buildUpon().appendPath(PATH_API)
				.appendPath(PATH_V1).appendPath(PATH_MAPS).appendPath(mapId)
				.build();
		String url = uri.toString();

		InputStream is = new URL(url).openStream();
		OutputStream os = new FileOutputStream(mapFile);
		Util.CopyStream(is, os);
		os.close();

		return mapFile;
	}

}
