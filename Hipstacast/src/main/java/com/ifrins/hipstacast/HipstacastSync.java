package com.ifrins.hipstacast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import com.ifrins.hipstacast.model.Podcast;
import com.ifrins.hipstacast.parser.Parser;
import com.ifrins.hipstacast.parser.models.PodcastChannel;
import com.ifrins.hipstacast.parser.models.PodcastItem;
import com.ifrins.hipstacast.parser.models.PodcastRss;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.HipstacastLogging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class HipstacastSync extends IntentService {
	
	public final static String ACTION_SYNC = "com.ifrins.hipstacast.ACTION_SYNC";
	public final static String ACTION_SUBSCRIBE = "com.ifrins.hipstacast.ACTION_SUBSCRIBE";
	public final static String ACTION_UNSUBSCRIBE = "com.ifrins.hipstacast.ACTION_UNSUBSCRIBE";

	public final static String EXTRA_FEED_URL = "feedUrl";
	public final static String EXTRA_UNSUBSCRIPTION_ID = "subscriptionIds";

	Parser mParser = new Parser();

	public HipstacastSync() {
		super(HipstacastSync.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		HipstacastLogging.log("SyncService");
		if (intent.getAction().equals(ACTION_SYNC)) {
			handleSyncIntent();
		} else if (intent.getAction().equals(ACTION_SUBSCRIBE)) {
			handleSubscribeIntent(intent.getStringExtra(EXTRA_FEED_URL));
		} else if (intent.getAction().equals(ACTION_UNSUBSCRIBE)) {
			handleUnsubscribeIntent(intent.getIntExtra(EXTRA_UNSUBSCRIPTION_ID, -1));
		}
	}
	
	private void handleSubscribeIntent(String feedUrl) {
		Podcast mNewPodcast = new Podcast();
		mNewPodcast.feed_link = feedUrl;
		
		InputStream mFeedInputStream = this.getFeedInputStream(mNewPodcast);
		PodcastRss mParsedPodcast = mParser.parse(mFeedInputStream);
		int episodesCount = mParsedPodcast.channel.items.size();
		
		int subscriptionId = addSubscription(mParsedPodcast.channel, feedUrl);
		
		for (int i = 0; i < episodesCount; i++) {
			boolean isOld;
			if (i == 0) {
				isOld = false;
			} else {
				isOld = true;
			}
			saveEpisode(mParsedPodcast.channel.items.get(i), subscriptionId, isOld);
		}
		
		notifyAddedSubscription(mParsedPodcast.channel.title);
	}
	
	private void handleSyncIntent() {
		HipstacastLogging.log("Init logging");
		List<Podcast> subscriptionsList = this.getSubscriptionList();
		int subscriptionsCount = subscriptionsList.size();
		
		for (int i = 0; i < subscriptionsCount; i++) {
			Podcast mCurrentPodcast = subscriptionsList.get(i);
			InputStream mFeedInputStream = this.getFeedInputStream(mCurrentPodcast);
			
			if (mFeedInputStream == null) {
				continue;
			}
			
			PodcastRss mPodcast = mParser.parse(mFeedInputStream);
			List<PodcastItem> mPodcastItems = mPodcast.channel.items;
			int podcastItemsCount = mPodcastItems.size();		
			
			for (int ii = 0; ii < podcastItemsCount; ii++) {
				PodcastItem mPodcastItem = mPodcastItems.get(ii);
				if (!checkIfEpisodeAlreadyExists(mPodcastItem.link)) {
					saveEpisode(mPodcastItem, mCurrentPodcast.id, false);
				}
			}
		}
	}

	private void handleUnsubscribeIntent(int subscription_id) {
		getContentResolver().delete(HipstacastProvider.EPISODES_URI,
				HipstacastProvider.EPISODE_PODCAST_ID + " = ?",
				new String[] { String.valueOf(subscription_id) });

		getContentResolver().delete(HipstacastProvider.SUBSCRIPTIONS_URI,
				"_id = ?",
				new String[] { String.valueOf(subscription_id) });

		//TODO: Handle file deletions
	}
	
	private List<Podcast> getSubscriptionList() {
		List<Podcast> subscriptionsList = new ArrayList<Podcast>();
		
		Cursor c = this.getContentResolver().query(HipstacastProvider.SUBSCRIPTIONS_URI, 
										new String[] {	"_id", 
														HipstacastProvider.PODCAST_FEED,
														HipstacastProvider.PODCAST_LAST_CHECK,
														HipstacastProvider.PODCAST_ETAG,
														HipstacastProvider.PODCAST_ETAG_LASTMODIFIED}, 
										null,
										null,
										null);
		
		while (c.moveToNext()) {
			Podcast mCurrentPodcast = new Podcast();
			mCurrentPodcast.id = c.getInt(c.getColumnIndex("_id"));
			mCurrentPodcast.setFeed_link(c.getString(c.getColumnIndex(HipstacastProvider.PODCAST_FEED)));
			mCurrentPodcast.setEtag(c.getString(c.getColumnIndex(HipstacastProvider.PODCAST_ETAG)));
			mCurrentPodcast.setLastCheck(c.getInt(c.getColumnIndex(HipstacastProvider.PODCAST_LAST_CHECK)));
			mCurrentPodcast.setEtagLastModified(c.getString(c.getColumnIndex(HipstacastProvider.PODCAST_ETAG_LASTMODIFIED)));
			subscriptionsList.add(mCurrentPodcast);
		}
		return subscriptionsList;
	}
	
	private InputStream getFeedInputStream(Podcast mPodcast) {
		URL mFeedUrl = null;
		HttpURLConnection mConnection = null;
		String etag = mPodcast.getEtag();
		String etagLM = mPodcast.getEtagLastModified();
		InputStream mInputStream = null;
		
		try {
			mFeedUrl = new URL(mPodcast.getFeed_link());
		} catch (MalformedURLException e) {
			HipstacastLogging.log("MalformedURLException");
			e.printStackTrace();
			return null;
		}
		
		try {
			mConnection = (HttpURLConnection) mFeedUrl.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			HipstacastLogging.log("IOException openConnection");
			return null;
		}
		
		mConnection.addRequestProperty("Accept-Encoding", "gzip, deflate");
		
		if (etag != null && etag.length() > 0) {
			mConnection.addRequestProperty("If-None-Match", etag);
		}
		
		if (etagLM != null && etagLM.length() > 0) {
			mConnection.addRequestProperty("If-Modified-Since", etagLM);
		}
		
		try {
			mConnection.connect();
			
			if (mConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
				HipstacastLogging.log("HTTP NOT MODIFIED");
				mConnection.disconnect();
				return null;
			} else {
				this.storeRequestParameters(mPodcast.id,
											mConnection.getHeaderField("Etag"), 
											mConnection.getHeaderField("Last-Modified"));
				String encoding = mConnection.getContentEncoding();
				
				//TODO: CHECK FOR REDIRECTIONS AND SET THE APPROPIATE FEED URL
				
				if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
					mInputStream = new GZIPInputStream(mConnection.getInputStream());
				} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
					mInputStream = new InflaterInputStream(mConnection.getInputStream());
				} else {
					mInputStream = mConnection.getInputStream();
				}
				HipstacastLogging.log("InputStream");

					
			}

		} catch (IOException e) {
			HipstacastLogging.log("connect IOException");
			e.printStackTrace();
			return null;
		}
		
		if (mInputStream != null) {
			HipstacastLogging.log("notNull mInputStream");
			return mInputStream;
		}
		
		return null;
	}
	
	private void storeRequestParameters(int id, String etag, String etagLM) {
		ContentValues mContentValues = new ContentValues();
		mContentValues.put(HipstacastProvider.PODCAST_ETAG, etag);
		mContentValues.put(HipstacastProvider.PODCAST_ETAG_LASTMODIFIED, etagLM);
		
		this.getContentResolver().update(HipstacastProvider.SUBSCRIPTIONS_URI,
										mContentValues,
										"_id = ?",
										new String[] { String.valueOf(id) });
	}
	
	private Boolean checkIfEpisodeAlreadyExists(String guid) {
		Cursor c = this.getContentResolver().query(HipstacastProvider.EPISODES_URI, 
										new String[] { HipstacastProvider.EPISODE_GUID }, 
										"guid = ?", 
										new String[] { guid },
										null);
		
		if (c.getCount() == 0) {
			return false;
		}
		return true;
	}
	
	private void saveEpisode(PodcastItem mPodcastItem, int subscription, boolean isOld) {
		if (mPodcastItem.enclosure == null || mPodcastItem.enclosure.url == null) {
			return;
		}

		long pubdate = mPodcastItem.pubdate.getTime();
		HipstacastLogging.log("GUID " + mPodcastItem.link);
		
		ContentValues mContentValues = new ContentValues();
		mContentValues.put(HipstacastProvider.EPISODE_TITLE, mPodcastItem.title);
		mContentValues.put(HipstacastProvider.EPISODE_DESCRIPTION, mPodcastItem.description);
		mContentValues.put(HipstacastProvider.EPISODE_GUID, mPodcastItem.link);
		mContentValues.put(HipstacastProvider.EPISODE_CONTENT_URL, mPodcastItem.enclosure.url);
		mContentValues.put(HipstacastProvider.EPISODE_CONTENT_LENGTH, mPodcastItem.enclosure.length);
		mContentValues.put(HipstacastProvider.EPISODE_PODCAST_ID, subscription);
		mContentValues.put(HipstacastProvider.EPISODE_PUB_DATE, pubdate);
		mContentValues.put(HipstacastProvider.EPISODE_AUTHOR, "");
		mContentValues.put(HipstacastProvider.EPISODE_DURATION, mPodcastItem.duration);
		mContentValues.put(HipstacastProvider.EPISODE_TYPE, 0);

		if (!isOld) {
			mContentValues.put(HipstacastProvider.EPISODE_STATUS, HipstacastProvider.EPISODE_STATUS_UNDOWNLOADED);
		} else {
			mContentValues.put(HipstacastProvider.EPISODE_STATUS, HipstacastProvider.EPISODE_STATUS_FINISHED);
		}
		
		this.getContentResolver().insert(HipstacastProvider.EPISODES_URI, mContentValues);
	}
		
	private int addSubscription(PodcastChannel parsedSubscription, String feedUrl) {
		ContentValues mContentValues = new ContentValues();
		mContentValues.put(HipstacastProvider.PODCAST_TITLE, parsedSubscription.title);
		mContentValues.put(HipstacastProvider.PODCAST_LINK, parsedSubscription.link);
		mContentValues.put(HipstacastProvider.PODCAST_AUTHOR, parsedSubscription.author);
		mContentValues.put(HipstacastProvider.PODCAST_DESCRIPTION, parsedSubscription.description);
		mContentValues.put(HipstacastProvider.PODCAST_IMAGE, parsedSubscription.image.href);
		mContentValues.put(HipstacastProvider.PODCAST_FEED, feedUrl);
		mContentValues.put(HipstacastProvider.PODCAST_LAST_CHECK, 0);
		mContentValues.put(HipstacastProvider.PODCAST_LAST_UPDATE, 0);
		
		Uri mNewSubscriptionUri = this.getContentResolver().insert(HipstacastProvider.SUBSCRIPTIONS_URI, mContentValues);
		List<String> pathSegments = mNewSubscriptionUri.getPathSegments();
		return Integer.parseInt(pathSegments.get(pathSegments.size() - 1));
	}
	
	private void notifyAddedSubscription(String title) {
		Notification mNotification = new NotificationCompat.Builder(this)
										.setContentTitle(String.format(getString(R.string.subscription_ok), title))
										.setSmallIcon(R.drawable.ic_stat_subscription_added)
										.setPriority(NotificationCompat.PRIORITY_LOW)
										.build();
		NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(title, 0, mNotification);
	}

}
