package com.ifrins.hipstacast.model;

public class Podcast {
	public int id;
	public String title;
	public String link;
	public String feed_link;
	public String author;
	public String description;
	public String imageUrl;
	
	public Podcast(String _title, String _feed_link, String _author, String _imageUrl) {
		title = _title;
		feed_link = _feed_link;
		author = _author;
		imageUrl = _imageUrl;
	}
}
