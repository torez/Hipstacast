package com.ifrins.hipstacast.tasks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.crashlytics.android.Crashlytics;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import com.ifrins.hipstacast.HipstacastSync;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import org.json.JSONObject;

public class ImportTask extends AsyncTask<Integer, Void, Void> {
	Context context;
	OnTaskCompleted callback;

	public ImportTask(Context context, OnTaskCompleted callback) {
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected Void doInBackground(Integer... val) {
		int sn1 = val[0];
		int sn2 = val[1];
		String response = null;
		List<String> urls = new ArrayList<String>();
		
		URL url = null;
		try {
			url = new URL(
					"http://beta.hipstacast.appspot.com/api/import?id=" +
					String.valueOf(sn1) +
					String.valueOf(sn2)
			);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Crashlytics.logException(e);
		}

		try {
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			int statusCode = urlConnection.getResponseCode();
			if (statusCode == 200) {
				response = IOUtils.toString(in);
			} else if (statusCode == 404) {
				response = "[]";
				callback.onError();
			} else if (statusCode == 500) {
				response = "[]";
				callback.onError();
			}
			urlConnection.disconnect();
		} catch (IOException e) {
			Crashlytics.logException(e);
			callback.onError();
		}

		JSONArray jsonArray = null;
		if (response == null) {
			return null;
		}

		try {
			jsonArray = new JSONArray(response);
			int subscriptionCount = jsonArray.length();

			for (int i = 0; i < subscriptionCount; i++) {
				JSONObject currentPodcast = jsonArray.getJSONObject(i);
				if (currentPodcast.has("url")) {
					urls.add(currentPodcast.getString("url"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (urls.size() > 0) {
			for (int i = 0; i < urls.size(); i++) {
				Intent mSubscriptionIntent = new Intent(context, HipstacastSync.class);
				mSubscriptionIntent.setAction(HipstacastSync.ACTION_SUBSCRIBE);
				mSubscriptionIntent.putExtra(HipstacastSync.EXTRA_FEED_URL, urls.get(i));
				context.startService(mSubscriptionIntent);
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void v) {
		callback.onTaskCompleted(this.getClass().getName());
	}
	
}
