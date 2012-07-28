package com.ifrins.hipstacast.fragments;

import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.model.Podcast;
import com.ifrins.hipstacast.tasks.AddPodcastProvider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.search, null);
    	final ListView searchListView = (ListView)v.findViewById(R.id.searchListView);
    	searchListView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				
				final Podcast c = (Podcast) searchListView.getAdapter().getItem(position);
				new AlertDialog.Builder(searchListView.getContext())
						.setTitle(R.string.subscribe)
						.setMessage(
								String.format(
										getString(R.string.podcast_subscribe),
										c.title))
						.setPositiveButton(R.string.subscribe,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
										String value = c.feed_link;

										ProgressDialog progressDialog;
										progressDialog = new ProgressDialog(searchListView.getContext());
										progressDialog
												.setProgressStyle(ProgressDialog.STYLE_SPINNER);
										progressDialog
												.setMessage(getString(R.string.podcast_url_alert_add_fetching));
										progressDialog.setCancelable(false);
										progressDialog.show();
										Log.i("HIP-POD-URL", value);

										new AddPodcastProvider().execute(new String[]{value},
												progressDialog,
												searchListView.getContext());
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										// Do nothing.
									}
								}).show();

			}

		});
    	return v;
    }

}
