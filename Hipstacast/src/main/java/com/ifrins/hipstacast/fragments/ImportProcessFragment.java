package com.ifrins.hipstacast.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.tasks.ImportTask;
import com.ifrins.hipstacast.tasks.OnTaskCompleted;

import java.util.Random;

public class ImportProcessFragment extends Fragment {

	Button nextButton;

	int currentPage = 0;
	int firstCode;
	int secondCode;

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
		nextButton.setText(R.string.import_menu);
		nextButton.setOnClickListener(buttonOnClickListener);

		return v;
	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Random r = new Random();
		r.nextInt();
		firstCode = r.nextInt(99999);
		secondCode = r.nextInt(99999);
		getChildFragmentManager().beginTransaction().replace(R.id.setup_content, new ImportWelcomeFragment()).commit();
	}


	private void moveToNextPage() {
		if (currentPage == 0) {
			setupPage(1);
		} else if (currentPage == 1) {
			setupPage(2);

			nextButton.setVisibility(View.INVISIBLE);
			nextButton.setText(R.string.finish);
		} else if (currentPage == 2) {
			getActivity().finish();
		}
	}

	private void setupPage(int page) {
		currentPage = page;

		if (page == 1) {
			getChildFragmentManager().beginTransaction().replace(R.id.setup_content, new ImportProcessingFragment()).commit();
		}
	}

	public class ImportWelcomeFragment extends Fragment {

		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			View v = inflater.inflate(R.layout.import_instructions, null);

			((TextView)v.findViewById(R.id.import_code_1)).setText(String.valueOf(firstCode));
			((TextView)v.findViewById(R.id.import_code_2)).setText(String.valueOf(secondCode));

			return v;
		}
	}

	public class ImportProcessingFragment extends Fragment {
		OnTaskCompleted onImportFinished = new OnTaskCompleted() {
			@Override
			public void onTaskCompleted(String task) {
				nextButton.setVisibility(View.VISIBLE);
			}
		};

		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			View v = inflater.inflate(R.layout.fragment_welcome_migration, null);
			((TextView)v.findViewById(R.id.migration_title)).setText(R.string.importing);
			((TextView)v.findViewById(R.id.migration_more_info)).setText(R.string.importing_after_done);

			new ImportTask(getActivity(), onImportFinished).execute(firstCode, secondCode);

			return v;
		}

	}
}
