package com.ifrins.hipstacast.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ifrins.hipstacast.HipstacastDownloadsScheduler;
import com.ifrins.hipstacast.utils.HipstacastLogging;

public class DownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

        HipstacastLogging.log(action);
        HipstacastLogging.log(
                "Download ID:",
                intent.getIntExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        );

		if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
			handleDownlaodComplete(context, intent);
		}
	}

	private void handleDownlaodComplete(Context context, Intent intent) {
		Intent downloadSchedulerIntent = new Intent(context, HipstacastDownloadsScheduler.class);
		downloadSchedulerIntent.setAction(HipstacastDownloadsScheduler.ACTION_DOWNLOAD_COMPLETED);
		context.startService(downloadSchedulerIntent);
	}

}
