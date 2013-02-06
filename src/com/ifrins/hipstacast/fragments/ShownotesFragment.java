package com.ifrins.hipstacast.fragments;

import com.ifrins.hipstacast.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ShownotesFragment extends Fragment {
	
	private WebViewClient wVClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url) {
        	Log.d("HOP", url);
        	if (!url.contains("data:text/html") || !url.contains("https://hipstacast.appspot.com/api/ads") || !url.contains("htts://hipstacast.appspot.com/api/ads")) {
                if (view != null) {
                	Context context = view.getContext();
                	if (context != null)
                		context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    	view.stopLoading();
                    return true;
                }
        	}
        	return false;
        }
        
        @Override
        public void onLoadResource (WebView view, String url) {
            if (url.contains("http://a-ads.com/catalog")) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    view.stopLoading();
                    Log.i("RESLOAD", Uri.parse(url).toString());
            }
        }


	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int episodeId = this.getArguments().getInt("episode_id");
		Cursor p = this.getActivity().getContentResolver()
				.query(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes"),
						new String[] { "_id", "shownotes" }, "_id = ?", new String[] { String.valueOf(episodeId) }, null);
		p.moveToFirst();
		View shownotesView = inflater.inflate(R.layout.shownotes_viewer, null);
		WebView webView = (WebView)shownotesView.findViewById(R.id.playerEpisodeDesc);
		webView.setWebViewClient(wVClient);
		webView.getSettings().setJavaScriptEnabled(true);
		String data = p.getString(p.getColumnIndex("shownotes"));
		webView.loadDataWithBaseURL("http://hipstacast.appspot.com", data, "text/html", null, null);

		p.close();
		return shownotesView;
	}
}
