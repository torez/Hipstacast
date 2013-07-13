package com.ifrins.hipstacast;

import java.io.File;
import java.io.IOException;

import android.app.PendingIntent;
import android.content.*;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Build;
import android.view.Surface;
import com.ifrins.hipstacast.HipstacastPlayerService.Preparation.PlayerStatus;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.remotecontrol.RemoteControlEventReceiver;
import com.ifrins.hipstacast.utils.HipstacastLogging;
import com.ifrins.hipstacast.utils.HipstacastUtils;
import com.ifrins.hipstacast.utils.PlayerCallbacks;

import android.app.Service;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;

public class HipstacastPlayerService extends Service {

    public final static String ACTION_PLAY = "com.ifrins.hipstacast.action.PLAY";
    public final static String ACTION_PAUSE = "com.ifrins.hipstacast.action.PAUSE";
	public final static String ACTION_TOGGLE = "com.ifrins.hipstacast.action.PLAY_PAUSE";

	AudioManager mAudioManager;
	Preparation mPreparation;
	MediaPlayer mPlayer;
	PlayerCallbacks mPlayerCallbacks;
	RemoteControlClient mRemoteControlClient;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
		HipstacastLogging.log("Broadcast receiver from service");
        if (intent.getAction().equals(HipstacastPlayerService.ACTION_PLAY)) {
            play();
        } else if (intent.getAction().equals(HipstacastPlayerService.ACTION_PAUSE)) {
            pause();
        } else if (intent.getAction().equals(HipstacastPlayerService.ACTION_TOGGLE)) {
	        if (HipstacastPlayerService.this.isPlaying()) {
		        pause();
	        } else {
		        play();
	        }
        }
        }
    };

	private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
		HipstacastLogging.log("FocusChange", focusChange);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
		                    RemoteControlEventReceiver.class.getName()));
		this.registerBroadcastReceiver();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	        this.registerRemoteControlClient();
        }

        mPlayer = new MediaPlayer();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		HipstacastLogging.log("onStartCommand");
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		this.unregisterReceiver(mReceiver);
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
		mPlayer.setOnCompletionListener(mOnCompletionListener);

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

		this.setRemoteControlDetails();
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
			mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN);

			mPlayer.start();
			this.setRemoteControlState(true);
		}
	}
	
	public void pause() {
		if (mPlayer != null && mPreparation.status == PlayerStatus.PREPARED) {
			mPlayer.pause();
			saveCurrentPosition();
			mAudioManager.abandonAudioFocus(mAudioFocusListener);
			this.setRemoteControlState(false);
		}
	}
	
	public void destroy() {
		mAudioManager.abandonAudioFocus(mAudioFocusListener);
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

	public void setPlayerSurface(Surface surface) {
		if (mPlayer != null) {
			mPlayer.setSurface(surface);
		}
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

	public String getSubscriptionName() {
		return  mPreparation.getSubscriptionName();
	}

	public int getSavedPosition() {
		return mPreparation.getSavedPosition() * 1000;
	}
	
	private OnPreparedListener mPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
		if (mp.getAudioSessionId() == mPreparation.episodeId) {
			mPreparation.status = PlayerStatus.PREPARED;
			seekTo(getSavedPosition());
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

	private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			setEpisodeListened();
		}
	};

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(HipstacastPlayerService.ACTION_PAUSE);
		filter.addAction(HipstacastPlayerService.ACTION_PLAY);
	    filter.addAction(HipstacastPlayerService.ACTION_TOGGLE);

	    registerReceiver(mReceiver, filter);
    }

	private void registerRemoteControlClient() {
		Intent mIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mIntent.setComponent(new ComponentName(getPackageName(),
				RemoteControlEventReceiver.class.getName()));
		mRemoteControlClient = new RemoteControlClient(PendingIntent.getBroadcast(this, 0, mIntent, 0));
		mAudioManager.registerRemoteControlClient(mRemoteControlClient);

		mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_BUFFERING);
		mRemoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE);
	}

	private void setRemoteControlDetails() {
		if (mRemoteControlClient != null) {
			mRemoteControlClient.editMetadata(true)
					.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, this.getEpisodeTitle())
					.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, this.getSubscriptionName())
					.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, this.getEpisodeDuration())
					.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
							BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_web))

					.apply();
		}
	}

	private void setRemoteControlState(boolean nowPlaying) {
		if (mRemoteControlClient != null && nowPlaying) {
			mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
		} else if (mRemoteControlClient != null && !nowPlaying) {
			mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
		}
	}

	private void saveCurrentPosition() {
		int currentPosition = this.getCurrentPosition();

		ContentValues mEpisodeUpdate = new ContentValues();
		mEpisodeUpdate.put(HipstacastProvider.EPISODE_CURRENT_POSITION, currentPosition / 1000);
		mEpisodeUpdate.put(HipstacastProvider.EPISODE_STATUS, HipstacastProvider.EPISODE_STATUS_STARTED);

		this.getContentResolver().update(HipstacastProvider.EPISODES_URI,
				mEpisodeUpdate,
				"_id = ?",
				new String[] { String.valueOf(mPreparation.episodeId) });
	}

	private void setEpisodeListened() {
		ContentValues mEpisodeUpdate = new ContentValues();
		mEpisodeUpdate.put(HipstacastProvider.EPISODE_CURRENT_POSITION, 0);
		mEpisodeUpdate.put(HipstacastProvider.EPISODE_STATUS, HipstacastProvider.EPISODE_STATUS_FINISHED);

		this.getContentResolver().update(HipstacastProvider.EPISODES_URI,
				mEpisodeUpdate,
				"_id = ?",
				new String[] { String.valueOf(mPreparation.episodeId) });
	}

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

		private Context context;
		
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
			this.context = context;
		}
		
		public String getPath() {
			HipstacastLogging.log("count", episodeCursor.getCount());
			episodeCursor.moveToFirst();
			String originalRemotePath = episodeCursor.getString(
					episodeCursor.getColumnIndex(HipstacastProvider.EPISODE_CONTENT_URL)
			);

			int downloaded = episodeCursor.getInt(episodeCursor.getColumnIndex(HipstacastProvider.EPISODE_DOWNLOADED));
			HipstacastLogging.log("Downloaded status", downloaded);
			if (downloaded == 0) {
				return originalRemotePath;
			} else {
				File localFile = new File(HipstacastUtils.getLocalUriForEpisodeId(this.context, this.episodeId).getPath());
				if (localFile.exists()) {
					HipstacastLogging.log("Using local path");
					return localFile.getPath();
				} else {
					return originalRemotePath;
				}
			}

		}
		
		public String getEpisodeTitle() {
			if (episodeCursor.getPosition() != 0) {
				episodeCursor.moveToFirst();
			}
			return episodeCursor.getString(episodeCursor.getColumnIndex(HipstacastProvider.EPISODE_TITLE));
		}

		public String getSubscriptionName() {
			if (episodeCursor.getPosition() != 0) {
				episodeCursor.moveToFirst();
			}
			int id = episodeCursor.getInt(episodeCursor.getColumnIndex(HipstacastProvider.EPISODE_PODCAST_ID));

			Cursor subscriptionCursor = context.getContentResolver().query(HipstacastProvider.SUBSCRIPTIONS_URI,
					new String[]{"_id", HipstacastProvider.PODCAST_TITLE},
					"_id = ?",
					new String[]{String.valueOf(id)},
					null);

			if (subscriptionCursor.getCount() > 0) {
				subscriptionCursor.moveToFirst();
				return  subscriptionCursor.getString(subscriptionCursor.getColumnIndex(HipstacastProvider.PODCAST_TITLE));
			} else {
				return "";
			}

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

            return fullPath;
		}

		public int getSavedPosition() {
			if (episodeCursor.getPosition() != 0) {
				episodeCursor.moveToFirst();
			}

			return episodeCursor.getInt(episodeCursor.getColumnIndex(HipstacastProvider.EPISODE_CURRENT_POSITION));
		}
	}

	
}
