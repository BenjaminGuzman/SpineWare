/*
 * Copyright © 2020 Benjamín Guzmán
 * Author: Benjamín Guzmán <9benjaminguzman@gmail.com>
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
import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.timers.TimerSettings;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

public class BreakCountDown extends JDialog {
	private static ImageIcon spineWareIcon; // static to avoid reading the image each time the dialog is shown

	private final TimerSettings breakSettings;
	private final CountDownLatch countDownLatch;

	private final JLabel[] hmsRemainingTimeLabels;

	private final Timer timerCountDown;

	public BreakCountDown(final String breakMessage, final TimerSettings breakSettings, final CountDownLatch countDownLatch) {
		super();
		assert !SwingUtilities.isEventDispatchThread();

		//this.breakSettings = new TimerSettings(breakSettings); // we need a copy because this class will modify it
		this.breakSettings = new TimerSettings(breakSettings);
		//this.remaining_s = this.breakSettings;
		this.countDownLatch = countDownLatch;

		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// add SpineWare logo
		this.loadSpineWareIcon();
		JLabel spineWareIconLabel = new JLabel();
		if (BreakCountDown.spineWareIcon != null)
			spineWareIconLabel.setIcon(BreakCountDown.spineWareIcon);
		else
			spineWareIconLabel.setText("SW");

		// create break time message label
		JLabel breakMessageLabel = new JLabel(breakMessage + " (" + breakSettings.getHMSAsString() + ")",
			SwingConstants.CENTER);
		breakMessageLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));

		// create remaining time message label
		JLabel remainingTimeLabel = new JLabel(SWMain.messagesBundle.getString("remaining_break_time"));
		remainingTimeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

		// create actual remaining time
		this.hmsRemainingTimeLabels = new JLabel[]{
			new JLabel(String.valueOf(this.breakSettings.getHours())),
			new JLabel(String.valueOf(this.breakSettings.getMinutes())),
			new JLabel(String.valueOf(this.breakSettings.getSeconds()))
		};

		// create cancel button
		JButton cancelButton = new JButton(SWMain.messagesBundle.getString("cancel"));
		cancelButton.addActionListener((ActionEvent evt) -> this.dispose());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.ipady = 10;
		gridBagConstraints.ipadx = 10;

		// add spineware logo
		gridBagConstraints.gridwidth = 6;
		mainPanel.add(spineWareIconLabel, gridBagConstraints);

		// add break time message
		gridBagConstraints.gridy = 1;
		mainPanel.add(breakMessageLabel, gridBagConstraints);

		// add remaining time message
		gridBagConstraints.gridy = 2;
		mainPanel.add(remainingTimeLabel, gridBagConstraints);

		// add countdown indicators
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.weightx = 1;

		String[] hms = new String[]{
			SWMain.messagesBundle.getString("hours"),
			SWMain.messagesBundle.getString("minutes"),
			SWMain.messagesBundle.getString("seconds")
		};
		byte i = 0;
		for (JLabel label : this.hmsRemainingTimeLabels) {
			label.setFont(Fonts.MONOSPACED_BOLD_24);
			label.setHorizontalAlignment(JLabel.RIGHT);
			mainPanel.add(label, gridBagConstraints); // add number label

			++gridBagConstraints.gridx;
			label.setHorizontalAlignment(JLabel.LEFT);
			mainPanel.add(new JLabel(hms[i]), gridBagConstraints); // add unit label

			++gridBagConstraints.gridx;
			++i;
		}

		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = 6;
		mainPanel.add(cancelButton, gridBagConstraints);

		String iconPath = "/resources/media/SW_white.min.png";
		InputStream iconInputStream = SWMain.getImageAsStream(iconPath);
		try {
			this.setIconImage(ImageIO.read(iconInputStream));
		} catch (IOException e) {
			Loggers.errorLogger.log(Level.WARNING, "Error while setting JFrame image icon", e);
		}

		this.setContentPane(mainPanel);

		this.setTitle(breakMessage);
		this.setResizable(false);
		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);

		this.timerCountDown = new Timer(1_000, (ActionEvent evt) -> this.countDown());
		this.timerCountDown.start();
	}

	public void countDown() {
		if (!this.breakSettings.subtractSeconds((byte) 1)) { // if the subtraction could not be done because timer is at 0
			this.dispose();
			return;
		}

		this.hmsRemainingTimeLabels[0].setText(String.valueOf(this.breakSettings.getHours()));
		this.hmsRemainingTimeLabels[1].setText(String.valueOf(this.breakSettings.getMinutes()));
		this.hmsRemainingTimeLabels[2].setText(String.valueOf(this.breakSettings.getSeconds()));
	}

	/**
	 * If the image icon has been already loaded, this will simply do nothing
	 * If it hasn't been loaded, it tries to load it and stores it in the swIcon static member
	 */
	private void loadSpineWareIcon() {
		if (BreakCountDown.spineWareIcon != null)
			return;

		String iconImagePath = "/resources/media/SpineWare_white.png";
		InputStream iconInputStream = SWMain.getImageAsStream(iconImagePath);
		Image icon;
		try {
			icon = ImageIO.read(iconInputStream);
		} catch (IOException e) {
			Loggers.errorLogger.log(Level.SEVERE, "Error while reading SpineWare icon in path: " + iconImagePath, e);
			return;
		}
		icon = icon.getScaledInstance(400, 120, Image.SCALE_AREA_AVERAGING);

		BreakCountDown.spineWareIcon = new ImageIcon(icon);
	}

	/**
	 * Same as JDialog#dispose
	 * but this method will also count down the latch
	 * it will also stop the timer that handles the countdown in the GUI
	 */
	@Override
	public void dispose() {
		this.timerCountDown.stop();
		this.countDownLatch.countDown();
		super.dispose();
	}
}