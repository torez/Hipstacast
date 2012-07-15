package com.ifrins.hipstacast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
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

			new AlertDialog.Builder(this)
					.setTitle(R.string.menu_search)
					.setView(searchInput)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String value = searchInput.getText()
											.toString();

									dialog.dismiss();
									new ITunesStoreSearch().execute(value);
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
		new ITunesStoreSearch().execute(input.getText().toString());
	}

	public class ITunesStoreSearch extends
			AsyncTask<String, Void, List<Podcast>> {

		@Override
		protected List<Podcast> doInBackground(String... params) {

			String query = params[0];
			String _url;
			String response = null;

			try {
				_url = "http://itunes.apple.com/search?country=US&media=podcast&limit=10&term="
						+ URLEncoder.encode(query, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				_url = "http://itunes.apple.com/search?country=US&media=podcast&limit=10&term="
						+ query;
				e.printStackTrace();
			}

			Log.d("HIP-S-URL", _url);

			URL url = null;
			try {
				url = new URL(_url);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				
				InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());
				response = IOUtils.toString(in);
				urlConnection.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			List<Podcast> presp = new ArrayList<Podcast>();

			if (response != null) {
				try {
					JSONObject r = new JSONObject(response);
					JSONArray a = r.getJSONArray("results");
					for (int i = 0; i < a.length(); i++) {
						JSONObject c = a.getJSONObject(i);
						Log.d("HIP_P", c.getString("artworkUrl600"));
						Podcast t = new Podcast(c.getString("collectionName"),
								c.getString("feedUrl"),
								c.getString("artistName"),
								c.getString("artworkUrl600"));
						presp.add(t);
					}

				} catch (JSONException e) {
					return null;
				}
				Log.d("HIP_SEARCH_R", String.valueOf(presp.size()));
				return presp;
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Podcast> result) {
			Log.d("HIP", String.valueOf(result.size()));

			Object[] r = result.toArray();
			ShowsSearchCursorAdapter a = new ShowsSearchCursorAdapter(
					getApplicationContext(), r);
			a.notifyDataSetChanged();
			setListAdapter(a);
			ListView listView = getListView();
			listView.setTextFilterEnabled(true);

		}
	}

}
