package com.ifrins.hipstacast.fragments;

import com.ifrins.hipstacast.Hipstacast;
import com.ifrins.hipstacast.HipstacastPlayerService;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.HipstacastPlayerService.LocalBinder;
import com.ifrins.hipstacast.model.PodcastEpisode;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.tasks.OnTaskCompleted;
import com.ifrins.hipstacast.utils.PlayerCallbacks;
import com.ifrins.hipstacast.utils.PlayerUIUtils;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerFragment extends Fragment {

	int podcast_id;
	int show_id;
	HipstacastPlayerService player;
	Boolean bound = false;
	Boolean fromNotification = false;
	int start_position;
	int duration;
	Boolean complete = false;
	Handler seekBarUpdateHandler = new Handler();
	SeekBar seekBar = null;
	TextView timeCounter = null;
	ImageButton playToggleButton = null;
	Boolean showingControls = true;
	String name;
	String websiteLink;
	String donationLink;
	int type;
	
	PodcastEpisode mPodcast = new PodcastEpisode();
	
	PlayerCallbacks mPlayerCallbacks = new PlayerCallbacks() {

		@Override
		public void onPrepared() {
			PlayerFragment.this.getView()
				.findViewById(R.id.playerControls)
				.setVisibility(View.VISIBLE);
		}
		
	};
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("HIP_S", "Service connected");
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			player = binder.getService();
			bound = true;
			
			if (player != null) {
				player.initMediaPlayer(mPodcast, mPlayerCallbacks);
			}
			
			if (player != null && player.isPlaying()) {
				playToggleButton.setImageResource(R.drawable.ic_action_pause);
				seekBarUpdateHandler.post(updateRunnable);
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
			Log.d("HIP_S", "Service disconnected");
		}
	};

	private OnClickListener playToggleClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (player != null && !player.isPlaying()) {
				((ImageButton)v).setImageResource(R.drawable.ic_action_pause);
				startPlaying();
			} else if (player != null && player.isPlaying()) {
				((ImageButton)v).setImageResource(R.drawable.ic_action_play);
				stopPlaying();
			}
		}
	};
	
	private OnClickListener fastForwardClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (player != null && player.isPlaying()) {
				int newPosition = player.getCurrentPosition() + 30000;
				player.seekTo(newPosition);
				seekBar.setProgress(newPosition);
				timeCounter.setText(PlayerUIUtils.convertSecondsToDuration(newPosition/1000));
			}
		}
	};
	
	private OnClickListener rewindClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (player != null && player.isPlaying()) {
				int newPosition = player.getCurrentPosition() - 30000;
				player.seekTo(newPosition);
				seekBar.setProgress(newPosition);
				timeCounter.setText(PlayerUIUtils.convertSecondsToDuration(newPosition/1000));
			}
		}
	};
	
	private OnClickListener pictureClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (player != null && player.isPlaying()) {
				if (showingControls) {
					getView().findViewById(R.id.playerEpisodeName).setSelected(false);
					getView().findViewById(R.id.playerDetails).animate().alpha(0);
					seekBarUpdateHandler.removeCallbacks(updateRunnable);
					showingControls = false;
				} else {
					getView().findViewById(R.id.playerDetails).animate().alpha(1);
					getView().findViewById(R.id.playerEpisodeName).setSelected(true);
					seekBarUpdateHandler.post(updateRunnable);
					showingControls = true;
				}
			}
		}
	};
	
	private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
			if (fromUser && player != null) {
				player.seekTo(progress);
				timeCounter.setText(PlayerUIUtils.convertSecondsToDuration(progress/1000));
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		
	};
	
	public OnTaskCompleted completionListener = new OnTaskCompleted() {

		@Override
		public void onTaskCompleted(String task) {
			Log.d("HIP-TASK", task);
			if (task.equals(Hipstacast.TASK_PLAYBACK_COMPLETED)) {
				playToggleButton.setImageResource(R.drawable.ic_action_play);
				seekBarUpdateHandler.removeCallbacks(updateRunnable);
				seekBar.setProgress(player.getDuration());
				timeCounter.setText(PlayerUIUtils.convertSecondsToDuration(player.getDuration()/1000));
				start_position = 0;
				player.destroy();
				PlayerUIUtils.setEpisodeAsListened(getActivity(), podcast_id);
			} else if (task.equals(Hipstacast.TASK_OPEN_WEBPAGE)) {
				if (websiteLink.length() > 0) {
					Intent openIntent = new Intent(Intent.ACTION_VIEW);
					openIntent.setData(Uri.parse(websiteLink));
					startActivity(openIntent);
				}
			} else if (task.equals(Hipstacast.TASK_OPEN_DONATIONS)) {
				if (donationLink.length() > 0) {
					Intent donateIntent = new Intent(Intent.ACTION_VIEW);
					donateIntent.setData(Uri.parse(donationLink));
					startActivity(donateIntent);
				}
			} else if (task.equals(Hipstacast.TASK_SHARE)) {

					Intent sharingIntent = new Intent(Intent.ACTION_SEND);
					sharingIntent.setType("text/plain");
					sharingIntent.putExtra(
							android.content.Intent.EXTRA_TEXT,
							String.format(getString(R.string.share_text), name + " - "
									+ websiteLink));
					startActivity(Intent.createChooser(sharingIntent,
							getString(R.string.share)));

			}
		}
		
	};
	
	
	final Runnable updateRunnable = new Runnable()
	{
	    public void run() 
	    {
	        if (player.isPlaying()) {
	        	seekBar.setProgress(player.getCurrentPosition());
	        	timeCounter.setText(PlayerUIUtils.convertSecondsToDuration(player.getCurrentPosition()/1000));
	        	seekBarUpdateHandler.postDelayed(updateRunnable, 1100);
	        }
	    }
	};

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(this.getActivity(), HipstacastPlayerService.class);

		this.getActivity().startService(intent);
		this.getActivity().bindService(intent, mConnection, Context.BIND_DEBUG_UNBIND);
		
		podcast_id = this.getArguments().getInt("podcast_id");
		Cursor episode = getEpisodeDetails();
		episode.moveToFirst();
		show_id = episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_PODCAST_ID));
		//name = episode.getString(episode.getColumnIndex(HipstacastProvider.EPISODE_TITLE));
		//type = episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_TYPE));
		websiteLink = episode.getString(episode.getColumnIndex("guid"));
		donationLink = episode.getString(episode.getColumnIndex(HipstacastProvider.EPISODE_DONATION));
		
		mPodcast.podcast_id = podcast_id;
		mPodcast.title = episode.getString(episode.getColumnIndex(HipstacastProvider.EPISODE_TITLE));
		mPodcast.type = episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_TYPE));
		mPodcast.content_url = episode.getString(episode.getColumnIndex(HipstacastProvider.EPISODE_CONTENT_URL));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View finalView = inflater.inflate(R.layout.player, null);
		Cursor episode = getEpisodeDetails();
		episode.moveToFirst();
		show_id = episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_PODCAST_ID));
		name = episode.getString(episode.getColumnIndex(HipstacastProvider.EPISODE_TITLE));
		type = episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_TYPE));
		websiteLink = episode.getString(episode.getColumnIndex("guid"));
		donationLink = episode.getString(episode.getColumnIndex(HipstacastProvider.EPISODE_DONATION));
		
		TextView episodeTitle = (TextView)finalView.findViewById(R.id.playerEpisodeName);
		episodeTitle.setText(episode.getString(episode.getColumnIndex(HipstacastProvider.EPISODE_TITLE)));
		episodeTitle.setSelected(true);
		
		ImageView coverImage = (ImageView)finalView.findViewById(R.id.playerCoverImage);
		Uri coverImageUri = getImageUri(episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_PODCAST_ID)));
		if (coverImageUri != null) {
			coverImage.setImageURI(coverImageUri);
		}
		coverImage.setOnClickListener(pictureClickListener);

		
		TextView totalDuration = (TextView)finalView.findViewById(R.id.playerTotalDuration);
		duration = episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_DURATION));
		totalDuration.setText(PlayerUIUtils.convertSecondsToDuration(duration));
		
		seekBar = (SeekBar)finalView.findViewById(R.id.playerSeekBar);
		seekBar.setMax(episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_DURATION))*1000);
		seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
		
		int currentPosition = episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_CURRENT_POSITION));
		timeCounter = (TextView)finalView.findViewById(R.id.timeCounter);

		if (currentPosition > 0) {
			timeCounter.setText(PlayerUIUtils.convertSecondsToDuration(currentPosition));
			seekBar.setProgress(currentPosition * 1000);
			start_position = currentPosition * 1000;
		}
		
		playToggleButton = (ImageButton)finalView.findViewById(R.id.playToggleButton);
		playToggleButton.setOnClickListener(playToggleClickListener);
		((ImageButton)finalView.findViewById(R.id.playerFastForward)).setOnClickListener(fastForwardClickListener);
		((ImageButton)finalView.findViewById(R.id.playerRewind)).setOnClickListener(rewindClickListener);
		
		episode.close();
		return finalView;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		Log.d("HIP-DETACH", "Detach");
		if (player != null && !player.isPlaying())
			player.destroy();
		if (bound)
			this.getActivity().unbindService(mConnection);
		seekBarUpdateHandler.removeCallbacks(updateRunnable);
	}

	private Cursor getEpisodeDetails() {
		Log.d("HIP_ID", String.valueOf(podcast_id));
		Cursor p = this
				.getActivity()
				.getContentResolver()
				.query(Uri
						.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes"),
						new String[] { "_id", "title", "duration",
								"podcast_id", "status", "position",
								"content_url", "shownotes", "guid",
								"donation_url", "type" }, "_id = ?",
						new String[] { String.valueOf(podcast_id) }, null);
		return p;
	}

	private Uri getImageUri(int show_id) {
		Cursor p = this
				.getActivity()
				.getContentResolver()
				.query(Uri
						.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts"),
						new String[] { "_id", HipstacastProvider.PODCAST_IMAGE },
						"_id = ?", new String[] { String.valueOf(show_id) },
						null);
		p.moveToFirst();
		String fullPath = p.getString(p.getColumnIndex(HipstacastProvider.PODCAST_IMAGE));
		p.close();
		String[] imagePath = fullPath.split("/");
		String imgName = imagePath[imagePath.length-1];
		if (imgName.length() > 3) {
			imgName = imgName.substring(0, imgName.length()-3) + "w.jpg";
			return Uri.parse(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/hipstacast/img/"+ imgName);
		} else {
			return null;
		}

	}
	
	private void startPlaying() {
		player.clean();
		player.podcastToPlayUrl = android.os.Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/Android/data/com.ifrins.hipstacast/files/shows/"
				+ show_id
				+ "/" + podcast_id + ".mp3";
		player.n = PlayerUIUtils.buildNotification(this.getActivity(), name, show_id, podcast_id, type);
		Log.d("HIP-NW-SP", String.valueOf(start_position));
		player.completionListener = completionListener;
		player.start_position = start_position;
		player.play();
		if (duration == 0)
			fixDuration();
		seekBarUpdateHandler.postDelayed(updateRunnable, 1100);
		Log.d("HIP-STATUS", "Should start");
	}
	
	private void stopPlaying() {
		player.pause();
		start_position = player.mediaPlayer.getCurrentPosition();
		PlayerUIUtils.savePosition(this.getActivity(), podcast_id, start_position);
		Log.d("HIP-STATUS", "Should stop");
	}
	private void fixDuration() {
		View finalView = this.getView();
		
		SeekBar seekBar = (SeekBar)finalView.findViewById(R.id.playerSeekBar);
		seekBar.setMax(player.mediaPlayer.getDuration());
		
		TextView timeCounter = (TextView)finalView.findViewById(R.id.playerTotalDuration);
		timeCounter.setText(PlayerUIUtils.convertSecondsToDuration(player.mediaPlayer.getDuration()/1000));
		
		PlayerUIUtils.fixDuration(this.getActivity(), podcast_id, player.mediaPlayer.getDuration()/1000);
	}
		

}
