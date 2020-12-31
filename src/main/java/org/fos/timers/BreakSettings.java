/*
 * Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
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

package org.fos.timers;

import org.fos.core.BreakType;

import java.util.Objects;

/**
 * Wrapper class containing information about the break settings including
 * working time settings
 * break settings
 * and a boolean value to indicate if the break is enabled or not
 */
public class BreakSettings
{
	private final BreakType breakType;
	private final HooksConfig hooksConfig;
	private Clock workClock;
	private Clock breakClock;
	private Clock postponeClock;
	private boolean is_enabled;
	private String notificationAudioPath;
	private String breakAudiosDirStr;

	private BreakSettings(
		final Clock workClock,
		final Clock breakClock,
		final Clock postponeClock,
		final BreakType breakType,
		final String notificationAudioPath,
		final String breakAudiosDirStr,
		final HooksConfig hooksConfig,
		final boolean is_enabled
	)
	{
		this.workClock = Objects.requireNonNull(workClock);
		this.breakClock = breakClock;
		this.postponeClock = Objects.requireNonNull(postponeClock);
		this.breakType = Objects.requireNonNull(breakType);
		this.is_enabled = is_enabled;
		this.notificationAudioPath = notificationAudioPath;
		this.breakAudiosDirStr = breakAudiosDirStr;
		this.hooksConfig = hooksConfig;
	}

	/**
	 * Use this method when you want ALL hooks threads to be interrupted and therefore stopped
	 * <p>
	 * In other words, this method will stop the audio and/or the command execution (if exists)
	 */
	synchronized public void stopHooks()
	{
		// TODO: stop hooks
	}

	/**
	 * Use this method to start the hooks associated to the break starting up
	 *
	 * @return false if the hooks are already executing, true otherwise
	 */
	synchronized public boolean startBreakHooks()
	{
		return this.stopBreakHooks() & this.stopNotificationHooks();
	}

	/**
	 * Use this method to stop the hooks associated to the break ending
	 *
	 * @return false if the hooks are already stopped, true otherwise
	 */
	synchronized public boolean stopBreakHooks()
	{
		// TODO: stop hooks
		return true;
	}

	/**
	 * Use this method to start the hooks associated to the notification showing up
	 *
	 * @return false if the hooks are already executing, true otherwise
	 */
	synchronized public boolean startNotificationHooks()
	{
		// TODO: start hooks
		return true;
	}

	/**
	 * Use this method to end the hooks associated to the notification disposing/closing
	 *
	 * @return false if the hooks are already stopped, true otherwise
	 */
	synchronized public boolean stopNotificationHooks()
	{
		// TODO: stop hooks
		return true;
	}

	public BreakType getBreakType()
	{
		return this.breakType;
	}

	public Clock getWorkTimerSettings()
	{
		return workClock;
	}

	public void setWorkTimerSettings(Clock workClock)
	{
		this.workClock = workClock;
	}

	public Clock getBreakTimerSettings()
	{
		return breakClock;
	}

	public void setBreakTimerSettings(Clock breakClock)
	{
		this.breakClock = breakClock;
	}

	public Clock getPostponeTimerSettings()
	{
		return this.postponeClock;
	}

	public void setPostponeTimerSettings(Clock postponeClock)
	{
		this.postponeClock = postponeClock;
	}

	public String getNotificationAudioPath()
	{
		return notificationAudioPath;
	}

	public void setNotificationAudioPath(String notificationAudioPath)
	{
		this.notificationAudioPath = notificationAudioPath;
	}

	public String getBreakAudiosDirStr()
	{
		return breakAudiosDirStr;
	}

	public void setBreakAudiosDirStr(String breakAudiosDirStr)
	{
		this.breakAudiosDirStr = breakAudiosDirStr;
	}

	public boolean isEnabled()
	{
		return this.is_enabled;
	}

	public void setEnabled(final boolean is_enabled)
	{
		this.is_enabled = is_enabled;
	}

	@Override
	public String toString()
	{
		return "BreakSettings{" +
			"breakType=" + breakType +
			", workClock=" + workClock +
			", breakClock=" + breakClock +
			", postponeClock=" + postponeClock +
			", is_enabled=" + is_enabled +
			", notificationAudioPath='" + notificationAudioPath + '\'' +
			", breakAudiosDirStr='" + breakAudiosDirStr + '\'' +
			'}';
	}

	public static class Builder
	{
		private Clock workClock;
		private Clock breakClock;
		private Clock postponeClock;
		private BreakType breakType;
		private String notificationAudioPath;
		private String breakAudiosDirStr;
		private boolean is_enabled = true;

		private HooksConfig hooksConfig;

		public Builder workTimerSettings(Clock workClock)
		{
			this.workClock = workClock;
			return this;
		}

		public Builder breakTimerSettings(Clock breakClock)
		{
			this.breakClock = breakClock;
			return this;
		}

		public Builder postponeTimerSettings(Clock postponeClock)
		{
			this.postponeClock = postponeClock;
			return this;
		}

		public Builder breakType(BreakType breakType)
		{
			this.breakType = breakType;
			return this;
		}

		public Builder notificationAudioPath(String notificationAudioPath)
		{
			this.notificationAudioPath = notificationAudioPath;
			return this;
		}

		public Builder breakAudiosDirStr(String breakAudiosDirStr)
		{
			this.breakAudiosDirStr = breakAudiosDirStr;
			return this;
		}

		public Builder enabled(boolean is_enabled)
		{
			this.is_enabled = is_enabled;
			return this;
		}

		public Builder hooksConfig(HooksConfig hooksConfig)
		{
			this.hooksConfig = hooksConfig;
			return this;
		}

		public BreakSettings createBreakSettings()
		{
			return new BreakSettings(
				this.workClock,
				this.breakClock,
				this.postponeClock,
				this.breakType,
				this.notificationAudioPath,
				this.breakAudiosDirStr,
				this.hooksConfig,
				this.is_enabled
			);
		}
	}
}
