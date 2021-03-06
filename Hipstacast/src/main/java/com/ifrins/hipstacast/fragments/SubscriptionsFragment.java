package com.ifrins.hipstacast.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import android.widget.Toast;
import com.ifrins.hipstacast.HipstacastEpisodeView;
import com.ifrins.hipstacast.HipstacastSync;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.adapters.SubscriptionsCursorAdapter;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.HipstacastLogging;

public class SubscriptionsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	SubscriptionsCursorAdapter mAdapter;
	
	ListView.OnItemClickListener mListClickListener = new ListView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			Cursor c = (Cursor) parent.getAdapter().getItem(position);
			Intent openIntent = new Intent(SubscriptionsFragment.this.getActivity(),
					HipstacastEpisodeView.class);
			openIntent.putExtra("show_id",
					c.getInt(c.getColumnIndex("_id")));
			openIntent.putExtra("img_url",
					c.getString(c.getColumnIndex("imageUrl")));
			openIntent.putExtra("show_title", c.getString(c.getColumnIndex("title")));
			startActivity(openIntent);
		}

	};
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new SubscriptionsCursorAdapter(this.getActivity(), null);
		this.registerForContextMenu(this.getListView());
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		this.setListAdapter(mAdapter);
		this.setListShown(false);
		this.getListView().setOnItemClickListener(mListClickListener);
		
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
								HipstacastProvider.SUBSCRIPTIONS_DEFAULT_COUNT_PROJECTION,
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.subscriptions_contextmenu, menu);
		
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor selectedSubscription = (Cursor) this.getListView().getItemAtPosition(info.position);
		int show_id = selectedSubscription.getInt(selectedSubscription.getColumnIndex("_id"));
		String feedUrl = selectedSubscription.getString(
				selectedSubscription.getColumnIndex(HipstacastProvider.PODCAST_FEED)
		);
	    
	    switch(item.getItemId()) {
	    	case R.id.menu_delete_subscription:
			    Intent unsubscriptionIntent = new Intent(this.getActivity(), HipstacastSync.class);
			    unsubscriptionIntent.setAction(HipstacastSync.ACTION_UNSUBSCRIBE);
			    unsubscriptionIntent.putExtra(HipstacastSync.EXTRA_UNSUBSCRIPTION_ID, show_id);
			    this.getActivity().startService(unsubscriptionIntent);
	    		return true;
	    	case R.id.menu_report_subscription:
			    HipstacastLogging.reportFeedError(feedUrl);
			    Toast.makeText(this.getActivity(), R.string.thanks_feed_report, Toast.LENGTH_LONG).show();
			    return true;
	    	default:
	    		return super.onContextItemSelected(item);
	    }
	}

}
