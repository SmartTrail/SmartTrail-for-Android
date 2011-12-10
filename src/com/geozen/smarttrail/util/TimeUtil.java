/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author matt@geozen.com
 *
 */


public class TimeUtil {
	
	
	public static final long SEC_MS = 1000L;
	public static final long MIN_MS = 60L* SEC_MS;
	public static final long HOUR_MS = 60L* MIN_MS;
	public static final long DAY_MS = 24L*HOUR_MS;


	public static String ago(long delta) {
		boolean inFuture = false;
		StringBuilder sb = new StringBuilder();

		if (delta < 0) {
			delta *= -1L;
			inFuture = true;
			//hack for bma app as server time could be off from app.
			return "now";
		}
		if (delta > (1000L * 60L * 60L * 24L * 10000L)) {
			return "never";
		}
		long seconds = delta / 1000L;

		if (seconds < 60L)
			sb.append(seconds).append(" seconds ");
		else {
			long minutes = delta / (1000L * 60L);

			if (minutes < 60L)
				if (minutes == 1)
					sb.append(minutes).append(" minute ");
				else
					sb.append(minutes).append(" minutes ");
			else {
				long hours = delta / (1000L * 60L * 60L);
				if (hours < 24L)
					if (hours == 1L) {
						sb.append(hours).append(" hour ");
					} else {
						sb.append(hours).append(" hours ");
					}
				else {
					long days = delta / (1000L * 60L * 60L * 24L);

					if (days == 1L) {
						sb.append(days).append(" day ");
					} else {
						sb.append(days).append(" days ");
					}

				}

			}
		}
		if (inFuture) {
			sb.append("from now");
		} else {
			sb.append("ago");
		}
		return sb.toString();
	}


	public static String eventFormat(long start, long end) {
		Date startDate = new Date(start);
		Date endDate = new Date(end);
		SimpleDateFormat formatter1 = new SimpleDateFormat("EEEE, MMMM d");
		formatter1.format(startDate);
		SimpleDateFormat formatter2 = new SimpleDateFormat("h:mma");
		return formatter2.format(startDate) + "-"+ formatter2.format(endDate) + " "+ formatter1.format(startDate);
	}
}
