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

package net.benjaminguzman.gui.sections;

import net.benjaminguzman.gui.Hideable;
import net.benjaminguzman.gui.Initializable;
import net.benjaminguzman.gui.Showable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractSection extends JScrollPane implements Initializable, Hideable, Showable
{
	protected Window owner;

	/**
	 * Tells if {@link #initComponents()} has been called
	 * <p>
	 * When children classes override {@link #initComponents()} they should set this value to true
	 * (or just call super.initComponents())
	 */
	protected boolean initialized = false;

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
	public void initComponents() {
		initialized = true;
	}

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
