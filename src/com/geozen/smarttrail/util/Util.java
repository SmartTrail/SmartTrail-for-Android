/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.util;

import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {

	/**
	 * Check if we have network connectivity.
	 * 
	 * @return
	 */
	static public boolean isConnectedToNetwork(Context context) {

		boolean connected = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

			if (netInfo != null) {
				connected = netInfo.isConnected();
			}
		}
		return connected;
	}
	
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
}
