package com.ifrins.hipstacast.tasks;

import java.io.IOException;
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
import com.ifrins.hipstacast.Hipstacast;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class LoadRemoteEpisodesTask extends AsyncTask<Void, Void, Void> {
	
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

	
	private static final String START_HTML = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><style>body{background-color:#000;color:#fff;}body a{color:#33b5e5;} img{max-width:100%}</style></head><body>";
	private static final String END_HTML = "</body></html>";
	private XPath xpath = null;
	private DocumentBuilder builder = null;
	private DocumentBuilderFactory factory = null;
	
	Context context;
	String feed;
	int show_id;
	int totalItems;
	SharedPreferences sPreferences = null;
	
	Boolean shouldCheck;

	
	public LoadRemoteEpisodesTask(Context context, String feed, int show_id) {
		this.context = context;
		this.feed = feed;
		this.show_id = show_id;
	}
	
	@Override
	protected void onPreExecute() {
		sPreferences = context.getSharedPreferences(Hipstacast.FULL_SHOW_PREFERENCES, 0);
		shouldCheck = sPreferences.getBoolean("check_"+show_id, true);
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
	if (shouldCheck) {	
		
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
			totalItems = items.getLength();
			for (int i = 0; i < totalItems; i++) {
				String content_url = xpath.compile(String.format(MEDIALINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
				String guid = xpath.compile(String.format(LINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();

				if (content_url.length() > 0 && !SyncUtils.episodeExists(context, guid)) {
					long pubDate = SyncUtils.convertTimeStrToTimestamp(xpath.compile(String.format(PUBDATE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
					String author = xpath.compile(String.format(AUTHOR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					String title = xpath.compile(String.format(TITLE_ITEM_XPATH, i+1)).evaluate(doc,XPathConstants.STRING).toString();
					String description = xpath.compile(String.format(DESCR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					double content_length;
					try {
						content_length = Double.parseDouble(xpath.compile(String.format(MEDIALENGHT_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
					}
					catch (NumberFormatException e) {
						content_length = 0;
					}
					int duration = SyncUtils.convertDurationToSeconds(xpath.compile(String.format(DURATION_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
					String donation_url = xpath.compile(String.format(DONATE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					String shownotes = xpath.compile(String.format(SHOWNOTES_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					int type;
					if (shownotes == "") {
						shownotes = START_HTML + xpath.compile(String.format(DESCR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString() + END_HTML;
					} else {
						shownotes = START_HTML + shownotes + END_HTML;
					}
					String mediaType = xpath.compile(String.format(MEDIATYPE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					if (mediaType.length() > 4) {
						mediaType = mediaType.substring(0, 5);
					}
	
					if (mediaType.equals("video")) {
						type = 1;
					} else {
						type = 0;
					}				
					
					ContentValues episodeContentValues = new ContentValues();
					episodeContentValues.put("podcast_id", show_id);
					episodeContentValues.put("publication_date", pubDate);
					episodeContentValues.put("author", author);
					episodeContentValues.put("description", description);
					episodeContentValues.put("content_url", content_url);
					episodeContentValues.put("content_length", content_length);
					episodeContentValues.put("duration", duration);
					episodeContentValues.put("title", title);
					episodeContentValues.put("guid", guid);
					episodeContentValues.put("donation_url", donation_url);
					episodeContentValues.put("status", 3);
					episodeContentValues.put("shownotes", shownotes);
					episodeContentValues.put("type", type);
					context.getContentResolver().insert(Hipstacast.EPISODES_PROVIDER_URI, episodeContentValues);
				}
			}

							
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	return null;
	}	
	
	@Override
	protected void onPostExecute(Void p) {
		if (shouldCheck) {
			SharedPreferences.Editor editorP = sPreferences.edit();
			editorP.putBoolean("check_"+show_id, false);
			editorP.commit();
		}
			
	}
}
