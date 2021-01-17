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

package org.fos.hooks;

import java.util.Objects;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.fos.Loggers;
import org.fos.core.BreakType;
import org.jetbrains.annotations.NotNull;

public final class HooksConfig
{
	private boolean start_audio_is_dir;
	@NotNull
	private BreakType breakType;
	private boolean is_notification_hook;

	private boolean start_enabled; // flag to know if the start hooks should execute
	private boolean end_enabled; // flag to know if the end hooks should execute

	/**
	 * If {@link #start_audio_is_dir} is true this attribute will hold the directory path
	 * containing all files to be played on break start
	 * <p>
	 * If {@link #start_audio_is_dir} is false this attribute will hold the path to the file to be
	 * played on notification start
	 * <p>
	 * In other words
	 * - start_audio_is_dir == true -> directory
	 * - start_audio_is_dir == false -> single file
	 * <p>
	 * If this is null, then no sound should be played
	 */
	private String onStartAudioStr;

	/**
	 * Path to the file to be played on notification/break termination
	 * <p>
	 * This WILL always contain a filepath regardless the {@link #start_audio_is_dir}
	 */
	private String onEndAudioStr;

	// the command (and arguments) to execute on break/notification start
	private String onStartCmdStr;

	// the command (and arguments) to execute on break/notification termination
	private String onEndCmdStr;

	public HooksConfig(
		boolean start_audio_is_dir,
		String onStartAudioStr,
		String onEndAudioStr,
		String onStartCmdStr,
		String onEndCmdStr,
		boolean start_enabled,
		boolean end_enabled,
		@NotNull BreakType breakType,
		boolean is_notification_hook
	) throws InstantiationException
	{
		if (breakType == BreakType.DAY_BREAK && onEndAudioStr != null && onEndCmdStr != null && end_enabled)
			throw new InstantiationException("If break type is DAY BREAK, there is no ending parameters");
		if (is_notification_hook && start_audio_is_dir)
			throw new InstantiationException("If this hook is for notification, the audio MUST be a file," +
				" not a directory");

		this.start_audio_is_dir = start_audio_is_dir;
		this.onStartAudioStr = onStartAudioStr;
		this.onEndAudioStr = onEndAudioStr;
		this.onStartCmdStr = onStartCmdStr;
		this.onEndCmdStr = onEndCmdStr;
		this.start_enabled = start_enabled;
		this.end_enabled = end_enabled;
		this.breakType = Objects.requireNonNull(breakType);
		this.is_notification_hook = is_notification_hook;
	}

	/**
	 * Loads a {@link HooksConfig} object from preferences
	 *
	 * @param breakType            the break type for which the preferences will be loaded
	 * @param is_notification_hook tells if the preference you're looking for is a notification hook or a break hook
	 * @return the created object from preferences
	 * @see #savePrefs() savePrefs()
	 */
	synchronized public static HooksConfig fromPrefs(BreakType breakType, boolean is_notification_hook) throws InstantiationException
	{
		Preferences prefs = Preferences.userNodeForPackage(HooksConfig.class);
		try {
			prefs.sync();
		} catch (BackingStoreException e) {
			Loggers.getDebugLogger().log(Level.WARNING, "Couldn't sync preferences", e);
		}

		String prefix = HooksConfig.prefPrefix(breakType, is_notification_hook);

		return new Builder()
			.isNotificationHook(is_notification_hook)
			.startEnabled(prefs.getBoolean(prefix + "start enabled", false))
			.endEnabled(prefs.getBoolean(prefix + "end enabled", false))
			.startAudioIsDir(prefs.getBoolean(prefix + "start audio is dir", false))
			.breakType(breakType)
			.onStartAudioStr(prefs.get(prefix + "on start audio str", null))
			.onEndAudioStr(prefs.get(prefix + "on end audio str", null))
			.onStartCmdStr(prefs.get(prefix + "on start cmd str", null))
			.onEndCmdStr(prefs.get(prefix + "on end cmd str", null))
			.createHooksConfig();
	}

	public void updateAll(HooksConfig config)
	{
		this.start_audio_is_dir = config.start_audio_is_dir;
		this.onStartAudioStr = config.onStartAudioStr;
		this.onEndAudioStr = config.onEndAudioStr;
		this.onStartCmdStr = config.onStartCmdStr;
		this.onEndCmdStr = config.onEndCmdStr;
		this.start_enabled = config.start_enabled;
		this.end_enabled = config.end_enabled;
		this.breakType = config.breakType;
		this.is_notification_hook = config.is_notification_hook;
	}

	/**
	 * Constructs the prefix used to save preferences
	 *
	 * @param breakType            the break type for the saved preferences
	 * @param is_notification_hook this indicates whether or not the saved preferences are for a notification
	 * @return the prefix
	 */
	private static String prefPrefix(BreakType breakType, boolean is_notification_hook)
	{
		return breakType.getName() + (is_notification_hook ? " notification " : " break ") + "hooks ";
	}

