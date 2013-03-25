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
import android.content.Context;
import android.os.AsyncTask;
import com.ifrins.hipstacast.model.Podcast;

public class ITunesStoreSearchTask extends AsyncTask<String, Void, List<Podcast>> {

	private static final String SEARCH_BASE_URL = "http://itunes.apple.com/search?country=US&media=podcast&limit=10&term=";
	
	String query;
	Context context;
	OnSearchFinished completitionCallback;

	public ITunesStoreSearchTask(Context context, String query, OnSearchFinished completitionCallback) {
		this.context = context;
		this.query = query;
		this.completitionCallback = completitionCallback;
	}

	@Override
	protected List<Podcast> doInBackground(String... params) {

		String _url;
		String response = null;

		try {
			_url = SEARCH_BASE_URL + URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			_url = SEARCH_BASE_URL + query;
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
		Object[] r = null;
		
		if (result != null) {
			r = result.toArray();
		}
		
		completitionCallback.onSearchFinished(r);
	}
}
