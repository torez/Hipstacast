package com.ifrins.hipstacast.tasks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import com.ifrins.hipstacast.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class ImportTask extends AsyncTask<Integer, Void, Void> {
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
			url = new URL("https://hipstacast.appspot.com/api/import?sn1="+sn1+"&sn2="+sn2);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			int statusCode = urlConnection.getResponseCode();
			if (statusCode == 200) {
				response = IOUtils.toString(in);
			} else if (statusCode == 404) {
				response = "[]";
				Toast.makeText(context, R.string.import_not_found_error, Toast.LENGTH_LONG).show();
			} else if (statusCode == 500) {
				response = "[]";
				Toast.makeText(context, R.string.import_server_error, Toast.LENGTH_LONG).show();
			}
			urlConnection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONArray a = null;
		try {
			a = new JSONArray(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		int len = a.length();
		for (int i = 0; i < len; i++) {
			try {
				urls.add(a.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		new AddPodcastProvider(context, null).execute(Arrays.copyOf(urls.toArray(), urls.toArray().length, String[].class),
				progress,
				context);

		return null;
	}
	
}
