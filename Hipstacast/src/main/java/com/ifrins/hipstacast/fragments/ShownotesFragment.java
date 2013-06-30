package com.ifrins.hipstacast.fragments;

import java.io.IOException;

import android.support.v4.app.Fragment;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import org.apache.commons.io.IOUtils;

import com.ifrins.hipstacast.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ShownotesFragment extends Fragment {
    public static final String EXTRA_EPISODE_ID = "episode_id";

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
			int episodeId = ShownotesFragment.this.getArguments().getInt(EXTRA_EPISODE_ID);
			Cursor p = getActivity().getContentResolver().query(
                    HipstacastProvider.EPISODES_URI,
					new String[] { "_id", HipstacastProvider.EPISODE_DESCRIPTION},
                    "_id = ?",
                    new String[] { String.valueOf(episodeId) },
                    null
            );
			p.moveToFirst();

			String shownotesData = p.getString(p.getColumnIndex(HipstacastProvider.EPISODE_DESCRIPTION));
			p.close();
			
			try {
				String template = IOUtils.toString(getActivity().getAssets().open("index.html"));
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
