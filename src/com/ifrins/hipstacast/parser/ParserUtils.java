package com.ifrins.hipstacast.parser;

public final class ParserUtils {
	
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
	
	

}
