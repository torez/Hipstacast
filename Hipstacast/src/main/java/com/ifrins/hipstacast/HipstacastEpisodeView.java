package com.ifrins.hipstacast;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Menu;
import android.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.ifrins.hipstacast.fragments.EpisodeDetailsFragment;
import com.ifrins.hipstacast.fragments.EpisodesFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class HipstacastEpisodeView extends FragmentActivity implements ActionBar.TabListener {
	public static final String EXTRA_SUBSCRIPTION_ID = "show_id";
	public static final String EXTRA_SUBSCRIPTION_TITLE = "show_title";

	int show_id;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.basic_layout_viewpager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(this.getSupportFragmentManager());
        final ActionBar actionBar = this.getActionBar();
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
				.setTitle(getIntent().getExtras().getString(EXTRA_SUBSCRIPTION_TITLE));
		
		show_id = getIntent().getExtras().getInt(EXTRA_SUBSCRIPTION_ID);

		new ActionBarIconLoader(this, show_id).execute();
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.episodes, menu);
        return super.onCreateOptionsMenu(menu);
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
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

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
        public android.support.v4.app.Fragment getItem(int i) {
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

	private class ActionBarIconLoader extends AsyncTask<Void, Void, Bitmap> {
		Activity activity;
		int subscription_id;

		public ActionBarIconLoader(Activity activity, int subscription_id) {
			this.activity = activity;
			this.subscription_id = subscription_id;
		}

		@Override
		protected Bitmap doInBackground(Void... voids) {
			Cursor subscription = activity.getContentResolver().query(
					HipstacastProvider.SUBSCRIPTIONS_URI,
					HipstacastProvider.SUBSCRIPTIONS_DEFAULT_PROJECTION,
					"_id = ?",
					new String[] { String.valueOf(subscription_id) },
					null
			);
			subscription.moveToFirst();

			String imageUrl = subscription.getString(subscription.getColumnIndex(HipstacastProvider.PODCAST_IMAGE));
			return UrlImageViewHelper.getCachedBitmap(imageUrl);
		}

		@Override
		protected void onPostExecute(Bitmap icon) {
			if (icon == null) {
				return;
			}

			activity.getActionBar().setIcon(new BitmapDrawable(activity.getResources(), icon));
		}

	}
}
