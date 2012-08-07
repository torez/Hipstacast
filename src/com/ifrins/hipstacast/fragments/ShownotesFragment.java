package com.ifrins.hipstacast.fragments;

import com.ifrins.hipstacast.R;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ShownotesFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int episodeId = this.getArguments().getInt("episode_id");
		Cursor p = this.getActivity().getContentResolver()
				.query(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes"),
						new String[] { "_id", "shownotes" }, "_id = ?", new String[] { String.valueOf(episodeId) }, null);
		p.moveToFirst();
		View shownotesView = inflater.inflate(R.layout.shownotes_viewer, null);
		WebView webView = (WebView)shownotesView.findViewById(R.id.playerEpisodeDesc);
		webView.loadData(p.getString(p.getColumnIndex("shownotes")), "text/html; charset=UTF-8", null);
		p.close();
		return shownotesView;
	}
}
