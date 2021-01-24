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

package org.fos.sw.timers.breaks;

import org.fos.sw.timers.WallClock;
import org.jetbrains.annotations.NotNull;

public class ActiveHours
{
	/**
	 * When the active hours start
	 * this should be less than or equal to {@link #end}
	 */
	@NotNull
	private final WallClock start;

	/**
	 * When the active hours end
	 * this should be greater than or equal to {@link #start}
	 */
	@NotNull
	private final WallClock end;


	public ActiveHours(@NotNull WallClock start, @NotNull WallClock end)
	{
		this.start = start;
		this.end = end;
	}

	/**
	 * Method to invoke if the current local time is before the specified in {@link #start}
	 */
	public void runBeforeStart()
	{

	}

	/**
	 * Method to invoke if the current local time is after the specified in {@link #end}
	 */
	public void runAfterEnd()
	{

	}

	public @NotNull WallClock getStart()
	{
		return start;
	}

	public @NotNull WallClock getEnd()
	{
		return end;
	}
}
