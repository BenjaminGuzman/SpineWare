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

package org.fos.hooks;

/**
 * Wrapper class containing two {@link HooksConfig} objects
 * <p>
 * One for the notification hooks, and another for the break hooks
 */
public class BreakHooksConfig
{
	private final HooksConfig notificationHooksConf;
	private final HooksConfig breakHooksConf; // can be null if the break type is DAY_BREAK

	private final HooksExecutor executor;

	public BreakHooksConfig(HooksConfig notificationHooksConf, HooksConfig breakHooksConf)
	{
		this.notificationHooksConf = notificationHooksConf;
		this.breakHooksConf = breakHooksConf;
		this.executor = new HooksExecutor();
	}

	/**
	 * Use this method when you want ALL hooks threads to be interrupted and therefore stopped
	 * <p>
	 * In other words, this method will stop the audio and/or the command execution (if exists)
	 */
	synchronized public void stopHooks()
	{
		this.executor.stop();
	}

	/**
	 * Use this method to start the hooks associated to the break starting up
	 */
	synchronized public void onStartBreakHooks()
	{
		this.executor.stop();
		this.executor.setConfig(this.breakHooksConf);
		this.executor.runStart();
	}

	/**
	 * Use this method to stop the hooks associated to the break ending
	 */
	synchronized public void onEndBreakHooks()
	{
		this.executor.stop();
		this.executor.setConfig(this.breakHooksConf);
		this.executor.runEnd();
	}

	/**
	 * Use this method to start the hooks associated to the notification showing up
	 */
	synchronized public void onStartNotificationHooks()
	{
		this.executor.stop();
		this.executor.setConfig(this.notificationHooksConf);
		this.executor.runStart();
	}

	/**
	 * Use this method to end the hooks associated to the notification disposing/closing
	 */
	synchronized public void onEndNotificationHooks()
	{
		this.executor.stop();
		this.executor.setConfig(this.notificationHooksConf);
		this.executor.runEnd();
	}
}
