package de.schoar.nagroid.notification;

import java.util.HashMap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Vibrator;
import de.schoar.nagroid.ConfigurationAccess;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.HealthState;
import de.schoar.nagroid.PIFactory;
import de.schoar.nagroid.R;
import de.schoar.nagroid.activity.ProblemsActivity;
import de.schoar.nagroid.nagios.NagiosState;
import android.util.Log;

public class HealthNotificationHelper extends NotificationHelper {
	private static final String LOGT = "NotificationHelper";

	private ConfigurationAccess mConfigurationAccess;

	private HealthState mLastHealthState = null;

	private boolean mIsDisplayed = false;
		
	private SoundPool SoundPool;
	private HashMap<Integer, Integer> SoundMap;
	
	public static int SOUND_CRITICAL = 1;
	public static int SOUND_WARNING = 2;
	public static int SOUND_HOSTDOWN = 3;
	public static int SOUND_POLLFAILURE = 4;

	public HealthNotificationHelper(Context ctx, ConfigurationAccess ca) {
		super(ctx, R.layout.problems);
		mConfigurationAccess = ca;

		SoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
		SoundMap = new HashMap<Integer, Integer>();
		Log.d(LOGT, "Loading sounds");
		SoundMap.put(SOUND_CRITICAL, SoundPool.load(ctx, R.raw.critical, 1));
		SoundMap.put(SOUND_WARNING, SoundPool.load(ctx, R.raw.warning, 1));
		SoundMap.put(SOUND_HOSTDOWN, SoundPool.load(ctx, R.raw.hostdown,1));
		SoundMap.put(SOUND_POLLFAILURE, SoundPool.load(ctx, R.raw.pollfailure,1));
		// TODO: should check that sounds are loaded before playing
	}

	public void updateNagiosState(Context ctx, NagiosState stateHost,
			NagiosState stateService, boolean noErrorOccured) {
		updateNagiosState(ctx, new HealthState(noErrorOccured, stateHost,
				stateService), false);
	}

	public void showLast(Context ctx) {
		if (mLastHealthState == null) {
			return;
		}
		updateNagiosState(ctx, mLastHealthState, true);
	}

	@Override
	public void clear() {
		super.clear();
		mIsDisplayed = false;
	}

	private void updateNagiosState(Context ctx, HealthState hs, boolean quiet) {

		if (hs.getVibrate().length != 0 && !quiet) {
			Vibrator v = (Vibrator) ctx
					.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(hs.getVibrate(), -1);
		}

		if (DM.I.getConfiguration().getNotificationAlarmEnabled()
				&& hs.getSoundId() != 0 && !quiet) {
			
			AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
			float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float volume = actualVolume / maxVolume;
			
			
			SoundPool.play(SoundMap.get(hs.getSoundId()), volume, volume, 1, 0, 1f);
			Log.d(LOGT, "Played sound");
		}

		if (mLastHealthState != null && mLastHealthState.equals(hs)
				&& mIsDisplayed) {
			return;
		}

		if (mConfigurationAccess.getNotificationHideIfOk() && hs.isOk()) {
			clear();
			return;
		}

		if (!DM.I.getConfiguration().getPollingEnabled()) {
			return;
		}

		show(ctx, hs);

	}

	private void show(Context ctx, HealthState hs) {
		mIsDisplayed = true;
		mLastHealthState = hs;

		PendingIntent pi = PIFactory
				.getForActivity(ctx, ProblemsActivity.class);

		notify(ctx, hs.getText(), hs.getResourceId(),
				Notification.FLAG_NO_CLEAR, pi);
	}
}
