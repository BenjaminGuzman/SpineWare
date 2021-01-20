/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <9benjaminguzman@gmail.com>
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

package org.fos.sw.prefs.timers;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import org.fos.sw.Loggers;
import org.fos.sw.hooks.BreakHooksConfig;
import org.fos.sw.hooks.HooksConfig;
import org.fos.sw.prefs.PrefsIO;
import org.fos.sw.timers.WallClock;
import org.fos.sw.timers.breaks.BreakConfig;
import org.fos.sw.timers.breaks.BreakType;

public class TimersPrefsIO extends PrefsIO
{
	private final HooksPrefsIO hooksPrefsIO;
	private final NotificationPrefsIO notificationPrefsIO;

	public TimersPrefsIO()
	{
		super();
		prefs = Preferences.userNodeForPackage(this.getClass());
		hooksPrefsIO = new HooksPrefsIO(prefs);
		notificationPrefsIO = new NotificationPrefsIO(prefs);
	}

	/////////////////
	// load values //
	/////////////////

	/**
	 * Returns the loaded breaks settings in the order specified in {@link BreakType}
	 *
	 * @return the loaded breaks in order
	 */
	public List<BreakConfig> loadBreaksConfig()
	{
		syncPrefs();

		return Arrays.stream(BreakType.values())
			.map(this::loadBreakConfig)
			.collect(Collectors.toList());
	}

	/**
	 * Loads settings from the preferences
	 *
	 * @param breakType type of the break, this method will take the name of the break and load the preferences
	 *                  associated with that name
	 * @return a {@link BreakConfig} object constructed from the preferences values
	 */
	private BreakConfig loadBreakConfig(final BreakType breakType)
	{
		String breakName = breakType.getName();
		boolean is_enabled = prefs.getBoolean(breakName + " enabled", false);

		// load timer settings
		int working_time = prefs.getInt(breakName + " working time", 0);
		int break_time = prefs.getInt(breakName + " break time", 0);
		int postpone_time = prefs.getInt(breakName + " postpone time", 0);

		// load hooks settings
		HooksConfig notifHooksConf;
		try {
			notifHooksConf = hooksPrefsIO.loadForBreak(breakType, true);
		} catch (InstantiationException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Some preferences for the break: " + breakType
					+ " and notification hooks not saved correctly",
				e
			);
			return null;
		}

		HooksConfig breakHooksConf = null;
		try {
			breakHooksConf = hooksPrefsIO.loadForBreak(breakType, false);
		} catch (InstantiationException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Some preferences for the break: " + breakType
					+ " and break hooks were not saved correctly",
				e
			);
		}

		BreakConfig.Builder builder = new BreakConfig.Builder()
			.enabled(is_enabled)
			.workTimerSettings(WallClock.from(working_time))
			.postponeTimerSettings(WallClock.from(postpone_time))
			.hooksConfig(new BreakHooksConfig(notifHooksConf, breakHooksConf))
			.breakType(breakType);

		return breakType == BreakType.DAY_BREAK
			? builder.breakTimerSettings(null).createBreakSettings()
			: builder.breakTimerSettings(WallClock.from(break_time)).createBreakSettings();
	}

	/////////////////
	// save values //
	/////////////////
	public void setBreakEnabled(BreakType breakType, boolean enabled)
	{
		prefs.putBoolean(breakType.getName() + " enabled", enabled);
	}

	/**
	 * Saves the break settings in the user preferences
	 * <p>
	 *
	 * @param breakConfig the break settings to be saved
	 */
	public void saveBreakConfig(final BreakConfig breakConfig)
	{
		String breakName = breakConfig.getBreakType().getName();

		prefs.putBoolean(breakName + " enabled", breakConfig.isEnabled());

		if (!breakConfig.isEnabled())
			return;

		// save timer settings
		prefs.putInt(
			breakName + " working time",
			breakConfig.getWorkTimerSettings().getHMSAsSeconds()
		);
		prefs.putInt(
			breakName + " postpone time",
			breakConfig.getPostponeTimerSettings().getHMSAsSeconds()
		);
		WallClock breakWallClock = breakConfig.getBreakTimerSettings();
		if (breakWallClock != null)
			prefs.putInt(breakName + " break time", breakWallClock.getHMSAsSeconds());

		// save hook configs
		if (breakConfig.getHooksConfig() != null)
			hooksPrefsIO.save(breakConfig.getHooksConfig());
	}

	/**
	 * Saves all the breaks settings in the array
	 *
	 * @param breaksSettings the array of settings
	 */
	public void saveBreaksConfig(final List<BreakConfig> breaksSettings)
	{
		syncPrefs();

		breaksSettings.forEach(this::saveBreakConfig);

		try {
			prefs.flush(); // ensure preferences are saved
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Couldn't ensure preferences were saved", e);
		}
	}

	/////////////
	// getters //
	/////////////


	public HooksPrefsIO getHooksPrefsIO()
	{
		return hooksPrefsIO;
	}

	public NotificationPrefsIO getNotificationPrefsIO()
	{
		return notificationPrefsIO;
	}
}