	/**
	 * Saves the values for this instance in the preferences
	 *
	 * @see #savePrefs() the analogous function
	 */
	synchronized public void savePrefs()
	{
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		// add a prefix to each key to avoid collisions
		String prefix = HooksConfig.prefPrefix(breakType, is_notification_hook);

		prefs.putBoolean(prefix + "start enabled", this.start_enabled);
		prefs.putBoolean(prefix + "end enabled", this.end_enabled);
		prefs.putBoolean(prefix + "start audio is dir", this.start_audio_is_dir);

		if (this.onStartAudioStr == null)
			prefs.remove(prefix + "on start audio str");
		else
			prefs.put(prefix + "on start audio str", this.onStartAudioStr);

		if (this.onEndAudioStr == null)
			prefs.remove(prefix + "on end audio str");
		else
			prefs.put(prefix + "on end audio str", this.onEndAudioStr);

		if (this.onStartCmdStr == null)
			prefs.remove(prefix + "on start cmd str");
		else
			prefs.put(prefix + "on start cmd str", this.onStartCmdStr);

		if (this.onEndCmdStr == null)
			prefs.remove(prefix + "on end cmd str");
		else
			prefs.put(prefix + "on end cmd str", this.onEndCmdStr);

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Couldn't force preferences to be saved", e);
		}

	}

	/**
	 * Tells if the start hooks are enabled
	 *
	 * @return true if the start hooks should be executed
	 */
	public boolean isStartEnabled()
	{
		return this.start_enabled;
	}

	/**
	 * Tells if the end hooks are enabled
	 *
	 * @return true if the end hooks should be executed
	 */
	public boolean isEndEnabled()
	{
		return this.end_enabled;
	}

	public boolean startAudioIsDir()
	{
		return start_audio_is_dir;
	}

	public String getOnStartAudioStr()
	{
		return onStartAudioStr;
	}

	public String getOnEndAudioStr()
	{
		return onEndAudioStr;
	}

	public String getOnStartCmdStr()
	{
		return onStartCmdStr;
	}

	public String getOnEndCmdStr()
	{
		return onEndCmdStr;
	}

	public @NotNull BreakType getBreakType()
	{
		return this.breakType;
	}

	public boolean isNotificationHook()
	{
		return this.is_notification_hook;
	}

	@Override
	public String toString()
	{
		return "HooksConfig{" +
			"start_audio_is_dir=" + start_audio_is_dir +
			", breakType=" + breakType +
			", start_enabled=" + start_enabled +
			", end_enabled=" + end_enabled +
			", onStartAudioStr='" + onStartAudioStr + '\'' +
			", onEndAudioStr='" + onEndAudioStr + '\'' +
			", onStartCmdStr='" + onStartCmdStr + '\'' +
			", onEndCmdStr='" + onEndCmdStr + '\'' +
			'}';
	}

	public static class Builder
	{
		private boolean start_audio_is_dir;
		private boolean is_notification_hook;
		private BreakType breakType;
		private boolean start_enabled;
		private boolean end_enabled;

		/**
		 * If {@link #start_audio_is_dir} is true this attribute will hold the directory path
		 * containing all files to be played on break start
		 * <p>
		 * If {@link #start_audio_is_dir} is false this attribute will hold the path to the file to be
		 * played on notification start
		 * <p>
		 * In other words
		 * - start_audio_is_dir == true -> directory
		 * - start_audio_is_dir == false -> single file
		 * <p>
		 * If this is null, then no sound should be played
		 */
		private String onStartAudioStr;

		/**
		 * Path to the file to be played on notification/break termination
		 * <p>
		 * This WILL always contain a filepath regardless the {@link #start_audio_is_dir}
		 */
		private String onEndAudioStr;

		// the command (and arguments) to execute on break/notification start
		private String onStartCmdStr;

		// the command (and arguments) to execute on break/notification termination
		private String onEndCmdStr;

		public Builder startAudioIsDir(boolean start_audio_is_dir)
		{
			this.start_audio_is_dir = start_audio_is_dir;
			return this;
		}

		public Builder isNotificationHook(boolean is_notification_hook)
		{
			this.is_notification_hook = is_notification_hook;
			return this;
		}

		public Builder breakType(BreakType breakType)
		{
			this.breakType = breakType;
			return this;
		}

		public Builder startEnabled(boolean is_enabled)
		{
			this.start_enabled = is_enabled;
			return this;
		}

		public Builder endEnabled(boolean is_enabled)
		{
			this.end_enabled = is_enabled;
			return this;
		}

		public Builder onStartAudioStr(String onStartAudioStr)
		{
			this.onStartAudioStr = onStartAudioStr;
			return this;
		}

		public Builder onEndAudioStr(String onEndAudioStr)
		{
			this.onEndAudioStr = onEndAudioStr;
			return this;
		}

		public Builder onStartCmdStr(String onStartCmdStr)
		{
			this.onStartCmdStr = onStartCmdStr;
			return this;
		}

		public Builder onEndCmdStr(String onEndCmdStr)
		{
			this.onEndCmdStr = onEndCmdStr;
			return this;
		}

		public HooksConfig createHooksConfig() throws InstantiationException
		{
			return new HooksConfig(
				this.start_audio_is_dir,
				this.onStartAudioStr,
				this.onEndAudioStr,
				this.onStartCmdStr,
				this.onEndCmdStr,
				this.start_enabled,
				this.end_enabled,
				this.breakType,
				this.is_notification_hook
			);
		}
	}
}
