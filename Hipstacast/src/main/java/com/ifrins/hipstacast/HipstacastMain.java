package com.ifrins.hipstacast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import java.util.Random;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.google.analytics.tracking.android.EasyTracker;
import com.ifrins.hipstacast.fragments.AddUrlDialogFragment;
import com.ifrins.hipstacast.fragments.SubscriptionsFragment;
import com.ifrins.hipstacast.tasks.ExportTask;
import com.ifrins.hipstacast.utils.HipstacastLogging;

public class HipstacastMain extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);

		if (((Hipstacast) getApplication()).shouldDisplayWelcomeActivity()) {
			Intent welcomeIntent = new Intent(this, HipstacastWelcome.class);
			startActivityForResult(welcomeIntent, 1);
		}

		setContentView(R.layout.basic_layout);
		
		Fragment mainFragment = new SubscriptionsFragment();
		
		this.getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.container, mainFragment)
			.commit();
		
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
			this.onSearchRequested();
			return true;
		case R.id.menuRefresh:
			Intent mSyncIntent = new Intent(this, HipstacastSync.class);
			mSyncIntent.setAction(HipstacastSync.ACTION_SYNC);
			this.startService(mSyncIntent);
			return true;
		case R.id.menuSettings:
			startActivity(new Intent(this, HipstacastSettings.class));
			return true;
		case R.id.menuImport:
			startActivity(new Intent(this, HipstacastImport.class));
			return true;
		case R.id.menuExport:
			startExport();
			return true;
		case R.id.menuAbout:
			startActivity(new Intent(this, HipstacastAbout.class));
			return true;
		case R.id.menuAddFeedWithUrl:
			showAddUrlDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);

		// Periodical sync registration
		Intent syncIntent = new Intent(this, HipstacastSync.class);
		syncIntent.setAction(HipstacastSync.ACTION_SYNC);
		PendingIntent syncPendingIntent = PendingIntent.getService(this, 0, syncIntent, 0);
		long d = SystemClock.elapsedRealtime() + 10000;

		AlarmManager m = ((AlarmManager) getSystemService(ALARM_SERVICE));
		m.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, d, AlarmManager.INTERVAL_HOUR, syncPendingIntent);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		HipstacastLogging.log("Welcome Result Code", resultCode);
	}


	private void startExport() {
		final int n = new Random().nextInt(9999);
		final int s = new Random().nextInt(9999);

		new ExportTask(this, null).execute(n,s);

	}

	public void showAddUrlDialog() {
		DialogFragment dialog = new AddUrlDialogFragment();
		dialog.show(getSupportFragmentManager(), "AddUrlDialogFragment");
	}
}