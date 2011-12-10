/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.error;


public class SmartTrailException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private String mExtra;

    public SmartTrailException(String message) {
        super(message);
    }

    public SmartTrailException(String message, String extra) {
        super(message);
        mExtra = extra;
    }
    
    public String getExtra() {
        return mExtra;
    }
}
