package com.ifrins.hipstacast.utils;

public interface PlayerCallbacks {
	public void onPrepared();
	public void onBufferingUpdate(int progress);
}
