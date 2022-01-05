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

package net.benjaminguzman.prefs.timers;

import java.util.Optional;
import java.util.prefs.Preferences;

import net.benjaminguzman.timers.WallClock;
import net.benjaminguzman.timers.breaks.ActiveHours;
import net.benjaminguzman.prefs.PrefsIO;
import org.jetbrains.annotations.NotNull;

public class ActiveHoursPrefsIO extends PrefsIO
{
	private static final String ACTIVE_HOURS_START = "active hours start";
	private static final String ACTIVE_HOURS_END = "active hours end";
	private static final String ACTIVE_HOURS_ENABLED = "active hours enabled";

	ActiveHoursPrefsIO(@NotNull Preferences prefs)
	{
		this.prefs = prefs;
	}

	/**
	 * Saves the given object in the preferences
	 *
	 * @param activeHours the object to be saved
	 */
	public void saveActiveHoursPref(@NotNull ActiveHours activeHours)
	{
		prefs.putInt(ACTIVE_HOURS_START, activeHours.getStart().getHMSAsSeconds());
		prefs.putInt(ACTIVE_HOURS_END, activeHours.getEnd().getHMSAsSeconds());
		prefs.putBoolean(ACTIVE_HOURS_ENABLED, activeHours.isEnabled());

		flushPrefs();
	}

	/**
	 * Saves the enabled preference for the active hours
	 *
	 * @param enabled true if the active hours should be enabled
	 */
	public void setActiveHoursEnabled(boolean enabled)
	{
		prefs.putBoolean(ACTIVE_HOURS_ENABLED, enabled);

		flushPrefs();
	}

	/**
	 * Loads the {@link ActiveHours} object previously saved
	 * If no value was saved previously this method will return {@code null}
	 *
	 * @return the loaded active hours or null
	 */
	public Optional<ActiveHours> loadActiveHours(HooksPrefsIO hooksPrefsIO)
	{
		syncPrefs();

		int active_hours_start = prefs.getInt(ACTIVE_HOURS_START, -1);
		int active_hours_end = prefs.getInt(ACTIVE_HOURS_END, -1);

		if (active_hours_start == -1 || active_hours_end == -1)
			return Optional.empty();

		return Optional.of(
			new ActiveHours(
				WallClock.from(active_hours_start),
				WallClock.from(active_hours_end),
				prefs.getBoolean(ACTIVE_HOURS_ENABLED, false)
			)
				.setBeforeStartHooks(hooksPrefsIO.loadForActiveHours(false))
				.setAfterEndHooks(hooksPrefsIO.loadForActiveHours(true))
		);
	}
}
