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

package net.benjaminguzman.prefs;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.benjaminguzman.core.Loggers;
import net.benjaminguzman.core.NotificationLocation;
import org.jetbrains.annotations.NotNull;

public class NotificationPrefsIO extends PrefsIO
{
	private static final Preferences prefs = Preferences.userNodeForPackage(NotificationPrefsIO.class);
	private static final String TIMERS_NOTIFICATION_LOCATION = "timer notification location";
	private static final String CV_NOTIFICATION_LOCATION = "cv notification location";

	@NotNull
	private static final HashMap<String, NotificationLocation> notificationLocationsCache = new HashMap<>();

	static {
		notificationLocationsCache.put(TIMERS_NOTIFICATION_LOCATION, NotificationLocation.BOTTOM_RIGHT);
		notificationLocationsCache.put(CV_NOTIFICATION_LOCATION, NotificationLocation.TOP_RIGHT);
	}

	private NotificationPrefsIO()
	{
	}

	/**
	 * Saves the notification preferred location
	 * <p>
	 * And reloads it so the internal cached value is updated when calling
	 * {@link #getNotificationPrefLocation(boolean, NotificationPreferenceType)}
	 *
	 * @param location        the notification location to be saved
	 * @param cv_notification indicates whether or not the searched notification location is for the cv feature
	 */
	public static void saveNotificationPrefLocation(NotificationLocation location, boolean cv_notification)
	{
		String prefKey = cv_notification ? CV_NOTIFICATION_LOCATION : TIMERS_NOTIFICATION_LOCATION;

		prefs.putInt(prefKey, location.getLocationIdx());

		notificationLocationsCache.put(prefKey, location);
	}

	public static NotificationLocation getNotificationPrefLocation(
		boolean reload,
		NotificationPreferenceType notificationType
	)
	{
		if (reload) {
			try {
				prefs.sync();
			} catch (BackingStoreException e) {
				Loggers.getErrorLogger().log(Level.SEVERE, "Error while syncing preferences", e);
			}

			Optional<NotificationLocation> optNotificationLocation = NotificationLocation.getInstance(
				prefs.getInt(notificationType.getLocationPrefKey(), 0)
			);

			if (optNotificationLocation.isPresent())
				notificationLocationsCache.put(notificationType.getLocationPrefKey(), optNotificationLocation.get());
			else
				Loggers.getErrorLogger().severe("Something really really bad happened, couldn't load neither preferred nor default Notification Location");
		}

		return notificationLocationsCache.get(notificationType.getLocationPrefKey());
	}

	/**
	 * Gets the notification location preference without querying the preferences
	 * It will return the cached value from previous reads, if there were no previous reads it will return
	 * the default value.
	 *
	 * @param notificationType indicates the type of the notification
	 * @return the notification location preference
	 */
	public static NotificationLocation getNotificationPrefLocation(NotificationPreferenceType notificationType)
	{
		return getNotificationPrefLocation(false, notificationType);
	}

	public enum NotificationPreferenceType
	{
		CV_NOTIFICATION("cv notification location"),
		TIMER_NOTIFICATION("timer notification location");

		private final String locationPrefKey;

		NotificationPreferenceType(String notificationPrefKey)
		{
			this.locationPrefKey = notificationPrefKey;
		}

		private String getLocationPrefKey() {
			return locationPrefKey;
		}
	}
}
