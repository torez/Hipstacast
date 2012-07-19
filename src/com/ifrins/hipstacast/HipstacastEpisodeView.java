package com.ifrins.hipstacast;

import java.io.File;
import java.io.IOException;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class HipstacastEpisodeView extends ListActivity {
	int show_id;
	int episodes_count;

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Hipstacast)getApplicationContext()).trackPageView("/episodes");
        getActionBar().setTitle(getIntent().getExtras().getString("show_title"));
        show_id = Integer.parseInt(getIntent().getExtras().getString("show_id"));
        Cursor p = managedQuery(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"+getIntent().getExtras().getString("show_id")+"/episodes"), 
        						new String[] {"_id", "title", "duration", "podcast_id", "status", "position", "content_url", "content_length", "publication_date", "type"}, "podcast_id = ?", new String[] {getIntent().getExtras().getString("show_id")}, "publication_date DESC");
        episodes_count = p.getCount();
        setListAdapter(new EpisodeListCursorAdapter(getApplicationContext(), p));
		
		final ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		registerForContextMenu(listView);
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {

				 final Cursor c = (Cursor) getListAdapter().getItem(position);
				 int status = c.getInt(c.getColumnIndex("status"));
				 final int episode_id = c.getInt(c.getColumnIndex("_id"));
				 final int podcast_id = c.getInt(c.getColumnIndex("podcast_id"));
				 final String content_url = c.getString(c.getColumnIndex("content_url"));
				 final String title = c.getString(c.getColumnIndex("title"));
				 final long content_length = c.getLong(c.getColumnIndex("content_length"));
				 final int content_type = c.getInt(c.getColumnIndex("type"));
				 
				 File f = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.ifrins.hipstacast/files/shows/"+podcast_id+"/"+episode_id + ".mp3");
				 File _f = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.ifrins.hipstacast/files/shows/.nomedia");
				 if (!_f.exists()) {
					 try {
						_f.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 }
				 if (status == 0 && !f.exists()) {
		            	new AlertDialog.Builder(listView.getContext())
		                .setTitle(c.getString(c.getColumnIndex("title")))
		                .setMessage(String.format(getString(R.string.episode_not_downloaded), (content_length/1024)/1024))
		                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int whichButton) {
		                    	File d = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.ifrins.hipstacast/files/shows/"+podcast_id);
		                    	if(d.exists() == false) {
		        	                d.mkdirs();
		                    	}
		                    	
		                    	d = null;
		                    	DownloadManager mgr = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		                    	mgr.enqueue(new DownloadManager.Request(Uri.parse(content_url))
		                    				.setTitle(title)
		                    				.setDestinationInExternalFilesDir(getApplicationContext(), null, "shows/"+podcast_id+"/"+episode_id + ".mp3"));
		                    				
		                    }
		                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int whichButton) {
		                        // Do nothing.
		                    }
		                }).show();

				 }
				 else if (status == 0 && f.exists()) {
					 ContentValues up = new ContentValues();
					 up.put("status", 1);
					 getContentResolver().update(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"+getIntent().getExtras().getString("show_id")+"/episodes/"+episode_id), 
							 up, "_id = ?", new String[] {String.valueOf(episode_id)});
					 Intent openIntent = new Intent(getApplicationContext(), EpisodePlayer.class);
					 openIntent.putExtra("show_id", Integer.parseInt(getIntent().getExtras().getString("show_id")));
					 openIntent.putExtra("episode_id", episode_id);
					 openIntent.putExtra("type", content_type);
					 startActivity(openIntent);

				 }
				 
				 else {
					 Intent openIntent = new Intent(getApplicationContext(), EpisodePlayer.class);
					 openIntent.putExtra("show_id", Integer.parseInt(getIntent().getExtras().getString("show_id")));
					 openIntent.putExtra("episode_id", episode_id);
					 openIntent.putExtra("type", content_type);
					 startActivity(openIntent);
				 }
				 
			}

		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.episodes, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuEpisodeUnsubscribe:
			new UnsubscribeTask(this).execute(show_id);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = null;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		Cursor c = (Cursor) getListAdapter().getItem(info.position);
		menu.setHeaderTitle(c.getString(c.getColumnIndex("title")));
		if (episodes_count > 1 && info.position > 0
				&& c.getInt(c.getColumnIndex("string")) > 0) {
			menu.add(0, 1, 1, R.string.delete);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor c = (Cursor) getListAdapter().getItem(info.position);

		switch (item.getItemId()) {
		case 1:
			int pid = c.getInt(c.getColumnIndex("_id"));
			this.getContentResolver()
					.delete(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
							+ show_id + "/episodes"), "_id = ?",
							new String[] { String.valueOf(pid) });
			File f = new File(android.os.Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/Android/data/com.ifrins.hipstacast/files/shows/"
					+ show_id + "/" + pid + ".mp3");
			if (f.exists()) {
				f.delete();
				Intent i = new Intent(getApplicationContext(),
						HipstacastMain.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);

			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private class UnsubscribeTask extends AsyncTask<Integer, Void, Void> {
		ProgressDialog progressDialog;

		public UnsubscribeTask(Context c) {
			progressDialog = new ProgressDialog(c);
			progressDialog.setCancelable(false);
			progressDialog.setMessage(getString(R.string.unsubscribing));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setProgress(0);
			progressDialog.show();

		}

		@Override
		protected Void doInBackground(Integer... params) {
			int id = params[0];

			getApplicationContext()
					.getContentResolver()
					.delete(Uri
							.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts"),
							"_id = ?", new String[] { String.valueOf(id) });
			getApplicationContext()
					.getContentResolver()
					.delete(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
							+ id + "/episodes"), "podcast_id = ?",
							new String[] { String.valueOf(id) });
			File f = new File(android.os.Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/Android/data/com.ifrins.hipstacast/files/shows/" + id);
			if (f.isDirectory()) {
				String[] children = f.list();
				int len = children.length;
				for (int i = 0; i < len; i++) {
					new File(f, children[i]).delete();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void r) {
			progressDialog.dismiss();
			Intent i = new Intent(getApplicationContext(), HipstacastMain.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}

	}
}
