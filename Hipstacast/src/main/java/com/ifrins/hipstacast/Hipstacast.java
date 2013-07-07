package com.ifrins.hipstacast;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class Hipstacast extends Application {
	public static final String MIGRATION2_0_DONE = "is_migration_2_done";
	public static final String WELCOME_PREFERENCES = "WELCOME";
	public static final String TASK_UPGRADE = "Upgrade";

	private Boolean shouldDisplayWelcomeActivity = null;

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
			shouldDisplayWelcomeActivity = pref.getBoolean(MIGRATION2_0_DONE,  true);
			return shouldDisplayWelcomeActivity;
		}
	}
	public final void setWelcomeActivityShown() {
		SharedPreferences pref = getSharedPreferences(WELCOME_PREFERENCES, 0);
		Editor editor = pref.edit();
		editor.putBoolean(MIGRATION2_0_DONE, false);
		editor.commit();

		shouldDisplayWelcomeActivity = false;
	}
}
