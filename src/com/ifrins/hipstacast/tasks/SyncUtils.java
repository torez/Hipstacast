package com.ifrins.hipstacast.tasks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.ifrins.hipstacast.Hipstacast;
import android.content.Context;
import android.database.Cursor;

public class SyncUtils {
	public static final String START_HTML = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><style>body{background-color:#000;color:#fff;}body a{color:#33b5e5;} img{max-width:100%}iframe{width:99%; height:auto;border:0; margin-top:5px}</style></head><body><center><iframe data-aa='336' src='http://ad.a-ads.com/336?background_color=000000&text_color=ffffff' scrolling='no' style='width:234px; height:60px; border:0px; padding:0;overflow:hidden'></iframe></center>";
	public static final String END_HTML = "</body></html>";
	
	public static final int convertDurationToSeconds(String duration) {
		String[] tokens = duration.split(":");
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		try {
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
		} catch (NumberFormatException e) {
			
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
	public final static Boolean episodeExists(Context context, String guid) {
		Cursor c = context.getContentResolver().query(Hipstacast.EPISODES_PROVIDER_URI, new String[] {"_id", "podcast_id", "guid"}, "guid = ?", new String[] {guid}, null);
		int co = c.getCount();
		c.close();
		if (co > 0) {
			return true;
		} else {
			return false;
		}
	}


}
