package com.ifrins.hipstacast.model;

public class Podcast {
	public int id;
	public String title;
	public String link;
	public String feed_link;
	public String author;
	public String description;
	public String imageUrl;
	
	public Podcast(int _id, String _title, String _link, String _feed_link, String _author, String _description, String _imageUrl) {
		id = _id;
		title = _title;
		link = _link;
		feed_link = _feed_link;
		author = _author;
		description = _description;
		imageUrl = _imageUrl;
	}
}
