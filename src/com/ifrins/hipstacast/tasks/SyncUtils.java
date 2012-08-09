package com.ifrins.hipstacast.tasks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.text.format.DateFormat;

public class SyncUtils {
	public static final int convertDurationToSeconds(String duration) {
		String[] tokens = duration.split(":");
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		if (tokens.length == 2) {
			minutes = Integer.parseInt(tokens[0]);
			seconds = Integer.parseInt(tokens[1]);
		} else if (tokens.length == 3) {
			hours = Integer.parseInt(tokens[0]);
			minutes = Integer.parseInt(tokens[1]);
			seconds = Integer.parseInt(tokens[2]);
		} else if (tokens.length == 0) {
			seconds = Integer.parseInt(duration);
		}
		return (3600 * hours) + (60 * minutes) + seconds;
	}
	public final static long convertTimeStrToTimestamp(String timestamp) {
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
		try {
			return format.parse(timestamp).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

}
