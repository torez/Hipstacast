package com.ifrins.hipstacast.fragments;

import com.actionbarsherlock.app.SherlockListFragment;
import com.ifrins.hipstacast.HipstacastSync;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.SubscriptionsSearchCursorAdapter;
import com.ifrins.hipstacast.model.Podcast;
import com.ifrins.hipstacast.tasks.ITunesStoreSearchTask;
import com.ifrins.hipstacast.tasks.OnSearchFinished;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
									String feedUrl = c.feed_link;
									
									Intent subscribeIntent = new Intent(SearchFragment.this.getActivity(), HipstacastSync.class);
									subscribeIntent.setAction(HipstacastSync.ACTION_SUBSCRIBE);
									subscribeIntent.putExtra("feedUrl", feedUrl);
									SearchFragment.this.getActivity().startService(subscribeIntent);
									Toast.makeText(SearchFragment.this.getActivity(), "Subscribing to " + c.title, Toast.LENGTH_LONG).show();
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
		public void onSearchFinished(Context context, Podcast[] results) {
			if (results != null && results.length > 0 && context != null) {
				mAdapter = new SubscriptionsSearchCursorAdapter(context, results);
				SearchFragment.this.setListAdapter(mAdapter);
			}
			
			SearchFragment.this.setListShown(true);
		}
	};
	
	SubscriptionsSearchCursorAdapter mAdapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		this.getListView().setOnItemClickListener(mListClickListener);
	}
	
	@Override
	public void onStart () {
		super.onStart();
		
		String query = this.getArguments().getString(SearchManager.QUERY);
		new ITunesStoreSearchTask(this.getActivity(), query, mSearchFinished).execute();
	}
	
}
