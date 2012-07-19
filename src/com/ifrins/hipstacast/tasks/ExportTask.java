package com.ifrins.hipstacast.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import com.ifrins.hipstacast.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

public class ExportTask extends AsyncTask<Integer, Void, Void> {
	Context context;
	ProgressDialog progress;
	Integer[] sns;

	public ExportTask(Context ct, ProgressDialog pd) {
		context = ct;
		progress = pd;
	}

	@Override
	protected Void doInBackground(Integer... params) {
		int sn1 = params[0];
		int sn2 = params[1];
		HttpResponse response = null;
		List<String> urls = new ArrayList<String>();

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://10.0.2.2:8080/api/export");
		Cursor c = context
				.getContentResolver()
				.query(Uri
						.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts"),
						new String[] { "_id", "feed_link" },
						null, null, "title ASC");
		
		while (c.moveToNext() != false) {
			urls.add(c.getString(c.getColumnIndex("feed_link")));
		}
		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("sn1", String
					.valueOf(sn1)));
			nameValuePairs.add(new BasicNameValuePair("sn2", String
					.valueOf(sn2)));
			nameValuePairs.add(new BasicNameValuePair("urls", new JSONArray(urls).toString()));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			response = httpclient.execute(httppost);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		if (response.getStatusLine().getStatusCode() == 200) {
			sns = params;
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void v) {
		if (sns != null) {
			new AlertDialog.Builder(context)
			.setTitle(R.string.import_menu) 
			.setMessage(String.format(context.getString(R.string.export_done), "http://goo.gl/kFyTo", sns[0], sns[1]))
			.setPositiveButton(R.string.done,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
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
	

}
