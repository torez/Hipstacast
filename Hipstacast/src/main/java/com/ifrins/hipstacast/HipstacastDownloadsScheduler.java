package com.ifrins.hipstacast;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.preference.PreferenceManager;

import android.provider.MediaStore;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.HipstacastLogging;
import com.ifrins.hipstacast.utils.HipstacastUtils;

import java.io.File;

/**
 * Created by francesc on 15/06/13.
 */
public class HipstacastDownloadsScheduler extends IntentService {

    public final static String ACTION_ADD_DOWNLOAD = "com.ifrins.hipstacast.ACTION_ADD_DOWNLOAD";
    public final static String ACTION_DOWNLOAD_COMPLETED = "com.ifrins.hipstacast.ACTION_DOWNLOAD_FINISHED";
    public final static String ACTION_REMOVE_DOWNLOAD = "com.ifrins.hipstacast.ACTION_REMOVE_DOWNLOAD";
    public final static String EXTRA_EPISODE_ID = "episode_id";

    public HipstacastDownloadsScheduler() {
        super(HipstacastDownloadsScheduler.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        HipstacastLogging.log(action);

       if (action.equals(ACTION_ADD_DOWNLOAD)) {
            doAddDownload(intent);
        } else if (action.equals(ACTION_DOWNLOAD_COMPLETED)) {
           doDownloadCompleted(intent);
       } else if (action.equals(ACTION_REMOVE_DOWNLOAD)) {
           doRemoveDownload(intent);
       }
    }

    private void doAddDownload(Intent intent) {
        int episodeId = intent.getIntExtra(EXTRA_EPISODE_ID, -1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Cursor episodeCursor = getContentResolver().query(HipstacastProvider.EPISODES_URI,
                HipstacastProvider.EPISODES_PLAYBACK_PROJECTION,
                "_id = ?",
                new String[] { String.valueOf(episodeId)},
                null);

        if (episodeCursor.getCount() == 0) {
            return;
        }
        episodeCursor.moveToFirst();


        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(
                        episodeCursor.getString(
                                episodeCursor.getColumnIndex(
                                        HipstacastProvider.EPISODE_CONTENT_URL)
                        )
                )
        );

        request.setDestinationUri(HipstacastUtils.getLocalUriForEpisodeId(this, episodeId));

        String episodeTitle = episodeCursor.getString(
                episodeCursor.getColumnIndex(
                        HipstacastProvider.EPISODE_TITLE
                )
        );
        request.setTitle(episodeTitle);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        int allowedNetworkTypes;
        if (prefs.getBoolean("allowCellular", false)) {
            allowedNetworkTypes = DownloadManager.Request.NETWORK_MOBILE |
                    DownloadManager.Request.NETWORK_WIFI;
        } else {
            allowedNetworkTypes = DownloadManager.Request.NETWORK_WIFI;
        }
        request.setAllowedNetworkTypes(allowedNetworkTypes);
        request.allowScanningByMediaScanner();

        DownloadManager manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

    }

    private void doDownloadCompleted(Intent intent) {
	    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
	    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

	    if (downloadId == -1) {
		    return;
	    }
		Cursor downloadedQuery = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
	    if (downloadedQuery.getCount() == 0) {
		    return;
	    }
	    downloadedQuery.moveToFirst();

	    int status = downloadedQuery.getInt(
			    downloadedQuery.getColumnIndex(DownloadManager.COLUMN_STATUS)
	    );
	    Uri downloadedFileUri = Uri.parse(
			    downloadedQuery.getString(downloadedQuery.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
	    );

	    int episodeId = HipstacastUtils.getEpisodeIdFromFile(downloadedFileUri);
	    if (episodeId == -1) {
		    return;
	    }

	    if (status == DownloadManager.STATUS_SUCCESSFUL) {
		    //Set file as not media
		    MediaScannerConnection.scanFile(
				    this,
				    new String[] { downloadedFileUri.toString() },
				    null,
				    new MediaScannerConnection.OnScanCompletedListener() {
			            @Override
			            public void onScanCompleted(String path, Uri uri) {
						    HipstacastLogging.log("Finished scan of " + uri.toString());
						    ContentValues fileContentValues = new ContentValues();
						    fileContentValues.put(
								    MediaStore.Files.FileColumns.MEDIA_TYPE,
								    MediaStore.Files.FileColumns.MEDIA_TYPE_NONE
						    );
						    getContentResolver().update(uri, fileContentValues, null, null);
			            }
		    });

		    //Register it on our DB
		    ContentValues hContentValues = new ContentValues();
		    hContentValues.put(HipstacastProvider.EPISODE_DOWNLOADED, 1);

		    getContentResolver().update(
				    HipstacastProvider.EPISODES_URI,
				    hContentValues, "_id = ?",
				    new String[] { String.valueOf(episodeId) }
		    );


	    }

    }

    private void doRemoveDownload(Intent intent) {
        int episode_id = intent.getIntExtra(EXTRA_EPISODE_ID, -1);
        if (episode_id == -1) {
            return;
        }

        Uri localPath = HipstacastUtils.getLocalUriForEpisodeId(this, episode_id);
        File file = new File(localPath.getPath());

        if (file.exists()) {
            file.delete();
        }

        ContentValues notDownloaded = new ContentValues();
        notDownloaded.put(HipstacastProvider.EPISODE_DOWNLOADED, HipstacastProvider.EPISODE_STATUS_UNDOWNLOADED);

        getContentResolver().update(
                HipstacastProvider.EPISODES_URI,
                notDownloaded,
                "_id = ?",
                new String[] { String.valueOf(episode_id) }
        );

    }
}
