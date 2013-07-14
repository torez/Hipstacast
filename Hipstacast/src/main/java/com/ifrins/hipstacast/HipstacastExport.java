package com.ifrins.hipstacast;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.ifrins.hipstacast.fragments.TakeoutProcessFragment;

/**
 * Created by francesc on 07/07/13.
 */
public class HipstacastExport extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.basic_layout);

		Fragment takeoutFragment = new TakeoutProcessFragment();
		Bundle args = new Bundle();
		args.putInt(TakeoutProcessFragment.EXTRA_METHOD, TakeoutProcessFragment.METHOD_EXPORT);
		takeoutFragment.setArguments(args);

		this.getSupportFragmentManager().beginTransaction().replace(R.id.container, takeoutFragment).commit();

	}
}
