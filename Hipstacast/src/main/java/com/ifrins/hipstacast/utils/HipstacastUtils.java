package com.ifrins.hipstacast.utils;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public class HipstacastUtils {
	public static final Boolean hasBeatsSoundConfig(Context c) {
	    final Intent enhanceSoundIntent = new Intent(
	            "com.htc.HtcSoundEnhancerSetting.ShowSettingPage");

	    // Check if API is supported.
	    final List<ResolveInfo> activities = c.getPackageManager()
	            .queryIntentActivities(enhanceSoundIntent, 0);
	    if (activities.isEmpty()) {
	        return false;
	    } else {
	    	return true;
	    }


	}

}
