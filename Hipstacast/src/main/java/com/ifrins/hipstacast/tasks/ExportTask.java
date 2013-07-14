package com.ifrins.hipstacast.tasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import com.crashlytics.android.Crashlytics;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.HipstacastLogging;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ExportTask extends AsyncTask<Integer, Void, Boolean> {

	Context context;
	OnTaskCompleted callback;

	public ExportTask(Context context, OnTaskCompleted callback) {
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected Boolean doInBackground(Integer... val) {
		int sn1 = val[0];
		int sn2 = val[1];
		String jsonData = getSubscriptions();

		URL url = null;
		try {
			url = new URL(
					"http://beta.hipstacast.appspot.com/api/export?id=" +
							String.valueOf(sn1) +
							String.valueOf(sn2)
			);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Crashlytics.logException(e);
		}

		try {
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			HipstacastLogging.log(jsonData);

			OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
			wr.write(jsonData);
			wr.flush();

			int statusCode = urlConnection.getResponseCode();
			urlConnection.disconnect();

			if (statusCode >= 400) {
				return  false;
			} else {
				return true;
			}

		} catch (IOException e) {
			Crashlytics.logException(e);
		}

		return false;
	}
	
	@Override
	protected void onPostExecute(Boolean status) {
		if (status) {
			callback.onTaskCompleted(this.getClass().getName());
		} else {
			callback.onError();
		}
	}

	private String getSubscriptions() {
		JSONArray podcastList = new JSONArray();
		Cursor subscriptions = context.getContentResolver().query(
				HipstacastProvider.SUBSCRIPTIONS_URI,
				new String[] {
						"_id",
						HipstacastProvider.PODCAST_TITLE,
						HipstacastProvider.PODCAST_FEED
				},
				null,
				null,
				null
		);

		while (subscriptions.moveToNext()) {
			JSONObject podcast = new JSONObject();
			try {
				podcast.put(
						"title",
						subscriptions.getString(subscriptions.getColumnIndex(HipstacastProvider.PODCAST_TITLE))
				);
				podcast.put(
						"url",
						subscriptions.getString(subscriptions.getColumnIndex(HipstacastProvider.PODCAST_FEED))
				);
				podcastList.put(podcast);
			} catch (JSONException e) {
				Crashlytics.logException(e);
			}
		}

		return podcastList.toString();
	}
	

}
