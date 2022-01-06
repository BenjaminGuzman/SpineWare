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

package net.benjaminguzman.core;

import java.util.Arrays;
import java.util.Optional;

public enum NotificationLocation
{
	BOTTOM_RIGHT(0),
	BOTTOM_LEFT(1),
	TOP_RIGHT(2),
	TOP_LEFT(3);

	private final int location_idx;

	NotificationLocation(int location_idx)
	{
		this.location_idx = location_idx;
	}

	public static Optional<NotificationLocation> getInstance(int preference_idx)
	{
		return Arrays.stream(NotificationLocation.values())
			.filter(notificationLocation -> notificationLocation.location_idx == preference_idx)
			.findAny();
	}

	/**
	 * @return the location index for the current notification
	 */
	public int getLocationIdx()
	{
		return this.location_idx;
	}

	@Override
	public String toString()
	{
		return "NotificationLocation{" +
			"location_idx=" + location_idx +
			'}';
	}
}
