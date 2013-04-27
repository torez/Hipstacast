package com.ifrins.hipstacast.parser.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PodcastChannel {
	public String title;
	public String link;
	public String description;
	public PodcastImage image;
	public List<PodcastItem> items = new ArrayList<PodcastItem>();
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setPodcastImage(PodcastImage podcast_image) {
		this.image = podcast_image;
	}
	
	public void addPodcastItem(PodcastItem podcastItem) {
		items.add(podcastItem);
	}
	
	public Iterator<PodcastItem> iterator() {
		return items.iterator();
	}

	
}
