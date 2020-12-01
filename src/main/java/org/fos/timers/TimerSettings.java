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

package org.fos.timers;

public class TimerSettings {
	private byte hours;
	private byte minutes;
	private byte seconds;

	private int hms_cache = -1;

	/**
	 * Constructs a ne TimerSettings object with the given arguments
	 * and the default is_enabled as true
	 *
	 * @param hours   configured hours
	 * @param minutes configured minutes
	 * @param seconds configured seconds
	 */
	public TimerSettings(final byte hours, final byte minutes, final byte seconds) {
		this.hours = hours < 0 ? 1 : hours;
		this.minutes = minutes < 0 ? 5 : minutes;
		this.seconds = seconds < 0 ? 10 : seconds;
	}

	/**
	 * Creates a new timer object with the given arguments
	 *
	 * @param h_m_s_as_seconds stands for Hours Minutes Seconds, therefore in this variable should be stored
	 *                         the number of hours, minutes and seconds
	 */
	public TimerSettings(final int h_m_s_as_seconds) {
		byte[] h_m_s = this.seconds2HoursMinutesSeconds(h_m_s_as_seconds);
		this.hours = h_m_s[0] < 0 ? 1 : h_m_s[0];
		this.minutes = h_m_s[1] < 0 ? 5 : h_m_s[1];
		this.seconds = h_m_s[2] < 0 ? 10 : h_m_s[2];
	}

	/**
	 * Copy constructor
	 *
	 * @param timerSettings the original object
	 */
	public TimerSettings(final TimerSettings timerSettings) {
		this(timerSettings.getHours(), timerSettings.getMinutes(), timerSettings.getSeconds());
		this.hms_cache = timerSettings.hms_cache;
	}

	/**
	 * Converts the given amount of seconds to hours, minutes and seconds
	 *
	 * @param h_m_s_as_seconds the hours minutes and seconds as seconds
	 *
	 * @return an array of ints of length 3, index 0 -> hours, index 1 -> minutes, index 2 -> seconds
	 */
	public byte[] seconds2HoursMinutesSeconds(final int h_m_s_as_seconds) {
		int seconds = h_m_s_as_seconds % 60;
		int hours_and_minutes_as_seconds = h_m_s_as_seconds - seconds;

		int minutes = (hours_and_minutes_as_seconds / 60) % 60;
		int hours_as_seconds = hours_and_minutes_as_seconds - minutes;

		int hours = hours_as_seconds / 60 / 60; // no modulus because it should be less than 24, also no need to call Math.floor

		return new byte[]{(byte) hours, (byte) minutes, (byte) seconds};
	}

	/**
	 * @return the hours minutes and seconds
	 * as seconds (all summed up)
	 */
	public int getHMSAsSeconds() {
		if (this.hms_cache == -1) // avoid computing this many times
			this.hms_cache = this.seconds + this.minutes * 60 + this.hours * 60 * 60;
		return this.hms_cache;
	}

	/**
	 * @return the hours minutes and seconds as a string with the format
	 * <hours>h <minutes>m <seconds>s
	 * if the hours, minutes or seconds are 0, they're omitted from the string
	 */
	public String getHMSAsString() {
		StringBuilder builder = new StringBuilder(20);

		if (this.hours != 0)
			builder.append(this.hours).append("h ");
		if (this.minutes != 0)
			builder.append(this.minutes).append("m ");
		if (this.seconds != 0)
			builder.append(this.seconds).append('s');

		return builder.toString().trim();

	}

	/**
	 * This method will subtract n_seconds to the internal hours, minutes and seconds
	 * If the total number of seconds is 0 already, this method will do nothing and return false
	 *
	 * @param n_seconds number of seconds to subtract
	 *
	 * @return true if the subtraction could be done, false otherwise
	 */
	public boolean subtractSeconds(byte n_seconds) {
		int new_hms_as_seconds = this.getHMSAsSeconds() - n_seconds;
		if (new_hms_as_seconds < 0)
			return false;

		byte[] hms = this.seconds2HoursMinutesSeconds(new_hms_as_seconds);

		this.hours = hms[0];
		this.minutes = hms[1];
		this.seconds = hms[2];

		this.hms_cache = -1; // invalidate the cache
		return true;
	}

	public byte getHours() {
		return hours;
	}

	public byte getMinutes() {
		return minutes;
	}

	public byte getSeconds() {
		return seconds;
	}
}
