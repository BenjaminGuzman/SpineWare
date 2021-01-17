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

package org.fos.timers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import org.fos.Loggers;
import org.fos.core.BreakType;
import org.fos.core.NotificationLocation;
import org.fos.core.TimersManager;

public class TimersPrefsIO
{
	private final Preferences prefs;
	private NotificationLocation notificationPreferredLocation;

	public TimersPrefsIO()
	{
		prefs = Preferences.userNodeForPackage(this.getClass());
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
	 * @param breakType type of the break, this method will take the name of the break and load the preferences associated with that
	 *                  name
	 * @return a {@link BreakConfig} object constructed from the preferences values
	 */
	private BreakConfig loadBreakConfig(final BreakType breakType)
	{
		return BreakConfig.fromPrefs(prefs, breakType);
	}

	public NotificationLocation getNotificationPrefLocation()
	{
		return getNotificationPrefLocation(false);
	}

	/**
	 * Gets the notification location preference
	 *
	 * @param reload if true, this method will try to reload the loaded preferences
	 * @return the notification location preference
	 */
	public NotificationLocation getNotificationPrefLocation(boolean reload)
	{
		if (reload) {
			syncPrefs();
			Optional<NotificationLocation> optNotificationLocation = NotificationLocation.getInstance(
				prefs.getInt("notification location", 0)
			);
			if (optNotificationLocation.isPresent())
				notificationPreferredLocation = optNotificationLocation.get();
			else
				Loggers.getErrorLogger().severe("Something really really bad happened, couldn't load neither preferred nor default Notification Location");
		}

		return notificationPreferredLocation;
	}

	/////////////////
	// save values //
	/////////////////
	public void setBreakEnabled(BreakType breakType, boolean enabled)
	{
		prefs.putBoolean(breakType.getName(), enabled);
	}

	/**
	 * Saves the break settings in the user preferences
	 * <p>
	 *
	 * @param breakConfig the break settings to be saved
	 */
	public void saveBreakConfig(final BreakConfig breakConfig)
	{
		BreakConfig.saveBreakSettings(prefs, breakConfig);
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

	/**
	 * Saves the notification preferred location
	 * <p>
	 * And reloads it so the internal cached value is updated when calling {@link #getNotificationPrefLocation()}
	 *
	 * @param location the location index
	 */
	public void saveNotificationPrefLocation(byte location)
	{
		syncPrefs();

		prefs.putInt("notification location", location);

		NotificationLocation.getInstance(location).ifPresent(
			notificationLocation -> notificationPreferredLocation = notificationLocation
		);
	}

	private void syncPrefs()
	{
		try {
			prefs.sync(); // ensure we read updated values
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.SEVERE, "Couldn't sync preferences for package " + TimersManager.class, e);
		}
	}
}