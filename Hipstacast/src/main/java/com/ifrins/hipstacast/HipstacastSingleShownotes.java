package com.ifrins.hipstacast;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.ifrins.hipstacast.fragments.ShownotesFragment;

/**
 * Created by francesc on 30/06/13.
 */
public class HipstacastSingleShownotes extends FragmentActivity {
    public final static String EXTRA_EPISODE_ID = "episode_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.basic_layout);

        int episodeId = this.getIntent().getIntExtra(EXTRA_EPISODE_ID, -1);
        if (episodeId == -1) {
            return;
        }

        Fragment shownotesFragment = new ShownotesFragment();
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putInt(ShownotesFragment.EXTRA_EPISODE_ID, episodeId);
        shownotesFragment.setArguments(fragmentArgs);

        this.getSupportFragmentManager().beginTransaction().replace(R.id.container, shownotesFragment).commit();

    }
}
