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

package dev.benjaminguzman.timers;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;

public class WallClock
{
	private byte hours;
	private byte minutes;
	private byte seconds;

	private int hms_cache = -1;

	public WallClock(int hours, int minutes, int seconds)
	{
		this((byte) hours, (byte) minutes, (byte) seconds);
		assert Byte.MIN_VALUE <= hours && hours <= Byte.MAX_VALUE;
		assert Byte.MIN_VALUE <= minutes && minutes <= Byte.MAX_VALUE;
		assert Byte.MIN_VALUE <= seconds && seconds <= Byte.MAX_VALUE;
	}

	/**
	 * Constructs a ne TimerSettings object with the given arguments
	 * and the default is_enabled as true
	 *
	 * @param hours   configured hours
	 * @param minutes configured minutes
	 * @param seconds configured seconds
	 */
	public WallClock(byte hours, byte minutes, byte seconds)
	{
		this.hours = hours < 0 ? 1 : hours;
		this.minutes = minutes < 0 ? 5 : minutes;
		this.seconds = seconds < 0 ? 10 : seconds;
	}

	/**
	 * Copy constructor
	 *
	 * @param wallClock the original object from which all values will be copied
	 */
	public WallClock(@NotNull WallClock wallClock)
	{
		this(wallClock.getHours(), wallClock.getMinutes(), wallClock.getSeconds());
		this.hms_cache = wallClock.hms_cache;
	}

	/**
	 * Converts the given amount of seconds to a clock with hours, minutes and seconds
	 *
	 * @param hms_as_seconds the hours minutes and seconds as seconds
	 * @return a new clock created based on the given seconds
	 */
	public static WallClock from(final int hms_as_seconds)
	{
		byte[] hms = WallClock.getHMSFromSeconds(hms_as_seconds);

		return new WallClock(hms[0], hms[1], hms[2]);
	}

	/**
	 * @return a {@link WallClock} object with the local time
	 */
	public static WallClock localNow()
	{
		LocalTime now = LocalTime.now();

		return new WallClock(
			now.getHour(),
			now.getMinute(),
			now.getSecond()
		);
	}

	/**
	 * Converts the given amount of seconds to a byte array with hours, minutes and seconds
	 * index 0 -> hours
	 * index 1 -> minutes
	 * index 2 -> seconds
	 *
	 * @param hms_as_seconds the hours minutes and seconds as seconds
	 * @return the byte array
	 */
	private static byte[] getHMSFromSeconds(long hms_as_seconds)
	{
		int seconds = (int) (hms_as_seconds % 60);
		int hours_and_minutes_as_seconds = (int) (hms_as_seconds - seconds);

		int minutes = (hours_and_minutes_as_seconds / 60) % 60;
		int hours_as_seconds = hours_and_minutes_as_seconds - minutes;

		int hours = hours_as_seconds / 60 / 60; // no modulus because it should be less than 24, also no need to call Math.floor

		return new byte[]{(byte) hours, (byte) minutes, (byte) seconds};
	}

	/**
	 * Converts the given amount of seconds to a string containing the number of hours, minutes and seconds
	 * equivalent to the given amount of seconds
	 *
	 * @param hms_as_seconds the hours minutes and seconds as seconds
	 * @return the string
	 */
	public static String getHMSFromSecondsAsString(long hms_as_seconds)
	{
		byte[] hms = getHMSFromSeconds(hms_as_seconds);
		return getHMSAsString(hms);
	}

	/**
	 * @param HMSAsSeconds the hours minutes and seconds as an array. The first index corresponds to the hours
	 * @return the given hours minutes and seconds concatenaded in a string separated by the corresponding SI units
	 */
	public static String getHMSAsString(byte[] HMSAsSeconds)
	{
		StringBuilder builder = new StringBuilder(20);

		if (HMSAsSeconds[0] != 0)
			builder.append(HMSAsSeconds[0]).append("h ");
		if (HMSAsSeconds[1] != 0)
			builder.append(HMSAsSeconds[1]).append("m ");
		if (HMSAsSeconds[2] != 0)
			builder.append(HMSAsSeconds[2]).append('s');

		return builder.toString().trim();
	}

	/**
	 * @return the hours minutes and seconds
	 * as seconds (all summed up)
	 */
	public int getHMSAsSeconds()
	{
		if (this.hms_cache == -1) // avoid computing this many times
			this.hms_cache = this.seconds + this.minutes * 60 + this.hours * 60 * 60;
		return this.hms_cache;
	}

	/**
	 * @return the hours minutes and seconds as a string with the format
	 * <hours>h <minutes>m <seconds>s
	 * if the hours, minutes or seconds are 0, they're omitted from the string
	 */
	public String getHMSAsString()
	{
		StringBuilder builder = new StringBuilder(20);

		if (this.hours != 0)
			builder.append(this.hours).append("h ");
		if (this.minutes != 0)
			builder.append(this.minutes).append("m ");
		if (this.seconds != 0)
			builder.append(this.seconds).append('s');

		return builder.toString().trim();

	}

	public void updateAll(byte hours, byte minutes, byte seconds)
	{
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}

	/**
	 * This method will subtract n_seconds to the internal hours, minutes and seconds
	 * If the total number of seconds is 0 already, this method will do nothing and return false
	 *
	 * @param n_seconds number of seconds to subtract
	 * @return true if the subtraction could be done, false otherwise
	 */
	public boolean subtractSeconds(byte n_seconds)
	{
		int new_hms_as_seconds = this.getHMSAsSeconds() - n_seconds;
		if (new_hms_as_seconds < 0)
			return false;

		byte[] hms = WallClock.getHMSFromSeconds(new_hms_as_seconds);

		this.hours = hms[0];
		this.minutes = hms[1];
		this.seconds = hms[2];

		this.hms_cache = -1; // invalidate the cache
		return true;
	}

	public byte getHours()
	{
		return hours;
	}

	public byte getMinutes()
	{
		return minutes;
	}

	public byte getSeconds()
	{
		return seconds;
	}

	@Override
	public String toString()
	{
		return "WallClock{" +
			"hours=" + hours +
			", minutes=" + minutes +
			", seconds=" + seconds +
			", hms_cache=" + hms_cache +
			'}';
	}
}
