package com.ifrins.hipstacast.fragments;

import java.io.File;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.*;

import com.ifrins.hipstacast.HipstacastDownloadsScheduler;
import com.ifrins.hipstacast.adapters.EpisodeListCursorAdapter;
import com.ifrins.hipstacast.EpisodePlayer;
import com.ifrins.hipstacast.Hipstacast;
import com.ifrins.hipstacast.HipstacastMain;
import com.ifrins.hipstacast.R;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.HipstacastLogging;


public class EpisodesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	int show_id;

	ListView.OnItemClickListener episodeClickListener = new ListView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				final int position, long id) {

			final Cursor c = (Cursor) EpisodesFragment.this.getListAdapter().getItem(position);
			final int episode_id = c.getInt(c.getColumnIndex("_id"));
			final int content_type = c.getInt(c.getColumnIndex("type"));
			
			Intent openIntent = new Intent(getActivity(), EpisodePlayer.class);
			openIntent.putExtra("episode_id", episode_id);
			openIntent.putExtra("type", content_type);
			startActivity(openIntent);

		}

	};
	
	EpisodeListCursorAdapter mAdapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new EpisodeListCursorAdapter(this.getActivity(), null);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		this.setListAdapter(mAdapter);
		this.setListShown(false);
		this.getListView().setOnItemClickListener(episodeClickListener);
		
		this.getLoaderManager().initLoader(0, null, this);
		
		show_id = this.getArguments().getInt("show_id");
		this.setHasOptionsMenu(true);
    	registerForContextMenu(this.getListView());


	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		this.getLoaderManager().restartLoader(0, null, this);
	}
	
    
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		Cursor c = (Cursor) getListView().getAdapter().getItem(info.position);
		menu.setHeaderTitle(c.getString(c.getColumnIndex("title")));

        int status = c.getInt(c.getColumnIndex(HipstacastProvider.EPISODE_STATUS));
        if (status != HipstacastProvider.EPISODE_STATUS_FINISHED) {
            menu.add(0, 1, 1, R.string.mark_as_listened);
        }

        menu.add(0, 2, 2, R.string.download);

	}

    @Override
    public boolean onContextItemSelected (MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        HipstacastLogging.log("onContextItemSelected");
        HipstacastLogging.log("Selected CI id", item.getItemId());

        switch (item.getItemId()) {
            case 2:
                Intent downloadSchedulingIntent = new Intent(
                        this.getActivity(),
                        HipstacastDownloadsScheduler.class
                );

                downloadSchedulingIntent.setAction(HipstacastDownloadsScheduler.ACTION_ADD_DOWNLOAD);

                downloadSchedulingIntent.putExtra(
                        HipstacastDownloadsScheduler.ACTION_ADD_DOWNLOAD_EPISODE_ID,
                        info.position
                );
                getActivity().startService(downloadSchedulingIntent);
            return true;

            default:
                return super.onContextItemSelected(item);
        }


    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor c = (Cursor) this.getListView().getAdapter().getItem(info.position);
		int pid = c.getInt(c.getColumnIndex("_id"));
        HipstacastLogging.log("OptionItemSelected");
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.episodes, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(this.getActivity(),
								Hipstacast.EPISODES_PROVIDER_URI,
								new String[] { "_id", "title",
										"duration", "podcast_id", "status", "position", "description",
										"content_url", "content_length", "publication_date",
										"type" },
								"podcast_id = ?",
								 new String[] { String.valueOf(show_id) }, 
								 "publication_date DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		mAdapter.swapCursor(newCursor);
		getActivity()
			.getActionBar()
			.setSubtitle(String.format(getActivity().getString(R.string.episodes_number), newCursor.getCount()));
		
		if (this.isResumed()) {
			this.setListShown(true);
		} else {
			this.setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
}
