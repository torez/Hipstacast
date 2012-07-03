package com.ifrins.hipstacast;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class HipstacastEpisodeView extends ListActivity {
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HIP-EP-ID", getIntent().getExtras().getString("show_id"));
        
        Cursor p = managedQuery(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"+getIntent().getExtras().getString("show_id")+"/episodes"), 
        						new String[] {"_id", "title", "duration", "podcast_id", "status", "position", "content_url", "content_length"}, "podcast_id = ?", new String[] {getIntent().getExtras().getString("show_id")}, null);
        //ActionBar a = getActionBar();
        
        setListAdapter(new EpisodeListCursorAdapter(getApplicationContext(), p));
		
		final ListView listView = getListView();
		listView.setTextFilterEnabled(true);
 
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
					 Toast.makeText(getApplicationContext(), "The file is there!", Toast.LENGTH_SHORT).show();
					 ContentValues up = new ContentValues();
					 up.put("status", 1);
					 int r = getContentResolver().update(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"+getIntent().getExtras().getString("show_id")+"/episodes/"+episode_id), 
							 up, "podcast_id = ?", new String[] {getIntent().getExtras().getString("show_id")});
					 Log.d("UPD-ROWS", Integer.valueOf(r).toString());
				 }
				 
				 else {
					 Intent openIntent = new Intent(getApplicationContext(), EpisodePlayer.class);
					 openIntent.putExtra("show_id", Integer.parseInt(getIntent().getExtras().getString("show_id")));
					 openIntent.putExtra("episode_id", episode_id);
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
