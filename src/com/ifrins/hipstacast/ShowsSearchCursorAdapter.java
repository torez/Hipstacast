package com.ifrins.hipstacast;

import java.util.List;
import com.ifrins.hipstacast.model.Podcast;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowsSearchCursorAdapter extends ArrayAdapter<Object> {

	private Context ctx;
	private Object[] podcsts;

	public ShowsSearchCursorAdapter(Context context, Object[] podcasts) {
		super(context, R.layout.podcasts_list, podcasts);
		ctx = context;
		podcsts = podcasts;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.podcasts_list, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.podcastTitle);
		ImageView imageView = (ImageView) rowView
				.findViewById(R.id.podcastLogo);
		textView.setText(((Podcast) podcsts[position]).title);
		UrlImageViewHelper.setUrlDrawable(imageView,
				((Podcast) podcsts[position]).imageUrl);

		return rowView;
	}

}
