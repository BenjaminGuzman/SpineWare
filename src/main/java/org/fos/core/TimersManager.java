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
import org.fos.timers.BreakSettings;
import org.fos.timers.TimerSettings;

import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class TimersManager {

	/**
	 * Loads settings from the preferences
	 * @param breakName name of the timer, this could be small breaks, stretch breaks, or day break
	 * @return a TimerSettings object constructed from the preferences values
	 */
	private static BreakSettings loadBreakSettings(final String breakName, final Preferences prefs) {
		String _breakName = TimersManager.normalizeBreakName(breakName);

		boolean is_enabled = prefs.getBoolean(_breakName + " enabled", true);
		if (!is_enabled)
			return new BreakSettings(null, null, false);

		int working_time = prefs.getInt(_breakName + " working time", 0);
		int break_time = prefs.getInt(_breakName + " break time", 0);

		return new BreakSettings(new TimerSettings(working_time), !_breakName.equals("day break") ? new TimerSettings(break_time) : null);
	}

	/**
	 * Returns the loaded breaks settings with the following indexes
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -> day break
	 * @return the loaded breaks
	 */
	public static BreakSettings[] loadBreaksSettings() {
		Preferences prefs = TimersManager.getPreferences();
		assert prefs != null;

		String[] breaksNames = new String[] {"small breaks", "stretch breaks", "day break"};
		BreakSettings[] breaksSettings = new BreakSettings[3];
		byte i = 0;
		for (String breakName : breaksNames) {
			breaksSettings[i] = TimersManager.loadBreakSettings(breakName, prefs);
			++i;
		}

		return breaksSettings;
	}

	/**
	 * Saves the break settings in the user preferences
	 * @param breakName the break name
	 * @param prefs the preferences for the user
	 * @param breakSettings the break settings to be saved
	 */
	private static void saveBreakSettings(final String breakName, final Preferences prefs, final BreakSettings breakSettings) {
		prefs.putBoolean(breakName + " enabled", breakSettings.isEnabled());

		if (!breakSettings.isEnabled())
			return;

		prefs.putInt(breakName + " working time", breakSettings.workTimerSettings.getHMSAsSeconds());
		if (breakSettings.breakTimerSettings != null)
			prefs.putInt(breakName + " break time", breakSettings.breakTimerSettings.getHMSAsSeconds());
	}

	/**
	 * Saves all the breaks settings in the array
	 * It is important the settings be in the following order
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -? day break
	 * @param breaksSettings the array of settings
	 */
	public static void saveBreaksSettings(final BreakSettings[] breaksSettings) {
		Preferences prefs = TimersManager.getPreferences();
		assert prefs != null;

		TimersManager.saveBreakSettings("small breaks", prefs, breaksSettings[0]);
		TimersManager.saveBreakSettings("stretch breaks", prefs, breaksSettings[1]);
		TimersManager.saveBreakSettings("day break", prefs, breaksSettings[2]);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Loggers.errorLogger.log(Level.WARNING, "Couldn't ensure preferences were saved", e);
		}
	}

	/**
	 * Checks if the breakName is a valid and calls String.toLowerCase
	 * @param breakName name of the break
	 * @return the normalized break name
	 */
	private static String normalizeBreakName(final String breakName) {
		String _breakName = breakName.toLowerCase();
		if (!_breakName.equals("small breaks") && !_breakName.equals("stretch breaks") & !_breakName.equals("day break"))
			throw new IllegalArgumentException("Argument timerName with value \"" + breakName + "\" is not valid");

		return _breakName;
	}

	public static Preferences getPreferences() {
		Preferences preferences = Preferences.userNodeForPackage(TimersManager.class);
		try {
			preferences.sync(); // ensure we read updated values
		} catch (BackingStoreException e) {
			Loggers.errorLogger.log(Level.SEVERE, "Couldn't read preferences for package " + TimersManager.class, e);
			return null;
		}
		return preferences;
	}

	/*private WorkingTimeTimer smallBreaksWorkingTimeTimer;
	private WorkingTimeTimer stretchBreaksWorkingTimeTimer;
	private WorkingTimeTimer dayBreakWorkingTimeTimer;

	public void createExecutorsFromPreferences() {
		Preferences preferences = Preferences.userNodeForPackage(this.getClass());
		try {
			preferences.sync(); // ensure we read updated values
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

		boolean small_breaks_disabled = preferences.getBoolean(PREF_KEY_SMALL_BREAKS_DISABLED, true);
		boolean stretch_breaks_disabled = preferences.getBoolean(PREF_KEY_STRETCH_BREAKS_DISABLED, true);
		boolean day_breaks_disabled = preferences.getBoolean(PREF_KEY_DAY_BREAKS_DISABLED, true);

		if (!small_breaks_disabled) {
			int small_breaks_working_time = preferences.getInt(PREF_KEY_SMALL_BREAKS_WORKING_TIME, 5 * 60);
			int small_breaks_break_time = preferences.getInt(PREF_KEY_SMALL_BREAKS_BREAK_TIME, 60);

			this.smallBreaksWorkingTimeTimer = new WorkingTimeTimer(small_breaks_working_time, small_breaks_break_time, SmallBreakTimer.class);
			this.smallBreaksWorkingTimeTimer.init();
		}

		if (!stretch_breaks_disabled) {

		}

		if (!day_breaks_disabled) {

		}

	}*/

	/**
	 * shutdowns all timers and resets the timers array
	 * leaving the object as it were "brand-new" or recently instantiated
	 *//*
	public void killAllTimers() {
		//for (ScheduledExecutorService scheduledExecutor : this.scheduledExecutors)
		//    scheduledExecutor.shutdown();
		//this.scheduledExecutors.clear();
		Loggers.debugLogger.exiting(TimersManager.class.getCanonicalName(), "killAllTimers");
	}*/
}
