package com.ifrins.hipstacast;

import com.google.analytics.tracking.android.EasyTracker;
import com.ifrins.hipstacast.fragments.PlayerFragment;
import com.ifrins.hipstacast.fragments.ShownotesFragment;
import com.ifrins.hipstacast.tasks.OnTaskCompleted;
import com.ifrins.hipstacast.utils.HipstacastUtils;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class EpisodePlayer extends FragmentActivity implements ActionBar.TabListener {
	ViewPager pager = null;
    SectionsPagerAdapter mSectionsPagerAdapter;
	int show_id;
	int podcast_id;
	int type;
	Boolean fromNotification = false;
	Notification n;
	Boolean videoShowingControls = true;
	
	PlayerFragment playerFragment = null;
	OnTaskCompleted performActionInFragment = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		show_id = getIntent().getExtras().getInt("show_id");
		podcast_id = getIntent().getExtras().getInt("episode_id");
		
		setContentView(R.layout.activity_hipstacast_search_neue);
		pager = (ViewPager)findViewById(R.id.pager);
	    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
	    
	    final ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    
	    pager.setAdapter(mSectionsPagerAdapter);
	    pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
		fromNotification = getIntent().getExtras().getBoolean("from_notif");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.player, menu);
		
		if (!HipstacastUtils.hasBeatsSoundConfig(getApplicationContext())) {
			menu.findItem(R.id.menuPlaySoundConfig).setVisible(false);
		}
		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.menuPlaySoundConfig: 
			Intent enhanceSoundIntent = new Intent("com.htc.HtcSoundEnhancerSetting.ShowSettingPage");
			startActivity(enhanceSoundIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}

	@Override
	protected void onDestroy() {
		Log.d("HIP-DS", "On Destroy");
		super.onDestroy();
	}
	
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

	
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
    	private ShownotesFragment shownotesFragment;
    	
        public SectionsPagerAdapter(FragmentManager fm, Context ctx) {
            super(fm);
    		playerFragment = new PlayerFragment();
    		Bundle args = new Bundle();
    		if (fromNotification)
    			args.putBoolean("from_notification", fromNotification);
    		args.putInt("episode_id", podcast_id);
    		playerFragment.setArguments(args);
    		shownotesFragment = new ShownotesFragment();
    		Bundle argsSF = new Bundle();
    		argsSF.putInt("episode_id", podcast_id);
    		shownotesFragment.setArguments(argsSF);

        }
 

        @Override
        public Fragment getItem(int i) {
        	switch (i) {
        		case 0: return playerFragment;
        		case 1: return shownotesFragment;
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
                case 0: return getString(R.string.player);
                case 1: return getString(R.string.shownotes);
            }
            return null;
        }
    }

}
