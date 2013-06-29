package com.ifrins.hipstacast.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import com.ifrins.hipstacast.HipstacastEpisodeView;
import com.ifrins.hipstacast.R;

/**
 * Created by francesc on 28/06/13.
 */
public class HipstacastSubscriptionsWidgetProvider extends AppWidgetProvider {
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];

			Intent widgetServiceIntent = new Intent(context, HipstacastSubscriptionsWidgetService.class);
			widgetServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			widgetServiceIntent.setData(Uri.parse(widgetServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.subscriptions_widget);

			remoteViews.setRemoteAdapter(R.id.stack_view, widgetServiceIntent);
			remoteViews.setEmptyView(R.id.stack_view, R.id.empty_view);

			Intent openSubscriptionIntent = new Intent(context, HipstacastEpisodeView.class);
			PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openSubscriptionIntent, 0);
			remoteViews.setPendingIntentTemplate(R.id.stack_view, openPendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}
	}

}
