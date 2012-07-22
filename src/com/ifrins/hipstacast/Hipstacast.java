package com.ifrins.hipstacast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;

public class Hipstacast extends Application {
	public GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate() {
		super.onCreate();
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-33122624-1", 30, this);
		tracker.setCustomVar(1, "Screen", String.valueOf(HUtils
				.getScreenCategory(getApplicationContext())));
		try {
			tracker.setProductVersion("Hipstacast", getPackageManager()
					.getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tracker.setCustomVar(1, "Device", android.os.Build.MODEL);
	}

	@Override
	public void onTerminate() {
		tracker.stopSession();
	}

	public void trackPageView(String page) {
		tracker.trackPageView(page);
	}

}
