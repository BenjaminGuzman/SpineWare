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

package org.fos.sw.timers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.fos.sw.timers.breaks.ActiveHours;
import org.fos.sw.timers.breaks.BreakToDo;
import org.fos.sw.timers.breaks.BreakType;
import org.fos.sw.utils.DaemonThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainTimerLoop implements Runnable
{
	private static boolean instantiated = false;

	/**
	 * The number of seconds to wait before the {@link #run()} is invoked again
	 */
	public static final int UPDATE_RATE_S = 1;

	/**
	 * Flag to tell if the {@link #run()} should be stopped or not
	 * <p>
	 * It is an {@link AtomicBoolean} to avoid concurrency problems while reding in the {@link #run()} method
	 * from one thread, and updating its value on another thread
	 */
	private final AtomicBoolean stopped;

	/**
	 * A deque containing the list of todos
	 * (actions) to execute after a certain period of time
	 * <p>
	 * Contents in the deque should be sorted by next execution time
	 * <p>
	 * Note: This should be the ONE and ONLY reference in all the program
	 * and the list should have 3 elements in order specified by {@link BreakType}
	 */
	@NotNull
	private final ConcurrentHashMap<BreakType, BreakToDo> breaksToDoList;
	/**
	 * Factory used to create daemon threads
	 */
	@NotNull
	private final DaemonThreadFactory threadFactory;
	/**
	 * The thread of the executing to do break (if exists)
	 */
	private volatile Thread breakThreadRunning;

	/**
	 * The thread that handles the execution of the events when the user is working not during active hours
	 */
	private Thread notActiveHoursThread;

	/**
	 * The type of the break corresponding to the break executing in the thread {@link #breakThreadRunning}
	 */
	private volatile BreakType breakTypeRunning;

	/**
	 * The configuration for the active hours of the user
	 */
	@Nullable
	private volatile ActiveHours activeHours;

	private int active_hours_start_cache_s = -1, active_hours_end_cache_s = -1;

	private MainTimerLoop(@NotNull List<BreakToDo> breaksToDoList)
	{
		this.breaksToDoList = new ConcurrentHashMap<>(3);
		breaksToDoList.forEach(toDo -> this.breaksToDoList.put(toDo.getBreakConfig().getBreakType(), toDo));

		this.threadFactory = new DaemonThreadFactory();
		this.stopped = new AtomicBoolean(false);
	}

	private MainTimerLoop(@NotNull List<BreakToDo> breakToDoList, @NotNull ActiveHours activeHours)
	{
		this(breakToDoList);
		this.setActiveHours(activeHours);
	}

	/**
	 * Creates a new instance of the class {@link MainTimerLoop}
	 *
	 * @param todoList the list of the list of todos to be executed in this
	 * @return the new instance of the {@link MainTimerLoop} class
	 * @throws InstantiationException if the class have already been instantiated. A single instance should be
	 *                                created per application
	 */
	public static MainTimerLoop createMainTimer(@NotNull List<BreakToDo> todoList) throws InstantiationException
	{
		if (instantiated)
			throw new InstantiationException("A timer has already been created");
		instantiated = true;

		return new MainTimerLoop(todoList);
	}

	/**
	 * Creates a new instance of the class {@link MainTimerLoop}
	 *
	 * @param todoList    the list of the list of todos to be executed in this
	 * @param activeHours the preferred active hours
	 * @return the new instance of the {@link MainTimerLoop} class
	 * @throws InstantiationException if the class have already been instantiated. A single instance should be
	 *                                created per application
	 */
	public static MainTimerLoop createMainTimer(@NotNull List<BreakToDo> todoList, @NotNull ActiveHours activeHours) throws InstantiationException
	{
		if (instantiated)
			throw new InstantiationException("A timer has already been created");
		instantiated = true;

		return new MainTimerLoop(todoList, activeHours);
	}

	@Override
	public void run()
	{
		if (isBreakHappening()) {
			// if a break is happening, postpone all other breaks
			for (BreakToDo breakToDo : breaksToDoList.values()) {
				if (breakToDo.isCancelled()
					|| !breakToDo.getBreakConfig().isEnabled()
					|| breakToDo.getBreakConfig().getBreakType() == breakTypeRunning)
					continue;

				breakToDo.postponeExecution(UPDATE_RATE_S);
			}

			ensureUserIsWorkingDuringActiveHours();
			return;
		}

		long curr_s_since_epoch = System.currentTimeMillis() / 1_000;

		if (stopped.get()) {
			// if the main loop is stopped
			// postpone the execution of every to do
			for (BreakToDo breakToDo : breaksToDoList.values()) {
				if (breakToDo.isCancelled() || !breakToDo.getBreakConfig().isEnabled())
					continue;

				breakToDo.postponeExecution(UPDATE_RATE_S);
			}

			ensureUserIsWorkingDuringActiveHours();
			return;
		}

		// run the break that should be executed by now
		for (BreakToDo breakToDo : breaksToDoList.values()) {
			if (breakToDo.isCancelled()
				|| !breakToDo.getBreakConfig().isEnabled()
				|| !breakToDo.shouldExecuteNow(curr_s_since_epoch))
				continue;
			shutdown();

			breakThreadRunning = threadFactory.newThread(breakToDo);
			breakThreadRunning.setName("Thread for " + breakToDo.getBreakConfig().getBreakType().getName());
			breakThreadRunning.start();
			breakTypeRunning = breakToDo.getBreakConfig().getBreakType();
			break;
		}

		ensureUserIsWorkingDuringActiveHours();
	}

	private boolean isNotActiveHoursThreadRunning()
	{
		return notActiveHoursThread != null && notActiveHoursThread.isAlive() && !notActiveHoursThread.isInterrupted();
	}

	private void ensureUserIsWorkingDuringActiveHours()
	{
		if (activeHours == null)
			return;

		// if the alerts or whatever configured hooks is currently running, do nothing
		if (isNotActiveHoursThreadRunning())
			return;

		// check if the user is still working during active hours
		int curr_time_s = Math.toIntExact(System.currentTimeMillis() / 1_000);

		// the user is working before the start of the active hours
		if (curr_time_s < active_hours_start_cache_s) {
			notActiveHoursThread = threadFactory.newThread(activeHours::runBeforeStart);
			notActiveHoursThread.start();
		} else if (curr_time_s > active_hours_end_cache_s) {
			notActiveHoursThread = threadFactory.newThread(activeHours::runAfterEnd);
			notActiveHoursThread.start();
		}
	}

	/**
	 * Shutdowns the current break thread running
	 * (if there is a break happening) at the time of the invocation
	 */
	public void shutdown()
	{
		if (isBreakHappening())
			breakThreadRunning.interrupt();
	}

	/////////////
	// setters //
	/////////////

	/**
	 * Sets whether or not the main timer loop should be stopped
	 *
	 * @param stopped the flag indicating the status of the loop
	 */
	public void setStopped(boolean stopped)
	{
		this.stopped.set(stopped);
	}

	/**
	 * Updates the given break to do
	 *
	 * @param breakToDo the new updated value
	 */
	public void updateBreakToDo(@NotNull BreakToDo breakToDo)
	{
		if (breakToDo.getBreakConfig().getBreakType() == breakTypeRunning)
			shutdown();

		breaksToDoList.put(breakToDo.getBreakConfig().getBreakType(), breakToDo);
	}

	/**
	 * Updates the configured active hours
	 *
	 * @param activeHours the new active hours
	 */
	public void setActiveHours(@NotNull ActiveHours activeHours)
	{
		this.activeHours = activeHours;
		this.active_hours_start_cache_s = activeHours.getStart().getHMSAsSeconds();
		this.active_hours_end_cache_s = activeHours.getEnd().getHMSAsSeconds();
	}

	/**
	 * Updates the given break to do enabled status
	 *
	 * @param breakType the break type to update
	 * @param enabled   the flag indicating the status
	 */
	public void setBreakEnabled(@NotNull BreakType breakType, boolean enabled)
	{
		breaksToDoList.get(breakType).getBreakConfig().setEnabled(enabled);
		if (enabled)
			breaksToDoList.get(breakType).reloadTimes();
	}

	/////////////
	// getters //
	/////////////
	public @NotNull ConcurrentHashMap<BreakType, BreakToDo> getBreaksToDoList()
	{
		return breaksToDoList;
	}

	public boolean isBreakHappening()
	{
		return breakThreadRunning != null && breakThreadRunning.isAlive() && !breakThreadRunning.isInterrupted();
	}

	public boolean isStopped() {
		return stopped.get();
	}
}
