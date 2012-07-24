package com.ifrins.hipstacast.tasks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import com.ifrins.hipstacast.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class CheckForUpdates extends AsyncTask<Void, Void, Boolean> {
	private final Context context;
	private final SharedPreferences sharedPreferences;

	public CheckForUpdates(Context ctx) {
		context = ctx;
		sharedPreferences = ctx.getSharedPreferences("HIP_UPDATE",
				Context.MODE_PRIVATE);
		Log.d("HIP-UPD", "Init");
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		int currentvc = 0;
		try {
			currentvc = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (shouldCheckForUpdates()) {
			Log.d("HIP-UPD", "Checking");
			int servervc = getServerVersionCode();
			updateLastCheck();
			if (servervc > currentvc) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}
		return false;
 
		
	}
	@Override
	protected void onPostExecute(Boolean p) {
		if (p) {
			showUpdateNotification();
		}
		
	}

	private int getServerVersionCode() {
		String _url = "https://hipstacast.appspot.com/api/updates";

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
			int v = Integer.parseInt(IOUtils.toString(in));
			urlConnection.disconnect();
			return v;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}

	}

	private Boolean shouldCheckForUpdates() {
		long lastCheck = sharedPreferences.getLong("last_check", 0);
		Log.d("HIP-UPD-LC", String.valueOf(lastCheck)); 
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (System.currentTimeMillis() > lastCheck + (86400000 * 2)
				&& info != null && info.isConnected() && !info.isRoaming()) {
			return true;
		} else {
			return false;
		}

	}
 
	private void updateLastCheck() {
		Log.d("HIP-PREF-SAVE", "THIS SHOULD BE SAVED");
		Editor edit = sharedPreferences.edit();
		edit.clear();
		edit.putLong("last_check", System.currentTimeMillis());
		edit.commit();
	}

	private void showUpdateNotification() {
		new AlertDialog.Builder(context)
		.setTitle(R.string.update_title)
		.setMessage(R.string.update_msg)
		.setPositiveButton(R.string.update,
				new DialogInterface.OnClickListener() {
					public void onClick(
							DialogInterface dialog,
							int whichButton) {
						
						Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.ifrins.hipstacast"));
						context.startActivity(marketIntent);
						
					}})
		.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(
							DialogInterface dialog,
							int whichButton) {
						// Do nothing.
					}
				}).show();

	}

}
