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

package org.fos.timers;

public class HooksConfig
{
	/**
	 * Directory where the audio files to be played during break are stored
	 */
	protected String breakAudioDirStr;

	/**
	 * Path to the audio file to be played when the "take a break notification" shows
	 */
	protected String notificationShownAudioPathStr;

	/**
	 * Path to the audio file to be played when the "take a break notification" is closed/dismissed/postponed
	 */
	protected String notificationClosedAudioPathStr;

	/**
	 * Path to the audio file to be played when the break stops
	 */
	protected String breakEndAudioPathStr;

	/**
	 * Command to execute when the break starts
	 */
	protected String cmdStartStr;

	/**
	 * Command to execute when the break ends
	 */
	protected String cmdEndStr;

	public HooksConfig(Builder builder)
	{
		this.breakAudioDirStr = builder.breakAudioDirStr;
		this.notificationShownAudioPathStr = builder.notificationShownAudioPathStr;
		this.notificationClosedAudioPathStr = builder.notificationClosedAudioPathStr;
		this.breakEndAudioPathStr = builder.breakEndAudioPath;
		this.cmdStartStr = builder.cmdStartStr;
		this.cmdEndStr = builder.cmdEndStr;
	}

	public String getBreakAudioDirStr()
	{
		return breakAudioDirStr;
	}

	public String getNotificationShownAudioPathStr()
	{
		return notificationShownAudioPathStr;
	}

	public String getBreakEndAudioPathStr()
	{
		return breakEndAudioPathStr;
	}

	public String getNotificationClosedAudioPathStr()
	{
		return notificationClosedAudioPathStr;
	}

	public String getCmdStartStr()
	{
		return cmdStartStr;
	}

	public String getCmdEndStr()
	{
		return cmdEndStr;
	}

	public static class Builder
	{
		private String breakAudioDirStr;
		private String notificationShownAudioPathStr;
		private String notificationClosedAudioPathStr;
		private String breakEndAudioPath;
		private String cmdStartStr;
		private String cmdEndStr;

		/**
		 * Set the directory where all the audio files will be played during the break
		 *
		 * @param breakAudioDirStr the directory path
		 */
		public Builder breakAudioDirStr(String breakAudioDirStr)
		{
			this.breakAudioDirStr = breakAudioDirStr;
			return this;
		}

		/**
		 * Set the path to the file to play when the "take a break" notification is shown
		 *
		 * @param notificationShownAudioPathStr the notification sound path
		 */
		public Builder notificationShownAudioPathStr(String notificationShownAudioPathStr)
		{
			this.notificationShownAudioPathStr = notificationShownAudioPathStr;
			return this;
		}

		/**
		 * Set the path to the file to play when the "take a break" notification is closed/dismissed/postponed
		 *
		 * @param notificationClosedAudioPathStr the notification sound path
		 */
		public Builder notificationClosedAudioPathStr(String notificationClosedAudioPathStr)
		{
			this.notificationClosedAudioPathStr = notificationClosedAudioPathStr;
			return this;
		}

		/**
		 * Set the path to the file to play when the break has ended
		 *
		 * @param notificationEndAudioPathStr the notification sound path
		 */
		public Builder breakEndAudioPath(String notificationEndAudioPathStr)
		{
			this.breakEndAudioPath = notificationEndAudioPathStr;
			return this;
		}

		/**
		 * Set the command to execute when the break starts
		 *
		 * @param cmdStartStr the command (and arguments)
		 */
		public Builder cmdStartStr(String cmdStartStr)
		{
			this.cmdStartStr = cmdStartStr;
			return this;
		}

		/**
		 * Set the command to execute when the break ends
		 *
		 * @param cmdEndStr the command (and arguments)
		 */
		public Builder cmdEndStr(String cmdEndStr)
		{
			this.cmdEndStr = cmdEndStr;
			return this;
		}
	}
}
