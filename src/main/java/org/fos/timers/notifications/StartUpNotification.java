/*
 * Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
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

package org.fos.timers.notifications;

import org.fos.Fonts;
import org.fos.SWMain;
import org.fos.core.NotificationLocation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class StartUpNotification extends Notification
{
	public StartUpNotification()
	{
		super(4_000, NotificationLocation.BOTTOM_RIGHT);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.ipadx = 5;
		gridBagConstraints.ipady = 5;

		JLabel startUpLabel = new JLabel(SWMain.getMessagesBundle().getString("spineware_has_started"));
		startUpLabel.setFont(Fonts.SANS_SERIF_BOLD_15);

		// add SW icon
		gridBagConstraints.gridheight = 2;
		super.mainPanel.add(super.swIconLabel, gridBagConstraints);

		// add start up label
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 2;
		super.mainPanel.add(startUpLabel, gridBagConstraints);

		// add close button
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		super.mainPanel.add(super.closeBtn, gridBagConstraints);
		super.closeBtn.addActionListener((ActionEvent e) -> this.dispose());

		super.showJDialog();
	}

}
