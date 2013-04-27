package com.ifrins.hipstacast.parser.models;

public class PodcastItemEnclosure {
	public String url;
	public long length;
	public ENCLOSURE_MEDIA_TYPE enclosureType;
	
	public enum ENCLOSURE_MEDIA_TYPE {
	    AUDIO,VIDEO 
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setLength(long length) {
		this.length = length;
	}
	
	public void setType(String type) {
		this.enclosureType = ENCLOSURE_MEDIA_TYPE.AUDIO;
	}
}
