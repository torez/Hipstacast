package com.ifrins.hipstacast.model;

public class Podcast {
	public int id;
	public String title;
	public String link;
	public String feed_link;
	public String author;
	public String description;
	public String imageUrl;
	public String etag;
	public String etag_lastModified;
	public int lastCheck;
	
	public Podcast(String _title, String _feed_link, String _author, String _imageUrl) {
		title = _title;
		feed_link = _feed_link;
		author = _author;
		imageUrl = _imageUrl;
	}
	
	public Podcast() {
		
	}
	
	public String getFeed_link() {
		return feed_link;
	}

	public void setFeed_link(String feed_link) {
		this.feed_link = feed_link;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public int getLastCheck() {
		return lastCheck;
	}

	public void setLastCheck(int lastCheck) {
		this.lastCheck = lastCheck;
	}
	
	public String getEtagLastModified() {
		return etag_lastModified;
	}
	
	public void setEtagLastModified(String etag_lastModified) {
		this.etag_lastModified = etag_lastModified;
	}
}
