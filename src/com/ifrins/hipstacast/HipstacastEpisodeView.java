package com.ifrins.hipstacast;

import java.io.File;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.ifrins.hipstacast.fragments.EpisodeDetailsFragment;
import com.ifrins.hipstacast.fragments.EpisodesFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

public class HipstacastEpisodeView extends SherlockFragmentActivity implements TabListener {
	int show_id;
	int episodes_count;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_hipstacast_search_neue);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
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
			new UnsubscribeTask(this).execute(show_id);
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
        public SectionsPagerAdapter(FragmentManager fm, Context ctx) {
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


	private class UnsubscribeTask extends AsyncTask<Integer, Void, Void> {
		ProgressDialog progressDialog;

		public UnsubscribeTask(Context c) {
			progressDialog = new ProgressDialog(c);
			progressDialog.setCancelable(false);
			progressDialog.setMessage(getString(R.string.unsubscribing));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setProgress(0);
			progressDialog.show();

		}

		@Override
		protected Void doInBackground(Integer... params) {
			int id = params[0];

			getApplicationContext()
					.getContentResolver()
					.delete(Uri
							.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts"),
							"_id = ?", new String[] { String.valueOf(id) });
			getApplicationContext()
					.getContentResolver()
					.delete(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
							+ id + "/episodes"), "podcast_id = ?",
							new String[] { String.valueOf(id) });
			File f = new File(android.os.Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/Android/data/com.ifrins.hipstacast/files/shows/" + id);
			if (f.isDirectory()) {
				String[] children = f.list();
				int len = children.length;
				for (int i = 0; i < len; i++) {
					new File(f, children[i]).delete();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void r) {
			progressDialog.dismiss();
			Intent i = new Intent(getApplicationContext(), HipstacastMain.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}

	}
}
