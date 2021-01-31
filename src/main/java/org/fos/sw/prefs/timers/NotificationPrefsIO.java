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

import java.util.Optional;
import java.util.prefs.Preferences;
import org.fos.sw.Loggers;
import org.fos.sw.core.NotificationLocation;
import org.fos.sw.prefs.PrefsIO;
import org.jetbrains.annotations.NotNull;

public class NotificationPrefsIO extends PrefsIO
{
	private static final String NOTIFICATION_LOCATION = "notification location";

	@NotNull
	private NotificationLocation notificationLocationCache = NotificationLocation.BOTTOM_RIGHT;

	NotificationPrefsIO(@NotNull Preferences prefs)
	{
		this.prefs = prefs;
	}

	/**
	 * Saves the notification preferred location
	 * <p>
	 * And reloads it so the internal cached value is updated when calling {@link #getNotificationPrefLocation(boolean)}
	 *
	 * @param location the location index
	 */
	public void saveNotificationPrefLocation(byte location)
	{
		prefs.putInt(NOTIFICATION_LOCATION, location);

		NotificationLocation.getInstance(location).ifPresent(
			notificationLocation -> notificationLocationCache = notificationLocation
		);
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
				prefs.getInt(NOTIFICATION_LOCATION, 0)
			);
			if (optNotificationLocation.isPresent())
				notificationLocationCache = optNotificationLocation.get();
			else
				Loggers.getErrorLogger().severe("Something really really bad happened, couldn't load neither preferred nor default Notification Location");
		}

		return notificationLocationCache;
	}

	/**
	 * Gets the notification location preference without querying the preferences
	 * It will return the cached value from previous reads, if there were no previous reads it will return
	 * {@link NotificationLocation#BOTTOM_RIGHT}
	 *
	 * @return the notification location preference
	 */
	public NotificationLocation getNotificationPrefLocation()
	{
		return getNotificationPrefLocation(false);
	}
}
