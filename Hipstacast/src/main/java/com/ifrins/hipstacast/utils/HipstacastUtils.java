package com.ifrins.hipstacast.utils;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import com.crashlytics.android.Crashlytics;

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

	public static final int getEpisodeIdFromFile(Uri uri) {
		int id = -1;
		List<String> segments = uri.getPathSegments();
		String filename = segments.get(segments.size() - 1);

		if (!filename.contains(".mp3")) {
			return id;
		}

		filename = filename.replace(".mp3", "");
		if (filename.startsWith("/")) {
			filename = filename.substring(1);
		}

		try {
			id = Integer.parseInt(filename);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Crashlytics.logException(e);
		}

		return id;
	}

	public static final Uri getLocalUriForEpisodeId(Context context, int episodeId) {
		Uri externalDir = Uri.parse(
				"file://" +
				context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS).getAbsolutePath()
		);
		return Uri.withAppendedPath(externalDir, String.format("%d.mp3", episodeId));
	}

}
