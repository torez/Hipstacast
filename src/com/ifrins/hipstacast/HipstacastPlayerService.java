package com.ifrins.hipstacast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.ifrins.hipstacast.tasks.OnTaskCompleted;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;

public class HipstacastPlayerService extends Service implements
		AudioManager.OnAudioFocusChangeListener {
	public MediaPlayer mediaPlayer;
	AudioManager audioManager;
	public String podcastToPlayUrl;
	private final IBinder mBinder = new LocalBinder();
	public int start_position;
	public int show_id;
	public int podcast_id;
	public int type;
	public SurfaceHolder surface;
	public Notification n;
	public OnTaskCompleted completionListener = null;
	
	private final BroadcastReceiver brodcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
				Log.d("HIP-MUSIC", "Audio becoming noisy");
				if (mediaPlayer.isPlaying())
					mediaPlayer.stop();
				destroy();
			}
		}
	};

	private final OnCompletionListener onCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			mp.pause();
			stopForeground(true);
			if (completionListener != null) {
				completionListener.onTaskCompleted(Hipstacast.TASK_PLAYBACK_COMPLETED);
			}
		}
	};
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter fAudio = new IntentFilter(
				android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		this.registerReceiver(brodcastReceiver, fAudio);
		return startId;

	}

	public class LocalBinder extends Binder {
		public HipstacastPlayerService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return HipstacastPlayerService.this;
		}
	}

	public void initMediaPlayer() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		if (type == 1) {
			mediaPlayer.setDisplay(surface);
		}
		Log.d("HIP-URL", podcastToPlayUrl);
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(podcastToPlayUrl);
		} catch (FileNotFoundException e2) {
		}

		try {
			if (fileInputStream != null) {
				mediaPlayer.setDataSource(fileInputStream.getFD());
			}
		} catch (IllegalArgumentException e1) {
		} catch (SecurityException e1) {
		} catch (IllegalStateException e1) {
		} catch (IOException e1) {
		}
		try {
			fileInputStream.close();
		} catch (IOException e1) {
		}
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
		mediaPlayer.setOnCompletionListener(onCompletionListener);
	}

	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			if (mediaPlayer == null)
				initMediaPlayer();
			else if (!mediaPlayer.isPlaying())
				mediaPlayer.start();
			mediaPlayer.setVolume(1.0f, 1.0f);
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			if (mediaPlayer.isPlaying())
				mediaPlayer.stop();
			destroy();
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			if (mediaPlayer.isPlaying())
				mediaPlayer.pause();
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			if (mediaPlayer.isPlaying())
				mediaPlayer.setVolume(0.1f, 0.1f);
			break;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public boolean isPlaying() {
		if (mediaPlayer == null)
			return false;
		else
			return mediaPlayer.isPlaying();
	}

	public void stop() {
		if (mediaPlayer != null)
			mediaPlayer.pause();
	}

	public void play() {
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		int result = audioManager.requestAudioFocus(this,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			if (mediaPlayer == null)
				initMediaPlayer();
			if (n != null) { 
				startForeground(podcast_id, n);
			}
			mediaPlayer.seekTo(start_position);
			mediaPlayer.start();

		}
	}
	public void seekTo(int pos) {
		if (mediaPlayer != null)
			mediaPlayer.seekTo(pos);
	}
	public void pause() {
		stop();
		stopForeground(true);
	}

	public void destroy() {
		if (mediaPlayer != null) {
			ContentValues c = new ContentValues();
			c.put("position", (int) mediaPlayer.getCurrentPosition() / 1000);
			c.put("status", 2);
			getContentResolver()
					.update(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
							+ show_id + "/episodes/" + podcast_id), c,
							"_id = ?",
							new String[] { String.valueOf(podcast_id) });
			Log.d("HIP-PLAYER-DESTROY", "SHOULD BE DESTROYED");
			start_position = mediaPlayer.getCurrentPosition();
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}

	}

	public void clean() {
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer = null;
		}
	}
	public int getCurrentPosition() {
		if (mediaPlayer != null)
			return mediaPlayer.getCurrentPosition();
		else
			return -1;
	}
	public int getDuration() {
		if (mediaPlayer != null)
			return mediaPlayer.getDuration();
		else
			Log.d("HIP-PLAY", "getDuration() invalid value");
			return -1;

	}
}
