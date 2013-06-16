package com.ifrins.hipstacast.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ifrins.hipstacast.utils.HipstacastLogging;

public class DownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        HipstacastLogging.log(intent.getAction());
        HipstacastLogging.log(
                "Download ID:",
                intent.getIntExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        );
	}

}
