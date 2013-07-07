package com.ifrins.hipstacast;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class Hipstacast extends Application {
	public static final String WELCOME_PREFERENCES = "WELCOME";
	public static final String FULL_SHOW_PREFERENCES = "FULLSHOWPREFERENCES";
	public static final String TASK_ADD_PROVIDER = "AddPodcastProvider";
	public static final String TASK_PLAYBACK_COMPLETED = "PlaybackCompleted";
	public static final String TASK_OPEN_WEBPAGE = "OpenWebpageFromEpisode";
	public static final String TASK_OPEN_DONATIONS = "OpenDonationsFromEpisode";
	public static final String TASK_SHARE ="ShareFromEpisode";
	public static final String TASK_UPGRADE = "Upgrade";

	public Boolean shouldDisplayWelcomeActivity = null;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onTerminate() {
	}

	public final Boolean shouldDisplayWelcomeActivity() {
		if (shouldDisplayWelcomeActivity != null) {
			return shouldDisplayWelcomeActivity;
		} else {
			SharedPreferences pref = getSharedPreferences(WELCOME_PREFERENCES, 0);
			shouldDisplayWelcomeActivity = pref.getBoolean("shown_"+getString(R.string.version_number), true);
			return false;
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
