package com.ifrins.hipstacast;

import java.io.IOException;

import com.ifrins.hipstacast.model.PodcastEpisode;
import com.ifrins.hipstacast.tasks.OnTaskCompleted;
import com.ifrins.hipstacast.utils.PlayerCallbacks;
import com.ifrins.hipstacast.utils.PlayerUIUtils;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;

public class HipstacastPlayerService extends Service implements
		AudioManager.OnAudioFocusChangeListener {
	
	AudioManager audioManager;
	WakeLock mWakeLock;
	PodcastEpisode mPodcast;
	
	public MediaPlayer mediaPlayer;
	public String podcastToPlayUrl;
	private final IBinder mBinder = new LocalBinder();
	public int start_position;
	public SurfaceHolder surface;
	public Notification n;
	public OnTaskCompleted completionListener = null;
	
	private final BroadcastReceiver brodcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
				Log.d("HIP-MUSIC", "Audio becoming noisy");
				if (mediaPlayer != null && mediaPlayer.isPlaying())
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
	
	@Override
	public void onCreate() {
		super.onCreate();
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnCompletionListener(onCompletionListener);

		audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		
		//TODO: audioManager.registerMediaButtonEventReceiver(eventReceiver);
		
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
	}

	public void initMediaPlayer(PodcastEpisode podcast, PlayerCallbacks callback) {
		mPodcast = podcast;
		
		try {
			mediaPlayer.setDataSource(podcast.content_url);
			mediaPlayer.setOnPreparedListener(PlayerUIUtils.getOnPlayerPreparedListener(callback));
			mediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void onAudioFocusChange(int focusChange) {
		/*
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			if (mediaPlayer == null)
				//initMediaPlayer();
			else if (!mediaPlayer.isPlaying())
				mediaPlayer.start();
			mediaPlayer.setVolume(1.0f, 1.0f);
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			if (mediaPlayer != null && mediaPlayer.isPlaying())
				mediaPlayer.stop();
			destroy();
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			if (mediaPlayer != null && mediaPlayer.isPlaying())
				mediaPlayer.pause();
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			if (mediaPlayer != null && mediaPlayer.isPlaying())
				mediaPlayer.setVolume(0.1f, 0.1f);
			break;
		}
		*/
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public boolean isPlaying() {
		if (mediaPlayer == null) {
			return false;
		} else {
			return mediaPlayer.isPlaying();
		}
	}

	public void stop() {
		if (mediaPlayer != null)
			mediaPlayer.pause();
	}

	public void play() {
		mediaPlayer.start();
	}
	
	public void seekTo(int pos) {

	}
	
	public void pause() {
	
	}

	public void destroy() {
	
	}

	public void clean() {
	
	}
	
	public int getCurrentPosition() {
		if (mediaPlayer != null) {
			return mediaPlayer.getCurrentPosition();
		} else {
			return -1;
		}
	}
	
	public int getDuration() {
		if (mediaPlayer != null)
			return mediaPlayer.getDuration();
		else
			Log.d("HIP-PLAY", "getDuration() invalid value");
			return -1;

	}
}
