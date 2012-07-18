package com.ifrins.hipstacast;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.htc.lockscreen.fusion.open.SimpleEngine;
import com.htc.lockscreen.fusion.open.SimpleIdleScreenService;
import com.ifrins.hipstacast.HipstacastPlayerService.LocalBinder;

public class HipstacastHTCLockScreenService extends SimpleIdleScreenService {

	@Override
	public SimpleEngine onCreateEngine() {
		// TODO Auto-generated method stub
		return null;
	}

	public class IdleScreenRemoteEngine extends SimpleEngine {
		Boolean bound;
		HipstacastPlayerService player;

		public IdleScreenRemoteEngine() {
		}

		public void onCreate(SurfaceHolder holder) {
			this.setContent(R.layout.lockscreen);
			Intent intent = new Intent(getApplicationContext(),
					HipstacastPlayerService.class);

			getApplicationContext().startService(intent);
			bindService(intent, mConnection, Context.BIND_DEBUG_UNBIND);

			// this.setContent(R.layout.lockscreen);
			// this.setShowLiveWallpaper(false);
			// this.setBackground(R.drawable.background);
		}

		public void onStart() {
		}

		public void onResume() {
		}

		public void onPause() {
		}

		public void onStop() {
		}

		public void onDestroy() {
			if (player != null && !player.isPlaying())
				player.destroy();
		}

		private void setupUI() {
			this.setContent(R.layout.lockscreen);
			Cursor p = getContentResolver()
					.query(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
							+ player.show_id + "/episodes"),
							new String[] { "_id", "title" }, "_id = ?",
							new String[] { String.valueOf(player.podcast_id) },
							null);
			p.moveToFirst();
			((TextView) findViewById(R.id.lockScreenPlaybackName)).setText(p
					.getString(p.getColumnIndex("title")));

		}

		public void startStopPlayToggle(Button v) {
			if (player.isPlaying()) {
				player.pause();
				v.setText(R.string.menu_play);
			} else if (!player.isPlaying()) {
				player.play();
				v.setText(R.string.menu_pause);
			}
		}

		private ServiceConnection mConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				Log.d("HIP_S", "Service connected");
				LocalBinder binder = (LocalBinder) service;
				player = binder.getService();
				bound = true;
				if (player.isPlaying())
					setupUI();
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				bound = false;
				Log.d("HIP_S", "Service disconnected");
			}
		};

	}

}
