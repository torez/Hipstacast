package com.ifrins.hipstacast;

import android.app.ListActivity;
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

public class HipstacastMain extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Cursor p = managedQuery(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts"), new String[] {"_id", "title", "imageUrl"}, null, null, null);
        
        setListAdapter(new PodcastMainListCursorAdapter(getApplicationContext(), p));
		 
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				 Cursor c = (Cursor) getListAdapter().getItem(position);
				 Intent openIntent = new Intent(getApplicationContext(), HipstacastEpisodeView.class);
				 openIntent.putExtra("show_id", c.getString(c.getColumnIndex("_id")));
				 openIntent.putExtra("img_url", c.getString(c.getColumnIndex("imageUrl")));
				 startActivity(openIntent);
				 //c.close();
			}

		});

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.menuAdd:
        		Log.d("HIP-CLICK", "menuAdd");
        		Intent openIntent = new Intent(getApplicationContext(), HipstacastSearch.class);
        		startActivity(openIntent);
        		return true;
            /*case R.id.menuCompose:
                openComposer();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

