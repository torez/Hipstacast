package com.ifrins.hipstacast.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.tasks.ExportTask;
import com.ifrins.hipstacast.tasks.ImportTask;
import com.ifrins.hipstacast.tasks.OnTaskCompleted;

import java.util.Random;

public class TakeoutProcessFragment extends Fragment {

	public final static String EXTRA_METHOD = "method";
	public final static int METHOD_IMPORT = 0;
	public final static int METHOD_EXPORT = 1;

	Button nextButton;

	int currentPage = 0;
	int firstCode;
	int secondCode;
	int method;

	View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			moveToNextPage();
		}
	};

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		method = this.getArguments().getInt(EXTRA_METHOD);
		View v = inflater.inflate(R.layout.setup, null);
		nextButton = (Button) v.findViewById(R.id.setupButton);
		nextButton.setOnClickListener(buttonOnClickListener);

		int nextTitle;
		if (method == METHOD_IMPORT) {
			nextTitle = R.string.import_menu;
		} else {
			nextTitle = R.string.export;
		}
		nextButton.setText(nextTitle);

		return v;
	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Random r = new Random();
		r.nextInt();
		firstCode = r.nextInt(99999);
		secondCode = r.nextInt(99999);
		getChildFragmentManager().beginTransaction().replace(R.id.setup_content, new TakeoutWelcomeFragment()).commit();
	}


	private void moveToNextPage() {
		if (currentPage == 0) {
			setupPage(1);
		} else if (currentPage == 1) {
			getActivity().finish();
		}
	}

	private void setupPage(int page) {
		currentPage = page;

		if (page == 1) {
			getChildFragmentManager().beginTransaction().replace(R.id.setup_content, new TakeoutProcessingFragment()).commit();
			nextButton.setVisibility(View.INVISIBLE);
			nextButton.setText(R.string.finish);
		}
	}

	public class TakeoutWelcomeFragment extends Fragment {

		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			View v = inflater.inflate(R.layout.import_instructions, null);

			int titleText;
			int instructionsText;
			int processURL;
			if (method == METHOD_IMPORT) {
				instructionsText = R.string.new_import_instructions_1;
				processURL = R.string.new_import_instructions_url;
				titleText = R.string.import_menu;
			} else {
				instructionsText = R.string.new_export_instructions_1;
				processURL = R.string.new_export_instructions_url;
				titleText = R.string.export;
			}
			((TextView)v.findViewById(R.id.takeout_instructions)).setText(instructionsText);
			((TextView)v.findViewById(R.id.takeout_url)).setText(processURL);
			((TextView)v.findViewById(R.id.takeout_title)).setText(titleText);

			((TextView)v.findViewById(R.id.import_code_1)).setText(String.valueOf(firstCode));
			((TextView)v.findViewById(R.id.import_code_2)).setText(String.valueOf(secondCode));

			return v;
		}
	}

	public class TakeoutProcessingFragment extends Fragment {
		OnTaskCompleted onTakeoutFinished = new OnTaskCompleted() {
			@Override
			public void onTaskCompleted(String task) {
				nextButton.setVisibility(View.VISIBLE);
			}

			@Override
			public void onError() {
				TextView info = (TextView) getView().findViewById(R.id.migration_more_info);
				info.setTextAppearance(getActivity(), R.style.TextError);
				info.setText(R.string.takeout_server_error);
			}
		};

		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			View v = inflater.inflate(R.layout.fragment_welcome_migration, null);

			int textTitle;
			if (method == METHOD_IMPORT) {
				textTitle = R.string.importing;
				new ImportTask(getActivity(), onTakeoutFinished).execute(firstCode, secondCode);
			} else {
				textTitle = R.string.exporting;
				new ExportTask(getActivity(), onTakeoutFinished).execute(firstCode, secondCode);
			}

			((TextView)v.findViewById(R.id.migration_title)).setText(textTitle);
			((TextView)v.findViewById(R.id.migration_more_info)).setText(R.string.takeout_after_done);


			return v;
		}

	}
}
