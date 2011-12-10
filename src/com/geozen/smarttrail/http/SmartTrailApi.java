/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.http;

import static com.geozen.smarttrail.http.SmartTrailApiV1.ERROR_DETAIL;
import static com.geozen.smarttrail.http.SmartTrailApiV1.HTTP_CODE;
import static com.geozen.smarttrail.http.SmartTrailApiV1.HTTP_CODE_SUCCESS;
import static com.geozen.smarttrail.http.SmartTrailApiV1.META;
import static com.geozen.smarttrail.http.SmartTrailApiV1.STATUS;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.geozen.smarttrail.error.CredentialsException;
import com.geozen.smarttrail.error.SmartTrailException;
import com.geozen.smarttrail.model.Condition;

/**
 * This is an abstraction layer to the server api and thus is public. Version
 * specific api classes are package only.
 * 
 * @author matt
 * 
 */
public class SmartTrailApi {
	public static final int SIGNIN_FAIL = -1;
	public static final int SIGNIN_SUCCESS = 0;
	public static final int SIGNIN_LEGAL_CONSENT_REQ = 1;

	/**
	 * Annotation marker to show which methods adhere to the SmartTrail API and
	 * the api version.
	 */
	@interface api {
		String version();
	}

	// version specific api layer.
	private SmartTrailApiV1 mApiV1;

	public SmartTrailApi(SmartTrailApiV1 httpApi) {
		mApiV1 = httpApi;
	}

	public void setCredentials(String email, String password) {
		mApiV1.setCredentials(email, password);
	}

	public void clearCredentials() {
		mApiV1.clearCredentials();
	}

	public boolean hasCredentials() {
		return mApiV1.hasCredentials();
	}

	public String getVersion() {
		return mApiV1.getVersion();
	}

	public static final SmartTrailApiV1 createHttpApi(String domain,
			String clientVersion, boolean useOAuth, boolean ssl) {
		return new SmartTrailApiV1(domain, clientVersion, useOAuth, ssl);
	}

	public Uri getApiUri() {
		return mApiV1.getApiUri();
	}

