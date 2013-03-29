package com.ifrins.hipstacast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ifrins.hipstacast.model.Podcast;
import com.ifrins.hipstacast.tasks.AddPodcastProvider;
import com.ifrins.hipstacast.tasks.OnTaskCompleted;
import com.ifrins.hipstacast.tasks.UpgradeTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class HipstacastWelcome extends Activity {
	private int currentStep = 1;
	private Button nextButton = null;
	private View contentView = null;
    ViewGroup parent = null;
	LayoutInflater i = null;
	private Boolean openUpdater = null;
	int latestVersion;
	List<String> toSubscribe = new ArrayList<String>();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		openUpdater = shouldOpenUpdater();
		setContentView(R.layout.setup);
		nextButton = (Button)findViewById(R.id.setupButton);
		contentView = findViewById(R.id.setupContentView);
		parent = (ViewGroup) contentView.getParent();
		i = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		setViewForStep(1);
	}
	public void moveNext(View v) {
		Log.d("HIP-NXT", String.valueOf(currentStep));
		if (currentStep == 1) {
			currentStep++;
			setViewForStep(2);
		} else if (currentStep == 2) {
			currentStep++;
			startSubscribing();
		} else if (currentStep == 3) {
			((Hipstacast)getApplicationContext()).setWelcomeActivityShown();
			Intent backIntent = new Intent(this, HipstacastMain.class);
			backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(backIntent);
		}
	}
	
	private void setViewForStep(int step) {
	    int index = parent.indexOfChild(contentView);
	    parent.removeView(contentView);

		if (step == 1) {
		    contentView = getLayoutInflater().inflate(R.layout.setup_step_1, parent, false);
		    nextButton.setText(R.string.start);
		} else if (step == 2) {
			if (openUpdater) {
				contentView = getLayoutInflater().inflate(R.layout.setup_step_upgrade, parent, false);
				nextButton.setEnabled(false);
				new UpgradeTask(getApplicationContext(), listener, latestVersion).execute();
			} else {
			    contentView = getLayoutInflater().inflate(R.layout.setup_step_2, parent, false);
			    setUpFeaturedList(contentView);
			}
		    nextButton.setText(R.string.next);
		} else if (step == 3) {
			contentView = getLayoutInflater().inflate(R.layout.setup_step_3, parent, false);
			nextButton.setText(R.string.done);
		}
	    parent.addView(contentView, index);

	}
	private void setUpFeaturedList(View v) {
		String json = null;
		List<Podcast> presp = new ArrayList<Podcast>();
		try {
			json = IOUtils.toString(getResources().openRawResource(R.raw.features));
		} catch (NotFoundException e) {
		} catch (IOException e) {
		}
		if (json != null) {
			try {
				JSONArray a = new JSONArray(json);
				for (int i = 0; i < a.length(); i++) {
					JSONObject c = a.getJSONObject(i);
					Podcast t = new Podcast(c.getString("name"),
							c.getString("feed"), c.getString("author"),
							c.getString("cover"));
					presp.add(t);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		final ListView featuredList = (ListView)v.findViewById(R.id.setupFeaturedList);
		featuredList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				final Podcast c = (Podcast) featuredList.getAdapter().getItem(position);
				if (toSubscribe.indexOf(c.feed_link) == -1)
					toSubscribe.add(c.feed_link);
				else {
					Toast.makeText(arg1.getContext(), "You've already selected this podcast", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		if (featuredList != null && presp != null)
			featuredList.setAdapter(new SubscriptionsSearchCursorAdapter(this, presp.toArray()));
	}
	
	private void startSubscribing() {
		ProgressDialog progressDialog;
		progressDialog = new ProgressDialog(this);
		progressDialog
				.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog
				.setMessage(getString(R.string.podcast_url_alert_add_fetching));
		progressDialog.setCancelable(false);
		progressDialog.show();
		new AddPodcastProvider(this, listener).execute(Arrays.copyOf(toSubscribe.toArray(), toSubscribe.toArray().length, String[].class), progressDialog);
		
	}
	private OnTaskCompleted listener = new OnTaskCompleted() {

		@Override
		public void onTaskCompleted(String task) {
			if (task == Hipstacast.TASK_ADD_PROVIDER) {
				setViewForStep(3);
			} else if (task == Hipstacast.TASK_UPGRADE) {
				nextButton.setEnabled(true);
				setViewForStep(3);
			}
		}
		
	};
	
	private Boolean shouldOpenUpdater() {
		SharedPreferences pref = getSharedPreferences(Hipstacast.WELCOME_PREFERENCES, 0);
		latestVersion = pref.getInt("latest-version", 0);
		Cursor episodes = getContentResolver().query(Hipstacast.SUBSCRIPTIONS_PROVIDER_URI, new String[]{"_id"}, null, null, null);
		if (episodes.getCount() > 0 && latestVersion < 7)
			return true;
		else
			return false;
	}
}
