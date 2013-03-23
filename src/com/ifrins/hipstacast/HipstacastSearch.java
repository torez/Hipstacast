package com.ifrins.hipstacast;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ifrins.hipstacast.fragments.SearchFragment;

public class HipstacastSearch extends SherlockFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.basic_layout);
		
		Intent searchIntent = this.getIntent();
		if (Intent.ACTION_SEARCH.equals(searchIntent.getAction())) {
			String query = searchIntent.getStringExtra(SearchManager.QUERY);
			
			Fragment mFragment = new SearchFragment();
			Bundle args = new Bundle();
			args.putString(SearchManager.QUERY, query);
			mFragment.setArguments(args);
			
			this.getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragment).commit();
		}
	}
}
