package com.ifrins.hipstacast;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class HipstacastSettings extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		((Hipstacast)getApplicationContext()).trackPageView("/settings");
		addPreferencesFromResource(R.xml.settings);
	}
}
