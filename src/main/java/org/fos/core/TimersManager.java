/*
 * Copyright (c) 2020. Benjamín Guzmán
 * Author: Benjamín Guzmán <9benjaminguzman@gmail.com>
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
import org.fos.timers.NotificationLocation;
import org.fos.timers.TimerSettings;
import org.fos.timers.WorkingTimeTimer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class TimersManager
{
	private static boolean alreadyInstantiated = false;
	private NotificationLocation notificationPreferredLocation;

	private Preferences prefs;

	private List<WorkingTimeTimer> workingTimeTimers;

	public TimersManager()
	{
		if (TimersManager.alreadyInstantiated)
			throw new IllegalStateException("This class has already been instantiated");
		TimersManager.alreadyInstantiated = true;
		this.workingTimeTimers = new LinkedList<>();
	}

	/**
	 * Loads settings from the preferences
	 *
	 * @param breakType
	 * 	type of the break, this method will take the name of the break and load the preferences associated with that
	 * 	name
	 *
	 * @return a TimerSettings object constructed from the preferences values
	 */
	private BreakSettings loadBreakSettings(final BreakType breakType)
	{
		String breakName = breakType.getName();
		boolean is_enabled = this.prefs.getBoolean(breakName + " enabled", false);
		if (!is_enabled)
			return new BreakSettings.Builder()
				.workTimerSettings(null)
				.breakTimerSettings(null)
				.postponeTimerSettings(null)
				.breakType(breakType)
				.notificationAudioPath(null)
				.breakAudiosDirStr(null)
				.enabled(false)
				.createBreakSettings();

		int working_time = this.prefs.getInt(breakName + " working time", 5);
		int break_time = this.prefs.getInt(breakName + " break time", 5);
		int postpone_time = this.prefs.getInt(breakName + " postpone time", 5);
		String notificationAudioPath = this.prefs.get(breakName + " notification audio", null);
		String breakAudiosDir = this.prefs.get(breakName + " break audios dir", null);

		if (breakType == BreakType.DAY_BREAK)
			return new BreakSettings.Builder()
				.workTimerSettings(new TimerSettings(working_time))
				.breakTimerSettings(null)
				.postponeTimerSettings(new TimerSettings(postpone_time))
				.breakType(breakType)
				.notificationAudioPath(notificationAudioPath)
				.breakAudiosDirStr(breakAudiosDir)
				.createBreakSettings();
		else
			return new BreakSettings.Builder()
				.workTimerSettings(new TimerSettings(working_time))
				.breakTimerSettings(new TimerSettings(break_time))
				.postponeTimerSettings(new TimerSettings(postpone_time))
				.breakType(breakType)
				.notificationAudioPath(notificationAudioPath)
				.breakAudiosDirStr(breakAudiosDir)
				.createBreakSettings();
	}

	/**
	 * Returns the loaded breaks settings with the following indexes
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -> day break
	 *
	 * @return the loaded breaks
	 */
	public List<BreakSettings> loadBreaksSettings()
	{
		this.loadPreferences();

		return Arrays.stream(BreakType.values())
			.map(this::loadBreakSettings)
			.collect(Collectors.toList());
	}

	/**
	 * Saves the break settings in the user preferences
	 *
	 * @param breakType
	 * 	type of the break, this method will take the name of the break and save the preferences associated with that
	 * 	name
	 * @param breakSettings
	 * 	the break settings to be saved
	 */
	private void saveBreakSettings(final BreakType breakType, final BreakSettings breakSettings)
	{
		String breakName = breakType.getName();

		this.prefs.putBoolean(breakName + " enabled", breakSettings.isEnabled());

		if (!breakSettings.isEnabled())
			return;

		TimerSettings breakTimerSettings = breakSettings.getBreakTimerSettings();
		this.prefs.putInt(breakName + " working time", breakSettings.getWorkTimerSettings().getHMSAsSeconds());
		this.prefs.putInt(breakName + " postpone time", breakSettings.getPostponeTimerSettings().getHMSAsSeconds());
		if (breakSettings.getNotificationAudioPath() == null)
			this.prefs.remove(breakName + " notification audio");
		else
			this.prefs.put(breakName + " notification audio", breakSettings.getNotificationAudioPath());

		if (breakTimerSettings != null) {
			this.prefs.putInt(breakName + " break time", breakTimerSettings.getHMSAsSeconds());
			if (breakSettings.getBreakAudiosDirStr() == null)
				this.prefs.remove(breakName + " break audios dir");
			else
				this.prefs.put(breakName + " break audios dir", breakSettings.getBreakAudiosDirStr());
		}
	}

	/**
	 * Saves all the breaks settings in the array
	 * It is important the settings be in the following order
	 * 0 -> small breaks
	 * 1 -> stretch breaks
	 * 2 -? day break
	 *
	 * @param breaksSettings
	 * 	the array of settings
	 */
	public void saveBreaksSettings(final List<BreakSettings> breaksSettings)
	{
		this.loadPreferences();

		this.saveBreakSettings(BreakType.SMALL_BREAK, breaksSettings.get(0));
		this.saveBreakSettings(BreakType.STRETCH_BREAK, breaksSettings.get(1));
		this.saveBreakSettings(BreakType.DAY_BREAK, breaksSettings.get(2));

		try {
			prefs.flush(); // ensure preferences are saved
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Couldn't ensure preferences were saved", e);
		}
	}

	/**
	 * If preferences were already loaded, this method will return those preferences
	 * Otherwise, it reads the preferences for this package and tries to sync them
	 * to have updated values
	 */
	private void loadPreferences()
	{
		if (this.prefs != null) {
			try {
				this.prefs.sync(); // ensure we read updated values
			} catch (BackingStoreException e) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Couldn't sync preferences for package " + TimersManager.class,
					e
				);
			}
			return;
		}

		this.prefs = Preferences.userNodeForPackage(TimersManager.class);
		try {
			this.prefs.sync(); // ensure we read updated values
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.SEVERE, "Couldn't sync preferences for package " + TimersManager.class, e);
		}
	}

	/**
	 * Creates all the timers for the breaks if enabled
	 */
	public void createExecutorsFromPreferences()
	{
		this.killAllTimers();
		List<BreakSettings> breaksSettings = this.loadBreaksSettings();

		if (this.workingTimeTimers != null)
			this.workingTimeTimers.clear();

		this.notificationPreferredLocation = this.getNotificationPrefLocation(true);

		// create the corresponding working time timer if it is enabled only
		this.workingTimeTimers = breaksSettings
			.stream()
			.map(breakSettings -> {
				     if (!breakSettings.isEnabled())
					     return null;

				     return breakSettings.getBreakType() == BreakType.DAY_BREAK ?
					     new WorkingTimeTimer(
						     breakSettings,
						     SWMain.messagesBundle.getString("time_for_a_day_break"),
						     SWMain.messagesBundle.getString("day_break_title"),
						     false
					     ) : new WorkingTimeTimer(
					     breakSettings,
					     SWMain.messagesBundle.getString("notification_time_for_a")
						     + " " + breakSettings.getBreakTimerSettings().getHMSAsString()
						     + " " + SWMain.messagesBundle.getString("break"),
					     SWMain.messagesBundle.getString("time_for_a_small_break")
				     );
			     }
			)
			.collect(Collectors.toList());

		this.workingTimeTimers
			.stream()
			.filter(Objects::nonNull)
			.forEach(WorkingTimeTimer::init); // start all the timers that are not null
	}

	public void saveNotificationPrefLocation(byte location)
	{
		this.loadPreferences();

		this.prefs.putInt("notification location", location);
	}

	public NotificationLocation getNotificationPrefLocation()
	{
		return this.getNotificationPrefLocation(false);
	}

	/**
	 * Gets the notification location preference
	 *
	 * @param reload
	 * 	if true, this method will try to reload the loaded preferences
	 *
	 * @return the notification location preference
	 */
	public NotificationLocation getNotificationPrefLocation(boolean reload)
	{
		if (reload) {
			this.loadPreferences();
			Optional<NotificationLocation> optNotificationLocation = NotificationLocation.getInstance(
				this.prefs.getInt("notification location", 0)
			);
			optNotificationLocation.ifPresentOrElse(
				notificationLocation -> this.notificationPreferredLocation = notificationLocation,
				() -> Loggers.getErrorLogger().severe("Something really really bad happened, couldn't load neither preferred nor default Notification Location")
			);
		}

		return this.notificationPreferredLocation;
	}

	/**
	 * shutdowns all timers and resets the timers array
	 * leaving the object as it were "brand-new" or recently instantiated
	 */
	public void killAllTimers()
	{
		if (this.workingTimeTimers == null)
			return;
		this.workingTimeTimers.stream().filter(Objects::nonNull).forEach(WorkingTimeTimer::destroy);
		this.workingTimeTimers.clear();
		this.workingTimeTimers = null;
	}

	public List<WorkingTimeTimer> getActiveWorkingTimers()
	{
		return this.workingTimeTimers;
	}
}
