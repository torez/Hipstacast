package com.ifrins.hipstacast.fragments;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.ifrins.hipstacast.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ShownotesFragment extends Fragment {
	WebView shownotesView;
	
	private WebViewClient wVClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url) {
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
            if (url.contains("click.a-ads.com") || url.contains("a-ads.com/catalog")) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    view.stopLoading();
            }
        }


	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View _shownotesView = inflater.inflate(R.layout.shownotes_viewer, null);
		shownotesView = (WebView)_shownotesView.findViewById(R.id.playerEpisodeDesc);
		shownotesView.setWebViewClient(wVClient);
		
		new LoadShownotesTask().execute();
		return _shownotesView;
	}
	
	private class LoadShownotesTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... arg0) {
			int episodeId = ShownotesFragment.this.getArguments().getInt("episode_id");
			Cursor p = ShownotesFragment.this.getActivity().getContentResolver()
					.query(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes"),
							new String[] { "_id", "shownotes" }, "_id = ?", new String[] { String.valueOf(episodeId) }, null);
			p.moveToFirst();

			String shownotesData = p.getString(p.getColumnIndex("shownotes"));
			p.close();
			
			try {
				String template = IOUtils.toString(ShownotesFragment.this.getActivity().getAssets().open("index.html"));
				return template + shownotesData;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;

		}
		
		@Override
		protected void onPostExecute(String source) {
			shownotesView.loadDataWithBaseURL("http://hipstacast.appspot.com", source, "text/html", null, null);
		}
		
	}
}
