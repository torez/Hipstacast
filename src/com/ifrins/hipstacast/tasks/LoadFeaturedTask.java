package com.ifrins.hipstacast.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.SubscriptionsSearchCursorAdapter;
import com.ifrins.hipstacast.model.Podcast;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class LoadFeaturedTask extends AsyncTask<Void, Void, List<Podcast>> {

	private Context context = null;
	private ListView featuredList = null;
	private OnItemClickListener itemClickListener = null;
	
	public LoadFeaturedTask(Context ctx, ListView list, OnItemClickListener listener) {
		context = ctx;
		featuredList = list;
		itemClickListener = listener;
	}
	
	@Override
	protected List<Podcast> doInBackground(Void... arg0) {
		List<Podcast> presp = new ArrayList<Podcast>();
		String json = null;
		try {
			json = IOUtils.toString(context.getResources().openRawResource(R.raw.features));
		} catch (NotFoundException e) {
		} catch (IOException e) {
		}
		if (json != null) {
			try {
				JSONArray a = new JSONArray(json);
				for (int i = 0; i < a.length(); i++) {
					JSONObject c = a.getJSONObject(i);
					Podcast t = new Podcast(c.getString("name"),
							c.getString("feed"), c.getString("author"),
							c.getString("cover"));
					presp.add(t);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return presp;
	}
	
	@Override
	protected void onPostExecute(List<Podcast> podcastList) {
		featuredList.setOnItemClickListener(itemClickListener);
		featuredList.setAdapter(new SubscriptionsSearchCursorAdapter(context, podcastList.toArray()));
	}

}
