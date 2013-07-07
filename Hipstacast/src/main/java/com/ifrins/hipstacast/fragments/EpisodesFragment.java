package com.ifrins.hipstacast.fragments;

import android.content.ContentValues;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.*;

import com.ifrins.hipstacast.*;
import com.ifrins.hipstacast.adapters.EpisodeListCursorAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
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
        int downloaded = c.getInt(c.getColumnIndex(HipstacastProvider.EPISODE_DOWNLOADED));

        if (status != HipstacastProvider.EPISODE_STATUS_FINISHED) {
            menu.add(0, 1, 1, R.string.mark_as_listened);
        }

        if (downloaded != HipstacastProvider.EPISODE_STATUS_DOWNLOADED) {
            menu.add(0, 2, 2, R.string.download);
        } else {
            menu.add(0, 3, 3, R.string.delete);
        }

        menu.add(0, 4, 4, R.string.more_information);

	}

    @Override
    public boolean onContextItemSelected (MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        HipstacastLogging.log("onContextItemSelected");
        HipstacastLogging.log("Selected CI id", item.getItemId());
        Cursor c = (Cursor) this.getListAdapter().getItem(info.position);
        int episodeId = c.getInt(c.getColumnIndex("_id"));


        switch (item.getItemId()) {
            case 1:
                ContentValues listenedValues = new ContentValues();
                listenedValues.put(HipstacastProvider.EPISODE_STATUS, HipstacastProvider.EPISODE_STATUS_NOT_LISTENED);
                getActivity().getContentResolver().update(
                        HipstacastProvider.EPISODES_URI,
                        listenedValues,
                        "_id = ?",
                        new String[] { String.valueOf(episodeId) }
                );
                return true;

            case 2:
                Intent downloadSchedulingIntent = new Intent(this.getActivity(), HipstacastDownloadsScheduler.class);
                downloadSchedulingIntent.setAction(HipstacastDownloadsScheduler.ACTION_ADD_DOWNLOAD);


                downloadSchedulingIntent.putExtra(HipstacastDownloadsScheduler.EXTRA_EPISODE_ID, episodeId);
                getActivity().startService(downloadSchedulingIntent);
                return true;

            case 3:
                Intent removeIntent = new Intent(this.getActivity(), HipstacastDownloadsScheduler.class);
                removeIntent.setAction(HipstacastDownloadsScheduler.ACTION_REMOVE_DOWNLOAD);
                removeIntent.putExtra(HipstacastDownloadsScheduler.EXTRA_EPISODE_ID, episodeId);

                getActivity().startService(removeIntent);
	            return true;

            case 4:
                Intent infoIntent = new Intent(this.getActivity(), HipstacastSingleShownotes.class);
                infoIntent.putExtra(HipstacastSingleShownotes.EXTRA_EPISODE_ID, episodeId);
                getActivity().startActivity(infoIntent);
	            return true;

            default:
                return super.onContextItemSelected(item);
        }


    }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(this.getActivity(),
								HipstacastProvider.EPISODES_URI,
								HipstacastProvider.EPISODES_PLAYBACK_PROJECTION,
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
