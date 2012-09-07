package com.ifrins.hipstacast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.tasks.SyncUtils;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log; 

public class HipstacastSyncService extends Service {

	private static String CONTENT_URL = "content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts";
	private static String EPISODES_URL = "content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes";
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		new SyncTask(this).execute();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	private class SyncTask extends AsyncTask<Void, Void, Void> {
		private static final String ITEMS_XPATH = "rss/channel/item";
		
		private static final String TITLE_ITEM_XPATH = "rss/channel/item[position() = %d]/title/text()";
		private static final String LINK_ITEM_XPATH = "rss/channel/item[position() = %d]/link/text()";
		private static final String PUBDATE_ITEM_XPATH = "rss/channel/item[position() = %d]/pubDate/text()";
		private static final String AUTHOR_ITEM_XPATH ="rss/channel/item[position() = %d]/author/text()";
		private static final String DESCR_ITEM_XPATH = "rss/channel/item[position() = %d]/description/text()";
		private static final String MEDIALINK_ITEM_XPATH = "rss/channel/item[position() = %d]/enclosure/@url";
		private static final String MEDIALENGHT_ITEM_XPATH = "rss/channel/item[position() = %d]/enclosure/@length";
		private static final String MEDIATYPE_ITEM_XPATH = "rss/channel/item[position() = %d]/enclosure/@type";
		private static final String SHOWNOTES_ITEM_XPATH = "rss/channel/item[position() = %d]/encoded/text()";
		private static final String DURATION_ITEM_XPATH = "rss/channel/item[position() = %d]/duration/text()";
		private static final String DONATE_ITEM_XPATH = "rss/channel/item[position() = %d]/link[@rel='payment']/@href";
		private SharedPreferences prefs = null;

		
		private final Context context;
		private XPath xpath = null;
		private DocumentBuilder builder = null;
		private DocumentBuilderFactory factory = null;
		private SimpleDateFormat format = null;
		
		private List<Integer> addedEpisodes = new ArrayList<Integer>();
		
		public SyncTask(Context ct) {
			context = ct;
		}
		private long convertTimeStrToTimestamp(String timestamp) {
			if (format == null ) {
				format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
			}
			try {
				return format.parse(timestamp).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
		
		private final int convertDurationToSeconds(String duration) {
			String[] tokens = duration.split(":");
			int hours = 0;
			int minutes = 0;
			int seconds = 0;
			if (tokens.length == 2) {
				minutes = Integer.parseInt(tokens[0]);
				seconds = Integer.parseInt(tokens[1]);
			} else if (tokens.length == 3) {
				hours = Integer.parseInt(tokens[0]);
				minutes = Integer.parseInt(tokens[1]);
				seconds = Integer.parseInt(tokens[2]);
			} else if (tokens.length == 0) {
				seconds = Integer.parseInt(duration);
			}
			return (3600 * hours) + (60 * minutes) + seconds;
		}

		
		private void checkFeed(String feed, int show_id, long itemPubDate) {
			if (factory == null) {
				factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(false);
			}
			
			if (builder == null) {
				try {
					builder = factory.newDocumentBuilder();
				} catch (ParserConfigurationException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
			Document doc = null;
			try {
				doc = builder.parse(feed);
			} catch (SAXException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
			}
			if (xpath == null) {
				xpath = XPathFactory.newInstance().newXPath();
			}

			try {
				NodeList items = (NodeList) xpath.compile(ITEMS_XPATH).evaluate(doc, XPathConstants.NODESET);
				int totalItems = items.getLength();
				for (int i = 0; i < totalItems; i++) {
					String link = xpath.compile(String.format(LINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					long ciPubDate = convertTimeStrToTimestamp(xpath.compile(String.format(PUBDATE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
					
					if (ciPubDate > itemPubDate && !SyncUtils.episodeExists(getApplicationContext(), link)) {
						String mediaType = "";
						ContentValues episodeContentValues = new ContentValues();
						String shownotes = xpath.compile(String.format(SHOWNOTES_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
						String content_url = xpath.compile(String.format(MEDIALINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
						String title = xpath.compile(String.format(TITLE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
						String r_mediaType = xpath.compile(String.format(MEDIATYPE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
						if (r_mediaType.length() > 4) {
							mediaType = r_mediaType.substring(0, 5);
						}
						if (content_url.length() > 0) {
							double content_length = (Double) xpath.compile(String.format(MEDIALENGHT_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.NUMBER);
							episodeContentValues.put("podcast_id", show_id);
							episodeContentValues.put("publication_date", ciPubDate);
							episodeContentValues.put("author", xpath.compile(String.format(AUTHOR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
							episodeContentValues.put("description", xpath.compile(String.format(DESCR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
							episodeContentValues.put("content_url", content_url);
							episodeContentValues.put("content_length", content_length);
							episodeContentValues.put("duration", convertDurationToSeconds(xpath.compile(String.format(DURATION_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString()));
							episodeContentValues.put("title", title);
							episodeContentValues.put("guid", xpath.compile(String.format(LINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
							episodeContentValues.put("donation_url", xpath.compile(String.format(DONATE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
							episodeContentValues.put("status", 0);
							if (shownotes == "") {
								String ds = xpath.compile(String.format(DESCR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
								episodeContentValues.put("shownotes", SyncUtils.START_HTML + ds + SyncUtils.END_HTML);
							} else {
								episodeContentValues.put("shownotes", SyncUtils.START_HTML + shownotes + SyncUtils.END_HTML);
							}
							if (mediaType.equals("video")) {
								episodeContentValues.put("type", 1);
							} else {
								episodeContentValues.put("type", 0);
							}				
	
							Uri episodeNewUri = context.getContentResolver().insert(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/" + show_id + "/episodes"),
									episodeContentValues);
							List<String> pS = episodeNewUri.getPathSegments();
							addedEpisodes.add(Integer.parseInt(pS.get(pS.size()-1)));
	
							DownloadManager mgr = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
							DownloadManager.Request r = new DownloadManager.Request(Uri.parse(content_url))
														.setTitle(title)
														.setDestinationInExternalFilesDir(getApplicationContext(), null, "shows/"+show_id+"/"+episodeNewUri.getLastPathSegment() + ".mp3");
							
							if (!prefs.getBoolean("allowCellular", false) || content_length == 0 || content_length >= (30*1024*1024)) {
								r.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
							}
							mgr.enqueue(r);
						}
						if (i == 0) {
							ContentValues show = new ContentValues();
							show.put("last_update", ciPubDate);
							int u = getApplicationContext().getContentResolver().update(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/" + show_id), show, "_id = ?", new String[]{ String.valueOf(show_id)});
						}
					} else {
						break;
					}
				}
								
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void deleteOldEpisodes(int show_id) {
			Cursor c = getApplicationContext().getContentResolver().query(Hipstacast.EPISODES_PROVIDER_URI, new String[]{"_id", HipstacastProvider.EPISODE_PODCAST_ID, HipstacastProvider.EPISODE_STATUS, HipstacastProvider.EPISODE_PUB_DATE }, "podcast_id = ? AND status = ?", new String[] {String.valueOf(show_id), "3"}, "publication_date DESC");
			int _i = 0;
			int shouldKeep = prefs.getInt("should_keep", 1)-1;
			while (c.moveToNext() != false) {
				if (_i > shouldKeep) {
					File f = new File(android.os.Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/Android/data/com.ifrins.hipstacast/files/shows/"
							+ show_id + "/" + c.getInt(c.getColumnIndex("_id")) + ".mp3");
					if (f.exists()) {
						f.delete();
						Log.d("HIP-DEL", "Deleting!");
					}
				}
				_i++;
			}
			c.close();
		}
	
		@Override
		protected Void doInBackground(Void... params) {
			Cursor c = getApplicationContext().getContentResolver().query(Uri.parse(CONTENT_URL), new String[] {"_id", "feed_link", "last_update"}, null, null, null);
			
			long d1 = System.currentTimeMillis();

			while (c.moveToNext() != false) {
				int show_id = c.getInt(c.getColumnIndex("_id"));
				checkFeed(c.getString(c.getColumnIndex("feed_link")), show_id, c.getLong(c.getColumnIndex("last_update")));
				deleteOldEpisodes(show_id);
			}
			long d2 = System.currentTimeMillis();
			Log.d("HIP-SYNC-PREF", String.valueOf(d2-d1));
			c.close();
			return null;
		}
		@Override
		protected void onPostExecute(Void params) {
			int length = addedEpisodes.size();
			Notification uNotif = null;
			if (length > 0) {
				Cursor c = context.getContentResolver().query(Uri.parse(EPISODES_URL), new String[] {"_id", "title"}, "_id = ?", new String[] {String.valueOf(addedEpisodes.get(0))}, null);
				String message = null;
				c.moveToFirst();
				if (length == 1) {
					message = String.format(context.getString(R.string.notif_new_episodes_desc_1), c.getString(c.getColumnIndex("title")));
				} else if (length > 1) {
					message = String.format(context.getString(R.string.notif_new_episodes_desc_2), c.getString(c.getColumnIndex("title")), length-1);
				}
				uNotif = new Notification.Builder(context)
						.setContentTitle(context.getString(R.string.notif_new_episodes_title))
						.setContentText(message)
						.setSmallIcon(R.drawable.ic_notification)
						.getNotification();
			}
			//System.exit(0);
			NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			notifManager.cancel(-1001);
			if (uNotif != null) {
				notifManager.notify(-1001, uNotif);
			}

			((Service) context).stopSelf();
		}
		@Override
		protected void onPreExecute() {
			Notification n = new Notification.Builder(context)
				.setContentTitle(context.getString(R.string.feed_sync_notification_title))
				.setSmallIcon(R.drawable.ic_stat_sync)
				.setOngoing(true)
				.getNotification();
			NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			notifManager.notify(-1001, n);
			prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		}
	}
}
