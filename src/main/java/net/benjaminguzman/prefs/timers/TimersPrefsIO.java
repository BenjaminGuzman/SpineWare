/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.net>
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

package net.benjaminguzman.prefs.timers;

import net.benjaminguzman.core.Loggers;
import net.benjaminguzman.hooks.BreakHooks;
import net.benjaminguzman.hooks.SingleBreakHooksConfig;
import net.benjaminguzman.prefs.PrefsIO;
import net.benjaminguzman.timers.WallClock;
import net.benjaminguzman.timers.breaks.BreakConfig;
import net.benjaminguzman.timers.breaks.BreakType;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class TimersPrefsIO extends PrefsIO
{
	private static final String ENABLED = " enabled";
	private static final String WORKING_TIME = " working time";
	private static final String BREAK_TIME = " break time";
	private static final String POSTPONE_TIME = " postpone time";

	private final HooksPrefsIO hooksPrefsIO;
	private final ActiveHoursPrefsIO activeHoursPrefsIO;

	public TimersPrefsIO()
	{
		super();
		prefs = Preferences.userNodeForPackage(this.getClass());
		hooksPrefsIO = new HooksPrefsIO(prefs);
		activeHoursPrefsIO = new ActiveHoursPrefsIO(prefs);
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
	private BreakConfig loadBreakConfig(BreakType breakType)
	{
		String breakName = breakType.getName();
		boolean is_enabled = prefs.getBoolean(breakName + ENABLED, false);

		// load timer settings
		int working_time = prefs.getInt(breakName + WORKING_TIME, 0);
		int break_time = prefs.getInt(breakName + BREAK_TIME, 0);
		int postpone_time = prefs.getInt(breakName + POSTPONE_TIME, 0);

		// load hooks settings
		SingleBreakHooksConfig notifHooksConf;
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

		SingleBreakHooksConfig breakHooksConf = null;
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
			.hooksConfig(new BreakHooks(notifHooksConf, breakHooksConf))
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
		prefs.putBoolean(breakType.getName() + ENABLED, enabled);
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

		prefs.putBoolean(breakName + ENABLED, breakConfig.isEnabled());

		if (!breakConfig.isEnabled())
			return;

		// save timer settings
		prefs.putInt(
			breakName + WORKING_TIME,
			breakConfig.getWorkWallClock().getHMSAsSeconds()
		);
		prefs.putInt(
			breakName + POSTPONE_TIME,
			breakConfig.getPostponeWallClock().getHMSAsSeconds()
		);
		breakConfig.getBreakWallClock().ifPresent(
			wallClock -> prefs.putInt(breakName + BREAK_TIME, wallClock.getHMSAsSeconds())
		);

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

		flushPrefs();
	}

	/////////////
	// getters //
	/////////////


	public HooksPrefsIO getHooksPrefsIO()
	{
		return hooksPrefsIO;
	}

	public ActiveHoursPrefsIO getActiveHoursPrefsIO()
	{
		return activeHoursPrefsIO;
	}
}