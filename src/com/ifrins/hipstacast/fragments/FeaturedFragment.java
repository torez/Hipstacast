package com.ifrins.hipstacast.fragments;

import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.model.Podcast;
import com.ifrins.hipstacast.tasks.AddPodcastProvider;
import com.ifrins.hipstacast.tasks.LoadFeaturedTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FeaturedFragment extends Fragment {

	ListView featuredList;
	Context context;
	OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0,
				final View featuredListView, int position, long id) {
			final Podcast c = (Podcast) featuredList.getAdapter().getItem(
					position);

			new AlertDialog.Builder(context)
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
									String value = c.feed_link;

									ProgressDialog progressDialog;
									progressDialog = new ProgressDialog(context);
									progressDialog
											.setProgressStyle(ProgressDialog.STYLE_SPINNER);
									progressDialog
											.setMessage(getString(R.string.podcast_url_alert_add_fetching));
									progressDialog.setCancelable(false);
									progressDialog.show();
									Log.i("HIP-POD-URL", value);

									new AddPodcastProvider().execute(
											new String[] { value },
											progressDialog,
											context);
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

	public FeaturedFragment(Context ctx) {
		context = ctx;
		featuredList = new ListView(ctx);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		new LoadFeaturedTask(context, featuredList, listener).execute();
		return featuredList;
	}

}
