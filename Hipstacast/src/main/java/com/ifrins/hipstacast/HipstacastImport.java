package com.ifrins.hipstacast;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.ifrins.hipstacast.fragments.ImportProcessFragment;

public class HipstacastImport extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.basic_layout);

		this.getSupportFragmentManager().beginTransaction().replace(R.id.container, new ImportProcessFragment()).commit();

	}
}