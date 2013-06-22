package com.ifrins.hipstacast.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ifrins.hipstacast.HipstacastDownloadsScheduler;

public class DownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
			handleDownlaodComplete(context, intent);
		}
	}

	private void handleDownlaodComplete(Context context, Intent intent) {
		Intent downloadSchedulerIntent = new Intent(context, HipstacastDownloadsScheduler.class);
		downloadSchedulerIntent.setAction(HipstacastDownloadsScheduler.ACTION_DOWNLOAD_COMPLETED);
		downloadSchedulerIntent.putExtra(
				DownloadManager.EXTRA_DOWNLOAD_ID,
				intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
		);
		context.startService(downloadSchedulerIntent);
	}

}
