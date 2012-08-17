package com.ifrins.hipstacast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.StrictMode;

public class Hipstacast extends Application {
	public static final String WELCOME_PREFERENCES = "WELCOME";
	public static final String FULL_SHOW_PREFERENCES = "FULLSHOWPREFERENCES";
	public static final String TASK_ADD_PROVIDER = "AddPodcastProvider";
	public static final String TASK_PLAYBACK_COMPLETED = "PlaybackCompleted";
	public static final String TASK_OPEN_WEBPAGE = "OpenWebpageFromEpisode";
	public static final String TASK_OPEN_DONATIONS = "OpenDonationsFromEpisode";
	public static final String TASK_SHARE ="ShareFromEpisode";
	public static final String TASK_UPGRADE = "Upgrade";
	
	public static final Uri SUBSCRIPTIONS_PROVIDER_URI = Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts");
	public static final Uri EPISODES_PROVIDER_URI = Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes");

	public GoogleAnalyticsTracker tracker;
	public Boolean shouldDisplayWelcomeActivity = null;

	@Override
	public void onCreate() {
	    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .build());

		super.onCreate();
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-33122624-1", 30, this);
		tracker.setCustomVar(1, "app_version", getString(R.string.version_number), 1);
		tracker.setCustomVar(1, "device", android.os.Build.MODEL, 1);
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
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		editor.putInt("latest-version", pInfo.versionCode);
		editor.commit();
		shouldDisplayWelcomeActivity = false;
	}
}
