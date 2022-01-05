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

package net.benjaminguzman.gui.sections;

import java.awt.Window;
import javax.swing.JScrollPane;
import net.benjaminguzman.gui.Hideable;
import net.benjaminguzman.gui.Initializable;
import net.benjaminguzman.gui.Showable;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSection extends JScrollPane implements Initializable, Hideable, Showable
{
	protected Window owner;

	/**
	 * Sets the owner window, this may be used by the subclasses. Make sure you set the owner to avoid
	 * NullPointerExceptions later
	 *
	 * @param owner the owner
	 * @return this
	 */
	public AbstractSection setOwner(@NotNull Window owner)
	{
		this.owner = owner;
		return this;
	}

	/**
	 * Method to init the internal components of the section
	 * This method is intended to be called just once
	 */
	public abstract void initComponents();

	/**
	 * Configures general stuff in the scroll bar
	 */
	public void configScrollBar()
	{
		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.getHorizontalScrollBar().setUnitIncrement(16);
		this.getVerticalScrollBar().setUnitIncrement(16);
	}
}
