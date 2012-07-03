package com.ifrins.hipstacast.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.util.ByteArrayBuffer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class AddPodcastProvider extends AsyncTask<Object, Void, ContentValues> {
	
	private static String NS = "http://www.itunes.com/dtds/podcast-1.0.dtd";
	private static String TITLE_XPATH = "rss/channel/title/text()";
	private static String LINK_XPATH = "rss/channel/link/text()";
	private static String PUBDATE_XPATH = "rss/channel/pubDate/text()";
	private static String DESC_XPATH = "rss/channel/description/text()";
	private static String AUTHOR_XPATH = "rss/channel/author/text()";
	private static String IMAGE_XPATH = "rss/channel/image/@href";
	
	private static String TITLE_ITEM_XPATH = "rss/channel/item[position() = 1]/title/text()";
	private static String LINK_ITEM_XPATH = "rss/channel/item[position() = 1]/link/text()";
	private static String PUBDATE_ITEM_XPATH = "rss/channel/item[position() = 1]/pubDate/text()";
	private static String AUTHOR_ITEM_XPATH ="rss/channel/item[position() = 1]/author/text()";
	private static String DESCR_ITEM_XPATH = "rss/channel/item[position() = 1]/summary/text()";
	private static String MEDIALINK_ITEM_XPATH = "rss/channel/item[position() = 1]/enclosure/@url";
	private static String MEDIALENGHT_ITEM_XPATH = "rss/channel/item[position() = 1]/enclosure/@length";
	private static String SHOWNOTES_ITEM_XPATH = "rss/channel/item[position() = 1]/encoded/text()";
	private static String DURATION_ITEM_XPATH = "rss/channel/item[position() = 1]/duration/text()";
	
	private static String START_HTML = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><style>body{background-color:#000;color:#fff;}body a{color:#33b5e5;}</style></head><body>";
	private static String END_HTML = "</body></html>";
	
	private String storeImage (String imageUrl) {
		Log.d("HIP-URL", "The image url is " + imageUrl);
		   try {
	           
	           File dir = new File (android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/hipstacast/img");
	           if(dir.exists()==false) {
	                dir.mkdirs();
	           }
	           	           
	           URL url = new URL("http://src.sencha.io/jpg95/700/"+imageUrl); //you can write here any link
	           File file = new File(dir, UUID.randomUUID().toString()+".jpg");

	           long startTime = System.currentTimeMillis();
	           Log.d("DownloadManager", "download begining");
	           Log.d("DownloadManager", "download url:" + url);

	           /* Open a connection to that URL. */
	           URLConnection ucon = url.openConnection();

	           /*
	            * Define InputStreams to read from the URLConnection.
	            */
	           InputStream is = ucon.getInputStream();
	           BufferedInputStream bis = new BufferedInputStream(is);

	           /*
	            * Read bytes to the Buffer until there is nothing more to read(-1).
	            */
	           ByteArrayBuffer baf = new ByteArrayBuffer(5000);
	           int current = 0;
	           while ((current = bis.read()) != -1) {
	              baf.append((byte) current);
	           }


	           /* Convert the Bytes read to a String. */
	           FileOutputStream fos = new FileOutputStream(file);
	           fos.write(baf.toByteArray());
	           fos.flush();
	           fos.close();
	           
	           Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
	           Log.d("HIP-DW", file.getAbsolutePath());
	           return file.getAbsolutePath();

	   } catch (IOException e) {
	       Log.d("DownloadManager", "Error: " + e);
	       Log.d("HIP-URL-IMG", imageUrl);
	       return "";
	   }

	}
 
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
	
	@Override
	protected ContentValues doInBackground(Object... obj) {
		String url = obj[0].toString();
		ProgressDialog c = (ProgressDialog)obj[1];
		Context ct = (Context)obj[2];
		if (url == null) return null;
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
			doc = builder.parse(url);
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			long d1 = System.currentTimeMillis();
			String title = xpath.compile(TITLE_XPATH).evaluate(doc, XPathConstants.STRING).toString();
			String link = xpath.compile(LINK_XPATH).evaluate(doc, XPathConstants.STRING).toString();
			String feed_link = url;
			String author = xpath.compile(AUTHOR_XPATH).evaluate(doc, XPathConstants.STRING).toString();
			String description = xpath.compile(DESC_XPATH).evaluate(doc, XPathConstants.STRING).toString();
			Log.d("HIP-IMG-RAW", xpath.compile(IMAGE_XPATH).evaluate(doc, XPathConstants.STRING).toString());
			String imageUrl = storeImage(xpath.compile(IMAGE_XPATH).evaluate(doc, XPathConstants.STRING).toString());
			long d2 = System.currentTimeMillis();
			Log.d("HIP-PERF", String.valueOf(d2-d1));
			Log.d("HIP-TITLE", title);
			Log.d("HIP-IMG", xpath.compile(IMAGE_XPATH).evaluate(doc, XPathConstants.STRING).toString());
			Log.d("HIP-AUTH", author);
			
			ContentValues mNewValues = new ContentValues();
			mNewValues.put("title", title);
			mNewValues.put("link", link);
			mNewValues.put("feed_link", feed_link);
			mNewValues.put("author", author);
			mNewValues.put("description", description);
			mNewValues.put("imageUrl", imageUrl);
			mNewValues.put("last_check", System.currentTimeMillis()/1000);
			mNewValues.put("last_update", System.currentTimeMillis()/1000);
			Uri mNewUri = c.getContext().getContentResolver().insert(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts"), mNewValues);

			ContentValues episodeContentValues = new ContentValues();
			String shownotes = xpath.compile(SHOWNOTES_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
			episodeContentValues.put("podcast_id", Integer.parseInt(mNewUri.getLastPathSegment()));
			episodeContentValues.put("publication_date", convertTimeStrToTimestamp(xpath.compile(PUBDATE_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString()));
			episodeContentValues.put("author", xpath.compile(AUTHOR_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
			episodeContentValues.put("description", xpath.compile(DESCR_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
			episodeContentValues.put("content_url", xpath.compile(MEDIALINK_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
			episodeContentValues.put("content_length", Integer.parseInt(xpath.compile(MEDIALENGHT_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString()));
			episodeContentValues.put("duration", convertDurationToSeconds(xpath.compile(DURATION_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString()));
			episodeContentValues.put("title", xpath.compile(TITLE_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
			episodeContentValues.put("guid", xpath.compile(LINK_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
			
			Log.d("HIP-DON", xpath.compile(LINK_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
			episodeContentValues.put("status", 0);
			if (shownotes == "") {
				String ds = xpath.compile(DESCR_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
				episodeContentValues.put("shownotes", START_HTML + ds + END_HTML);
			} else {
				episodeContentValues.put("shownotes", START_HTML + shownotes + END_HTML);
			}
			
			Uri episodeNewUri = c.getContext().getContentResolver().insert(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/" + mNewUri.getLastPathSegment() + "/episodes"),
																			episodeContentValues);
			
			Log.d("HIP-URL", episodeNewUri.toString());
			if (c != null) {
				c.dismiss();
			}
			Log.d("HIP-URL", mNewUri.toString());

			return null;
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	
}
