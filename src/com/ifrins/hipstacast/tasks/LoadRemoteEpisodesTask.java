package com.ifrins.hipstacast.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.ifrins.hipstacast.RemoteEpisodesArrayAdapter;
import com.ifrins.hipstacast.model.PodcastEpisode;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

public class LoadRemoteEpisodesTask extends AsyncTask<Void, Integer, PodcastEpisode[]> {
	
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
	ListView listView;
	String feed;
	int show_id;
	int totalItems;
	ProgressBar progress;
	
	public LoadRemoteEpisodesTask(Context context, ListView listView, String feed, int show_id, ProgressBar progress) {
		this.context = context;
		this.listView = listView;
		this.feed = feed;
		this.show_id = show_id;
		this.progress = progress;
	}
	
	@Override
	protected PodcastEpisode[] doInBackground(Void... arg0) {
		List<PodcastEpisode> episodes = new ArrayList<PodcastEpisode>();

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
				this.publishProgress(i+1);
				String content_url = xpath.compile(String.format(MEDIALINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
				if (content_url.length() > 0) {
					String guid = xpath.compile(String.format(LINK_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					long pubDate = SyncUtils.convertTimeStrToTimestamp(xpath.compile(String.format(PUBDATE_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString());
					String author = xpath.compile(String.format(AUTHOR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					String title = xpath.compile(String.format(TITLE_ITEM_XPATH, i+1)).evaluate(doc,XPathConstants.STRING).toString();
					String description = xpath.compile(String.format(DESCR_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.STRING).toString();
					double content_length = (Double) xpath.compile(String.format(MEDIALENGHT_ITEM_XPATH, i+1)).evaluate(doc, XPathConstants.NUMBER);
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
					
					PodcastEpisode currentEpisode = new PodcastEpisode(i, show_id, guid, pubDate, author, title, description, content_url, content_length, duration,
																		donation_url, type, shownotes);
					episodes.add(currentEpisode);
				}
			}

							
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		return Arrays.copyOf(episodes.toArray(), episodes.toArray().length, PodcastEpisode[].class);
	}
	@Override 
	protected void onPostExecute(PodcastEpisode[] ep) {
		RemoteEpisodesArrayAdapter adapter = new RemoteEpisodesArrayAdapter(context, ep);
		adapter.notifyDataSetChanged();
		listView.setAdapter(adapter);
		progress.setVisibility(View.GONE);
		
	}
	@Override
	public void onProgressUpdate(Integer... p) {
		progress.setMax(totalItems);
		progress.setProgress(p[0]);
	}
	

}
