package com.ifrins.hipstacast;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class HipstacastAbout extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_about);
		
		TextView versionStringTextView = (TextView) this.findViewById(R.id.versionString);
		
    	String baseVersionString = "%s (%d)";
    	String versionString = null;
    	
    	try {
    	    PackageInfo manager = getPackageManager().getPackageInfo(getPackageName(), 0);
    	    versionString = String.format(baseVersionString, manager.versionName, manager.versionCode);
    	
    	} catch (NameNotFoundException e) {
    	    e.printStackTrace();
    	}
    	
    	if (versionString != null) {
    		versionStringTextView.setText(versionString);
    	}
	}
	
	public void doSupport(View v) {
		this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:hipstacast.app@gmail.com?subject=Hipstacast Support")));
	}

}
