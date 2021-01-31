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

package org.fos.sw.timers.breaks;

import javax.swing.SwingUtilities;
import org.fos.sw.core.NotificationLocation;
import org.fos.sw.gui.notifications.OutsideActiveHoursNotification;
import org.fos.sw.hooks.HooksExecutor;
import org.jetbrains.annotations.NotNull;

public class ActiveHoursToDo
{
	@NotNull
	private final HooksExecutor hooksExecutor;

	@NotNull
	private final ActiveHours activeHours;

	private NotificationLocation notificationLocation;

	public ActiveHoursToDo(@NotNull ActiveHours activeHours)
	{
		this.activeHours = activeHours;
		hooksExecutor = new HooksExecutor();
		hooksExecutor.setWaitTermination(true);
	}

	/**
	 * Executes the configured after end hooks
	 * This may be run on a separate thread
	 */
	public void executeAfterEndHooks()
	{
		SwingUtilities.invokeLater(() -> new OutsideActiveHoursNotification(
			notificationLocation,
			null,
			hooksExecutor::stop
		));

		hooksExecutor.setConfig(activeHours.getAfterEndHooks());
		hooksExecutor.runStart();
	}

	/**
	 * Executes the configured before start hooks
	 * This may be run on a separate thread
	 */
	public void executeBeforeStartHooks()
	{
		SwingUtilities.invokeLater(() -> new OutsideActiveHoursNotification(
			notificationLocation,
			null,
			hooksExecutor::stop
		));

		hooksExecutor.setConfig(activeHours.getBeforeStartHooks());
		hooksExecutor.runStart();
	}

	public ActiveHoursToDo setNotificationLocation(NotificationLocation notificationLocation)
	{
		this.notificationLocation = notificationLocation;
		return this;
	}

	public @NotNull ActiveHours getActiveHours()
	{
		return activeHours;
	}
}
