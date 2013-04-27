package com.ifrins.hipstacast.parser.models;

import java.util.Date;

import com.ifrins.hipstacast.parser.ParserUtils;

public class PodcastItem {
	public String title;
	public String link;
	public String guid;
	public Date pubdate;
	public long duration;
	public String description;
	public PodcastItemEnclosure enclosure; 
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public void setDuration(String duration) {
		this.duration = ParserUtils.convertDurationToSeconds(duration);
	}
	
	public void setPubDate(Date pubDate) {
		this.pubdate = pubDate;
	}
	
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setPodcastItemEnclosure(PodcastItemEnclosure enclosure) {
		this.enclosure = enclosure;
	}
}
