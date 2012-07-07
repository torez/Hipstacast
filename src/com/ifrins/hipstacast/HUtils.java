package com.ifrins.hipstacast;

import android.content.Context;
import android.content.res.Configuration;

public class HUtils {
	public static final int getScreenCategory(Context c) {
		switch (c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
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

}
