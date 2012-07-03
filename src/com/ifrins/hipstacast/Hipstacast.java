package com.ifrins.hipstacast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Application;

public class Hipstacast extends Application {
	public GoogleAnalyticsTracker tracker;
	
	@Override
	public void onCreate() {
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-33122624-1", 30, this);
	}
	@Override
	public void onTerminate() {
		tracker.stopSession();
	}
	
	public void trackPageView(String page) {
		tracker.trackPageView(page);
	}
	
}
