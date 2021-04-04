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

import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.fos.sw.SWMain;
import org.fos.sw.gui.Initializable;
import org.fos.sw.gui.util.TimeInputComponent;
import org.fos.sw.timers.WallClock;
import org.jetbrains.annotations.Nullable;

public class PostponeTimeDialog extends JDialog implements Initializable
{
	@Nullable
	private WallClock postponeTime;

	private boolean cancelled;

	public PostponeTimeDialog(Dialog owner)
	{
		super(owner);
		initComponents();

		this.setUndecorated(true);
		this.setResizable(false);
		this.setType(Type.POPUP);
		this.setAlwaysOnTop(true);
		this.setLocation(owner.getX() + 10, owner.getY() + 10);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.pack();
		this.addWindowFocusListener(new WindowFocusListener()
		{
			@Override
			public void windowGainedFocus(WindowEvent e)
			{
			}

			@Override
			public void windowLostFocus(WindowEvent e)
			{
				cancelled = true;
				dispose();
			}
		});

		this.setVisible(true);
	}

	@Override
	public void initComponents()
	{
		ResourceBundle messagesBundle = SWMain.messagesBundle;

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new GridBagLayout());

		TimeInputComponent postponeTimeInput = new TimeInputComponent(
			new WallClock((byte) 0, (byte) 0, (byte) 5),
			new WallClock((byte) 0, (byte) 30, (byte) 0),
			new WallClock((byte) 0, (byte) 5, (byte) 0),
			true,
			TimeInputComponent.WarningLabelPosition.BOTTOM
		);
		postponeTimeInput.setHoursEnabled(false);
		JButton okButton = new JButton(messagesBundle.getString("ok"));
		JButton cancelButton = new JButton(messagesBundle.getString("cancel"));
		okButton.setHorizontalAlignment(JButton.CENTER);
		cancelButton.setHorizontalAlignment(JButton.CENTER);

		// add listeners
		cancelButton.addActionListener((ActionEvent evt) -> {
			cancelled = true;
			dispose();
		});
		okButton.addActionListener((ActionEvent evt) -> {
			if (!postponeTimeInput.checkInputValidity())
				return;
			postponeTime = WallClock.from(postponeTimeInput.getTime());
			dispose();
		});

		// add postpone time input
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.insets = new Insets(1, 3, 2, 3);
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		contentPane.add(
			new JLabel(SWMain.messagesBundle.getString("new_postpone_time")),
			gbc
		);

		++gbc.gridy;
		contentPane.add(postponeTimeInput, gbc);

		++gbc.gridy;
		gbc.gridwidth = 1;
		gbc.weightx = 0.5;
		gbc.anchor = GridBagConstraints.CENTER;
		contentPane.add(okButton, gbc);

		++gbc.gridx;
		contentPane.add(cancelButton, gbc);

		this.getRootPane().setDefaultButton(okButton);
	}

	/**
	 * @return the value of the selected postpone time. It may be null if the user didn't clicked "ok"
	 */
	public @Nullable WallClock getPostponeTime()
	{
		return postponeTime;
	}

	/**
	 * @return true if the dialog was cancelled, false otherwise
	 */
	public boolean wasCancelled()
	{
		return cancelled;
	}
}
