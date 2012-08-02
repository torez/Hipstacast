package com.ifrins.hipstacast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

public class Hipstacast extends Application {
	public static final String WELCOME_PREFERENCES = "WELCOME";
	public static final String TASK_ADD_PROVIDER = "AddPodcastProvider";
	public static final Uri SUBSCRIPTIONS_PROVIDER_URI = Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts");
	
	public GoogleAnalyticsTracker tracker;
	public Boolean shouldDisplayWelcomeActivity = null;

	@Override
	public void onCreate() {
		super.onCreate();
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-33122624-1", 30, this);
		tracker.setCustomVar(1, "app_version", getString(R.string.version_number), 1);
	}

	@Override
	public void onTerminate() {
		tracker.stopSession();
	}

	public void trackPageView(String page) {
		tracker.trackPageView(page);
	}
	public void trackEvent(String event_name, String event_cat, String event_action, int event_value) {
		tracker.trackEvent(event_cat, event_action, event_name, event_value);
	}
	public final Boolean shouldDisplayWelcomeActivity() {
		if (shouldDisplayWelcomeActivity != null) {
			return shouldDisplayWelcomeActivity;
		} else {
			SharedPreferences pref = getSharedPreferences(WELCOME_PREFERENCES, 0);
			shouldDisplayWelcomeActivity = pref.getBoolean("shown_"+getString(R.string.version_number), true);
			return shouldDisplayWelcomeActivity;
		}
	}
	public final void setWelcomeActivityShown() {
		SharedPreferences pref = getSharedPreferences(WELCOME_PREFERENCES, 0);
		Editor editor = pref.edit();
		editor.putBoolean("shown_"+getString(R.string.version_number), false);
		editor.commit();
		shouldDisplayWelcomeActivity = false;
	}
}
