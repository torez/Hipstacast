package com.ifrins.hipstacast.model;

public class PodcastEpisode {
	public int id;
	public int podcast_id;
	public String guid;
	public long publicationDate;
	public String author;
	public String title;
	public String description;
	public String content_url;
	public double content_length;
	public int duration;
	public String image_url;
	public String donation_url;
	public int type;
	public String shownotes;

	public PodcastEpisode(int pos, int podcast_id, String guid,
			long publicationDate, String author, String title,
			String description, String content_url, double content_length,
			int duration, String donation_url, int type, String shownotes) {

		this.id = pos;
		this.podcast_id = podcast_id;
		this.guid = guid;
		this.publicationDate = publicationDate;
		this.author = author;
		this.title = title;
		this.description = description;
		this.content_url = content_url;
		this.content_length = content_length;
		this.duration = duration;
		this.donation_url = donation_url;
		this.type = type;
		this.shownotes = shownotes;

	}

	public PodcastEpisode() {
		// TODO Auto-generated constructor stub
	}
}
