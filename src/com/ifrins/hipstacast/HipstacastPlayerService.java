package com.ifrins.hipstacast;


import java.io.IOException;

import com.ifrins.hipstacast.HipstacastPlayerService.Preparation.PlayerStatus;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.HipstacastLogging;
import com.ifrins.hipstacast.utils.PlayerCallbacks;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;

public class HipstacastPlayerService extends Service {

	AudioManager mAudioManager;
	Preparation mPreparation;
	MediaPlayer mPlayer;
	PlayerCallbacks mPlayerCallbacks;
	
	@Override
	public void onCreate() {
		super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //TODO: mAudioManager.registerMediaButtonEventReceiver(rec);
        //TODO: mAudioManager.registerRemoteControlClient(rcClient);
        
        mPlayer = new MediaPlayer();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder();
	}
	
	public void registerForCallbacks(PlayerCallbacks mPlayerCallbacks) {
		this.mPlayerCallbacks = mPlayerCallbacks;
	}

	public void prepare(int episodeId) {
		mPreparation = new Preparation(this, episodeId);
		mPlayer.reset();
		mPlayer.setAudioSessionId(episodeId);
		mPlayer.setOnPreparedListener(mPreparedListener);
		try {
			mPlayer.setDataSource(mPreparation.getPath());
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
		mPlayerCallbacks.onStartDoingUIWork();
		mPlayer.prepareAsync();
	}
	
	public void recover() {
		if (this.isAlreadyPrepared()) {
			mPlayerCallbacks.onStartDoingUIWork();
			mPlayerCallbacks.onPrepared();
		}
	}
	
	
	public Boolean isAlreadyPrepared() {
		if (mPreparation == null) {
			return false;
		}
		return true;
	}
	
	public Boolean isAlreadyPrepared(int episodeId) {
		if (mPreparation != null && mPreparation.episodeId == episodeId) {
			return true;
		}
		return false;
	}
	
	// PLAYER CONTROLS 
	
	public void play() {
		if (mPlayer != null && mPreparation.status == PlayerStatus.PREPARED) {
			mPlayer.start();
		}
	}
	
	public void pause() {
		if (mPlayer != null && mPreparation.status == PlayerStatus.PREPARED) {
			mPlayer.pause();
		}
	}
	
	// GET DATA
	
	public String getEpisodeTitle() {
		return mPreparation.getEpisodeTitle();
	}
	
	private OnPreparedListener mPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			if (mp.getAudioSessionId() == mPreparation.episodeId) {
				mPreparation.status = PlayerStatus.PREPARED;
				mPlayerCallbacks.onPrepared();
			}
			
		}
		
	};

	// OTHER CLASSES
	
	public class LocalBinder extends Binder {
		public HipstacastPlayerService getService() {
			return HipstacastPlayerService.this;
		}
	}

	public static class Preparation {
		public PlayerStatus status = PlayerStatus.EMPTY;
		public int episodeId = -1;
		public Cursor episodeCursor;
		
		public enum PlayerStatus {
			EMPTY,
			PREPARED
		}
		
		public Preparation(Context context, int episodeId) {
			HipstacastLogging.log("episode_id", episodeId);
			this.episodeId = episodeId;
			this.episodeCursor = context.getContentResolver()
											.query(HipstacastProvider.EPISODES_URI, 
													HipstacastProvider.EPISODES_PLAYBACK_PROJECTION, 
													"_id = ?", 
													new String[] {String.valueOf(episodeId)}, 
													null);
		}
		
		public String getPath() {
			HipstacastLogging.log("count", episodeCursor.getCount());
			episodeCursor.moveToFirst();
			return episodeCursor.getString(episodeCursor.getColumnIndex(HipstacastProvider.EPISODE_CONTENT_URL));
		}
		
		public String getEpisodeTitle() {
			if (episodeCursor.getPosition() != 0) {
				episodeCursor.moveToFirst();
			}
			return episodeCursor.getString(episodeCursor.getColumnIndex(HipstacastProvider.EPISODE_TITLE));
		}
	}

	
}
