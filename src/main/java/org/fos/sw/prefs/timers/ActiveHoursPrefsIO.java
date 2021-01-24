/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.dev>
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

import java.util.Optional;
import java.util.prefs.Preferences;
import org.fos.sw.prefs.PrefsIO;
import org.fos.sw.timers.WallClock;
import org.fos.sw.timers.breaks.ActiveHours;
import org.jetbrains.annotations.NotNull;

public class ActiveHoursPrefsIO extends PrefsIO
{
	private static final String ACTIVE_HOURS_START = "active hours start";
	private static final String ACTIVE_HOURS_END = "active hours end";

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

		flushPrefs();
	}

	/**
	 * Loads the {@link ActiveHours} object previously saved
	 * If no value was saved previously this method will return {@code null}
	 *
	 * @return the loaded active hours or null
	 */
	public Optional<ActiveHours> loadActiveHours()
	{
		int active_hours_start = prefs.getInt(ACTIVE_HOURS_START, -1);
		int active_hours_end = prefs.getInt(ACTIVE_HOURS_END, -1);

		if (active_hours_start == -1 || active_hours_end == -1)
			return Optional.empty();

		return Optional.of(new ActiveHours(
			WallClock.from(active_hours_start),
			WallClock.from(active_hours_end)
		));
	}
}
