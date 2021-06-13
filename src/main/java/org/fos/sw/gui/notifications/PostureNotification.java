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

package org.fos.sw.gui.notifications;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import org.fos.sw.SWMain;
import org.fos.sw.core.NotificationLocation;
import org.fos.sw.gui.Fonts;

public class PostureNotification extends AbstractNotification
{
	public PostureNotification()
	{
		super(4_000, NotificationLocation.BOTTOM_RIGHT);

		initComponents();
	}

	/**
	 * Use this function to initialize and add the components to the {@link #mainPanel}
	 */
	@Override
	public void initComponents()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 5;
		gbc.ipady = 5;

		JLabel startUpLabel = new JLabel(SWMain.messagesBundle.getString("spineware_has_started"));
		startUpLabel.setFont(Fonts.SANS_SERIF_BOLD_15);

		// add SW icon
		gbc.gridheight = 2;
		super.mainPanel.add(super.swIconLabel, gbc);

		// add start up label
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		super.mainPanel.add(startUpLabel, gbc);

		// add close button
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		super.mainPanel.add(super.closeBtn, gbc);
		super.closeBtn.addActionListener((ActionEvent e) -> this.dispose());

		super.showJDialog();
	}
}
