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
import com.ifrins.hipstacast.Hipstacast;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class AddPodcastProvider extends AsyncTask<Object, Void, ContentValues> {
	
	private static final String TITLE_XPATH = "rss/channel/title/text()";
	private static final String LINK_XPATH = "rss/channel/link/text()";
	private static final String DESC_XPATH = "rss/channel/description/text()";
	private static final String AUTHOR_XPATH = "rss/channel/author/text()";
	private static final String IMAGE_XPATH = "rss/channel/image/@href";
	
	private static final String TITLE_ITEM_XPATH = "rss/channel/item[position() = 1]/title/text()";
	private static final String LINK_ITEM_XPATH = "rss/channel/item[position() = 1]/link/text()";
	private static final String PUBDATE_ITEM_XPATH = "rss/channel/item[position() = 1]/pubDate/text()";
	private static final String AUTHOR_ITEM_XPATH ="rss/channel/item[position() = 1]/author/text()";
	private static final String DESCR_ITEM_XPATH = "rss/channel/item[position() = 1]/description/text()";
	private static final String MEDIALINK_ITEM_XPATH = "rss/channel/item[position() = 1]/enclosure/@url";
	private static final String MEDIALENGHT_ITEM_XPATH = "rss/channel/item[position() = 1]/enclosure/@length";
	private static final String MEDIATYPE_ITEM_XPATH = "rss/channel/item[position() = 1]/enclosure/@type";
	private static final String SHOWNOTES_ITEM_XPATH = "rss/channel/item[position() = 1]/encoded/text()";
	private static final String DURATION_ITEM_XPATH = "rss/channel/item[position() = 1]/duration/text()";
	private static final String DONATE_ITEM_XPATH = "/rss/channel/item[position() = 1]/link[@rel='payment']/@href";
	
	private static final String START_HTML = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><style>body{background-color:#000;color:#fff;}body a{color:#33b5e5;} img{max-width:100%}</style></head><body>";
	private static final String END_HTML = "</body></html>";
	
	private OnTaskCompleted listener = null;
	private Context context = null;
		
	public AddPodcastProvider(Context c, OnTaskCompleted l) {
		listener = l;
		context = c;
	}

	private String storeImage (String imageUrl, String imageName) {
		Log.d("HIP-URL", "The image url is " + imageUrl);
		   try {
	           
	           File dir = new File (android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/hipstacast/img");
	           if(dir.exists()==false) {
	                dir.mkdirs();
	           }
	           	           
	           URL url = new URL(imageUrl); //you can write here any link
	           File file = new File(dir, imageName);

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
		} else if (tokens.length == 0) {
			seconds = Integer.parseInt(duration);
		}
		return (3600 * hours) + (60 * minutes) + seconds;
	}
	
	private Boolean checkSubscriptionExistence(String feed) {
		Cursor c = context.getContentResolver().query(Hipstacast.SUBSCRIPTIONS_PROVIDER_URI, new String[] {"_id", "feed_link"}, "feed_link = ?", new String[] {feed}, null);
		int count = c.getCount();
		c.close();
		if (count == 0)
			return false;
		return true;
	}
	
	@Override
	protected ContentValues doInBackground(Object... obj) {
		String[] url = (String[])obj[0];
		ProgressDialog c = (ProgressDialog)obj[1];
		if (url == null) return null;
		int len = url.length;
		for (int i = 0; i < len; i++) {
			if (!checkSubscriptionExistence(url[i])) {
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
					doc = builder.parse(url[i]);
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
					String feed_link = url[i];
					String author = xpath.compile(AUTHOR_XPATH).evaluate(doc, XPathConstants.STRING).toString();
					String description = xpath.compile(DESC_XPATH).evaluate(doc, XPathConstants.STRING).toString();
					long itemPubDate = convertTimeStrToTimestamp(xpath.compile(PUBDATE_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
					Log.d("HIP-IMG-RAW", xpath.compile(IMAGE_XPATH).evaluate(doc, XPathConstants.STRING).toString());
					String uuid = UUID.randomUUID().toString();
					String remoteImageUrl = xpath.compile(IMAGE_XPATH).evaluate(doc, XPathConstants.STRING).toString();
					String imageUrl = storeImage("http://src.sencha.io/jpg100/700/"+ remoteImageUrl, uuid+".jpg");
					storeImage(remoteImageUrl, uuid+".w.jpg");
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
					mNewValues.put("last_update", itemPubDate);
					Uri mNewUri = c.getContext().getContentResolver().insert(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts"), mNewValues);
					
					String content_url = xpath.compile(String.format(MEDIALINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					if (content_url.length() > 0) {
						ContentValues episodeContentValues = new ContentValues();
						String shownotes = xpath.compile(SHOWNOTES_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
						episodeContentValues.put("podcast_id", Integer.parseInt(mNewUri.getLastPathSegment()));
						episodeContentValues.put("publication_date", itemPubDate);
						episodeContentValues.put("author", xpath.compile(AUTHOR_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("description", xpath.compile(DESCR_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("content_url", content_url);
						episodeContentValues.put("content_length", xpath.compile(MEDIALENGHT_ITEM_XPATH).evaluate(doc, XPathConstants.NUMBER).toString());
						episodeContentValues.put("duration", convertDurationToSeconds(xpath.compile(DURATION_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString()));
						episodeContentValues.put("title", xpath.compile(TITLE_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("guid", xpath.compile(LINK_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("donation_url", xpath.compile(DONATE_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString());
						episodeContentValues.put("status", 0);
						if (shownotes == "") {
							String ds = xpath.compile(DESCR_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
							episodeContentValues.put("shownotes", START_HTML + ds + END_HTML);
						} else {
							episodeContentValues.put("shownotes", START_HTML + shownotes + END_HTML);
						}
						String mediaType = xpath.compile(MEDIATYPE_ITEM_XPATH).evaluate(doc, XPathConstants.STRING).toString();
						if (mediaType.length() > 3) {
							mediaType = mediaType.substring(0,5);
						}
						if (mediaType.equals("video")) {
							episodeContentValues.put("type", 1);
						} else {
							episodeContentValues.put("type", 0);
						}				
						
						c.getContext().getContentResolver().insert(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/" + mNewUri.getLastPathSegment() + "/episodes"),
																						episodeContentValues);
						
					}
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (c != null) {
			c.dismiss();
		}
		return null;

	}
	@Override
	protected void onPostExecute(ContentValues cv) {
		if (listener != null)
			listener.onTaskCompleted(Hipstacast.TASK_ADD_PROVIDER);
	}
	
}
