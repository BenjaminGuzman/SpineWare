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

package dev.benjaminguzman.gui.notifications;

import dev.benjaminguzman.SpineWare;
import dev.benjaminguzman.core.NotificationLocation;
import dev.benjaminguzman.cv.PostureStatus;
import dev.benjaminguzman.gui.Colors;
import dev.benjaminguzman.gui.Fonts;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

/**
 * Notification to tell the user she/he is in a bad posture.
 * Unlike other notification classes extending {@link AbstractNotification}
 * you'll have to invoke {@link #initComponents()} to show the notification
 */
public class PostureNotification extends AbstractNotification
{
	private double distance_to_cam;

	private JLabel messageLabel;
	private JButton dismissBtn;

	private String message;
	private Color textColor;
	private boolean show_dismiss_button = false;

	public PostureNotification(@NotNull NotificationLocation notifLocation)
	{
		super(-1, notifLocation);
		this.initComponents();
	}

	/**
	 * Set the posture status.
	 *
	 * @param postureStatus the posture status. Depending on this value a different message will be shown to the
	 *                      user. {@link PostureStatus#getMessage()} is the message shown to the user.
	 */
	public void setPostureStatus(@NotNull PostureStatus postureStatus)
	{
		if (postureStatus == PostureStatus.TOO_CLOSE)
			message = MessageFormat.format(
				postureStatus.getMessage(),
				this.distance_to_cam
			);
		else
			message = postureStatus.getMessage();

		show_dismiss_button = postureStatus == PostureStatus.USER_IS_AWAY;
		textColor = postureStatus == PostureStatus.USER_IS_AWAY ? Colors.WHITE : Colors.RED;
	}

	/**
	 * Sets the approximate distance to the camera.
	 * Call this method before calling {@link #setPostureStatus(PostureStatus)} with
	 * {@link PostureStatus#TOO_CLOSE} as it will format the message depending on the value set here.
	 *
	 * @param distance The approximate distance from the user's face to the camera.
	 */
	public void setDistanceToCam(double distance)
	{
		this.distance_to_cam = distance;
	}

	/**
	 * Shows the notification.
	 * If the notification is already showing, this method will only update the jdialog by calling
	 * {@link JDialog#revalidate()} and {@link JDialog#repaint()}.
	 * Be sure to update the status with {@link #setPostureStatus(PostureStatus)} before making this call
	 * <p>
	 * Otherwise it will call {@link #showJDialog()}
	 */
	public void showNotification()
	{
		assert SwingUtilities.isEventDispatchThread();

		messageLabel.setText(message);
		messageLabel.setForeground(textColor);
		dismissBtn.setEnabled(show_dismiss_button);
		if (this.isShowing()) {
			// if the notification is already showing, there is no need to call showJDialog again,
			// just update the values and the ui
			// value update should have been done in setPostureStatus
			this.pack(); // the text length may have changed. Recompute component sizes & positions
			this.revalidate();
			this.repaint();
		} else
			this.showJDialog();
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

		ActionListener closeLambda = (ActionEvent e) -> this.dispose();

		this.messageLabel = new JLabel();
		messageLabel.setFont(Fonts.SANS_SERIF_BOLD_15);

		dismissBtn = new JButton(SpineWare.messagesBundle.getString("notification_dismiss"));
		dismissBtn.addActionListener(closeLambda);

		// add SW icon
		gbc.gridheight = 2;
		super.mainPanel.add(super.swIconLabel, gbc);

		// add message label
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		super.mainPanel.add(messageLabel, gbc);

		// add close button
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		super.mainPanel.add(super.closeBtn, gbc);
		super.closeBtn.addActionListener(closeLambda);

		// add dismiss button
		gbc.gridx = 1;
		++gbc.gridy;
		super.mainPanel.add(dismissBtn, gbc);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		if (this.onDisposed != null)
			this.onDisposed.run();
	}
}
