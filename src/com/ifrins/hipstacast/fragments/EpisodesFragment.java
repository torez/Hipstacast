package com.ifrins.hipstacast.fragments;

import java.io.File;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.ifrins.hipstacast.adapters.EpisodeListCursorAdapter;
import com.ifrins.hipstacast.EpisodePlayer;
import com.ifrins.hipstacast.Hipstacast;
import com.ifrins.hipstacast.HipstacastMain;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.utils.PlayerUIUtils;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;


public class EpisodesFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	int show_id;
	ListView episodesListView = null;
	
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
		/* AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		Cursor c = (Cursor) episodesListView.getAdapter().getItem(info.position);
		menu.setHeaderTitle(c.getString(c.getColumnIndex("title")));
		menu.add(0, 1, 1, R.string.delete);
		int status = c.getInt(c.getColumnIndex(HipstacastProvider.EPISODE_STATUS));
		Log.d("HIP-STATUS", String.valueOf(status));
		if (status != HipstacastProvider.EPISODE_STATUS_FINISHED) {
			menu.add(0, 2, 2, R.string.mark_as_listened);
		} */

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor c = (Cursor) episodesListView.getAdapter().getItem(info.position);
		int pid = c.getInt(c.getColumnIndex("_id"));

		switch (item.getItemId()) {
		case 1:
			getActivity().getContentResolver()
					.delete(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
							+ show_id + "/episodes"), "_id = ?",
							new String[] { String.valueOf(pid) });
			File f = new File(android.os.Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/Android/data/com.ifrins.hipstacast/files/shows/"
					+ show_id + "/" + pid + ".mp3");
			if (f.exists()) {
				f.delete();
				Intent i = new Intent(getActivity(),
						HipstacastMain.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);

			}
			return true;
		case 2:
			PlayerUIUtils.markAsListenedAndUpdate(getActivity(), pid, episodesListView);
			return true;
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
		getSherlockActivity()
			.getSupportActionBar()
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
