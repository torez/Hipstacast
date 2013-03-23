package com.ifrins.hipstacast.fragments;

import com.actionbarsherlock.app.SherlockListFragment;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.SubscriptionsSearchCursorAdapter;
import com.ifrins.hipstacast.model.Podcast;
import com.ifrins.hipstacast.tasks.AddPodcastProvider;
import com.ifrins.hipstacast.tasks.ITunesStoreSearchTask;
import com.ifrins.hipstacast.tasks.OnSearchFinished;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SearchFragment extends SherlockListFragment {
	
	ListView.OnItemClickListener mListClickListener = new ListView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, final View view,
				int position, long id) {
			final Context mContext = SearchFragment.this.getListView().getContext();
			
			final Podcast c = (Podcast) SearchFragment.this.getListAdapter().getItem(position);
			new AlertDialog.Builder(mContext)
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
									progressDialog = new ProgressDialog(mContext);
									progressDialog
											.setProgressStyle(ProgressDialog.STYLE_SPINNER);
									progressDialog
											.setMessage(getString(R.string.podcast_url_alert_add_fetching));
									progressDialog.setCancelable(false);
									progressDialog.show();
									Log.i("HIP-POD-URL", value);

									new AddPodcastProvider(mContext, null).execute(new String[]{value},
											progressDialog,
											mContext);
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

	};
	
	OnSearchFinished mSearchFinished = new OnSearchFinished() {

		@Override
		public void onSearchFinished(Object[] results) {
			mAdapter = new SubscriptionsSearchCursorAdapter(SearchFragment.this.getActivity(), results);
			SearchFragment.this.setListAdapter(mAdapter);
			
			SearchFragment.this.setListShown(true);
		}
	};
	
	SubscriptionsSearchCursorAdapter mAdapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setOnItemClickListener(mListClickListener);
				
		String query = this.getArguments().getString(SearchManager.QUERY);
		
		new ITunesStoreSearchTask(this.getActivity(), query, mSearchFinished).execute();
	}
	
}
