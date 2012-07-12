package com.ifrins.hipstacast;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;

public class HUtils {
	public static final int getScreenCategory(Context c) {
		switch (c.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) {
		case Configuration.SCREENLAYOUT_SIZE_SMALL:
			return 1;
		case Configuration.SCREENLAYOUT_SIZE_NORMAL:
			return 2;
		case Configuration.SCREENLAYOUT_SIZE_LARGE:
			return 3;
		case Configuration.SCREENLAYOUT_SIZE_XLARGE:
			return 4;
		case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
			return 0;
		default:
			return 0;
		}
	}

	public static final Boolean hasBeatsSoundConfig(Context c) {
		final Intent enhanceSoundIntent = new Intent(
				"com.htc.HtcSoundEnhancerSetting.ShowSettingPage");

		final List<ResolveInfo> activities = c.getPackageManager()
				.queryIntentActivities(enhanceSoundIntent, 0);
		if (activities.isEmpty()) {
			return false;
		} else {
			return true;
		}

	}

}
