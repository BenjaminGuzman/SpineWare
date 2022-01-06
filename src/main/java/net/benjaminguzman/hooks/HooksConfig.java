/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.net>
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

package net.benjaminguzman.hooks;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class HooksConfig
{
	protected boolean start_audio_is_dir;
	protected boolean is_notification_hook;

	protected boolean start_enabled; // flag to know if the start hooks should execute
	protected boolean end_enabled; // flag to know if the end hooks should execute

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
	@Nullable
	protected String onStartAudioStr;

	/**
	 * Path to the file to be played on notification/break termination
	 * <p>
	 * This WILL always contain a filepath regardless the {@link #start_audio_is_dir}
	 * If this is null, no sound should be played
	 */
	@Nullable
	protected String onEndAudioStr;

	/**
	 * the command (and arguments) to execute on break/notification start
	 * If this is null, no command will be executed
	 */
	@Nullable
	protected String onStartCmdStr;

	/**
	 * the command (and arguments) to execute on break/notification termination
	 */
	@Nullable
	protected String onEndCmdStr;

	public HooksConfig(
		boolean start_audio_is_dir,
		@Nullable String onStartAudioStr,
		@Nullable String onEndAudioStr,
		@Nullable String onStartCmdStr,
		@Nullable String onEndCmdStr,
		boolean start_enabled,
		boolean end_enabled,
		boolean is_notification_hook
	)
	{
		this.start_audio_is_dir = start_audio_is_dir;
		this.onStartAudioStr = onStartAudioStr;
		this.onEndAudioStr = onEndAudioStr;
		this.onStartCmdStr = onStartCmdStr;
		this.onEndCmdStr = onEndCmdStr;
		this.start_enabled = start_enabled;
		this.end_enabled = end_enabled;
		this.is_notification_hook = is_notification_hook;
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
		this.is_notification_hook = config.is_notification_hook;
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

	public boolean isStartAudioADirectory()
	{
		return start_audio_is_dir;
	}

	public @Nullable String getOnStartAudioStr()
	{
		return onStartAudioStr;
	}

	public @Nullable String getOnEndAudioStr()
	{
		return onEndAudioStr;
	}

	public @Nullable String getOnStartCmdStr()
	{
		return onStartCmdStr;
	}

	public @Nullable String getOnEndCmdStr()
	{
		return onEndCmdStr;
	}

	public boolean isNotificationHook()
	{
		return this.is_notification_hook;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HooksConfig that = (HooksConfig) o;
		return start_audio_is_dir == that.start_audio_is_dir
			&& is_notification_hook == that.is_notification_hook
			&& start_enabled == that.start_enabled
			&& end_enabled == that.end_enabled
			&& Objects.equals(onStartAudioStr, that.onStartAudioStr)
			&& Objects.equals(onEndAudioStr, that.onEndAudioStr)
			&& Objects.equals(onStartCmdStr, that.onStartCmdStr)
			&& Objects.equals(onEndCmdStr, that.onEndCmdStr);
	}

	@Override
	public String toString()
	{
		return "HooksConfig{" +
			"start_audio_is_dir=" + start_audio_is_dir +
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

		public Builder onStartAudioStr(@Nullable String onStartAudioStr)
		{
			this.onStartAudioStr = onStartAudioStr;
			return this;
		}

		public Builder onEndAudioStr(@Nullable String onEndAudioStr)
		{
			this.onEndAudioStr = onEndAudioStr;
			return this;
		}

		public Builder onStartCmdStr(@Nullable String onStartCmdStr)
		{
			this.onStartCmdStr = onStartCmdStr;
			return this;
		}

		public Builder onEndCmdStr(@Nullable String onEndCmdStr)
		{
			this.onEndCmdStr = onEndCmdStr;
			return this;
		}

		public HooksConfig createHooksConfig()
		{
			return new HooksConfig(
				this.start_audio_is_dir,
				this.onStartAudioStr,
				this.onEndAudioStr,
				this.onStartCmdStr,
				this.onEndCmdStr,
				this.start_enabled,
				this.end_enabled,
				this.is_notification_hook
			);
		}
	}
}
