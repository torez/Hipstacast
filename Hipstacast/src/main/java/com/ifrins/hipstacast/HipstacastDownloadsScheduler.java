package com.ifrins.hipstacast;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.HipstacastLogging;

/**
 * Created by francesc on 15/06/13.
 */
public class HipstacastDownloadsScheduler extends IntentService {

    public final static String ACTION_ADD_DOWNLOAD = "com.ifrins.hipstacast.ACTION_ADD_DOWNLOAD";
    public final static String ACTION_DOWNLOAD_COMPLETED = "com.ifrins.hipstacast.ACT_DOWN_FINISHED";
    public final static String ACTION_ADD_DOWNLOAD_EPISODE_ID = "episode_id";

    public HipstacastDownloadsScheduler() {
        super(HipstacastDownloadsScheduler.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        HipstacastLogging.log(action);

       if (action.equals(HipstacastDownloadsScheduler.ACTION_ADD_DOWNLOAD)) {
            doAddDownload(intent);
        } else if (action.equals(HipstacastDownloadsScheduler.ACTION_DOWNLOAD_COMPLETED)) {
           doDownloadCompleted(intent);
       }
    }

    private void doAddDownload(Intent intent) {
        int episodeId = intent.getIntExtra(
                HipstacastDownloadsScheduler.ACTION_ADD_DOWNLOAD_EPISODE_ID, -1
        );

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

        Uri externalDir = Uri.parse(
                "file://" +
                this.getExternalFilesDir(Environment.DIRECTORY_PODCASTS).getAbsolutePath()
        );
        Uri futureFile = Uri.withAppendedPath(externalDir, String.format("%d.mp3", episodeId));
        request.setDestinationUri(futureFile);

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

    }
}
