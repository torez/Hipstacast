package com.ifrins.hipstacast.utils;

import android.util.Log;

public class HipstacastLogging {
	public static final void log(String e) {
		Log.d("HIPSTACAST", e);
	}
	
	public static final void log(String e, int n) {
		Log.d("HIPSTACAST", String.format("%s: %d", e, n));
	}
}
