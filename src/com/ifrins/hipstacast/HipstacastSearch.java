package com.ifrins.hipstacast;


import com.ifrins.hipstacast.tasks.AddPodcastProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class HipstacastSearch extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.menuAddUrl:
            	final EditText input = new EditText(this);           
            	input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);

            	new AlertDialog.Builder(this)
                .setTitle(R.string.podcast_add_title)
                .setMessage(R.string.podcast_url_alert_add_msg)
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        
                        dialog.dismiss();
                        ProgressDialog progressDialog;
                        progressDialog = new ProgressDialog(input.getContext());
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setMessage(getString(R.string.podcast_url_alert_add_fetching));
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        Log.i("HIP-POD-URL", value);
                        
                        new AddPodcastProvider().execute(value, progressDialog);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();

        		return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
}
