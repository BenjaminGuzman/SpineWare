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

package dev.benjaminguzman.gui;

public interface Hideable
{
	/**
	 * Method to be invoked if the section will not be visible anymore
	 * Use this method to free any resources the panel required during its lifetime.
	 * Be aware its counterpart {@link Showable#onShown()} may be invoked again
	 *
	 * @see #onDispose()
	 */
	void onHide();

	/**
	 * Method to be invoked when the container is being disposed
	 * Difference with {@link #onHide()} is that this method should free resources as if it is never going to be
	 * shown again. That is {@link Showable#onShown()} will be never be invoked after this call
	 *
	 * @see #onHide()
	 */
	default void onDispose()
	{
		onHide();
	}
}
