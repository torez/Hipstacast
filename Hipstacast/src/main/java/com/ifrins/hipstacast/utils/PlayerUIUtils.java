package com.ifrins.hipstacast.utils;


public class PlayerUIUtils {
	
	public static String fixCoverPath(String url) {
		if (!url.startsWith("http")) {
			return "file://" + url;
		}
		return url;
	}

	public static String convertSecondsToDuration(int seconds) {
		
		if (seconds < 3600) {
			return String.format("%02d:%02d", seconds / 60, (seconds % 60));
		}
		return String.format("%d:%02d:%02d", seconds / 3600,  (seconds % 3600) / 60, (seconds % 60));
	}

}
