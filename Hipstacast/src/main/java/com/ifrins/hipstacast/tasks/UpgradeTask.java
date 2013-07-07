package com.ifrins.hipstacast.tasks;


import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import com.ifrins.hipstacast.Hipstacast;
import android.content.Context;
import android.os.AsyncTask;
import com.ifrins.hipstacast.HipstacastSync;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.HipstacastLogging;
import com.ifrins.hipstacast.utils.HipstacastUtils;

import java.io.File;

public class UpgradeTask extends AsyncTask<Void, Void, Void> {

	Context context;
	OnTaskCompleted onTaskCompletedListener;

	public UpgradeTask(Context context, OnTaskCompleted onTaskCompletedListener) {
		this.context = context;
		this.onTaskCompletedListener = onTaskCompletedListener;
	}
	
	
	@Override
	protected Void doInBackground(Void... params) {
		HipstacastLogging.log("BG Start");
		// Delete images directory
		File imagesDir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/hipstacast/img");
		if (imagesDir.exists()) {
			HipstacastLogging.log("BG Images Dir");
			File[] imagesList = imagesDir.listFiles();

			for (int fileIterator = 0; fileIterator > imagesList.length; fileIterator++) {
				imagesList[fileIterator].delete();
			}
			imagesDir.delete();
		}

		// Update statuses
		ContentValues contentValues = new ContentValues();
		contentValues.put(HipstacastProvider.EPISODE_DOWNLOADED, HipstacastProvider.EPISODE_STATUS_DOWNLOADED);
		context.getContentResolver().update(
				HipstacastProvider.EPISODES_URI,
				contentValues,
				"status != ? AND status != ?",
				new String[] { "0", "3" }
		);

		// Move podcast files
		File mediaFilesDir = new File(
				Environment.getExternalStorageDirectory().getAbsolutePath() +
						"/Android/data/com.ifrins.hipstacast/files/shows/"
		);
		if (mediaFilesDir.exists()) {
			// Move files
			File[] mediaFiles = mediaFilesDir.listFiles();

			for (int mediaIterator = 0; mediaIterator > mediaFiles.length; mediaIterator++) {
				File currentFile = mediaFiles[mediaIterator];
				int episodeId = HipstacastUtils.getEpisodeIdFromFile(
						Uri.parse("file://" + currentFile.getAbsolutePath())
				);
				if (currentFile.isFile()) {
					currentFile.renameTo(new File(
							HipstacastUtils.getLocalUriForEpisodeId(context, episodeId).getPath()
					));
				}
			}

			// Remove dirs
			mediaFiles = mediaFilesDir.listFiles();

			for (int mediaIterator = 0; mediaIterator > mediaFiles.length; mediaIterator++) {
				mediaFiles[mediaIterator].delete();
			}
			mediaFilesDir.delete();
		}
		HipstacastLogging.log("BG Move files");

		Intent syncIntent = new Intent(context, HipstacastSync.class);
		syncIntent.setAction(HipstacastSync.ACTION_SYNC);
		syncIntent.putExtra(HipstacastSync.EXTRA_REBUILD, true);
		context.startService(syncIntent);
		return null;
	}
	
	@Override
	protected void onPostExecute(Void v) {
		if (onTaskCompletedListener != null) {
			onTaskCompletedListener.onTaskCompleted(Hipstacast.TASK_UPGRADE);
		}
	}


}
