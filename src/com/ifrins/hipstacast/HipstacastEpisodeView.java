package com.ifrins.hipstacast;

import java.io.File;
import com.ifrins.hipstacast.fragments.EpisodesFragment;
import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class HipstacastEpisodeView extends FragmentActivity implements TabListener {
	int show_id;
	int episodes_count;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_hipstacast_search_neue);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
		((Hipstacast) getApplicationContext()).trackPageView("/featured");

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

		((Hipstacast) getApplicationContext()).trackPageView("/episodes");
		
		getActionBar()
				.setTitle(getIntent().getExtras().getString("show_title"));
		
		show_id = Integer
				.parseInt(getIntent().getExtras().getString("show_id"));
		/*Cursor p = managedQuery(
				Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
						+ getIntent().getExtras().getString("show_id")
						+ "/episodes"), new String[] { "_id", "title",
						"duration", "podcast_id", "status", "position",
						"content_url", "content_length", "publication_date",
						"type" }, "podcast_id = ?", new String[] { getIntent()
						.getExtras().getString("show_id") },
				"publication_date DESC");
		episodes_count = p.getCount();
		setListAdapter(new EpisodeListCursorAdapter(getApplicationContext(), p));

		final ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		registerForContextMenu(listView);
		listView.setOnItemClickListener(;*/
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
    	private Context context;
    	
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        public SectionsPagerAdapter(FragmentManager fm, Context ctx) {
            super(fm);
        	context = ctx;
        }
 

        @Override
        public Fragment getItem(int i) {
        	if (i == 0) {
	            EpisodesFragment eF = new EpisodesFragment();
	            Bundle attrs = new Bundle();
	            attrs.putInt("show_id", show_id);
	            eF.setArguments(attrs);
	            return eF;
        	}
        	return null;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.episodes).toUpperCase();
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
