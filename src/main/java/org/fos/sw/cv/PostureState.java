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

/**
 * Wrapper for the parameters indicating the state of the user's posture
 * These parameters include:
 * 1.- Distance to the screen
 * 2.- Margins
 * 3.- Ratio between the face and the screen size
 */
public class PostureState
{
	private double distance;
	private boolean to_the_left;
	private boolean to_the_right;
	private boolean to_the_top;
	private boolean to_the_bottom;

	/**
	 * @param to_the_left   if true, indicates the user's face is to the very left of the cam
	 * @param to_the_right  if true, indicates the user's face is to the very right of the cam
	 * @param to_the_top    if true, indicates the user's face is to the very top of the cam
	 * @param to_the_bottom if true, indicates the user's face is to the very bottom of the cam
	 */
	public void updateMargins(
		boolean to_the_left,
		boolean to_the_right,
		boolean to_the_top,
		boolean to_the_bottom
	)
	{
		this.to_the_left = to_the_left;
		this.to_the_right = to_the_right;
		this.to_the_top = to_the_top;
		this.to_the_bottom = to_the_bottom;
	}

	public double getDistance()
	{
		return distance;
	}

	public void setDistance(double distance)
	{
		this.distance = distance;
	}

	public boolean isToTheLeft()
	{
		return to_the_left;
	}

	public boolean isToTheRight()
	{
		return to_the_right;
	}

	public boolean isToTheTop()
	{
		return to_the_top;
	}

	public boolean isToTheBottom()
	{
		return to_the_bottom;
	}

	public boolean isPostureOk()
	{
		return distance <= CVUtils.SAFE_DISTANCE_CM
			&& !to_the_left
			&& !to_the_right
			&& !to_the_top
			&& !to_the_bottom;
	}
}
