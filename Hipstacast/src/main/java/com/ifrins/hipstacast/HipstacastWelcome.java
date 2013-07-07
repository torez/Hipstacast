package com.ifrins.hipstacast;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.ifrins.hipstacast.fragments.WelcomeFragment;

public class HipstacastWelcome extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.basic_layout);

		Fragment welcomeFragment = new WelcomeFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.container, welcomeFragment).commit();
	}


}
