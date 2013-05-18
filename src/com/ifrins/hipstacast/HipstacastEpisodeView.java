package com.ifrins.hipstacast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.ifrins.hipstacast.fragments.EpisodeDetailsFragment;
import com.ifrins.hipstacast.fragments.EpisodesFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

public class HipstacastEpisodeView extends SherlockFragmentActivity implements TabListener {
	int show_id;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_hipstacast_search_neue);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        final ActionBar actionBar = this.getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

		
		getActionBar()
				.setTitle(getIntent().getExtras().getString("show_title"));
		
		show_id = Integer
				.parseInt(getIntent().getExtras().getString("show_id"));
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop(){
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuEpisodeUnsubscribe:
			Intent unsubscriptionIntent = new Intent(this, HipstacastSync.class);
			unsubscriptionIntent.setAction(HipstacastSync.ACTION_UNSUBSCRIBE);
			unsubscriptionIntent.putExtra(HipstacastSync.EXTRA_UNSUBSCRIPTION_ID, show_id);
			startService(unsubscriptionIntent);

			Intent goTopIntent = new Intent(this, HipstacastMain.class);
			goTopIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(goTopIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
    	
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
        	if (i == 0) {
	            EpisodesFragment eF = new EpisodesFragment();
	            Bundle attrs = new Bundle();
	            attrs.putInt("show_id", show_id);
	            eF.setArguments(attrs);
	            return eF;
        	} else if (i == 1) {
	            EpisodeDetailsFragment eDF = new EpisodeDetailsFragment();
	            Bundle attrs = new Bundle();
	            attrs.putInt("show_id", show_id);
	            eDF.setArguments(attrs);
	            return eDF;

        	}
        	return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.episodes).toUpperCase();
                case 1: return getString(R.string.details).toUpperCase();
            }
            return null;
        }
    }
}
