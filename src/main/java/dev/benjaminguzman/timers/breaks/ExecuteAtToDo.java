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

import dev.benjaminguzman.timers.WallClock;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class ExecuteAtToDo extends ToDo
{
	/**
	 * the time when the execution should happen. This is a specific time of the day, it does
	 * not represent an amount of time. e. g. 6:30 means the execution should happen at
	 * 6:30, it does not represent the amount of time "six hours and 30 min"
	 */
	@NotNull
	private final WallClock executeAt;
	@NotNull
	private Runnable executable;

	public ExecuteAtToDo(@NotNull WallClock executeAt)
	{
		this.executeAt = executeAt;
		this.executable = () -> {
		};
		updateExecutionTimes();
	}

	/**
	 * @param executable the {@link Runnable} to execute when the time comes
	 * @param executeAt  the time when the execution should happen. This is a specific time of the day, it does
	 *                   not represent an amount of time. e. g. 6:30 means the execution should happen at
	 *                   6:30, it does not represent the amount of time "six hours and 30 min"
	 */
	public ExecuteAtToDo(
		@NotNull Runnable executable,
		@NotNull WallClock executeAt
	)
	{
		this(executeAt);
		this.executable = executable;
	}

	/**
	 * Method to execute when it is time to take the break
	 * and also update some values within this class
	 */
	@Override
	public void run()
	{
		if (is_cancelled || Thread.currentThread().isInterrupted())
			return;

		executable.run();
	}

	/**
	 * Updates the execution times in the class
	 * the update is done with the value set in {@link #executeAt}
	 */
	protected void updateExecutionTimes()
	{
		// use local time to calculate the next execution time
		LocalTime now = LocalTime.now();
		LocalTime executeAtLocalTime = LocalTime.of(
			executeAt.getHours(),
			executeAt.getMinutes(),
			executeAt.getSeconds()
		);

		// the execution time is in the past, add one day to the next execution time
		int offset = 0;
		if (now.compareTo(executeAtLocalTime) < 0)
			offset = 24 /* d -> h */ * 60 /* h -> m */ * 60 /* m -> s */;

		LocalTime execTime = now.plus(executeAt.getHMSAsSeconds() + offset, ChronoUnit.SECONDS);

		super.updateExecutionTimes(new WallClock(
			execTime.getHour(),
			execTime.getMinute(),
			execTime.getSecond()
		));
	}

	public void setExecutable(@NotNull Runnable executable)
	{
		this.executable = executable;
	}
}
