package com.ifrins.hipstacast;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class DownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		DownloadManager downloadManager = (DownloadManager) context
				.getSystemService(context.DOWNLOAD_SERVICE);
		String intentAction = intent.getAction();
		if ("android.intent.action.DOWNLOAD_COMPLETE".equals(intentAction)) {
			Long dwnId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,
					0);
			Cursor c = downloadManager.query(new DownloadManager.Query()
					.setFilterById(dwnId));
			c.moveToFirst();
			int downloadStatus = c.getInt(c
					.getColumnIndex(DownloadManager.COLUMN_STATUS));
			String downloadPath = c.getString(c
					.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
			String[] subDirs = downloadPath.split("/");
			int episodeId = Integer.parseInt(subDirs[subDirs.length - 1]
					.split(".mp3")[0]);
			int showId = Integer.parseInt(subDirs[subDirs.length - 2]);

			if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
				ContentValues updVal = new ContentValues();
				updVal.put("status", 1);
				context.getContentResolver()
						.update(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
								+ showId + "/episodes/" + episodeId), updVal,
								"_id = ?",
								new String[] { String.valueOf(episodeId) });
			}

			Log.d("HIP-STAT", String.format("File %s with status %d",
					downloadPath, downloadStatus));
		}
	}

}
