package com.ifrins.hipstacast;

import com.ifrins.hipstacast.fragments.FeaturedFragment;
import com.ifrins.hipstacast.fragments.SearchFragment;
import com.ifrins.hipstacast.tasks.AddPodcastProvider;
import com.ifrins.hipstacast.tasks.ITunesStoreSearchTask;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HipstacastSearchNeue extends FragmentActivity implements ActionBar.TabListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    SearchFragment searchFragment = new SearchFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hipstacast_search_neue);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
		((Hipstacast) getApplicationContext()).trackPageView("/featured");

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }
    
    class CustomURLClickListener implements View.OnClickListener {
	    private final AlertDialog dialog;
	    private final EditText input;
	    public CustomURLClickListener(AlertDialog dialog, EditText input) {
	        this.dialog = dialog;
	        this.input = input;
	    }
	    @Override
	    public void onClick(View v) {
	    	
			String value = input.getText().toString();
			if (URLUtil.isValidUrl(value)) {
				dialog.dismiss();
				ProgressDialog progressDialog;
				progressDialog = new ProgressDialog(input
						.getContext());
				progressDialog
						.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog
						.setMessage(getString(R.string.podcast_url_alert_add_fetching));
				progressDialog.setCancelable(false);
				progressDialog.show();
				Log.i("HIP-POD-URL", value);
	
				new AddPodcastProvider().execute(new String[]{value},
						progressDialog,
						getApplicationContext());
			} else {
				dialog.setMessage(getString(R.string.invalid_url));
			}
	    }
    }

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuAddUrl:
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			input.setText("http://");
			
			AlertDialog d = new AlertDialog.Builder(this)
					.setTitle(R.string.podcast_add_title)
					.setMessage(R.string.podcast_url_alert_add_msg)
					.setView(input)
					.setPositiveButton("Ok", null)
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
								}
							}).create();
			d.show();
			Button theButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
			theButton.setOnClickListener(new CustomURLClickListener(d, input));
							

			return true;
		case R.id.menuSearch:
			final EditText searchInput = new EditText(this);
			final Context c = this;
			new AlertDialog.Builder(c)
					.setTitle(R.string.menu_search)
					.setView(searchInput)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String value = searchInput.getText()
											.toString();
		
									dialog.dismiss();
									getActionBar().setSelectedNavigationItem(1);
									new ITunesStoreSearchTask(c, searchFragment).execute(value);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
								}
							}).show();

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
	            return new FeaturedFragment(context);
        	} else if (i == 1) {
        		return searchFragment;
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
                case 0: return getString(R.string.featured).toUpperCase();
                case 1: return getString(R.string.search_results).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        public DummySectionFragment() {
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            Bundle args = getArguments();
            textView.setText(Integer.toString(args.getInt(ARG_SECTION_NUMBER)));
            return textView;
        }
    }
}
