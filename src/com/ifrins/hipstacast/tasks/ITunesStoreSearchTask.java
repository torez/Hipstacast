package com.ifrins.hipstacast.tasks;

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
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.ShowsSearchCursorAdapter;
import com.ifrins.hipstacast.model.Podcast;

public class ITunesStoreSearchTask extends
		AsyncTask<String, Void, List<Podcast>> {

	String query;
	Context context;

	public ITunesStoreSearchTask(Context ctx) {
		context = ctx;
	}

	@Override
	protected List<Podcast> doInBackground(String... params) {

		query = params[0];
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

		URL url = null;
		try {
			url = new URL(_url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();

			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			response = IOUtils.toString(in);
			urlConnection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Podcast> presp = new ArrayList<Podcast>();

		if (response != null) {
			try {
				JSONObject r = new JSONObject(response);
				JSONArray a = r.getJSONArray("results");
				for (int i = 0; i < a.length(); i++) {
					JSONObject c = a.getJSONObject(i);
					Podcast t = new Podcast(c.getString("collectionName"),
							c.getString("feedUrl"), c.getString("artistName"),
							c.getString("artworkUrl600"));
					presp.add(t);
				}

			} catch (JSONException e) {
				return null;
			}
			return presp;
		} else {
			return null;
		}
	}

	@Override
	protected void onPostExecute(List<Podcast> result) {
		Object[] r = result.toArray();
		ShowsSearchCursorAdapter adapter = new ShowsSearchCursorAdapter(
				context, r);
		adapter.notifyDataSetChanged();
		ListActivity act = (ListActivity)context;
		act.setListAdapter(adapter);
		ListView listView = act.getListView();
		listView.setTextFilterEnabled(true);
		act.getActionBar().setTitle(String.format(act.getString(R.string.search_title), query));
	}
}
