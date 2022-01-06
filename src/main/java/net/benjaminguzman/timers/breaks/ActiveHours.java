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

package net.benjaminguzman.timers.breaks;

import net.benjaminguzman.hooks.HooksConfig;
import net.benjaminguzman.timers.WallClock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper class to have the start and end of the active hours for the user
 * <p>
 * it also includes whether or not the user disabled this feature
 */
public class ActiveHours
{
	private boolean is_enabled;

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

	@Nullable
	private HooksConfig beforeStartHooks;
	@Nullable
	private HooksConfig afterEndHooks;

	public ActiveHours(@NotNull WallClock start, @NotNull WallClock end)
	{
		this(start, end, true);
	}

	public ActiveHours(@NotNull WallClock start, @NotNull WallClock end, boolean is_enabled)
	{
		this.start = start;
		this.end = end;
		this.is_enabled = is_enabled;
	}

	/**
	 * @param curr_time the local time as seconds
	 * @return true if the curr_time is before (is less than) the start time
	 */
	public boolean isBeforeStart(long curr_time)
	{
		return curr_time < start.getHMSAsSeconds();
	}

	/**
	 * @param curr_time the local time as seconds
	 * @return true if the curr_time is after (is greater than) the end time
	 */
	public boolean isAfterEnd(long curr_time)
	{
		return curr_time > end.getHMSAsSeconds();
	}

	/**
	 * @param curr_time the local time as seconds
	 * @return true if the curr_time is between the start and end time
	 */
	public boolean isWithinActiveHours(long curr_time)
	{
		return !isBeforeStart(curr_time) && !isAfterEnd(curr_time);
	}

	/**
	 * @return true if the local time is within the start and end time
	 */
	public boolean isWithinActiveHours()
	{
		return isWithinActiveHours(WallClock.localNow().getHMSAsSeconds());
	}

	public @Nullable HooksConfig getBeforeStartHooks()
	{
		return beforeStartHooks;
	}

	public ActiveHours setBeforeStartHooks(HooksConfig beforeStartHooks)
	{
		this.beforeStartHooks = beforeStartHooks;
		return this;
	}

	public @Nullable HooksConfig getAfterEndHooks()
	{
		return afterEndHooks;
	}

	public ActiveHours setAfterEndHooks(HooksConfig afterEndHooks)
	{
		this.afterEndHooks = afterEndHooks;
		return this;
	}

	public @NotNull WallClock getStart()
	{
		return start;
	}

	public @NotNull WallClock getEnd()
	{
		return end;
	}

	public boolean isEnabled()
	{
		return is_enabled;
	}

	public void setEnabled(boolean is_enabled)
	{
		this.is_enabled = is_enabled;
	}
}
