package com.ifrins.hipstacast.fragments;

import com.ifrins.hipstacast.HipstacastPlayerService;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.HipstacastPlayerService.LocalBinder;
import com.ifrins.hipstacast.model.PodcastEpisode;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.HipstacastLogging;
import com.ifrins.hipstacast.utils.PlayerCallbacks;
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
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayerFragment extends Fragment {

	int episodeId;
	int show_id;
	HipstacastPlayerService player;
	Boolean bound = false;
	Boolean fromNotification = false;
	int start_position;
	int duration;
	Boolean complete = false;
	Handler seekBarUpdateHandler = new Handler();
	SeekBar seekBar = null;
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
			Log.d("HIP_S", "Service disconnected");
		}
	};

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		episodeId = this.getArguments().getInt("episode_id");
		
		Intent intent = new Intent(this.getActivity(), HipstacastPlayerService.class);

		this.getActivity().startService(intent);
		this.getActivity().bindService(intent, mConnection, Context.BIND_DEBUG_UNBIND);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View playerView = inflater.inflate(R.layout.player, null);
		((ProgressBar)playerView.findViewById(R.id.loadingProgress)).setIndeterminate(true);
		return playerView;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		/*Log.d("HIP-DETACH", "Detach");
		if (player != null && !player.isPlaying())
			player.destroy();
		if (bound)
			this.getActivity().unbindService(mConnection);
		seekBarUpdateHandler.removeCallbacks(updateRunnable);
		*/
	}

	private Cursor getEpisodeDetails() {
		Log.d("HIP_ID", String.valueOf(episodeId));
		Cursor p = this
				.getActivity()
				.getContentResolver()
				.query(Uri
						.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes"),
						new String[] { "_id", "title", "duration",
								"podcast_id", "status", "position",
								"content_url", "shownotes", "guid",
								"donation_url", "type" }, "_id = ?",
						new String[] { String.valueOf(episodeId) }, null);
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
	

}
