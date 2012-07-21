package com.ifrins.hipstacast;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import com.ifrins.hipstacast.tasks.ITunesStoreSearchTask;
import com.ifrins.hipstacast.model.Podcast;
import com.ifrins.hipstacast.tasks.AddPodcastProvider;

public class HipstacastSearch extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		((Hipstacast)getApplicationContext()).trackPageView("/search");
		
		final ListView listView = getListView();

		listView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {

				final Podcast c = (Podcast) getListAdapter().getItem(position);
				new AlertDialog.Builder(listView.getContext())
						.setTitle(R.string.subscribe)
						.setMessage(
								String.format(
										getString(R.string.podcast_subscribe),
										c.title))
						.setPositiveButton(R.string.subscribe,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
										String value = c.feed_link;

										ProgressDialog progressDialog;
										progressDialog = new ProgressDialog(listView.getContext());
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

		});

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuAddUrl:
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);

			new AlertDialog.Builder(this)
					.setTitle(R.string.podcast_add_title)
					.setMessage(R.string.podcast_url_alert_add_msg)
					.setView(input)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String value = input.getText().toString();

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
									new ITunesStoreSearchTask(c).execute(value);
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

	public void startSearch(View view) {
		EditText input = (EditText) findViewById(R.id.podcastSearchField);
		Log.d("HIP-SEARCH", input.getText().toString());
		new ITunesStoreSearchTask(this).execute(input.getText().toString());
	}


}
