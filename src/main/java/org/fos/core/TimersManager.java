/*
 * Copyright © 2020 Benjamín Guzmán
 * Author: Benjamín Guzmán <bg@benjaminguzman.dev>
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

public class TimersManager {
	private Preferences prefs;

	private WorkingTimeTimer smallBreaksWorkingTimeTimer;
	private WorkingTimeTimer stretchBreaksWorkingTimeTimer;
	private WorkingTimeTimer dayBreakWorkingTimeTimer;

	/**
	 * Loads settings from the preferences
	 *
	 * @param breakName name of the timer, this could be small breaks, stretch breaks, or day break
	 *
	 * @return a TimerSettings object constructed from the preferences values
	 */
	private BreakSettings loadBreakSettings(final String breakName) {
		String _breakName = this.normalizeBreakName(breakName);

		boolean is_enabled = this.prefs.getBoolean(_breakName + " enabled", false);
		if (!is_enabled)
			return new BreakSettings(null, null, false);

		int working_time = this.prefs.getInt(_breakName + " working time", 5);
		int break_time = this.prefs.getInt(_breakName + " break time", 5);

		return new BreakSettings(new TimerSettings(working_time), !_breakName.equals("day break") ? new TimerSettings(break_time) : null);
	}

	/**
	 * Returns the loaded breaks settings with the following indexes
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -> day break
	 *
	 * @return the loaded breaks
	 */
	public BreakSettings[] loadBreaksSettings() {
		this.loadPreferences();

		String[] breaksNames = new String[]{"small breaks", "stretch breaks", "day break"};
		BreakSettings[] breaksSettings = new BreakSettings[3];
		byte i = 0;
		for (String breakName : breaksNames) {
			breaksSettings[i] = this.loadBreakSettings(breakName);
			++i;
		}

		return breaksSettings;
	}

	/**
	 * Saves the break settings in the user preferences
	 *
	 * @param breakName     the break name
	 * @param breakSettings the break settings to be saved
	 */
	private void saveBreakSettings(final String breakName, final BreakSettings breakSettings) {
		this.prefs.putBoolean(breakName + " enabled", breakSettings.isEnabled());

		if (!breakSettings.isEnabled())
			return;

		this.prefs.putInt(breakName + " working time", breakSettings.workTimerSettings.getHMSAsSeconds());
		if (breakSettings.breakTimerSettings != null)
			this.prefs.putInt(breakName + " break time", breakSettings.breakTimerSettings.getHMSAsSeconds());
	}

	/**
	 * Saves all the breaks settings in the array
	 * It is important the settings be in the following order
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -? day break
	 *
	 * @param breaksSettings the array of settings
	 */
	public void saveBreaksSettings(final BreakSettings[] breaksSettings) {
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
	 * @param breakName name of the break
	 *
	 * @return the normalized break name
	 */
	private String normalizeBreakName(final String breakName) {
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
	private void loadPreferences() {
		if (this.prefs != null) {
			try {
				this.prefs.sync(); // ensure we read updated values
			} catch (BackingStoreException e) {
				Loggers.errorLogger.log(Level.SEVERE, "Couldn't sync preferences for package " + TimersManager.class, e);
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

	public void createExecutorsFromPreferences() {
		this.killAllTimers();
		BreakSettings[] breaksSettings = this.loadBreaksSettings();

		if (breaksSettings[0].isEnabled()) { // small breaks
			this.smallBreaksWorkingTimeTimer = new WorkingTimeTimer(
				breaksSettings[0].workTimerSettings,
				breaksSettings[0].breakTimerSettings,
				SWMain.messagesBundle.getString("notification_time_for_a")
					+ " " + breaksSettings[0].breakTimerSettings.getHMSAsString()
					+ " " + SWMain.messagesBundle.getString("break"),
				SWMain.messagesBundle.getString("time_for_a_small_break")
			);
			this.smallBreaksWorkingTimeTimer.init();
		}

		if (breaksSettings[1].isEnabled()) { // stretch breaks
			this.stretchBreaksWorkingTimeTimer = new WorkingTimeTimer(
				breaksSettings[1].workTimerSettings,
				breaksSettings[1].breakTimerSettings,
				SWMain.messagesBundle.getString("notification_time_for_a")
					+ " " + breaksSettings[1].breakTimerSettings.getHMSAsString()
					+ " " + SWMain.messagesBundle.getString("break"),
				SWMain.messagesBundle.getString("time_for_a_stretch_break")
			);
			this.stretchBreaksWorkingTimeTimer.init();
		}

		if (breaksSettings[2].isEnabled()) { // day breaks
			this.dayBreakWorkingTimeTimer = new WorkingTimeTimer(
				breaksSettings[2].workTimerSettings,
				null,
				SWMain.messagesBundle.getString("notification_time_for_a")
					+ " " + breaksSettings[2].breakTimerSettings.getHMSAsString()
					+ " " + SWMain.messagesBundle.getString("break"),
				SWMain.messagesBundle.getString("time_for_a_day_break"),
				false
			);
			this.dayBreakWorkingTimeTimer.init();
		}

	}

	/**
	 * shutdowns all timers and resets the timers array
	 * leaving the object as it were "brand-new" or recently instantiated
	 */
	public void killAllTimers() {
		if (this.smallBreaksWorkingTimeTimer != null)
			this.smallBreaksWorkingTimeTimer.destroy();

		if (this.stretchBreaksWorkingTimeTimer != null)
			this.stretchBreaksWorkingTimeTimer.destroy();

		if (this.dayBreakWorkingTimeTimer != null)
			this.dayBreakWorkingTimeTimer.destroy();

		this.smallBreaksWorkingTimeTimer = null;
		this.stretchBreaksWorkingTimeTimer = null;
		this.dayBreakWorkingTimeTimer = null;
	}
}
