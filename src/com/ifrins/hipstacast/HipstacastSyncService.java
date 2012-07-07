package com.ifrins.hipstacast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import android.app.DownloadManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class HipstacastSyncService extends Service {

	private static String CONTENT_URL = "content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts";
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.w("HIP-SYNC", "I'm running!");
		new SyncTask(this).execute();
	}
	
	@Override
	public void onDestroy() {
		Log.i("HIP-SYNC", "I'm destroyed");
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
		private static final String SHOWNOTES_ITEM_XPATH = "rss/channel/item[position() = %d]/encoded/text()";
		private static final String DURATION_ITEM_XPATH = "rss/channel/item[position() = %d]/duration/text()";
		private static final String DONATE_ITEM_XPATH = "rss/channel/item[position() = %d]/link[@rel='payment']/@href";

		
		private static final String START_HTML = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><style>body{background-color:#000;color:#fff;}body a{color:#33b5e5;}</style></head><body>";
		private static final String END_HTML = "</body></html>";
		private final Context context;
		private XPath xpath = null;
		private DocumentBuilder builder = null;
		private DocumentBuilderFactory factory = null;
		private SimpleDateFormat format = null;
		
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
		
		private int convertDurationToSeconds(String duration) {
			String[] tokens = duration.split(":");
			int len = tokens.length;
			int hours = 0;
			int minutes = 0;
			int seconds = 0;
			if (len == 2) {
				minutes = Integer.parseInt(tokens[0]);
				seconds = Integer.parseInt(tokens[1]);
			} else if (len == 3) {
				hours = Integer.parseInt(tokens[0]);
				minutes = Integer.parseInt(tokens[1]);
				seconds = Integer.parseInt(tokens[2]);
			}
			return (3600 * hours) + (60 * minutes) + seconds;
		}

		private Boolean episodeExists(String guid, int show_id) {
			Cursor c = getApplicationContext().getContentResolver().query(Uri.parse(CONTENT_URL+"/"+show_id+"/episodes"), new String[] {"_id", "podcast_id", "guid"}, "guid = ?", new String[] {guid}, null);
			int co = c.getCount();
			c.close();
			if (co > 0) {
				return true;
			} else {
				return false;
			}
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
					
					if (ciPubDate > itemPubDate && !episodeExists(link, show_id)) {
						Log.i("HIP-SYNC", String.format("Found a new episode for the feed %s", feed));
						ContentValues episodeContentValues = new ContentValues();
						String shownotes = xpath.compile(String.format(SHOWNOTES_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
						String content_url = xpath.compile(String.format(MEDIALINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
						String title = xpath.compile(String.format(TITLE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
						episodeContentValues.put("podcast_id", show_id);
						episodeContentValues.put("publication_date", ciPubDate);
						episodeContentValues.put("author", xpath.compile(String.format(AUTHOR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("description", xpath.compile(String.format(DESCR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("content_url", content_url);
						episodeContentValues.put("content_length", xpath.compile(String.format(MEDIALENGHT_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("duration", convertDurationToSeconds(xpath.compile(String.format(DURATION_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString()));
						episodeContentValues.put("title", title);
						episodeContentValues.put("guid", xpath.compile(String.format(LINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("donation_url", xpath.compile(String.format(DONATE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("status", 0);
						if (shownotes == "") {
							String ds = xpath.compile(String.format(DESCR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
							episodeContentValues.put("shownotes", START_HTML + ds + END_HTML);
						} else {
							episodeContentValues.put("shownotes", START_HTML + shownotes + END_HTML);
						}
						Uri episodeNewUri = context.getContentResolver().insert(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/" + show_id + "/episodes"),
								episodeContentValues);

						DownloadManager mgr = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						
						DownloadManager.Request r = new DownloadManager.Request(Uri.parse(content_url))
													.setTitle(title)
													.setDestinationInExternalFilesDir(getApplicationContext(), null, "shows/"+show_id+"/"+episodeNewUri.getLastPathSegment() + ".mp3");
						
						if (!prefs.getBoolean("allowCellular", false)) {
							r.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
						}
						mgr.enqueue(r);

						if (i == 0) {
							ContentValues show = new ContentValues();
							show.put("last_update", ciPubDate);
							int u = getApplicationContext().getContentResolver().update(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/" + show_id), show, "_id = ?", new String[]{ String.valueOf(show_id)});
							Log.d("HIP-NW-UP", String.valueOf(u));
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
		
		@Override
		protected Void doInBackground(Void... params) {
			Cursor c = getApplicationContext().getContentResolver().query(Uri.parse(CONTENT_URL), new String[] {"_id", "feed_link", "last_update"}, null, null, null);
			
			long d1 = System.currentTimeMillis();

			while (c.moveToNext() != false) {
				checkFeed(c.getString(c.getColumnIndex("feed_link")), c.getInt(c.getColumnIndex("_id")), c.getLong(c.getColumnIndex("last_update")));
			}
			long d2 = System.currentTimeMillis();
			Log.d("HIP-SYNC-PREF", String.valueOf(d2-d1));
			c.close();
			return null;
		}
		@Override
		protected void onPostExecute(Void params) {
			//System.exit(0);
			((Service) context).stopSelf();
		}
	}
}
