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

package org.fos.sw.hooks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActiveHoursHooksConfig
{
	@NotNull
	private final SingleBreakHooksConfig notificationHooksConf;
	@Nullable
	private SingleBreakHooksConfig breakHooksConf; // can be null if the break type is DAY_BREAK

	private HooksExecutor executor;

	public ActiveHoursHooksConfig(@NotNull SingleBreakHooksConfig notificationHooksConf, @Nullable SingleBreakHooksConfig breakHooksConf)
	{
		this.notificationHooksConf = notificationHooksConf;
		this.breakHooksConf = breakHooksConf;
	}

	public void updateAll(SingleBreakHooksConfig notificationHooksConf, SingleBreakHooksConfig breakHooksConf)
	{
		this.notificationHooksConf.updateAll(notificationHooksConf);
		if (this.breakHooksConf != null)
			this.breakHooksConf.updateAll(breakHooksConf);
		else
			this.breakHooksConf = breakHooksConf;
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
	public void shutdown()
	{
		if (executor != null)
			this.executor.stop();
	}

	/**
	 * Use this method to start the hooks associated to the break starting up
	 */
	public void onStartBreakHooks()
	{
		ensureExecutorExists();
		executor.stop();
		executor.setConfig(breakHooksConf);
		executor.runStart();
	}

	/**
	 * Use this method to stop the hooks associated to the break ending
	 */
	public void onEndBreakHooks()
	{
		ensureExecutorExists();
		executor.stop();
		executor.setConfig(breakHooksConf);
		executor.runEnd();
	}

	/**
	 * Use this method to start the hooks associated to the notification showing up
	 */
	public void onStartNotificationHooks()
	{
		ensureExecutorExists();
		executor.stop();
		executor.setConfig(notificationHooksConf);
		executor.runStart();
	}

	/**
	 * Use this method to end the hooks associated to the notification disposing/closing
	 */
	public void onEndNotificationHooks()
	{
		ensureExecutorExists();
		executor.stop();
		executor.setConfig(notificationHooksConf);
		executor.runEnd();
	}

	private void ensureExecutorExists()
	{
		if (executor == null)
			executor = new HooksExecutor();
	}
}
