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

package org.fos.sw.hooks;

import java.util.Objects;
import org.fos.sw.timers.breaks.BreakType;
import org.jetbrains.annotations.NotNull;

public final class SingleBreakHooksConfig extends HooksConfig
{
	@NotNull
	private BreakType breakType;


	public SingleBreakHooksConfig(
		boolean start_audio_is_dir,
		String onStartAudioStr,
		String onEndAudioStr,
		String onStartCmdStr,
		String onEndCmdStr,
		boolean start_enabled,
		boolean end_enabled,
		boolean is_notification_hook,
		@NotNull BreakType breakType
	) throws InstantiationException
	{
		super(
			start_audio_is_dir,
			onStartAudioStr,
			onEndAudioStr,
			onStartCmdStr,
			onEndCmdStr,
			start_enabled,
			end_enabled,
			is_notification_hook
		);

		if (breakType == BreakType.DAY_BREAK && onEndAudioStr != null && onEndCmdStr != null && end_enabled)
			throw new InstantiationException("If break type is DAY BREAK, there is no ending parameters");

		this.breakType = Objects.requireNonNull(breakType);
	}

	public void updateAll(SingleBreakHooksConfig config)
	{
		super.updateAll(config);
		breakType = config.breakType;
	}

	public @NotNull BreakType getBreakType()
	{
		return this.breakType;
	}

	@Override
	public String toString()
	{
		return "SingleBreakHooksConfig{" +
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
		private BreakType breakType;
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

		public Builder breakType(BreakType breakType)
		{
			this.breakType = breakType;
			return this;
		}

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

		public SingleBreakHooksConfig createHooksConfig() throws InstantiationException
		{
			return new SingleBreakHooksConfig(
				start_audio_is_dir,
				onStartAudioStr,
				onEndAudioStr,
				onStartCmdStr,
				onEndCmdStr,
				start_enabled,
				end_enabled,
				is_notification_hook,
				breakType
			);
		}
	}
}
