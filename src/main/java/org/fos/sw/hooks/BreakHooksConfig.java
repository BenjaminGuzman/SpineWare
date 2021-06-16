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

package org.fos.sw.hooks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper class containing two {@link SingleBreakHooksConfig} objects
 * <p>
 * One for the notification hooks, and another for the break hooks
 */
public class BreakHooksConfig
{
	@NotNull
	private final SingleBreakHooksConfig notificationHooksConf;

	@Nullable
	private final SingleBreakHooksConfig breakHooksConf; // can be null if the break type is DAY_BREAK

	private final HooksExecutor executor;

	public BreakHooksConfig(@NotNull SingleBreakHooksConfig notificationHooksConf, @Nullable SingleBreakHooksConfig breakHooksConf)
	{
		this.notificationHooksConf = notificationHooksConf;
		this.breakHooksConf = breakHooksConf;
		this.executor = new HooksExecutor();
	}

	public @NotNull SingleBreakHooksConfig getNotificationHooksConf()
	{
		return notificationHooksConf;
	}

	public @Nullable SingleBreakHooksConfig getBreakHooksConf()
	{
		return breakHooksConf;
	}

	/**
	 * Use this method when you want ALL hooks threads to be interrupted and therefore stopped/shutdown
	 * <p>
	 * In other words, this method will stop the audio and/or the command execution (if exists)
	 */
	synchronized public void shutdown()
	{
		executor.stop();
	}

	/**
	 * Use this method to start the hooks associated to the break starting up
	 */
	synchronized public void onStartBreakHooks()
	{
		executor.stop();
		executor.setConfig(breakHooksConf);
		executor.runStart();
	}

	/**
	 * Use this method to stop the hooks associated to the break ending
	 */
	synchronized public void onEndBreakHooks()
	{
		executor.stop();
		executor.setConfig(breakHooksConf);
		executor.runEnd();
	}

	/**
	 * Use this method to start the hooks associated to the notification showing up
	 */
	synchronized public void onStartNotificationHooks()
	{
		executor.stop();
		executor.setConfig(notificationHooksConf);
		executor.runStart();
	}

	/**
	 * Use this method to end the hooks associated to the notification disposing/closing
	 */
	synchronized public void onEndNotificationHooks()
	{
		executor.stop();
		executor.setConfig(notificationHooksConf);
		executor.runEnd();
	}
}
