/*
 * Copyright (c) 2020. Benjamín Guzmán
 * Author: Benjamín Guzmán <bg@benjaminguzman.dev>
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

package org.fos;

import org.fos.timers.WorkingTimeTimer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
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

public class SysTrayMenu extends JDialog
{
	private final ActionListener onClickExitButton;
	private final ActionListener onClickOpenButton;

	private final int[] remaining_seconds_for_notifications;
	private final JProgressBar smallBreakProgressBar;
	private final JProgressBar stretchBreakProgressBar;
	private final JProgressBar dayBreakProgressBar;
	private Timer statusTimer;

	public SysTrayMenu(final JFrame owner, final ActionListener onClickExitButton, final ActionListener onClickOpenButton)
	{
		super(owner, "SpineWare");
		this.onClickExitButton = onClickExitButton;
		this.onClickOpenButton = onClickOpenButton;

		this.smallBreakProgressBar = new JProgressBar(0, 1);
		this.stretchBreakProgressBar = new JProgressBar(0, 1);
		this.dayBreakProgressBar = new JProgressBar(0, 1);

		this.setUndecorated(true);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.setModal(false);
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

		this.remaining_seconds_for_notifications = new int[3];
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

		InputStream inputStreamSWLogo = SWMain.getFileAsStream("/resources/media/SW_white.min.png");
		ImageIcon swLogoImageIcon = null;
		try {
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

		JButton exitButton = new JButton(SWMain.messagesBundle.getString("systray_exit"));
		JButton openButton = new JButton(SWMain.messagesBundle.getString("systray_open"));

		exitButton.addActionListener((ActionEvent evt) -> {
			this.setVisible(false);
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

		// small break progress bar
		gridBagConstraints.gridx = 0;
		++gridBagConstraints.gridy;
		mainPanel.add(new JLabel(SWMain.messagesBundle.getString("small_breaks_title")), gridBagConstraints);

		gridBagConstraints.gridx = 1;
		mainPanel.add(this.smallBreakProgressBar, gridBagConstraints);

		// stretch break progress bar
		gridBagConstraints.gridx = 0;
		++gridBagConstraints.gridy;
		mainPanel.add(new JLabel(SWMain.messagesBundle.getString("stretch_breaks_title")), gridBagConstraints);

		gridBagConstraints.gridx = 1;
		mainPanel.add(this.stretchBreakProgressBar, gridBagConstraints);

		// day break progress bar
		gridBagConstraints.gridx = 0;
		++gridBagConstraints.gridy;
		mainPanel.add(new JLabel(SWMain.messagesBundle.getString("day_break_title")), gridBagConstraints);

		gridBagConstraints.gridx = 1;
		mainPanel.add(this.dayBreakProgressBar, gridBagConstraints);

		return mainPanel;
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);

		if (!visible) {
			if (this.statusTimer != null)
				this.statusTimer.stop(); // if the user is not seeing the dialog, don't waste system resources
			return;
		}

		WorkingTimeTimer[] timers = new WorkingTimeTimer[]{
			SWMain.timersManager.getSmallBreaksWorkingTimer(),
			SWMain.timersManager.getStretchBreaksWorkingTimer(),
			SWMain.timersManager.getDayBreakWorkingTimer()
		};

		JProgressBar[] progressBars = new JProgressBar[]{
			this.smallBreakProgressBar,
			this.stretchBreakProgressBar,
			this.dayBreakProgressBar
		};

		boolean set_timer = false;

		for (byte i = 0; i < 3; ++i) {
			progressBars[i].setStringPainted(true);
			progressBars[i].setForeground(Colors.GREEN);
			progressBars[i].setBackground(Colors.RED_WINE);

			this.remaining_seconds_for_notifications[i] = -1;
			if (timers[i] == null) { // that timer is disabled
				progressBars[i].setString(SWMain.messagesBundle.getString("feature_disabled"));
				progressBars[i].setEnabled(false);
				progressBars[i].setBackground(Color.DARK_GRAY);
				continue;
			}

			this.remaining_seconds_for_notifications[i] = timers[i].getRemainingSeconds();
			set_timer = true;

			progressBars[i].setMaximum(timers[i].getWorkingTimeSeconds());
		}

		if (!set_timer)
			return;

		this.statusTimer = new Timer(1_000, (ActionEvent evt) -> {
			for (byte i = 0; i < 3; ++i) {
				if (this.remaining_seconds_for_notifications[i] > 0) { // small break
					--this.remaining_seconds_for_notifications[i];
					progressBars[i].setString(this.remaining_seconds_for_notifications[i] + "s");
					progressBars[i].setValue(this.remaining_seconds_for_notifications[i]);
				} else if (timers[i] != null) { // if notification is enabled but the notification is showing
					progressBars[i].setString(SWMain.messagesBundle.getString("notification_is_showing"));
					progressBars[i].setValue(0);
					this.remaining_seconds_for_notifications[i] = timers[i].getRemainingSeconds();
				}
			}
		});
		this.statusTimer.setInitialDelay(0);
		this.statusTimer.start();
	}
}
