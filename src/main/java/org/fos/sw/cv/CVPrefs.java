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

package org.fos.sw.cv;

import org.fos.sw.core.NotificationLocation;

/**
 * Wrapper class containing all the cv configuration and preferences for the user
 */
public class CVPrefs
{
	public final double ideal_f_length;
	public final double margin_x;
	public final double margin_y;
	public final boolean is_enabled;

	/**
	 * Refresh rate in milliseconds
	 */
	public final int refresh_rate;
	public final NotificationLocation notifLocation;

	/**
	 * @param margin_x       the margin x, this value ideally goes from 0.1 to 0.4 if it is 0.1 then the user can
	 *                       move 90% away from the cam center in the X direction without triggering an
	 *                       alert of bad posture
	 * @param margin_y       the margin y, this value ideally goes from 0.1 to 0.4 if it is 0.1 then the user can
	 *                       move 90% away from the cam center in the Y direction without triggering an
	 *                       alert of bad posture
	 * @param ideal_f_length the ideal focal length, this is NOT the real focal length, just the ideal focal
	 *                       length used to approximate the distance to the camera
	 * @param is_enabled     indicates if the CV feature is enabled or not
	 */
	public CVPrefs(
		double margin_x,
		double margin_y,
		double ideal_f_length,
		boolean is_enabled,
		int refresh_rate,
		NotificationLocation notifLocation
	)
	{
		this.margin_x = margin_x;
		this.margin_y = margin_y;
		this.ideal_f_length = ideal_f_length;
		this.is_enabled = is_enabled;
		this.refresh_rate = refresh_rate;
		this.notifLocation = notifLocation;
	}

	@Override
	public String toString()
	{
		return "CVPrefs{" +
			"ideal_f_length=" + ideal_f_length +
			", margin_x=" + margin_x +
			", margin_y=" + margin_y +
			", is_enabled=" + is_enabled +
			'}';
	}
}
