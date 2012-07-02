package com.ifrins.hipstacast;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class HipstacastMain extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Cursor p = managedQuery(
				Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts"),
				new String[] { "_id", "title", "imageUrl" }, null, null, null);

		setListAdapter(new PodcastMainListCursorAdapter(
				getApplicationContext(), p));

		ListView listView = getListView();
		listView.setTextFilterEnabled(true);

		listView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Cursor c = (Cursor) getListAdapter().getItem(position);
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
		long d = SystemClock.elapsedRealtime()+100;

		((AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE))
				.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, d,
						120000, contentIntent);
	}
}
