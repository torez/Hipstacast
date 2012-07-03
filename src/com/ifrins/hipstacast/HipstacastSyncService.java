package com.ifrins.hipstacast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.xml.sax.SAXException;

import com.ifrins.hipstacast.model.Podcast;

import android.app.DownloadManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
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
		new SyncTask().execute();
	}
		
	private class SyncTask extends AsyncTask<Void, Void, Void> {
		
		private static final String TITLE_ITEM_XPATH = "rss/channel/item[position() = 1]/title/text()";
		private static final String LINK_ITEM_XPATH = "rss/channel/item[position() = 1]/link/text()";
		private static final String PUBDATE_ITEM_XPATH = "rss/channel/item[position() = 1]/pubDate/text()";
		private static final String AUTHOR_ITEM_XPATH ="rss/channel/item[position() = 1]/author/text()";
		private static final String DESCR_ITEM_XPATH = "rss/channel/item[position() = 1]/summary/text()";
		private static final String MEDIALINK_ITEM_XPATH = "rss/channel/item[position() = 1]/enclosure/@url";
		private static final String MEDIALENGHT_ITEM_XPATH = "rss/channel/item[position() = 1]/enclosure/@length";
		private static final String SHOWNOTES_ITEM_XPATH = "rss/channel/item[position() = 1]/encoded/text()";
		private static final String DURATION_ITEM_XPATH = "rss/channel/item[position() = 1]/duration/text()";
		private static final String DONATE_ITEM_XPATH = "/rss/channel/item[position() = 1]/link[@rel='payment']/@href";


		private static final String START_HTML = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><style>body{background-color:#000;color:#fff;}body a{color:#33b5e5;}</style></head><body>";
		private static final String END_HTML = "</body></html>";

		private long convertTimeStrToTimestamp(String timestamp) {
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
			try {
				Date d = format.parse(timestamp);
				return d.getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
		
		private int convertDurationToSeconds(String duration) {
			String[] tokens = duration.split(":");
			Log.d("HIP-TK", String.valueOf(tokens.length));
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
		
		private void checkFeed(String feed, int show_id) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
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
			
			XPath xpath = XPathFactory.newInstance().newXPath();

			try {
				String link = xpath.compile(LINK_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
				if(!episodeExists(link, show_id)) {
					ContentValues episodeContentValues = new ContentValues();
					String shownotes = xpath.compile(SHOWNOTES_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
					String content_url = xpath.compile(MEDIALINK_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
					String title = xpath.compile(TITLE_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
					episodeContentValues.put("podcast_id", show_id);
					episodeContentValues.put("publication_date", convertTimeStrToTimestamp(xpath.compile(PUBDATE_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString()));
					episodeContentValues.put("author", xpath.compile(AUTHOR_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
					episodeContentValues.put("description", xpath.compile(DESCR_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
					episodeContentValues.put("content_url", content_url);
					episodeContentValues.put("content_length", xpath.compile(MEDIALENGHT_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
					episodeContentValues.put("duration", convertDurationToSeconds(xpath.compile(DURATION_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString()));
					episodeContentValues.put("title", title);
					episodeContentValues.put("guid", xpath.compile(LINK_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
					episodeContentValues.put("donation_url", xpath.compile(DONATE_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
					episodeContentValues.put("status", 0);
					if (shownotes == "") {
						String ds = xpath.compile(DESCR_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
						episodeContentValues.put("shownotes", START_HTML + ds + END_HTML);
					} else {
						episodeContentValues.put("shownotes", START_HTML + shownotes + END_HTML);
					}
					
					Uri episodeNewUri = getApplicationContext().getContentResolver().insert(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/" + show_id + "/episodes"),
																					episodeContentValues);
					
                	DownloadManager mgr = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                	Log.i("HIP-S", content_url);
                	mgr.enqueue(new DownloadManager.Request(Uri.parse(content_url))
                				.setTitle(title)
                				.setDestinationInExternalFilesDir(getApplicationContext(), null, "shows/"+show_id+"/"+episodeNewUri.getLastPathSegment() + ".mp3")
                				.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI));

				}
				
				
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Cursor c = getApplicationContext().getContentResolver().query(Uri.parse(CONTENT_URL), new String[] {"_id", "feed_link"}, null, null, null);
			
			while (c.moveToNext() != false) {
				String feedUrl = c.getString(c.getColumnIndex("feed_link"));
				checkFeed(feedUrl, c.getInt(c.getColumnIndex("_id")));
			}
			
			return null;
		}
		@Override
		protected void onPostExecute(Void params) {
			//System.exit(0);
		}
	}
}
