package com.ifrins.hipstacast.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.crashlytics.android.Crashlytics;
import com.ifrins.hipstacast.HipstacastEpisodeView;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by francesc on 28/06/13.
 */
public class HipstacastSubscriptionsWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
	}

	class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

		private Context mContext;
		private int mAppWidgetId;
		private Cursor mSubscriptionsCursor;

		public StackRemoteViewsFactory(Context context, Intent intent) {
			mContext = context;
			mAppWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID
			);
		}

		public void onCreate() {
			mSubscriptionsCursor = mContext.getContentResolver().query(
					HipstacastProvider.SUBSCRIPTIONS_URI,
					HipstacastProvider.SUBSCRIPTIONS_DEFAULT_PROJECTION,
					null,
					null,
					null
			);
		}

		@Override
		public void onDataSetChanged() {

		}

		@Override
		public void onDestroy() {

		}

		@Override
		public int getCount() {
			return mSubscriptionsCursor.getCount();
		}

		public RemoteViews getViewAt(int position) {
			mSubscriptionsCursor.moveToPosition(position);
			final RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.subscriptions_widget_item);
			Bitmap b = null;
			try {
				b = Picasso
						.with(mContext)
						.load(mSubscriptionsCursor.getString(
								mSubscriptionsCursor.getColumnIndex(HipstacastProvider.PODCAST_IMAGE)
						))
						.get();
			} catch (IOException e) {
				Crashlytics.logException(e);
			}
			if (b != null) {
				rv.setImageViewBitmap(R.id.subscription_cover_image, b);
			}

			Bundle extras = new Bundle();
			extras.putInt(
					HipstacastEpisodeView.EXTRA_SUBSCRIPTION_ID,
					mSubscriptionsCursor.getInt(mSubscriptionsCursor.getColumnIndex("_id"))
			);
			extras.putString(
					HipstacastEpisodeView.EXTRA_SUBSCRIPTION_TITLE,
					mSubscriptionsCursor.getString(
							mSubscriptionsCursor.getColumnIndex(HipstacastProvider.PODCAST_TITLE)
					)
			);
			Intent fillClickIntent = new Intent();
			fillClickIntent.putExtras(extras);
			rv.setOnClickFillInIntent(R.id.subscription_cover_image, fillClickIntent);

			return rv;
		}

		@Override
		public RemoteViews getLoadingView() {
			return null;
		}

		@Override
		public int getViewTypeCount() {
			return 0;
		}

		@Override
		public long getItemId(int i) {
			mSubscriptionsCursor.moveToPosition(i);
			return mSubscriptionsCursor.getInt(mSubscriptionsCursor.getColumnIndex("_id"));
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}
}