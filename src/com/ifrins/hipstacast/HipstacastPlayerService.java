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
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
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
	public int onStartCommand(Intent intent, int flags, int startId) {
		HipstacastLogging.log("onStartCommand");
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		HipstacastLogging.log("Destroying service");
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
		mPlayer = new MediaPlayer();
		mPlayer.setAudioSessionId(episodeId);
		mPlayer.setOnPreparedListener(mPreparedListener);
		mPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
		mPlayer.setOnErrorListener(mOnErrorListener);
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
		if (mPreparation == null || mPreparation.status == PlayerStatus.EMPTY) {
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
	
	public Boolean isPlaying(){
		if (mPlayer != null && mPreparation.status == PlayerStatus.PREPARED) {
			return mPlayer.isPlaying();
		} else {
			return false;
		}
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
	
	public void destroy() {
		if (mPlayer != null) {
			mPlayer.release();
			mPreparation = null;
		}
	}
	
	public void ff() {
		if (mPlayer != null) {
			mPlayer.seekTo(mPlayer.getCurrentPosition() + (20 * 1000));
		}
	}
	
	public void rewind() {
		if (mPlayer != null) {
			mPlayer.seekTo(mPlayer.getCurrentPosition() - (20 * 1000));
		}
	}
	
	public void seekTo(int position) {
		if (mPlayer != null) {
			mPlayer.seekTo(position);
		}
	}
	
	public int getCurrentPosition() {
		if (mPlayer != null) {
			return mPlayer.getCurrentPosition();
		}
		return -1;
	}
	
	public int getMPEpisodeDuration() {
		if (mPlayer != null) {
			return mPlayer.getDuration();
		}
		return -1;
	}
	
	// GET DATA
	
	public String getEpisodeTitle() {
		return mPreparation.getEpisodeTitle();
	}
	
	public String getCoverPath(Context context) {
		return mPreparation.getCoverPath(context);
	}
	
	public int getEpisodeDuration() {
		return mPreparation.getEpisodeDuration();
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
	
	private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
		
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int ratio) {
			mPlayerCallbacks.onBufferingUpdate(ratio);
			
			if (ratio == 100) {
				mp.setOnBufferingUpdateListener(null);
			}
		}
		
	};
	
	private OnErrorListener mOnErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer arg0, int what, int extra) {
			HipstacastLogging.log("what", what);
			HipstacastLogging.log("extra", extra);
			return false;
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
		
		public Preparation() {
		
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
		
		public int getSubscriptionId() {
			if (episodeCursor.getPosition() != 0) {
				episodeCursor.moveToFirst();
			}
			return episodeCursor.getInt(episodeCursor.getColumnIndex(HipstacastProvider.EPISODE_PODCAST_ID));
		}
		
		public int getEpisodeDuration() {
			if (episodeCursor.getPosition() != 0) {
				episodeCursor.moveToFirst();
			}
			return episodeCursor.getInt(episodeCursor.getColumnIndex(HipstacastProvider.EPISODE_DURATION));
		}
		
		public String getCoverPath(Context context) {
			Cursor subscription = context.getContentResolver().query(
									HipstacastProvider.SUBSCRIPTIONS_URI, 
									new String[] { "_id", HipstacastProvider.PODCAST_IMAGE}, 
									"_id = ?", 
									new String[] {String.valueOf(this.getSubscriptionId())}, 
									null);
			
			subscription.moveToFirst();

			String fullPath = subscription.getString(subscription.getColumnIndex(HipstacastProvider.PODCAST_IMAGE));
			subscription.close();
			String[] imagePath = fullPath.split("/");
			String imgName = imagePath[imagePath.length-1];
			
			if (imgName.length() > 3) {
				imgName = imgName.substring(0, imgName.length()-3) + "w.jpg";
				
				return android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/hipstacast/img/"+ imgName;
			}
			
			return null;
		}
	}

	
}
