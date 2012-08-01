package com.ifrins.hipstacast;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

public class HipstacastWelcome extends Activity {
	private int currentStep = 1;
	private Button nextButton = null;
	private View contentView = null;
    ViewGroup parent = null;
	LayoutInflater i = null;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);
		nextButton = (Button)findViewById(R.id.setupButton);
		contentView = findViewById(R.id.setupContentView);
		parent = (ViewGroup) contentView.getParent();
		i = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		setViewForStep(1);
	}
	public void moveNext(View v) {
		if (currentStep == 1) {
			currentStep++;
			setViewForStep(2);
		}
	}
	
	private void setViewForStep(int step) {
	    int index = parent.indexOfChild(contentView);
	    parent.removeView(contentView);

		if (step == 1) {
		    contentView = getLayoutInflater().inflate(R.layout.setup_step_1, parent, false);
		    nextButton.setText(R.string.start);
		} else if (step == 2) {
		    contentView = getLayoutInflater().inflate(R.layout.setup_step_2, parent, false);
		    setUpFeaturedList();
		    nextButton.setText(R.string.next);
		}
	    parent.addView(contentView, index);

	}
	private void setUpFeaturedList() {
		ListView featuredList = (ListView)findViewById(R.id.setupFeaturedList);
		
	}
}
