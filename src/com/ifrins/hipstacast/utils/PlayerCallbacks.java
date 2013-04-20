package com.ifrins.hipstacast.utils;

public interface PlayerCallbacks {
	public void onStartDoingUIWork();
	public void onPrepared();
	public void onBufferingUpdate(int progress);
}
