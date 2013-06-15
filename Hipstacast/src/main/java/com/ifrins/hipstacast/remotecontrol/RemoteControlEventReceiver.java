package com.ifrins.hipstacast.remotecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import com.ifrins.hipstacast.HipstacastPlayerService;
import com.ifrins.hipstacast.utils.HipstacastLogging;

public class RemoteControlEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent mKeyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);

            if (mKeyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                return;
            }

            Intent mIntent;

            switch (mKeyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    mIntent = new Intent();
                    mIntent.setAction(HipstacastPlayerService.ACTION_PAUSE);
                    context.sendBroadcast(mIntent);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    mIntent = new Intent();
                    mIntent.setAction(HipstacastPlayerService.ACTION_PLAY);
                    context.sendBroadcast(mIntent);
                    break;

	            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
		            mIntent = new Intent();
		            mIntent.setAction(HipstacastPlayerService.ACTION_TOGGLE);
		            context.sendBroadcast(mIntent);
                default:
                    HipstacastLogging.log("KEY CODE VALUE", mKeyEvent.getKeyCode());
                    break;
            }
        }
    }
}
