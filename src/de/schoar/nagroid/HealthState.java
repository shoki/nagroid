package de.schoar.nagroid;

import de.schoar.nagroid.nagios.NagiosState;
import de.schoar.nagroid.notification.HealthNotificationHelper;

public class HealthState {
	private boolean mPollingSuccessfull;
	private NagiosState mStateHosts;
	private NagiosState mStateServices;

	private String mText;
	private long mVibrate[];
	private String mResourceId;
	
	private int mSoundId;

	public HealthState(boolean pollingSuccessfull, NagiosState stateHosts,
			NagiosState stateServices) {

		mPollingSuccessfull = pollingSuccessfull;
		mStateHosts = stateHosts;
		mStateServices = stateServices;
		
		init();
	}

	private void init() {
		long[] vibrate = new long[0];
		String text = "Everything is doing fine, relax...";

		if (!NagiosState.SERVICE_OK.equals(mStateServices)
				&& !NagiosState.SERVICE_LOCAL_ERROR.equals(mStateServices)) {
			text = "Houston, we have a service problem";
			vibrate = new long[] { 0, 400, 200, 100 };
		}
		if (!NagiosState.HOST_UP.equals(mStateHosts)
				&& !NagiosState.HOST_LOCAL_ERROR.equals(mStateHosts)) {
			text = "Houston, we have a host problem";
			vibrate = new long[] { 0, 400, 200, 100, 200, 100 };
		}
		if (!NagiosState.HOST_UP.equals(mStateHosts)
				&& !NagiosState.SERVICE_OK.equals(mStateServices)
				&& !NagiosState.SERVICE_LOCAL_ERROR.equals(mStateServices)
				&& !NagiosState.HOST_LOCAL_ERROR.equals(mStateHosts)) {
			text = "Ohoh - we have service and host problems. Hurry!";
			vibrate = new long[] { 0, 400, 200, 100, 200, 100, 200, 100 };
		}
		mVibrate = vibrate;
		mText = text;

		int soundId = 0;
		if (NagiosState.SERVICE_WARNING.equals(mStateServices)
				&& DM.I.getConfiguration().getNotificationAlarmWarning()) {
			soundId = HealthNotificationHelper.SOUND_WARNING;
		}
		if (NagiosState.SERVICE_CRITICAL.equals(mStateServices)
				&& DM.I.getConfiguration().getNotificationAlarmCritical()) { 
			soundId = HealthNotificationHelper.SOUND_CRITICAL;
		}
		if ((NagiosState.HOST_DOWN.equals(mStateHosts)
				|| NagiosState.HOST_UNREACHABLE.equals(mStateHosts))
				&& DM.I.getConfiguration().getNotificationAlarmDownUnreachable()) { 
			soundId = HealthNotificationHelper.SOUND_HOSTDOWN;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("de.schoar.nagroid:drawable/state");
		if (mPollingSuccessfull) {
			sb.append("_ok");
		} else {
			sb.append("_error");
			// alarm, we can't update state, nagios down?
			if (DM.I.getConfiguration().getPollingEnabled() && DM.I.getConfiguration().getNotificationAlarmEnabled()
					&& DM.I.getConfiguration().getNotificationAlarmPollFailure()) {
				// XXX: set to POLLFAILURE 
				soundId = HealthNotificationHelper.SOUND_HOSTDOWN;
			}
		}
		
		mSoundId = soundId;

		sb.append("_" + mStateHosts.toColorStrNoHash().toLowerCase());
		sb.append("_" + mStateServices.toColorStrNoHash().toLowerCase());
		mResourceId = sb.toString();
	}

	public boolean isOk() {
		if (!mPollingSuccessfull) {
			return false;
		}
		if (!NagiosState.HOST_UP.equals(mStateHosts)) {
			return false;
		}
		if (!NagiosState.SERVICE_OK.equals(mStateServices)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof HealthState)) {
			return false;
		}

		HealthState hs = (HealthState) o;
		if (mPollingSuccessfull != hs.getPollingSuccessfull()) {
			return false;
		}
		if (!mStateHosts.equals(hs.getStateHosts())) {
			return false;
		}
		if (!mStateServices.equals(hs.getStateServices())) {
			return false;
		}

		return true;
	}

	public boolean getPollingSuccessfull() {
		return mPollingSuccessfull;
	}

	public NagiosState getStateHosts() {
		return mStateHosts;
	}

	public NagiosState getStateServices() {
		return mStateServices;
	}

	public String getText() {
		return mText;
	}

	public long[] getVibrate() {
		if (!DM.I.getConfiguration().getNotificationVibrate()) {
			return new long[0];
		}

		return mVibrate;
	}

	public String getResourceId() {
		return mResourceId;
	}

	public int getSoundId() {
		return mSoundId;
	}
}
