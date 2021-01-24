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

import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.fos.sw.Loggers;
import org.fos.sw.hooks.BreakHooksConfig;
import org.fos.sw.hooks.HooksConfig;
import org.fos.sw.prefs.PrefsIO;
import org.fos.sw.timers.breaks.BreakType;
import org.jetbrains.annotations.NotNull;

public class HooksPrefsIO extends PrefsIO
{
	private static final String START_ENABLED = "start enabled";
	private static final String END_ENABLED = "end enabled";
	private static final String START_AUDIO_IS_DIR = "start audio is dir";
	private static final String ON_START_AUDIO_STR = "on start audio str";
	private static final String ON_END_AUDIO_STR = "on end audio str";
	private static final String ON_START_CMD_STR = "on start cmd str";
	private static final String ON_END_CMD_STR = "on end cmd str";

	HooksPrefsIO(@NotNull Preferences prefs)
	{
		this.prefs = prefs;
	}

	/**
	 * Loads a {@link HooksConfig} object from preferences
	 *
	 * @param breakType            the break type for which the preferences will be loaded
	 * @param is_notification_hook tells if the preference you're looking for is a notification hook or a break hook
	 * @return the created object from preferences
	 */
	public HooksConfig loadForBreak(@NotNull BreakType breakType, boolean is_notification_hook) throws InstantiationException
	{
		try {
			prefs.sync();
		} catch (BackingStoreException e) {
			Loggers.getDebugLogger().log(Level.WARNING, "Couldn't sync preferences", e);
		}

		String prefix = hookPrefPrefix(breakType, is_notification_hook);

		return new HooksConfig.Builder()
			.isNotificationHook(is_notification_hook)
			.startEnabled(prefs.getBoolean(prefix + START_ENABLED, false))
			.endEnabled(prefs.getBoolean(prefix + END_ENABLED, false))
			.startAudioIsDir(prefs.getBoolean(prefix + START_AUDIO_IS_DIR, false))
			.breakType(breakType)
			.onStartAudioStr(prefs.get(prefix + ON_START_AUDIO_STR, null))
			.onEndAudioStr(prefs.get(prefix + ON_END_AUDIO_STR, null))
			.onStartCmdStr(prefs.get(prefix + ON_START_CMD_STR, null))
			.onEndCmdStr(prefs.get(prefix + ON_END_CMD_STR, null))
			.createHooksConfig();
	}

	/**
	 * Saves the values for the given {@link HooksConfig} in the preferences
	 *
	 * @param hooksConfig the object to be saved in the preferences
	 */
	public void save(@NotNull HooksConfig hooksConfig)
	{
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		// add a prefix to each key to avoid collisions
		String prefix = hookPrefPrefix(hooksConfig.getBreakType(), hooksConfig.isNotificationHook());

		prefs.putBoolean(prefix + START_ENABLED, hooksConfig.isStartEnabled());
		prefs.putBoolean(prefix + END_ENABLED, hooksConfig.isEndEnabled());
		prefs.putBoolean(prefix + START_AUDIO_IS_DIR, hooksConfig.isStartAudioADirectory());

		if (hooksConfig.getOnStartAudioStr() == null)
			prefs.remove(prefix + ON_START_AUDIO_STR);
		else
			prefs.put(prefix + ON_START_AUDIO_STR, hooksConfig.getOnStartAudioStr());

		if (hooksConfig.getOnEndAudioStr() == null)
			prefs.remove(prefix + ON_END_AUDIO_STR);
		else
			prefs.put(prefix + ON_END_AUDIO_STR, hooksConfig.getOnEndAudioStr());

		if (hooksConfig.getOnStartCmdStr() == null)
			prefs.remove(prefix + ON_START_CMD_STR);
		else
			prefs.put(prefix + ON_START_CMD_STR, hooksConfig.getOnStartCmdStr());

		if (hooksConfig.getOnEndCmdStr() == null)
			prefs.remove(prefix + ON_END_CMD_STR);
		else
			prefs.put(prefix + ON_END_CMD_STR, hooksConfig.getOnEndCmdStr());

		flushPrefs();
	}

	public void save(@NotNull BreakHooksConfig breakHooksConfig)
	{
		if (breakHooksConfig.getBreakHooksConf() != null)
			this.save(breakHooksConfig.getBreakHooksConf());

		this.save(breakHooksConfig.getNotificationHooksConf());
	}

	/**
	 * Constructs the prefix used to save/load preferences
	 *
	 * @param breakType            the break type for the saved preferences
	 * @param is_notification_hook this indicates whether or not the saved preferences are for a notification
	 * @return the prefix
	 */
	private String hookPrefPrefix(@NotNull BreakType breakType, boolean is_notification_hook)
	{
		return breakType.getName() + (is_notification_hook ? " notification " : " break ") + "hooks ";
	}
}
