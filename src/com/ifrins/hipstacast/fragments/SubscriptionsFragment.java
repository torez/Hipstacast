package com.ifrins.hipstacast.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.ifrins.hipstacast.HipstacastEpisodeView;
import com.ifrins.hipstacast.PodcastMainListCursorAdapter;
import com.ifrins.hipstacast.provider.HipstacastProvider;

public class SubscriptionsFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	PodcastMainListCursorAdapter mAdapter;
	
	ListView.OnItemClickListener mListClickListener = new ListView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			Cursor c = (Cursor) parent.getAdapter().getItem(position);
			Intent openIntent = new Intent(SubscriptionsFragment.this.getActivity(),
					HipstacastEpisodeView.class);
			openIntent.putExtra("show_id",
					c.getString(c.getColumnIndex("_id")));
			openIntent.putExtra("img_url",
					c.getString(c.getColumnIndex("imageUrl")));
			openIntent.putExtra("show_title", c.getString(c.getColumnIndex("title")));
			startActivity(openIntent);
		}

	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new PodcastMainListCursorAdapter(this.getActivity(), null);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		this.setListAdapter(mAdapter);
		this.setListShown(false);
		
		this.getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		this.getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this.getActivity(), 
								HipstacastProvider.SUBSCRIPTIONS_URI, 
								new String[] { "_id", "title", "imageUrl", "author" },
								null,
								null,
								"title ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		mAdapter.swapCursor(newCursor);
		
		if (this.isResumed()) {
			this.setListShown(true);
		} else {
			this.setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}
}
