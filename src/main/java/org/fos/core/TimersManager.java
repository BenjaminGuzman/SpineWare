/*
 * Copyright (c) 2020. Benjamín Guzmán
 * Author: Benjamín Guzmán <9benjaminguzman@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fos.core;

import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.timers.BreakSettings;
import org.fos.timers.TimerSettings;
import org.fos.timers.WorkingTimeTimer;

import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class TimersManager
{
	private byte notification_location_pref = 0;

	private Preferences prefs;

	private WorkingTimeTimer smallBreaksWorkingTimer;
	private WorkingTimeTimer stretchBreaksWorkingTimer;
	private WorkingTimeTimer dayBreakWorkingTimer;

	/**
	 * Loads settings from the preferences
	 *
	 * @param breakName
	 * 	name of the timer, this could be small breaks, stretch breaks, or day break
	 *
	 * @return a TimerSettings object constructed from the preferences values
	 */
	private BreakSettings loadBreakSettings(final String breakName, final byte breakType)
	{
		String _breakName = this.normalizeBreakName(breakName);

		boolean is_enabled = this.prefs.getBoolean(_breakName + " enabled", false);
		if (!is_enabled)
			return new BreakSettings(null, null, null, breakType, null, null, false);

		int working_time = this.prefs.getInt(_breakName + " working time", 5);
		int break_time = this.prefs.getInt(_breakName + " break time", 5);
		int postpone_time = this.prefs.getInt(_breakName + " postpone time", 5);
		String notificationAudioPath = this.prefs.get(_breakName + " notification audio", null);
		String breakAudiosDir = this.prefs.get(_breakName + " break audios dir", null);

		if (_breakName.equals("day break"))
			return new BreakSettings(
				new TimerSettings(working_time),
				null, // day break does not have break time
				new TimerSettings(postpone_time),
				breakType,
				notificationAudioPath,
				breakAudiosDir
			);
		else
			return new BreakSettings(
				new TimerSettings(working_time),
				new TimerSettings(break_time),
				new TimerSettings(postpone_time),
				breakType,
				notificationAudioPath,
				breakAudiosDir
			);
	}

	/**
	 * Returns the loaded breaks settings with the following indexes
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -> day break
	 *
	 * @return the loaded breaks
	 */
	public BreakSettings[] loadBreaksSettings()
	{
		this.loadPreferences();

		String[] breaksNames = new String[]{"small breaks", "stretch breaks", "day break"};
		BreakSettings[] breaksSettings = new BreakSettings[3];
		byte i = 0;
		for (String breakName : breaksNames) {
			breaksSettings[i] = this.loadBreakSettings(breakName, i);
			++i;
		}

		return breaksSettings;
	}

	/**
	 * Saves the break settings in the user preferences
	 *
	 * @param breakName
	 * 	the break name
	 * @param breakSettings
	 * 	the break settings to be saved
	 */
	private void saveBreakSettings(final String breakName, final BreakSettings breakSettings)
	{
		this.prefs.putBoolean(breakName + " enabled", breakSettings.isEnabled());

		if (!breakSettings.isEnabled())
			return;

		TimerSettings breakTimerSettings = breakSettings.getBreakTimerSettings();
		this.prefs.putInt(breakName + " working time", breakSettings.getWorkTimerSettings().getHMSAsSeconds());
		this.prefs.putInt(breakName + " postpone time", breakSettings.getPostponeTimerSettings().getHMSAsSeconds());
		if (breakSettings.getNotificationAudioPath() == null)
			this.prefs.remove(breakName + " notification audio");
		else
			this.prefs.put(breakName + " notification audio", breakSettings.getNotificationAudioPath());

		if (breakTimerSettings != null) {
			this.prefs.putInt(breakName + " break time", breakTimerSettings.getHMSAsSeconds());
			if (breakSettings.getBreakAudiosDirStr() == null)
				this.prefs.remove(breakName + " break audios dir");
			else
				this.prefs.put(breakName + " break audios dir", breakSettings.getBreakAudiosDirStr());
		}
	}

	/**
	 * Saves all the breaks settings in the array
	 * It is important the settings be in the following order
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -? day break
	 *
	 * @param breaksSettings
	 * 	the array of settings
	 */
	public void saveBreaksSettings(final BreakSettings[] breaksSettings)
	{
		this.loadPreferences();

		this.saveBreakSettings("small breaks", breaksSettings[0]);
		this.saveBreakSettings("stretch breaks", breaksSettings[1]);
		this.saveBreakSettings("day break", breaksSettings[2]);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Loggers.errorLogger.log(Level.WARNING, "Couldn't ensure preferences were saved", e);
		}
	}

	/**
	 * Checks if the breakName is a valid and calls String.toLowerCase
	 *
	 * @param breakName
	 * 	name of the break
	 *
	 * @return the normalized break name
	 */
	private String normalizeBreakName(final String breakName)
	{
		String _breakName = breakName.toLowerCase();
		if (!_breakName.equals("small breaks") && !_breakName.equals("stretch breaks") & !_breakName.equals("day break"))
			throw new IllegalArgumentException("Argument timerName with value \"" + breakName + "\" is not valid");

		return _breakName;
	}

	/**
	 * If preferences were already loaded, this method will return those preferences
	 * Otherwise, it reads the preferences for this package and tries to sync them
	 * to have updated values
	 */
	private void loadPreferences()
	{
		if (this.prefs != null) {
			try {
				this.prefs.sync(); // ensure we read updated values
			} catch (BackingStoreException e) {
				Loggers.errorLogger.log(
					Level.SEVERE,
					"Couldn't sync preferences for package " + TimersManager.class,
					e
				);
			}
			return;
		}

		this.prefs = Preferences.userNodeForPackage(TimersManager.class);
		try {
			this.prefs.sync(); // ensure we read updated values
		} catch (BackingStoreException e) {
			Loggers.errorLogger.log(Level.SEVERE, "Couldn't sync preferences for package " + TimersManager.class, e);
		}
	}

	/**
	 * Creates all the timers for the breaks if enabled
	 */
	public void createExecutorsFromPreferences()
	{
		this.killAllTimers();
		BreakSettings[] breaksSettings = this.loadBreaksSettings();

		this.notification_location_pref = this.getNotificationPrefLocation(true);

		TimerSettings breakTimerSettings;
		if (breaksSettings[0].isEnabled()) { // small breaks
			breakTimerSettings = breaksSettings[0].getBreakTimerSettings();
			this.smallBreaksWorkingTimer = new WorkingTimeTimer(
				breaksSettings[0],
				SWMain.messagesBundle.getString("notification_time_for_a")
					+ " " + breakTimerSettings.getHMSAsString()
					+ " " + SWMain.messagesBundle.getString("break"),
				SWMain.messagesBundle.getString("time_for_a_small_break")
			);
			this.smallBreaksWorkingTimer.init();
		}

		if (breaksSettings[1].isEnabled()) { // stretch breaks
			breakTimerSettings = breaksSettings[1].getBreakTimerSettings();
			this.stretchBreaksWorkingTimer = new WorkingTimeTimer(
				breaksSettings[1],
				SWMain.messagesBundle.getString("notification_time_for_a")
					+ " " + breakTimerSettings.getHMSAsString()
					+ " " + SWMain.messagesBundle.getString("break"),
				SWMain.messagesBundle.getString("time_for_a_stretch_break")
			);
			this.stretchBreaksWorkingTimer.init();
		}

		if (breaksSettings[2].isEnabled()) { // day breaks
			this.dayBreakWorkingTimer = new WorkingTimeTimer(
				breaksSettings[2],
				SWMain.messagesBundle.getString("time_for_a_day_break"),
				SWMain.messagesBundle.getString("day_break_title"),
				false
			);
			this.dayBreakWorkingTimer.init();
		}

	}

	public void saveNotificationPrefLocation(byte location)
	{
		this.loadPreferences();

		this.prefs.putInt("notification location", location);
	}

	public byte getNotificationPrefLocation()
	{
		return this.getNotificationPrefLocation(false);
	}

	/**
	 * Gets the notification location preference
	 *
	 * @param reload
	 * 	if true, this method will try to reload the loaded preferences
	 *
	 * @return the notification location preference
	 */
	public byte getNotificationPrefLocation(boolean reload)
	{
		if (reload) {
			this.loadPreferences();
			this.notification_location_pref = (byte) this.prefs.getInt("notification location", 0);
		}

		return this.notification_location_pref;
	}

	/**
	 * shutdowns all timers and resets the timers array
	 * leaving the object as it were "brand-new" or recently instantiated
	 */
	public void killAllTimers()
	{
		if (this.smallBreaksWorkingTimer != null)
			this.smallBreaksWorkingTimer.destroy();

		if (this.stretchBreaksWorkingTimer != null)
			this.stretchBreaksWorkingTimer.destroy();

		if (this.dayBreakWorkingTimer != null)
			this.dayBreakWorkingTimer.destroy();

		this.smallBreaksWorkingTimer = null;
		this.stretchBreaksWorkingTimer = null;
		this.dayBreakWorkingTimer = null;
	}

	public WorkingTimeTimer getSmallBreaksWorkingTimer()
	{
		return smallBreaksWorkingTimer;
	}

	public WorkingTimeTimer getStretchBreaksWorkingTimer()
	{
		return stretchBreaksWorkingTimer;
	}

	public WorkingTimeTimer getDayBreakWorkingTimer()
	{
		return dayBreakWorkingTimer;
	}
}
