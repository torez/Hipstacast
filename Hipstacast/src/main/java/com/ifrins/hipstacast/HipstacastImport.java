package com.ifrins.hipstacast;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.ifrins.hipstacast.fragments.TakeoutProcessFragment;

public class HipstacastImport extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.basic_layout);

		Fragment takeoutFragment = new TakeoutProcessFragment();
		Bundle args = new Bundle();
		args.putInt(TakeoutProcessFragment.EXTRA_METHOD, TakeoutProcessFragment.METHOD_IMPORT);
		takeoutFragment.setArguments(args);

		this.getSupportFragmentManager().beginTransaction().replace(R.id.container, takeoutFragment).commit();

	}
}