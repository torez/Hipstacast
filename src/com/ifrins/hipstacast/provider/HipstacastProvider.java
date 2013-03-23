package com.ifrins.hipstacast.provider;

import android.net.Uri;

public class HipstacastProvider {
	public final static String PODCAST_TITLE = "title";
	public final static String PODCAST_LINK = "link";
	public final static String PODCAST_FEED = "feed_link";
	public final static String PODCAST_AUTHOR = "author";
	public final static String PODCAST_DESCRIPTION = "description";
	public final static String PODCAST_IMAGE = "imageUrl";
	public final static String PODCAST_LAST_CHECK = "last_check";
	public final static String PODCAST_LAST_UPDATE = "last_update";
	
	public final static String EPISODE_PODCAST_ID = "podcast_id";
	public final static String EPISODE_PUB_DATE = "publication_date";
	public final static String EPISODE_AUTHOR = "author";
	public final static String EPISODE_DESCRIPTION = "description";
	public final static String EPISODE_CONTENT_URL = "content_url";
	public final static String EPISODE_CONTENT_LENGTH = "content_length";
	public final static String EPISODE_DONATION = "donation_url";
	public final static String EPISODE_DURATION = "duration";
	public final static String EPISODE_TITLE = "title";
	public final static String EPISODE_SHOWNOTES = "shownotes";
	public final static String EPISODE_CURRENT_POSITION = "position";
	public final static String EPISODE_STATUS = "status";
	public final static String EPISODE_TYPE = "type";
	
	public final static int EPISODE_STATUS_UNDOWNLOADED = 0;
	public final static int EPISODE_STATUS_DOWNLOADED = 1;
	public final static int EPISODE_STATUS_STARTED = 2;
	public final static int EPISODE_STATUS_FINISHED = 3;
	
	public final static int EPISODE_TYPE_AUDIO = 0;
	public final static int EPISODE_TYPE_VIDEO = 1;
	
	public final static Uri SUBSCRIPTIONS_URI = Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts");
	
	
}
