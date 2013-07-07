package com.ifrins.hipstacast.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import com.ifrins.hipstacast.Hipstacast;
import com.ifrins.hipstacast.HipstacastSync;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.tasks.OnTaskCompleted;
import com.ifrins.hipstacast.tasks.UpgradeTask;
import com.ifrins.hipstacast.utils.HipstacastLogging;

/**
 * Created by francesc on 06/07/13.
 */
public class WelcomeFragment extends Fragment {
	Button nextButton;
	int currentPage = 0;

	View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			moveToNextPage();
		}
	};

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.setup, null);
		nextButton = (Button) v.findViewById(R.id.setupButton);
		nextButton.setOnClickListener(buttonOnClickListener);

		return v;
	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getChildFragmentManager().beginTransaction().replace(R.id.setup_content, new PresentationFragment()).commit();
	}

	public void moveToNextPage() {
		if (currentPage == 0) {
			Cursor subscriptionCount = getActivity().getContentResolver().query(
					HipstacastProvider.SUBSCRIPTIONS_URI,
					new String[] { "_id" },
					null,
					null,
					null);
			if (subscriptionCount.getCount() == 0) {
				setupStep(2);
			} else {
				setupStep(4);
			}
		} else if (currentPage == 1) {
			setupStep(4);
		} else if (currentPage == 2) {
			setupStep(3);
		} else if (currentPage == 3) {
			setupStep(4);
		}
	}

	/*
		Step 1: Import from 1.x app
		Step 2: Import from computer
		Step 3: Show recommendations
		Step 4: Finish
    */
	private void setupStep(int step) {
		currentPage = step;

		if (step == 1) {
			nextButton.setText(R.string.finish);
			nextButton.setVisibility(View.INVISIBLE);
			getChildFragmentManager().beginTransaction().replace(R.id.setup_content, new MigrationFragment()).commit();
		} else if (step == 2) {

		} else if (step == 3) {

		} else if (step == 4) {
			((Hipstacast)getActivity().getApplication()).setWelcomeActivityShown();
			getActivity().setResult(Activity.RESULT_OK);
			getActivity().finish();
		}
	}

	private class PresentationFragment extends Fragment {
		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			return inflater.inflate(R.layout.fragment_welcome_presentation, null);
		}
	}

	private class MigrationFragment extends Fragment {
		private ProgressBar progressBar;
		private int status = 0;
		OnTaskCompleted migrationFinished = new OnTaskCompleted() {
			@Override
			public void onTaskCompleted(String task) {
				incrementStatus();
			}
		};

		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				incrementStatus();
				getActivity().unregisterReceiver(receiver);
			}
		};

		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			new UpgradeTask(this.getActivity(), migrationFinished).execute();
			View v = inflater.inflate(R.layout.fragment_welcome_migration, null);
			progressBar = (ProgressBar) v.findViewById(R.id.migration_progressbar);

			this.getActivity().registerReceiver(receiver, new IntentFilter(HipstacastSync.BROADCAST_FINISHED));
			return v;
		}

		private void incrementStatus() {
			status++;
			HipstacastLogging.log("Incremented status", status);
			if (status == 2) {
				nextButton.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			}
		}
	}
}
