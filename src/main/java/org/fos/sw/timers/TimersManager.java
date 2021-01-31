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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.fos.sw.prefs.timers.TimersPrefsIO;
import org.fos.sw.timers.breaks.ActiveHours;
import org.fos.sw.timers.breaks.BreakConfig;
import org.fos.sw.timers.breaks.BreakToDo;
import org.fos.sw.timers.breaks.BreakType;
import org.fos.sw.utils.DaemonThreadFactory;
import org.jetbrains.annotations.NotNull;

public class TimersManager
{
	/**
	 * Make this volatile to remove visibility issues
	 * atomicity is not needed here (or at least it'd be an overkill)
	 */
	private static volatile boolean initialized;

	private static TimersPrefsIO prefsIO;

	private static MainTimerLoop mainTimerLoop;

	private static ScheduledExecutorService mainLoopExecutor;

	private TimersManager()
	{
		throw new RuntimeException(this.getClass().getName() + " cannot be instantiated");
	}

	/**
	 * Initiates some fields within the class
	 * <p>
	 * This method will not start the main loop, to do that call {@link #startMainLoop()}
	 *
	 * @throws InstantiationException if you call this method more than once
	 */
	public static void init() throws InstantiationException
	{
		init(true);
	}

	/**
	 * Only use this function with a true parameter if and only if you don't want to create the main loop
	 * This should be a rare case and almost never be used (it was written to allow test to execute without
	 * problems)
	 *
	 * @param create_main_timer_loop if true the main timer loop will be created
	 * @throws RuntimeException if you call this method more than once with a true parameter
	 */
	public static void init(boolean create_main_timer_loop) throws InstantiationException
	{
		if (TimersManager.initialized)
			throw new RuntimeException("You cannot invoke the TimersManager+init method more than once");

		prefsIO = new TimersPrefsIO();
		initialized = true;
		List<BreakToDo> toDoList = loadAndCreateBreakToDos();
		Optional<ActiveHours> activeHoursOptional = prefsIO.getActiveHoursPrefsIO().loadActiveHours(
			prefsIO.getHooksPrefsIO()
		);

		if (!create_main_timer_loop)
			return;

		if (activeHoursOptional.isPresent())
			mainTimerLoop = MainTimerLoop.createMainTimer(toDoList, activeHoursOptional.get());
		else
			mainTimerLoop = MainTimerLoop.createMainTimer(toDoList);
	}

	/**
	 * This method will start the main loop by using a {@link ScheduledExecutorService}
	 */
	public static void startMainLoop()
	{
		mainLoopExecutor = Executors.newSingleThreadScheduledExecutor(
			new DaemonThreadFactory("Main-Loop-Timer-Thread", Thread.MAX_PRIORITY)
		);
		mainLoopExecutor.scheduleAtFixedRate(
			mainTimerLoop,
			0,
			MainTimerLoop.UPDATE_RATE_S,
			TimeUnit.SECONDS
		);
	}

	public static void shutdownAllThreads()
	{
		mainTimerLoop.shutdownAllThreads();
	}

	/**
	 * Sets whether or not the main loop should be stopped
	 * <p>
	 * This will not cancel the main loop, it will just indicate that the main loop should do nothing
	 *
	 * @param stopped the stopped status of the main loop
	 */
	public static void setMainLoopStopped(boolean stopped)
	{
		mainTimerLoop.setStopped(stopped);
	}

	public static boolean mainLoopIsStopped()
	{
		return mainTimerLoop.isStopped();
	}

	/**
	 * Loads the configuration for each break {@link BreakConfig} and creates the corresponding {@link BreakToDo}
	 * objects
	 */
	private static List<BreakToDo> loadAndCreateBreakToDos()
	{
		// create the corresponding break to do according to the break config
		return prefsIO.loadBreaksConfig()
			.stream()
			.map(BreakToDo::from)
			.collect(Collectors.toList());
	}

	/**
	 * shutdowns all timers and stops the execution of the main timer loop
	 */
	public static void killAllTimers()
	{
		mainTimerLoop.shutdownBreakThread();
		if (mainLoopExecutor != null)
			mainLoopExecutor.shutdownNow();
	}

	/**
	 * Disables/enables a single break
	 *
	 * @param breakType the break type that will be disabled
	 */
	public static void setBreakEnabled(BreakType breakType, boolean enabled)
	{
		// update value in preferences
		prefsIO.setBreakEnabled(breakType, enabled);

		// also update the value in memory
		mainTimerLoop.setBreakEnabled(breakType, enabled);
	}

	/**
	 * Saves the break configuration in preferences and updates the memory value of the corresponding break config
	 *
	 * @param breakConfig the new break configuration
	 */
	public static void saveBreakConfig(@NotNull BreakConfig breakConfig)
	{
		// update value in preferences
		prefsIO.saveBreakConfig(breakConfig);

		// update value in memory
		mainTimerLoop.updateBreakToDo(BreakToDo.from(breakConfig));
	}

	/**
	 * Saves the list of break configurations
	 * <p>
	 * Note: the method {@link #saveBreakConfig(BreakConfig)} is preferred
	 * as it only updates a single value
	 *
	 * @param breaksConf the break configuration
	 */
	public static void saveBreaksConfig(List<BreakConfig> breaksConf)
	{
		// update value in preferences
		prefsIO.saveBreaksConfig(breaksConf);

		// update value in memory
		breaksConf
			.stream()
			.map(BreakToDo::from)
			.forEach(mainTimerLoop::updateBreakToDo);
	}

	public static void saveActiveHours(@NotNull ActiveHours activeHours) throws InstantiationException
	{
		// update value in preferences
		prefsIO.getActiveHoursPrefsIO().saveActiveHoursPref(activeHours);

		// update value in memory
		mainTimerLoop.setActiveHours(activeHours);
	}

	public static void setActiveHoursEnabled(boolean enabled)
	{
		// update value in preferences
		prefsIO.getActiveHoursPrefsIO().setActiveHoursEnabled(enabled);

		// update value in memory
		mainTimerLoop.setActiveHoursEnabled(enabled);
	}

	/////////////
	// getters //
	/////////////
	public static ConcurrentHashMap<BreakType, BreakToDo> getToDoList()
	{
		return mainTimerLoop.getBreaksToDoList();
	}

	public static TimersPrefsIO getPrefsIO() {
		return prefsIO;
	}

	public static boolean isBreakHappening() {
		return mainTimerLoop.isBreakHappening();
	}
}
