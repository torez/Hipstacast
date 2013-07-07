package com.ifrins.hipstacast;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by francesc on 07/07/13.
 */
public class HipstacastExport extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.basic_layout);

		this.getSupportFragmentManager().beginTransaction().replace(R.id.container, new Fragment()).commit();

	}
}
