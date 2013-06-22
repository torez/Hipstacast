package com.ifrins.hipstacast.utils;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.OkHttpClient;
import org.apache.http.client.utils.URIUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class HipstacastLogging {
	public static final void log(String e) {
		Log.d("HIPSTACAST", e);
	}
	
	public static final void log(String e, int n) {
		Log.d("HIPSTACAST", String.format("%s: %d", e, n));
	}

	public static final void reportFeedError(String feedUrl) {
		OkHttpClient client = new OkHttpClient();
		HttpURLConnection conn;

		try {
			conn = client.open(new URL("http://hipstacast.ifrins.cat/l.gif?t=feed&f=" + URLEncoder.encode(feedUrl, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Crashlytics.logException(e);
			return;
		} catch (MalformedURLException e) {
			Crashlytics.logException(e);
			return;
		}
		try {
			InputStream i = conn.getInputStream();
			i.close();
		} catch (IOException e) {
			Crashlytics.logException(e);
			return;
		}

	}

}
