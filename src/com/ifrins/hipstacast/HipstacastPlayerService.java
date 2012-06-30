package com.ifrins.hipstacast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

public class HipstacastPlayerService extends Service implements AudioManager.OnAudioFocusChangeListener{
	MediaPlayer mediaPlayer;
	AudioManager audioManager;
	public String podcastToPlayUrl;
	private final IBinder mBinder = new LocalBinder();
	public int start_position;
	public int show_id;
	public int podcast_id; 
	public Notification n;
	
    public class LocalBinder extends Binder {
        HipstacastPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return HipstacastPlayerService.this;
        }
    }
    
	public void initMediaPlayer() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//Log.d("HIP-LOCAL-FILE", podcastToPlayUrl);
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(podcastToPlayUrl);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			mediaPlayer.setDataSource(fileInputStream.getFD());
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				if (mediaPlayer == null) initMediaPlayer();
				else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
				mediaPlayer.setVolume(1.0f, 1.0f);
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				if (mediaPlayer.isPlaying()) mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				if (mediaPlayer.isPlaying()) mediaPlayer.pause();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
				break;
		}
    }

	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void openFile(String path) {
		
	}
	
	public boolean isPlaying() {
		if (mediaPlayer == null) return false;
		else return mediaPlayer.isPlaying();
	}
	
	public void stop() {
		mediaPlayer.pause();
	}
	
	public void play() {
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
		    AudioManager.AUDIOFOCUS_GAIN);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			if (mediaPlayer == null) initMediaPlayer();
			
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			nm.notify(podcast_id, n); 
			mediaPlayer.seekTo(start_position);
		    mediaPlayer.start();
		    
		}
	}
	
	public void pause() {
		stop();
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(podcast_id);
	}
	
	public void seek(long pos) {
		
	}

}
