/*
 * Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
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
package org.fos.core;

import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.timers.BreakSettings;
import org.fos.timers.Clock;
import org.fos.timers.WorkingTimeTimer;

import java.util.*;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class TimersManager
{
	private static volatile boolean initialized;
	private static volatile NotificationLocation notificationPreferredLocation;

	private static volatile Preferences prefs;

	/**
	 * The timers. They're placed in the following order
	 * <p>
	 * 0 -> micro break timer
	 * 1 -> stretch break timer
	 * 2 -> day break timer
	 */
	private static List<WorkingTimeTimer> workingTimeTimers;

	private TimersManager()
	{
		throw new RuntimeException(this.getClass().getName() + " cannot be instantiated");
	}

	synchronized public static void init()
	{
		if (TimersManager.initialized)
			throw new RuntimeException("You cannot invoke the TimersManager+init method more than once");

		TimersManager.initialized = true;
		TimersManager.loadTimers();
		TimersManager.loadPreferences();
	}

	/**
	 * Returns the loaded breaks settings with the following indexes
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -> day break
	 *
	 * @return the loaded breaks
	 */
	synchronized public static List<BreakSettings> loadBreaksSettings()
	{
		TimersManager.loadPreferences();

		return Arrays.stream(BreakType.values())
			.map(TimersManager::loadBreakSettings)
			.collect(Collectors.toList());
	}

	/**
	 * Saves the break settings in the user preferences
	 * <p>
	 * Once data is saved, this method will also reload the current timer
	 * No need to call {@link #reloadTimer(BreakType)}
	 *
	 * @param breakSettings the break settings to be saved
	 */
	synchronized public static void saveBreakSettings(final BreakSettings breakSettings)
	{
		String breakName = breakSettings.getBreakType().getName();

		TimersManager.prefs.putBoolean(breakName + " enabled", breakSettings.isEnabled());

		if (!breakSettings.isEnabled())
			return;

		Clock breakClock = breakSettings.getBreakTimerSettings();
		TimersManager.prefs.putInt(
			breakName + " working time",
			breakSettings.getWorkTimerSettings().getHMSAsSeconds()
		);
		TimersManager.prefs.putInt(
			breakName + " postpone time",
			breakSettings.getPostponeTimerSettings().getHMSAsSeconds()
		);
		if (breakSettings.getNotificationAudioPath() == null)
			TimersManager.prefs.remove(breakName + " notification audio");
		else
			TimersManager.prefs.put(
				breakName + " notification audio",
				breakSettings.getNotificationAudioPath()
			);

		if (breakClock != null) {
			TimersManager.prefs.putInt(breakName + " break time", breakClock.getHMSAsSeconds());
			if (breakSettings.getBreakAudiosDirStr() == null)
				TimersManager.prefs.remove(breakName + " break audios dir");
			else
				TimersManager.prefs.put(
					breakName + " break audios dir",
					breakSettings.getBreakAudiosDirStr()
				);
		}

		TimersManager.reloadTimer(breakSettings.getBreakType());
	}

	/**
	 * Saves all the breaks settings in the array
	 * It is not necessary but recommended the settings to be in the following order
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -> day break
	 *
	 * @param breaksSettings the array of settings
	 */
	synchronized public static void saveBreaksSettings(final List<BreakSettings> breaksSettings)
	{
		TimersManager.loadPreferences();

		breaksSettings.forEach(TimersManager::saveBreakSettings);

		try {
			prefs.flush(); // ensure preferences are saved
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Couldn't ensure preferences were saved", e);
		}
	}

	/**
	 * Reloads (kills the current timer and loads the configuration again for the same timer)
	 *
	 * @param breakType the break type. With this the method can tell which of the timers will be stopped and
	 *                  restarted
	 */
	synchronized public static void reloadTimer(BreakType breakType)
	{
		// kill the current timer if exists
		TimersManager.loadBreakSettings(breakType);
		WorkingTimeTimer timer = TimersManager.workingTimeTimers.get(breakType.getIndex());
		if (timer != null) timer.destroy();

		ResourceBundle messagesBundle = SWMain.getMessagesBundle();
		BreakSettings breakSettings = TimersManager.loadBreakSettings(breakType);
		if (!breakSettings.isEnabled()) {
			TimersManager.workingTimeTimers.set(breakType.getIndex(), null);
			return;
		}

		// set and start the new timer
		TimersManager.workingTimeTimers.set(
			breakType.getIndex(),
			breakSettings.getBreakType() == BreakType.DAY_BREAK
				? new WorkingTimeTimer(
				breakSettings,
				messagesBundle.getString("time_for_a_day_break"),
				messagesBundle.getString("day_break_title"),
				false
			) : new WorkingTimeTimer(
				breakSettings,
				messagesBundle.getString("notification_time_for_a")
					+ " " + breakSettings.getBreakTimerSettings().getHMSAsString()
					+ " " + messagesBundle.getString("break"),
				messagesBundle.getString("time_for_a_small_break")
			)
		);

		TimersManager.workingTimeTimers.get(breakType.getIndex()).init();
	}

	/**
	 * Saves the notification preferred location
	 * <p>
	 * And reloads it so the internal cached value is updated when calling {@link #getNotificationPrefLocation()}
	 *
	 * @param location the location index
	 */
	synchronized public static void saveNotificationPrefLocation(byte location)
	{
		TimersManager.loadPreferences();

		TimersManager.prefs.putInt("notification location", location);

		NotificationLocation.getInstance(location).ifPresent(
			notificationLocation -> TimersManager.notificationPreferredLocation = notificationLocation
		);
	}

	synchronized public static NotificationLocation getNotificationPrefLocation()
	{
		return TimersManager.getNotificationPrefLocation(false);
	}

	/**
	 * Gets the notification location preference
	 *
	 * @param reload if true, this method will try to reload the loaded preferences
	 * @return the notification location preference
	 */
	synchronized public static NotificationLocation getNotificationPrefLocation(boolean reload)
	{
		if (reload) {
			TimersManager.loadPreferences();
			Optional<NotificationLocation> optNotificationLocation = NotificationLocation.getInstance(
				TimersManager.prefs.getInt("notification location", 0)
			);
			if (optNotificationLocation.isPresent())
				TimersManager.notificationPreferredLocation = optNotificationLocation.get();
			else
				Loggers.getErrorLogger().severe("Something really really bad happened, couldn't load neither preferred nor default Notification Location");
		}

		return TimersManager.notificationPreferredLocation;
	}

	/**
	 * Creates all the timers for the breaks if enabled
	 */
	synchronized public static void createExecutorsFromPrefs()
	{
		TimersManager.killAllTimers();
		TimersManager.loadTimers();
		TimersManager.startWorkingTimers();
	}

	/**
	 * Loads the working timers from preferences into {@link #workingTimeTimers}
	 */
	private static void loadTimers()
	{
		List<BreakSettings> breaksSettings = TimersManager.loadBreaksSettings();
		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		if (TimersManager.workingTimeTimers != null)
			TimersManager.workingTimeTimers.clear();

		TimersManager.notificationPreferredLocation = TimersManager.getNotificationPrefLocation(true);

		// create the corresponding working time timer if it is enabled only
		TimersManager.workingTimeTimers = breaksSettings
			.stream()
			.map(breakSettings -> {
				if (!breakSettings.isEnabled())
					return null;

				return breakSettings.getBreakType() == BreakType.DAY_BREAK ?
					new WorkingTimeTimer(
						breakSettings,
						messagesBundle.getString("time_for_a_day_break"),
						messagesBundle.getString("day_break_title"),
						false
					) : new WorkingTimeTimer(
					breakSettings,
					messagesBundle.getString("notification_time_for_a")
						+ " " + breakSettings.getBreakTimerSettings().getHMSAsString()
						+ " " + messagesBundle.getString("break"),
					messagesBundle.getString("time_for_a_small_break")
				);
			})
			.collect(Collectors.toList());
	}

	/**
	 * shutdowns all timers and resets the timers array
	 * leaving the object as it were "brand-new" or recently instantiated
	 */
	synchronized public static void killAllTimers()
	{
		if (TimersManager.workingTimeTimers == null)
			return;
		TimersManager.workingTimeTimers.stream().filter(Objects::nonNull).forEach(WorkingTimeTimer::destroy);
		TimersManager.workingTimeTimers.clear();
		TimersManager.workingTimeTimers = null;
	}

	/**
	 * Disables/enables a single break
	 * Once it is disabled/enabled the executor is reloaded
	 * No need to call {@link #reloadTimer(BreakType)}
	 *
	 * @param breakType the break type that will be disabled
	 */
	synchronized public static void setBreakEnabled(BreakType breakType, boolean enabled)
	{
		String breakName = breakType.getName();

		TimersManager.prefs.putBoolean(breakName + " enabled", enabled);
		TimersManager.reloadTimer(breakType);
	}

	public static List<WorkingTimeTimer> getActiveWorkingTimers()
	{
		return TimersManager.workingTimeTimers;
	}

	private static void startWorkingTimers()
	{
		TimersManager.workingTimeTimers
			.stream()
			.filter(Objects::nonNull)
			.forEach(WorkingTimeTimer::init); // start all the timers that are not null
	}

	/**
	 * Loads settings from the preferences
	 *
	 * @param breakType type of the break, this method will take the name of the break and load the preferences associated with that
	 *                  name
	 * @return a {@link BreakSettings} object constructed from the preferences values
	 */
	private static BreakSettings loadBreakSettings(final BreakType breakType)
	{
		String breakName = breakType.getName();
		boolean is_enabled = TimersManager.prefs.getBoolean(breakName + " enabled", false);

		int working_time = TimersManager.prefs.getInt(breakName + " working time", 0);
		int break_time = TimersManager.prefs.getInt(breakName + " break time", 0);
		int postpone_time = TimersManager.prefs.getInt(breakName + " postpone time", 0);
		String notificationAudioPath = TimersManager.prefs.get(breakName + " notification audio", null);
		String breakAudiosDir = TimersManager.prefs.get(breakName + " break audios dir", null);

		if (breakType == BreakType.DAY_BREAK)
			return new BreakSettings.Builder()
				.enabled(is_enabled)
				.workTimerSettings(Clock.from(working_time))
				.breakTimerSettings(null)
				.postponeTimerSettings(Clock.from(postpone_time))
				.breakType(breakType)
				.notificationAudioPath(notificationAudioPath)
				.breakAudiosDirStr(breakAudiosDir)
				.createBreakSettings();
		else
			return new BreakSettings.Builder()
				.enabled(is_enabled)
				.workTimerSettings(Clock.from(working_time))
				.breakTimerSettings(Clock.from(break_time))
				.postponeTimerSettings(Clock.from(postpone_time))
				.breakType(breakType)
				.notificationAudioPath(notificationAudioPath)
				.breakAudiosDirStr(breakAudiosDir)
				.createBreakSettings();
	}

	/**
	 * If preferences were already loaded, this method will return those preferences
	 * Otherwise, it reads the preferences for this package and tries to sync them
	 * to have updated values
	 */
	private static void loadPreferences()
	{
		if (TimersManager.prefs != null) {
			try {
				TimersManager.prefs.sync(); // ensure we read updated values
			} catch (BackingStoreException e) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Couldn't sync preferences for package " + TimersManager.class,
					e
				);
			}
			return;
		}

		TimersManager.prefs = Preferences.userNodeForPackage(TimersManager.class);
		try {
			TimersManager.prefs.sync(); // ensure we read updated values
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.SEVERE, "Couldn't sync preferences for package " + TimersManager.class, e);
		}
	}
}
