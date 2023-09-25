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

package dev.benjaminguzman.cv;

/**
 * Wrapper class for the measurements of an Ideal Focal Length
 */
public class IdealFocalLengthMeasure
{
	/**
	 * The distance at which {@link #ideal_focal_length} was obtained
	 */
	private final double distance;

	/**
	 * The obtained IDEAL (not real) focal length at {@link #distance}
	 */
	private final double ideal_focal_length;

	public IdealFocalLengthMeasure(double distance, double ideal_focal_length)
	{
		this.distance = distance;
		this.ideal_focal_length = ideal_focal_length;
	}

	public double getDistance()
	{
		return distance;
	}

	public double getIdealFocalLength()
	{
		return ideal_focal_length;
	}
}
