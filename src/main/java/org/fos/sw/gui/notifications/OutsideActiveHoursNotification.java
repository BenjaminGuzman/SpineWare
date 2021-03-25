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

package org.fos.sw.gui.notifications;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.fos.sw.SWMain;
import org.fos.sw.core.NotificationLocation;
import org.fos.sw.gui.Fonts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OutsideActiveHoursNotification extends AbstractNotification
{

	public OutsideActiveHoursNotification(
		@NotNull NotificationLocation notificationLocation,
		@Nullable Runnable onShown,
		@Nullable Runnable onDisposed
	)
	{
		super(-1, notificationLocation);
		this.onShown = onShown;
		this.onDisposed = onDisposed;
		this.initComponents();
	}

	/**
	 * Use this function to initialize and add the components to the {@link #mainPanel}
	 */
	@Override
	public void initComponents()
	{
		ResourceBundle messagesBundle = SWMain.messagesBundle;

		// create warning working outside active hours label
		JLabel workingOutsideActiveHoursLabel = new JLabel(
			messagesBundle.getString("working_outside_active_hours")
		);
		workingOutsideActiveHoursLabel.setFont(Fonts.SANS_SERIF_BOLD_15);

		// create the extra information label
		JLabel extraInfoLabel = new JLabel(
			messagesBundle.getString("working_outside_active_hours_details")
		);

		// create ok/dismiss button
		JButton dismissButton = new JButton(messagesBundle.getString("ok"));
		dismissButton.addActionListener((ActionEvent evt) -> this.dispose());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 5;
		gbc.ipady = 5;

		// add SW icon
		gbc.gridheight = 3;
		super.mainPanel.add(super.swIconLabel, gbc);

		// add warning label
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		super.mainPanel.add(workingOutsideActiveHoursLabel, gbc);

		// add extra label
		++gbc.gridy;
		super.mainPanel.add(extraInfoLabel, gbc);

		// add dismiss button
		++gbc.gridy;
		super.mainPanel.add(dismissButton, gbc);

		super.showJDialog();
	}

	@Override
	public void dispose()
	{
		super.dispose();
		if (onDisposed != null)
			onDisposed.run();
	}
}
