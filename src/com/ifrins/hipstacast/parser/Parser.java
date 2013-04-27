package com.ifrins.hipstacast.parser;

import static com.sjl.dsl4xml.SAXDocumentReader.attributes;
import static com.sjl.dsl4xml.SAXDocumentReader.mappingOf;
import static com.sjl.dsl4xml.SAXDocumentReader.tag;

import java.io.InputStream;

import com.ifrins.hipstacast.parser.models.PodcastChannel;
import com.ifrins.hipstacast.parser.models.PodcastImage;
import com.ifrins.hipstacast.parser.models.PodcastItem;
import com.ifrins.hipstacast.parser.models.PodcastItemEnclosure;
import com.ifrins.hipstacast.parser.models.PodcastRss;
import com.sjl.dsl4xml.DocumentReader;

public class Parser {
	private static DocumentReader<PodcastRss> reader;

	public Parser() {
		reader = mappingOf("rss", PodcastRss.class).to(
				tag("channel", PodcastChannel.class).with(
					tag("title"),
					tag("link"),
					tag("description"),
					tag("itunes:image", PodcastImage.class).with(attributes("href")),
					tag("item", PodcastItem.class).with( 
						tag("title"),
						tag("link"),
						tag("pubDate"),
						tag("itunes:duration").withPCDataMappedTo("duration"),
						tag("description"),
						tag("enclosure", PodcastItemEnclosure.class).with(
							attributes("url", "length", "type")
						)
					)
				)
			);
		reader.registerConverters(new ThreadSafeDateLocaleConverter("EEE, dd MMM yyyy HH:mm:ss Z"));
	}
	
	public PodcastRss parse(InputStream in) {
		return reader.read(in, "utf-8");
	}
}
