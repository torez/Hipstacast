package com.ifrins.hipstacast.tasks;

import com.ifrins.hipstacast.model.Podcast;

import android.content.Context;

public interface OnSearchFinished {
	void onSearchFinished(Context context, Podcast[] results);
}
