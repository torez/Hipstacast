package com.ifrins.hipstacast.fragments;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.ifrins.hipstacast.HipstacastPlayerService;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.HipstacastPlayerService.LocalBinder;
import com.ifrins.hipstacast.utils.HipstacastLogging;
import com.ifrins.hipstacast.utils.PlayerCallbacks;
import com.ifrins.hipstacast.utils.PlayerUIUtils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class PlayerFragment extends Fragment {

	int episodeId;
	HipstacastPlayerService player;
	Boolean bound = false;
	SeekBar seekBar;
	

	OnClickListener playbackToggleClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ImageButton button = (ImageButton)v;
			
			if (player.isPlaying()) {
				button.setImageResource(R.drawable.ic_action_play);
				player.pause();
				seekBar.removeCallbacks(seekbarUpdaterRunnable);
			} else {
				button.setImageResource(R.drawable.ic_action_pause);
				player.play();
				seekBar.postDelayed(seekbarUpdaterRunnable, 3000);
			}
		}
	};
	
	OnClickListener ffClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			player.ff();
		}
	};
	
	OnClickListener rewindClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			player.rewind();
		}
	};
	
	Runnable seekbarUpdaterRunnable = new Runnable() {

		@Override
		public void run() {
			HipstacastLogging.log("playbackprogress", player.getCurrentPosition());
			seekBar.setProgress(player.getCurrentPosition());
			seekBar.postDelayed(seekbarUpdaterRunnable, 1500);
		}
		
	};
	
	PlayerCallbacks mPlayerCallbacks = new PlayerCallbacks() {

		@Override
		public void onPrepared() {
			PlayerFragment.this.getView()
			.findViewById(R.id.playerControls)
			.setVisibility(View.VISIBLE);
			
			ImageButton playbackToggle = (ImageButton) PlayerFragment.this.getView().findViewById(R.id.playToggleButton);
			
			if (player.isPlaying()) {
				playbackToggle.setImageResource(R.drawable.ic_action_pause);
			} else {
				playbackToggle.setImageResource(R.drawable.ic_action_play);
			}

			seekBar.setMax(player.getMPEpisodeDuration());
			seekBar.setProgress(player.getCurrentPosition());
			seekBar.setVisibility(View.VISIBLE);

		}

		@Override
		public void onBufferingUpdate(int progress) {
			double percentage = progress * 0.01;
			int seekPos = (int) (seekBar.getMax() * percentage);
			seekBar.setSecondaryProgress(seekPos);
		}

		@Override
		public void onStartDoingUIWork() {
			View fragmentView = PlayerFragment.this.getView();
			
			fragmentView.findViewById(R.id.mainPlayerView).setVisibility(View.VISIBLE);
			fragmentView.findViewById(R.id.loadingProgress).setVisibility(View.GONE);
			
			HipstacastLogging.log(("We are ready!"));
			
			TextView titleView = (TextView)fragmentView.findViewById(R.id.playerEpisodeName);
			titleView.setText(player.getEpisodeTitle());
			titleView.setSelected(true);
			
			ImageView coverView = (ImageView)fragmentView.findViewById(R.id.playerCoverImage);
            UrlImageViewHelper.setUrlDrawable(coverView, PlayerUIUtils.fixCoverPath(player.getCoverPath(getActivity())));

			seekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
			seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
			seekBar.setVisibility(View.INVISIBLE);

			ImageButton playbackToggle = (ImageButton) fragmentView.findViewById(R.id.playToggleButton);
			playbackToggle.setOnClickListener(playbackToggleClickListener);
			
			ImageButton ffButton = (ImageButton) fragmentView.findViewById(R.id.playerFastForward);
			ffButton.setOnClickListener(ffClickListener);
			
			ImageButton rewindButton = (ImageButton) fragmentView.findViewById(R.id.playerRewind);
			rewindButton.setOnClickListener(rewindClickListener);
		}
		
	};
	
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			player = binder.getService();
			bound = true;
			
			player.registerForCallbacks(mPlayerCallbacks);
			
			if (player.isAlreadyPrepared(episodeId)) {
				player.recover();
			} else {
				player.prepare(episodeId);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
		}
	};

	SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
			if (fromUser && player != null) {
				player.seekTo(position);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	};

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		episodeId = this.getArguments().getInt("episode_id");
		
		Intent intent = new Intent(this.getActivity(), HipstacastPlayerService.class);

		this.getActivity().startService(intent);
		this.getActivity().bindService(intent, mConnection, 0);
        this.setHasOptionsMenu(true);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View playerView = inflater.inflate(R.layout.player, null);
		((ProgressBar)playerView.findViewById(R.id.loadingProgress)).setIndeterminate(true);
		return playerView;
	}

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuPlayShare:
                String msg = String.format("#nowplaying %s %s", player.getEpisodeTitle(), player.getEpisodeLink());
                EasyTracker.getTracker().trackEvent("optionsmenu_action", "menu_press", "share", 1l);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
                startActivity(Intent.createChooser(shareIntent, null));
                return true;
            case R.id.menuPlayWebsite:
                EasyTracker.getTracker().trackEvent("optionsmenu_action", "menu_press", "website", 1l);
                Intent webPageVisitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(player.getEpisodeLink()));
                startActivity(webPageVisitIntent);
            default:
                HipstacastLogging.log("menu item id", item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
	public void onDestroy() {
		super.onDestroy();

		seekBar.removeCallbacks(seekbarUpdaterRunnable);
		
		if (player != null && !player.isPlaying()) {
			player.destroy();
		}
		
		if (bound) {
			this.getActivity().unbindService(mConnection);
		}

	}

}
