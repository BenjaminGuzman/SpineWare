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

package net.benjaminguzman.timers.breaks;

import net.benjaminguzman.timers.WallClock;
import org.jetbrains.annotations.NotNull;

public abstract class ToDo implements Runnable, Comparable<ToDo>
{
	/**
	 * Number of seconds since unix epoch when the to do
	 * was executed
	 * <p>
	 * This value should be always in the past
	 */
	protected long last_execution_at;

	/**
	 * Time in seconds since the unix epoch when the to do
	 * should be executed
	 * <p>
	 * This value should be most of the time in the future
	 * It may be in the past only if the {@link #run()} is executing
	 */
	protected long next_execution_at;

	/**
	 * Tells if the to do has been cancelled
	 * and therefore {@link #run()} should not be executed
	 */
	protected boolean is_cancelled;

	/**
	 * Updates the execution times in the class.
	 * Execution times refers to the time a next execution should happen
	 * and the time the last execution happened
	 *
	 * @param timeoutTime The timeout configuration used to set the {@link #next_execution_at} value
	 */
	protected void updateExecutionTimes(@NotNull WallClock timeoutTime)
	{
		last_execution_at = System.currentTimeMillis() / 1_000;
		next_execution_at = last_execution_at + timeoutTime.getHMSAsSeconds();
	}

	/**
	 * Postpone the execution of this to do by a given amount of seconds
	 *
	 * @param postponed_seconds the number of seconds to postpone the to do
	 */
	public void postponeExecution(int postponed_seconds)
	{
		last_execution_at += postponed_seconds;
		next_execution_at += postponed_seconds;
	}

	/**
	 * Tell if the {@link #run()} should be executed or not
	 * <p>
	 * This method will take into account if the to do is cancelled
	 *
	 * @param now the number of seconds since unix epoch at the time this function is called
	 * @return true if the runnable should be executed, false otherwise
	 */
	public boolean shouldExecuteNow(long now)
	{
		return !is_cancelled && remainingSecondsForExecution(now) <= 0;
	}

	/**
	 * @return true if the To Do is cancelled and therefore should not be executed
	 */
	public boolean isCancelled()
	{
		return is_cancelled;
	}

	/**
	 * Sets whether or not the to do should be cancelled
	 *
	 * @param cancelled true if the to do should be cancelled
	 */
	public void setCancelled(boolean cancelled)
	{
		this.is_cancelled = cancelled;
	}

	/**
	 * @param now the number of seconds since unix epoch at the time this function is called
	 * @return the number of seconds remaining for the {@link #run()} to be executed
	 */
	public long remainingSecondsForExecution(long now)
	{
		return next_execution_at - now;
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * <p>
	 * A {@link BreakToDo} object is less than other object if the {@link #next_execution_at} is
	 * less than the {@link #next_execution_at} of the other object
	 *
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                              from being compared to this object.
	 */
	@Override
	public int compareTo(@NotNull ToDo o)
	{
		long diff = this.next_execution_at - o.next_execution_at;
		try {
			return Math.toIntExact(diff);
		} catch (ArithmeticException ex) {
			return diff > 0 ? 1
				: diff == 0 ? 0 : -1;
		}
	}
}