	/**
	 * Signup adds a new user to the smarttrail server/database. Since username
	 * and password info are being passed as a POST, the connection must be over
	 * SSL.
	 * 
	 * @param username
	 *            The publicly visible handle name of the user (i.e. nickname).
	 *            Must be unique.
	 * @param email
	 *            The email of the user. Must be unique.
	 * @param password
	 *            The password of the user. 6+ chars
	 * 
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public void signup(String username, String email, String password)
			throws CredentialsException, IOException, SmartTrailException,
			JSONException {
		String json = mApiV1.signup(username, email, password);
		JSONObject result = new JSONObject(json);
		JSONObject meta = result.getJSONObject(META);
		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}
	}

	/**
	 * Signs the user into the server and returns a signin status code. Used to
	 * verify the user has valid credentials and if a legal consent is
	 * requested.
	 * 
	 * @return Signin result code. (fail, success, or legal consent requested)
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws CredentialsException
	 * @throws JSONException
	 */
	@api(version = "1")
	public int signin() throws IOException, SmartTrailException,
			CredentialsException, JSONException {

		String json = mApiV1.signin();

		JSONObject result = new JSONObject(json);
		JSONObject meta = result.getJSONObject(META);
		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return SIGNIN_SUCCESS;
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}

	}

	/**
	 * Adds a trail condition to the server database.
	 * 
	 * @param condition
	 *            Trail condition.
	 * @return Returns the trail inserted into the database.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONObject pushCondition(Condition condition)
			throws CredentialsException, IOException, SmartTrailException,
			JSONException {

		String jsonResult = mApiV1.pushCondition(condition.mType,
				condition.mTrailId, condition.mStatus, condition.mUserType,
				condition.mComment);

		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE)
					.getJSONObject(SmartTrailApiV1.CONDITION);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}
	}

	/**
	 * Gets conditions for a given trail.
	 * 
	 * @param trailId
	 *            MongoId string of the trail.
	 * @param afterTimestamp
	 *            Only returns conditions updated after this timestamp.
	 *            afterTimestamp should be in milliseconds from epoch.
	 * @param limit
	 *            Number of conditions to return. -1 for no limit.
	 * @return Returns an array of conditions in JSON format.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONArray pullConditionsByTrail(String trailId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {

		String jsonResult = mApiV1.pullConditionsByTrail(trailId,
				afterTimestamp, limit);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE).getJSONArray(
					SmartTrailApiV1.CONDITIONS);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}

	}

	/**
	 * Gets conditions for a given region from the server.
	 * 
	 * @param regionId
	 *            MongoId string of the region.
	 * @param afterTimestamp
	 *            Only returns conditions updated after this timestamp.
	 *            afterTimestamp should be in milliseconds from epoch.
	 * @param limit
	 *            Number of conditions to return. -1 for no limit.
	 * @return Returns an array of conditions in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONArray pullConditionsByRegion(String regionId,
			long afterTimestamp, int limit) throws CredentialsException,
			IOException, SmartTrailException, JSONException {

		String jsonResult = mApiV1.pullConditionsByRegion(regionId,
				afterTimestamp, limit);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE).getJSONArray(
					SmartTrailApiV1.CONDITIONS);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}

	}

	/**
	 * Gets events for a given region from the server.
	 * 
	 * @param regionId
	 *            MongoId string of the region.
	 * @param afterTimestamp
	 *            Only returns events updated after this timestamp.
	 *            afterTimestamp should be in milliseconds from epoch.
	 * @param limit
	 *            Number of events to return. -1 for no limit.
	 * @return Returns an array of events in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONArray pullEventsByRegion(String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		String jsonResult = mApiV1.pullEventsByRegion(regionId, afterTimestamp,
				limit);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE).getJSONArray(
					SmartTrailApiV1.EVENTS);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}
	}

	/**
	 * Gets trail areas for a given region from the server.
	 * 
	 * @param regionId
	 *            MongoId string of the region.
	 * @param afterTimestamp
	 *            Only returns areas after this timestamp. afterTimestamp should
	 *            be in milliseconds from epoch.
	 * @param limit
	 *            Number of areas to return. -1 for no limit.
	 * @return Returns an array of trail areas in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONArray pullAreasByRegion(String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		String jsonResult = mApiV1.pullAreasByRegion(regionId, afterTimestamp,
				limit);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE).getJSONArray(
					SmartTrailApiV1.AREAS);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}
	}

	/**
	 * Gets trails for a given area from the server.
	 * 
	 * @param areaId
	 *            MongoId string of the area.
	 * @param afterTimestamp
	 *            Only returns trails after this timestamp. afterTimestamp
	 *            should be in milliseconds from epoch.
	 * @param limit
	 *            Number of trails to return. -1 for no limit.
	 * @return Returns an array of trails in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONArray pullTrailsByArea(String areaId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		String jsonResult = mApiV1.pullTrailsByArea(areaId, afterTimestamp,
				limit);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE).getJSONArray(
					SmartTrailApiV1.TRAILS);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}
	}

	/**
	 * Gets trails for a given region from the server.
	 * 
	 * @param regionId
	 *            MongoId string of the region.
	 * @param afterTimestamp
	 *            Only returns trails after this timestamp. afterTimestamp
	 *            should be in milliseconds from epoch.
	 * @param limit
	 *            Number of trails to return. -1 for no limit.
	 * @return Returns an array of trails in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONArray pullTrailsByRegion(String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		String jsonResult = mApiV1.pullTrailsByRegion(regionId, afterTimestamp,
				limit);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE).getJSONArray(
					SmartTrailApiV1.TRAILS);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}
	}

	/**
	 * Gets trail statuses for a given region from the server.
	 * 
	 * @param regionId
	 *            MongoId string of the region.
	 * @param afterTimestamp
	 *            Returns trail statuses after this timestamp. afterTimestamp
	 *            should be in milliseconds from epoch.
	 * @param limit
	 *            Number of statuses to return. -1 for no limit.
	 * @return Only returns an array of statuses in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONArray pullStatusesByRegion(String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		String jsonResult = mApiV1.pullStatusesByRegion(regionId,
				afterTimestamp, limit);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE).getJSONArray(
					SmartTrailApiV1.STATUSES);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}
	}

	/**
	 * Gets trail status for a given trail from the server.
	 * 
	 * @param trailId
	 *            MongoId string of the trail.
	 * @param afterTimestamp
	 *            Only returns a trail status updated after this timestamp.
	 *            afterTimestamp should be in milliseconds from epoch.
	 * 
	 * @return Returns an trail status in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONObject pullStatusByTrail(String trailId, long afterTimestamp)
			throws CredentialsException, IOException, SmartTrailException,
			JSONException {
		String jsonResult = mApiV1.pullStatus(trailId, afterTimestamp);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {

			JSONObject response = result
					.getJSONObject(SmartTrailApiV1.RESPONSE);

			if (response.isNull(STATUS)) {
				return null;
			} else {
				return response.getJSONObject(STATUS);
			}

		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}
	}

	/**
	 * Gets regions from the server.
	 * 
	 * @param afterTimestamp
	 *            Only returns regions updated after this timestamp.
	 *            afterTimestamp should be in milliseconds from epoch.
	 * @param limit
	 *            Number of regions to return. -1 for no limit.
	 * @return Returns an array of regions in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONArray pullRegions(long afterTimestamp, int limit)
			throws CredentialsException, IOException, SmartTrailException,
			JSONException {

		String jsonResult = mApiV1.pullRegions(afterTimestamp, limit);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE).getJSONArray(
					SmartTrailApiV1.REGIONS);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}

	}

	/**
	 * Gets trail area reviews for a given area from the server.
	 * 
	 * @param areaId
	 *            MongoId string of the area.
	 * @param afterTimestamp
	 *            Only returns area reviews updated after this timestamp.
	 *            afterTimestamp should be in milliseconds from epoch.
	 * @param limit
	 *            Number of reviews to return. -1 for no limit.
	 * @return Returns an array of statuses in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONArray pullReviewsByArea(String areaId, long afterTimestamp,
			int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {

		String jsonResult = mApiV1.pullReviewsByArea(areaId, afterTimestamp,
				limit);
		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result.getJSONObject(SmartTrailApiV1.RESPONSE).getJSONArray(
					SmartTrailApiV1.REVIEWS);
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}

	}

	/**
	 * Pushes a trail area review statuses to the server.
	 * 
	 * @param areaId
	 *            MongoId string of the area.
	 * @param rating
	 *            Review rating (1-5). Higher is better.
	 * @param comment
	 *            Review comment
	 * @return Returns an array of statuses in JSON format from the server.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONObject pushReview(String areaId, float rating, String comment)
			throws CredentialsException, IOException, SmartTrailException,
			JSONException {
		String jsonResult = mApiV1.pushReview(areaId, rating, comment);

		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {
			return result;
		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}

	}

	/**
	 * Gets user info from the server.
	 * 
	 * @return User info from the server in JSON format.
	 * @throws CredentialsException
	 * @throws IOException
	 * @throws SmartTrailException
	 * @throws JSONException
	 */
	@api(version = "1")
	public JSONObject getUserInfo() throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		String jsonResult = mApiV1.getUserInfo();

		JSONObject result = new JSONObject(jsonResult);
		JSONObject meta = result.getJSONObject(META);

		if (meta.getInt(HTTP_CODE) == HTTP_CODE_SUCCESS) {

			return result.getJSONObject(SmartTrailApiV1.RESPONSE)
					.getJSONObject(SmartTrailApiV1.INFO);

		} else {
			String errorDetail = "";
			if (meta.has(ERROR_DETAIL)) {
				errorDetail = meta.getString(ERROR_DETAIL);
			}
			throw new SmartTrailException(errorDetail);
		}
	}

	/**
	 * Gets a trail map in GPX format.
	 * 
	 * @param mapId
	 *            MongoId string of the area.
	 * @param mapFile
	 *            File to save downloaded map to.
	 * @return trail map in GPX format.
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@api(version = "1")
	public File pullMap(String mapId, File mapFile)
			throws MalformedURLException, IOException {
		return mApiV1.pullMap(mapId, mapFile);

	}
}
