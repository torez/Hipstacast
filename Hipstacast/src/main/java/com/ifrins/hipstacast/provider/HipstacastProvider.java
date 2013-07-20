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
	public final static String PODCAST_ETAG = "etag";
	public final static String PODCAST_ETAG_LASTMODIFIED ="etag_lm";
	
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
	public final static String EPISODE_GUID = "guid";
	public final static String EPISODE_DOWNLOADED = "downloaded";
	
	public final static int EPISODE_STATUS_UNDOWNLOADED = 0;
    public final static int EPISODE_STATUS_NOT_LISTENED = 0;
	public final static int EPISODE_STATUS_DOWNLOADED = 1;
	public final static int EPISODE_STATUS_STARTED = 2;
	public final static int EPISODE_STATUS_FINISHED = 3;
	
	public final static int EPISODE_TYPE_AUDIO = 0;
	public final static int EPISODE_TYPE_VIDEO = 1;
	
	public final static Uri SUBSCRIPTIONS_URI = Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts");
	public final static Uri EPISODES_URI = Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes");

	public final static String[] EPISODES_PLAYBACK_PROJECTION = new String[]{
            "_id",
            EPISODE_TITLE,
            EPISODE_STATUS,
            EPISODE_CURRENT_POSITION,
            EPISODE_AUTHOR,
            EPISODE_PUB_DATE,
            EPISODE_PODCAST_ID,
            EPISODE_CONTENT_URL,
            EPISODE_SHOWNOTES,
            EPISODE_DURATION,
            EPISODE_DOWNLOADED,
			EPISODE_TYPE,
            EPISODE_GUID
    };

	public final static String[] SUBSCRIPTIONS_DEFAULT_PROJECTION = new String[]{
            "_id",
            PODCAST_TITLE,
            PODCAST_IMAGE,
            PODCAST_DESCRIPTION
    };
    public final static String[] SUBSCRIPTIONS_DEFAULT_COUNT_PROJECTION = new String[] {
            "_id",
            PODCAST_TITLE,
            PODCAST_IMAGE,
            PODCAST_AUTHOR,
            PODCAST_FEED,
            "(SELECT COUNT(*) FROM 'episodes' where episodes.podcast_id = podcasts._id AND episodes.status != 3 )"
    };

	//public final static String PLAYER_LEFT_JOIN_SUBSCRIPTION_DATA = "LEFT OUTER JOIN podcasts on episodes.podcast_id = podcasts._id";
}
