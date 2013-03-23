package com.ifrins.hipstacast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import java.util.Random;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.ifrins.hipstacast.fragments.SubscriptionsFragment;
import com.ifrins.hipstacast.tasks.CheckForUpdates;
import com.ifrins.hipstacast.tasks.ExportTask;
import com.ifrins.hipstacast.tasks.ImportTask;

public class HipstacastMain extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(R.layout.basic_layout);
		
		Fragment mainFragment = new SubscriptionsFragment();
		
		this.getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.container, mainFragment)
			.commit();
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuAdd:
			Log.d("HIP-CLICK", "menuAdd");
			Intent openIntent = new Intent(getApplicationContext(),
					HipstacastSearchNeue.class);
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
		case R.id.menuExport:
			startExport();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
		Intent newIntent = new Intent(this, HipstacastSyncService.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				newIntent, 0);
		long d = SystemClock.elapsedRealtime() + 100;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		AlarmManager m = ((AlarmManager) getApplicationContext()
				.getSystemService(ALARM_SERVICE));
		m.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, d,
				Long.parseLong(prefs.getString("fetchFrequency", "86400000")),
				contentIntent);
		new CheckForUpdates(this).execute();
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
	private 
	void startExport() {
		final int n = new Random().nextInt(9999);
		final int s = new Random().nextInt(9999);

		new ExportTask(this, null).execute(n,s);

	}
	private void startImport() {
		Random r = new Random();
		final int n = r.nextInt(9999);
		final int s = r.nextInt(9999);
		r = null;
		final Context c = this;
		new AlertDialog.Builder(c)
		.setTitle(R.string.import_menu)
		.setMessage(String.format(getString(R.string.import_msg), "http://goo.gl/yrv9e", n, s))
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
								.setMessage(getString(R.string.import_progress));
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
}