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

import java.util.prefs.Preferences;
import org.fos.sw.hooks.BreakHooks;
import org.fos.sw.hooks.HooksConfig;
import org.fos.sw.hooks.SingleBreakHooksConfig;
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
	 * Loads a {@link SingleBreakHooksConfig} object from preferences
	 *
	 * @param breakType            the break type for which the preferences will be loaded
	 * @param is_notification_hook tells if the preference you're looking for is a notification hook or a break hook
	 * @return the created object from preferences
	 */
	public SingleBreakHooksConfig loadForBreak(@NotNull BreakType breakType, boolean is_notification_hook) throws InstantiationException
	{
		syncPrefs();

		String prefix = hookPrefBreakPrefix(breakType, is_notification_hook);

		return new SingleBreakHooksConfig.Builder()
			.breakType(breakType)
			.isNotificationHook(is_notification_hook)
			.startEnabled(prefs.getBoolean(prefix + START_ENABLED, false))
			.endEnabled(prefs.getBoolean(prefix + END_ENABLED, false))
			.startAudioIsDir(prefs.getBoolean(prefix + START_AUDIO_IS_DIR, false))
			.onStartAudioStr(prefs.get(prefix + ON_START_AUDIO_STR, null))
			.onEndAudioStr(prefs.get(prefix + ON_END_AUDIO_STR, null))
			.onStartCmdStr(prefs.get(prefix + ON_START_CMD_STR, null))
			.onEndCmdStr(prefs.get(prefix + ON_END_CMD_STR, null))
			.createHooksConfig();
	}

	public HooksConfig loadForActiveHours(boolean after_active_hours)
	{
		syncPrefs();
		String prefix = hookPrefActiveHoursPrefix(after_active_hours);

		return new HooksConfig.Builder()
			.isNotificationHook(true)
			.startEnabled(prefs.getBoolean(prefix + START_ENABLED, false))
			.endEnabled(prefs.getBoolean(prefix + END_ENABLED, false))
			.startAudioIsDir(prefs.getBoolean(prefix + START_AUDIO_IS_DIR, false))
			.onStartAudioStr(prefs.get(prefix + ON_START_AUDIO_STR, null))
			.onEndAudioStr(prefs.get(prefix + ON_END_AUDIO_STR, null))
			.onStartCmdStr(prefs.get(prefix + ON_START_CMD_STR, null))
			.onEndCmdStr(prefs.get(prefix + ON_END_CMD_STR, null))
			.createHooksConfig();

	}

	public void saveActiveHoursHooks(@NotNull HooksConfig hooksConfig, boolean after_active_hours)
	{
		save(hooksConfig, hookPrefActiveHoursPrefix(after_active_hours));
	}

	public void save(@NotNull SingleBreakHooksConfig breakHooksConfig)
	{
		save(
			breakHooksConfig,
			hookPrefBreakPrefix(breakHooksConfig.getBreakType(), breakHooksConfig.isNotificationHook())
		);
	}

	/**
	 * Saves the values for the given {@link SingleBreakHooksConfig} in the preferences
	 *
	 * @param hooksConfig the object to be saved in the preferences
	 * @param prefix      the prefix for the preference name. This value will vary according to the
	 *                    {@link HooksConfig} object
	 */
	public void save(@NotNull HooksConfig hooksConfig, @NotNull String prefix)
	{
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

	public void save(@NotNull BreakHooks breakHooks)
	{
		if (breakHooks.getBreakHooksConf() != null)
			this.save(breakHooks.getBreakHooksConf());

		this.save(breakHooks.getNotificationHooksConf());
	}

	/**
	 * Constructs the prefix used to save/load break hooks preferences
	 *
	 * @param breakType            the break type for the saved preferences
	 * @param is_notification_hook this indicates whether or not the saved preferences are for a notification
	 * @return the prefix
	 */
	private String hookPrefBreakPrefix(@NotNull BreakType breakType, boolean is_notification_hook)
	{
		return breakType.getName() + (is_notification_hook ? " notification " : " break ") + "hooks ";
	}

	/**
	 * Constructs the prefix used to save/load active hours hooks preferences
	 *
	 * @param is_after_active_hours this indicates whether or not the saved preferences are for the after active
	 *                              hours hooks
	 * @return the prefix
	 */
	private String hookPrefActiveHoursPrefix(boolean is_after_active_hours)
	{
		return is_after_active_hours ? "after active hours hooks " : "before active hours hooks ";
	}
}
