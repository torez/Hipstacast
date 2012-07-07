package com.ifrins.hipstacast;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import com.ifrins.hipstacast.tasks.AddPodcastProvider;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class HipstacastMain extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		((Hipstacast)getApplicationContext()).trackPageView("/");
		
		Cursor p = managedQuery(
				Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts"),
				new String[] { "_id", "title", "imageUrl", "author" }, null,
				null, "title ASC"); 
		
		ListView listView = (ListView)findViewById(R.id.mainRegularListView);
		listView.setAdapter(new PodcastMainListCursorAdapter(
				getApplicationContext(), p));

		listView.setTextFilterEnabled(true);

		listView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Cursor c = (Cursor) parent.getAdapter().getItem(position);
				Intent openIntent = new Intent(getApplicationContext(),
						HipstacastEpisodeView.class);
				openIntent.putExtra("show_id",
						c.getString(c.getColumnIndex("_id")));
				openIntent.putExtra("img_url",
						c.getString(c.getColumnIndex("imageUrl")));
				startActivity(openIntent);
				// c.close();
			}

		});
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuAdd:
			Log.d("HIP-CLICK", "menuAdd");
			Intent openIntent = new Intent(getApplicationContext(),
					HipstacastSearch.class);
			startActivity(openIntent);
			return true;
		case R.id.menuRefresh:
			startService(new Intent(this, HipstacastSyncService.class));
			return true;
		case R.id.menuSettings:
			startActivity(new Intent(this, HipstacastSettings.class));
			return true;
		case R.id.menuImport:
			startImport();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Intent newIntent = new Intent(this, HipstacastSyncService.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				newIntent, 0);
		long d = SystemClock.elapsedRealtime() + 100;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		AlarmManager m = ((AlarmManager) getApplicationContext()
				.getSystemService(ALARM_SERVICE));
		m.cancel(contentIntent);
		m.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, d,
				Long.parseLong(prefs.getString("fetchFrequency", "86400000")),
				contentIntent);
	}
	
	private void startImport() {
		final int n = new Random().nextInt(9999);
		final int s = new Random().nextInt(9999);
		final Context c = this;
		new AlertDialog.Builder(c)
		.setTitle(R.string.import_menu)
		.setMessage(String.format(getString(R.string.import_msg), "http://goo.gl/kFyTo", n, s))
		.setPositiveButton(R.string.import_menu,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						dialog.dismiss();
						ProgressDialog progressDialog;
						progressDialog = new ProgressDialog(c); 
						progressDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progressDialog
								.setMessage(getString(R.string.podcast_url_alert_add_fetching));
						progressDialog.setCancelable(false);
						progressDialog.show();
						new ImportTask(getApplicationContext(), progressDialog).execute(n, s);

					}
				})
		.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// Do nothing.
					}
				}).show();

	}
	class ImportTask extends AsyncTask<Integer, Void, Void> {
		Context context;
		ProgressDialog progress;

		public ImportTask(Context ct, ProgressDialog pd) {
			context = ct;
			progress = pd;
		}
		@Override
		protected Void doInBackground(Integer... val) {
			int sn1 = val[0];
			int sn2 = val[1];
			String response = null;
			List<String> urls = new ArrayList<String>();
			
			URL url = null;
			try {
				url = new URL("http://hipstacast.appspot.com/api/import?sn1="+sn1+"&sn2="+sn2);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());
				if (urlConnection.getResponseCode() == 200) {
					response = IOUtils.toString(in);
				} else {
					response = "[]";
				}
				urlConnection.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONArray a = null;
			try {
				a = new JSONArray(response);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < a.length(); i++) {
				try {
					urls.add(a.getString(i));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			new AddPodcastProvider().execute(Arrays.copyOf(urls.toArray(), urls.toArray().length, String[].class),
					progress,
					context);

			return null;
		}
		
	}
}