package com.ifrins.hipstacast.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.ifrins.hipstacast.HipstacastSync;
import com.ifrins.hipstacast.R;

/**
 * Created by francesc on 22/06/13.
 */
public class AddUrlDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();

		final EditText innerUrlView = new EditText(getActivity());
		innerUrlView.setHint(R.string.podcast_url_alert_add_msg);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(innerUrlView);
		builder.setTitle(R.string.podcast_url_add_title);
		builder.setPositiveButton(R.string.menu_add, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				String url = innerUrlView.getText().toString();
				if (url != null) {
					Intent subscriptionIntent = new Intent(getActivity(), HipstacastSync.class);
					subscriptionIntent.setAction(HipstacastSync.ACTION_SUBSCRIBE);
					subscriptionIntent.putExtra(HipstacastSync.EXTRA_FEED_URL, url);
					getActivity().startService(subscriptionIntent);
				}
				dialogInterface.dismiss();

			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
			}
		});

		return builder.create();
	}


}
