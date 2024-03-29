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

package dev.benjaminguzman.timers.breaks;

public enum BreakType
{
	// IMPORTANT: PRESERVE THIS ORDERING, IF NOT BAD THING COULD HAPPEN WHEN ITERATING OVER THE ENUM VALUES
	SMALL_BREAK("small break", "small_breaks", (byte) 0),
	STRETCH_BREAK("stretch break", "stretch_breaks", (byte) 1),
	DAY_BREAK("day break", "day_break", (byte) 2);

	private final String messagesPrefix; // prefix to use in the messages bundle
	private final String breakName; // break name
	private final byte index; // all classes that work with breaks may have an array of objects for each break, this index will tell which object is from which break type

	BreakType(String breakName, String messagesPrefix, byte index)
	{
		this.breakName = breakName;
		this.index = index;
		this.messagesPrefix = messagesPrefix;
	}

	/**
	 * @return the name of the break (us it preferably to save/load stuff from the preferences)
	 */
	public String getName()
	{
		return this.breakName;
	}

	/**
	 * All classes that work with breaks may have an array of objects for each break, this index will tell which
	 * object is from which break type
	 *
	 * @return the index
	 */
	public byte getIndex()
	{
		return this.index;
	}

	/**
	 * Gets a break type from the given index
	 *
	 * @param idx the index, this can be 0, 1 or 2. If the index is not in range, this method is likely to throw
	 *            an ArrayIndexOutOfBoundsException
	 * @return the break type
	 * @throws ArrayIndexOutOfBoundsException if the values are not in range (0, 1 or 2)
	 */
	public static BreakType fromIndex(byte idx)
	{
		return BreakType.values()[idx];
	}

	/**
	 * @return prefix to use in the messages bundle
	 */
	public String getMessagesPrefix()
	{
		return this.messagesPrefix;
	}

	@Override
	public String toString()
	{
		return "BreakType{" +
			"messagesPrefix='" + messagesPrefix + '\'' +
			", breakName='" + breakName + '\'' +
			", index=" + index +
			'}';
	}
}
