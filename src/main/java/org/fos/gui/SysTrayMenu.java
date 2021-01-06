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

package org.fos.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.core.BreakType;
import org.fos.core.TimersManager;
import org.fos.timers.Clock;
import org.fos.timers.WorkingTimeTimer;

public class SysTrayMenu extends JDialog
{
	private final ActionListener onClickExitButton;
	private final ActionListener onClickOpenButton;

	private final Clock[] remainingNotificationTimes;
	private final List<JProgressBar> breaksProgressBars;
	private Timer statusTimer;

	public SysTrayMenu(final JFrame owner, final ActionListener onClickExitButton, final ActionListener onClickOpenButton)
	{
		super(owner, "SpineWare");
		this.onClickExitButton = onClickExitButton;
		this.onClickOpenButton = onClickOpenButton;

		this.breaksProgressBars = new LinkedList<>();
		this.breaksProgressBars.add(new JProgressBar(0, 1)); // for small break
		this.breaksProgressBars.add(new JProgressBar(0, 1)); // for stretch break
		this.breaksProgressBars.add(new JProgressBar(0, 1)); // for day break

		this.setUndecorated(true);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.setType(Type.POPUP);
		this.setModalityType(ModalityType.MODELESS);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		JPanel mainPanel = this.initComponents();
		this.setContentPane(mainPanel);
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
				SysTrayMenu.this.setVisible(false);
			}
		});

		this.remainingNotificationTimes = new Clock[3];
	}

	/**
	 * Creates the menu components
	 * this method will handle all the GUI stuff
	 *
	 * @return the panel containing all the elements the menu should have
	 */
	public JPanel initComponents()
	{
		JPanel mainPanel = new JPanel(new GridBagLayout());
		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		ImageIcon swLogoImageIcon = null;
		try (InputStream inputStreamSWLogo = SWMain.getFileAsStream("/resources/media/SW_white.min.png")) {
			Image img = ImageIO.read(inputStreamSWLogo);
			img = img.getScaledInstance(50, 44, Image.SCALE_AREA_AVERAGING);
			swLogoImageIcon = new ImageIcon(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel swLogoImageLabel;
		if (swLogoImageIcon != null)
			swLogoImageLabel = new JLabel(swLogoImageIcon);
		else
			swLogoImageLabel = new JLabel("SW");

		JButton exitButton = new JButton(messagesBundle.getString("systray_exit"));
		JButton openButton = new JButton(messagesBundle.getString("systray_open"));

		exitButton.addActionListener((ActionEvent evt) -> {
			this.dispose();
			this.onClickExitButton.actionPerformed(evt);
		});
		openButton.addActionListener((ActionEvent evt) -> {
			this.setVisible(false);
			this.onClickOpenButton.actionPerformed(evt);
		});

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.ipady = 10;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;

		gridBagConstraints.gridwidth = 2;
		mainPanel.add(swLogoImageLabel, gridBagConstraints);

		++gridBagConstraints.gridy;
		mainPanel.add(exitButton, gridBagConstraints);

		++gridBagConstraints.gridy;
		mainPanel.add(openButton, gridBagConstraints);

		gridBagConstraints.gridwidth = 1;

		// TODO: ADD PAUSE BUTTON

		// small break progress bar
		gridBagConstraints.gridx = 0;
		++gridBagConstraints.gridy;
		mainPanel.add(new JLabel(messagesBundle.getString("small_breaks_title")), gridBagConstraints);

		gridBagConstraints.gridx = 1;
		mainPanel.add(this.breaksProgressBars.get(BreakType.SMALL_BREAK.getIndex()), gridBagConstraints);

		// stretch break progress bar
		gridBagConstraints.gridx = 0;
		++gridBagConstraints.gridy;
		mainPanel.add(new JLabel(messagesBundle.getString("stretch_breaks_title")), gridBagConstraints);

		gridBagConstraints.gridx = 1;
		mainPanel.add(this.breaksProgressBars.get(BreakType.STRETCH_BREAK.getIndex()), gridBagConstraints);

		// day break progress bar
		gridBagConstraints.gridx = 0;
		++gridBagConstraints.gridy;
		mainPanel.add(new JLabel(messagesBundle.getString("day_break_title")), gridBagConstraints);

		gridBagConstraints.gridx = 1;
		mainPanel.add(this.breaksProgressBars.get(BreakType.DAY_BREAK.getIndex()), gridBagConstraints);

		return mainPanel;
	}

	/**
	 * Sets the system tray menu (panel) as visible or not
	 * If it is going to be visible, the progress bars are updated and a timer to update them every second is set
	 * If not, the timer (if exists) is stopped and the dialog is not visible
	 *
	 * @param visible whether the dialog should be visible or not
	 */
	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		if (!visible) {
			if (this.statusTimer != null)
				this.statusTimer.stop(); // if the user is not seeing the dialog, don't waste resources
			return;
		}

		List<WorkingTimeTimer> activeWorkingTimers = TimersManager.getActiveWorkingTimers();
		BreakType[] breakTypes = BreakType.values();

		if (activeWorkingTimers.size() != breakTypes.length) {
			Loggers.getErrorLogger().severe(
				"The activeWorkingTimers list object doesn't has exact same size as the enum break types, current size: "
					+ activeWorkingTimers.size()
			);
			return;
		}

		boolean set_timer = false;
		for (BreakType breakType : breakTypes) {
			byte break_idx = breakType.getIndex();
			JProgressBar progressBar = this.breaksProgressBars.get(break_idx);

			progressBar.setStringPainted(true);
			progressBar.setForeground(Colors.GREEN_DARK);
			progressBar.setBackground(Colors.RED_WINE);

			this.remainingNotificationTimes[break_idx] = null;
			if (activeWorkingTimers.get(break_idx) == null) { // that timer is disabled
				progressBar.setString(messagesBundle.getString("feature_disabled"));
				progressBar.setValue(0);
				progressBar.setEnabled(false);
				progressBar.setBackground(Color.DARK_GRAY);
				continue;
			} else if (activeWorkingTimers.get(break_idx).getRemainingSToShowNotif() < 0) {
				// notification is showing

				progressBar.setString(messagesBundle.getString("notification_is_showing"));
				progressBar.setValue(0);
				progressBar.setBackground(Colors.RED_WINE);
				progressBar.setMaximum(activeWorkingTimers.get(break_idx).getWorkingTimeSeconds());
				set_timer = true;
				continue;
			}

			this.remainingNotificationTimes[break_idx] =
				Clock.from(activeWorkingTimers.get(break_idx).getRemainingSToShowNotif());

			progressBar.setMaximum(activeWorkingTimers.get(break_idx).getWorkingTimeSeconds());

			set_timer = true;
		}

		if (!set_timer)
			return;

		final byte one = 1;
		// set timer to update progress bar value each second
		this.statusTimer = new Timer(1_000, (ActionEvent evt) -> {
			for (BreakType breakType : breakTypes) {
				byte i = breakType.getIndex();
				if (this.remainingNotificationTimes[i] != null &&
					this.remainingNotificationTimes[i].getHMSAsSeconds() > 0) {
					// update the progress bar
					this.breaksProgressBars.get(i).setString(
						this.remainingNotificationTimes[i].getHMSAsString()
					);
					this.breaksProgressBars.get(i).setValue(
						this.remainingNotificationTimes[i].getHMSAsSeconds()
					);

					// update the remaining notification time
					this.remainingNotificationTimes[i].subtractSeconds(one);
				} else if (activeWorkingTimers.get(i) != null) { // if notification is enabled and the
					// notification is showing

					this.breaksProgressBars.get(i).setString(
						messagesBundle.getString("notification_is_showing")
					);
					this.breaksProgressBars.get(i).setValue(0);

					// negate the value as the notification should be shown
					int elapsed_s_since_notification_shown =
						-activeWorkingTimers.get(i).getRemainingSToShowNotif();

					// helpful log to understand the code:
					// System.out.println("Elapsed seconds since notification was shown: " +
					// elapsed_s_since_notification_shown);

					if (elapsed_s_since_notification_shown >= 0)
						return;

					// reset the timer if the notification has been disposed
					this.remainingNotificationTimes[i] = Clock.from(
						activeWorkingTimers.get(i).getRemainingSToShowNotif() - 1
					);

					// update the progressbar
					this.breaksProgressBars.get(i).setString(
						this.remainingNotificationTimes[i].getHMSAsString()
					);
					this.breaksProgressBars.get(i).setValue(
						this.remainingNotificationTimes[i].getHMSAsSeconds()
					);
				}
			}
		});
		this.statusTimer.setInitialDelay(0);
		this.statusTimer.start();
	}

	@Override
	public void dispose()
	{
		super.dispose();
		if (this.statusTimer != null)
			this.statusTimer.stop();
	}

	@Override
	public String toString()
	{
		return "SysTrayMenu{" +
			"onClickExitButton=" + onClickExitButton +
			", onClickOpenButton=" + onClickOpenButton +
			", remaining_seconds_for_notifications=" + Arrays.toString(this.remainingNotificationTimes) +
			", breaksProgressBars=" + breaksProgressBars +
			", statusTimer=" + statusTimer +
			'}';
	}
}