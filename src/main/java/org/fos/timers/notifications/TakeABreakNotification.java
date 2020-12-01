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

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

public class TakeABreakNotification extends JDialog {
	private static ImageIcon swIcon; // static to avoid reading the image each time the notification is shown

	private boolean will_take_break = false;

	private final Timer timeoutTimer;

	// this countdown latch should be decremented when the dialog ends (its closed or "take a break" is clicked)
	private final CountDownLatch countDownLatch;

	public TakeABreakNotification(
		final String takeABreakMessage,
		final CountDownLatch countDownLatch,
		final boolean include_take_break_button
	) {
		super();
		assert SwingUtilities.isEventDispatchThread();

		this.countDownLatch = countDownLatch;

		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// create SW icon
		this.loadSWIcon();
		JLabel swIconLabel = new JLabel();
		if (TakeABreakNotification.swIcon != null)
			swIconLabel.setIcon(TakeABreakNotification.swIcon);
		else
			swIconLabel.setText("SW");

		// create take a break label
		JLabel takeABreakLabel = new JLabel(takeABreakMessage);
		takeABreakLabel.setFont(Fonts.SANS_SERIF_BOLD_15);

		// create buttons
		JPanel buttonsPanel = new JPanel();
		JButton dismissButton = new JButton(SWMain.messagesBundle.getString("notification_dismiss_break"));
		JButton takeBreakButton = null;
		if (include_take_break_button) {
			takeBreakButton = new JButton(SWMain.messagesBundle.getString("notification_take_break"));
			takeBreakButton.addActionListener(this::onClickTakeBreak);
		}
		dismissButton.addActionListener(this::onClickDismiss);

		buttonsPanel.add(takeBreakButton);
		buttonsPanel.add(dismissButton);


		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.ipadx = 5;
		gridBagConstraints.ipady = 5;

		// add SW icon
		gridBagConstraints.gridheight = 2;
		mainPanel.add(swIconLabel, gridBagConstraints);

		// add take a break label
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 1;
		mainPanel.add(takeABreakLabel, gridBagConstraints);

		// add buttons panel
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		mainPanel.add(buttonsPanel, gridBagConstraints);

		this.setContentPane(mainPanel);
		this.setUndecorated(true);
		this.setResizable(false);
		this.setType(Type.POPUP);
		this.setAlwaysOnTop(true);
		this.setAutoRequestFocus(false); // if you're working, this alert should not make you loose your focus in whatever you're doing
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // even though this is not likely to happen, is a good practice to have it
		this.pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension notificationSize = this.getSize();

		Point notificationLocation = new Point(
			screenSize.width - notificationSize.width,
			screenSize.height - notificationSize.height - 50
		);

		this.setLocation(notificationLocation);
		this.setVisible(true); // TODO: add an sliding animation to the notification

		// create timer to automatically dismiss the notification after some time
		this.timeoutTimer = new Timer(11_000, (ActionEvent evt) -> this.dispose());
		this.timeoutTimer.start();
	}

	/**
	 * If the image icon has been already loaded, this will simply do nothing
	 * If it hasn't been loaded, it tries to load it and stores it in the swIcon static member
	 */
	private void loadSWIcon() {
		if (TakeABreakNotification.swIcon != null)
			return;

		String iconImagePath = "/resources/media/SW_white.png";
		InputStream iconInputStream = SWMain.getImageAsStream(iconImagePath);
		Image icon;
		try {
			icon = ImageIO.read(iconInputStream);
		} catch (IOException e) {
			Loggers.errorLogger.log(Level.SEVERE, "Error while reading SW icon in path: " + iconImagePath, e);
			return;
		}
		icon = icon.getScaledInstance(67, 59, Image.SCALE_AREA_AVERAGING);

		TakeABreakNotification.swIcon = new ImageIcon(icon);
	}

	/**
	 * Invoked when the user clicks the take break button
	 * This will set the will_take_break property to true
	 *
	 * @param evt event
	 */
	private void onClickTakeBreak(ActionEvent evt) {
		this.will_take_break = true;
		this.dispose();
	}

	/**
	 * Invoked when the user clicks the dismiss button
	 * This will set the will_take_break property to false
	 *
	 * @param evt event
	 */
	private void onClickDismiss(ActionEvent evt) {
		this.will_take_break = false;
		this.dispose();
	}

	/**
	 * @return true or false depending on what the user chose
	 * If this method is invoked after the user makes the choice, this method has undefined behaviour
	 */
	public boolean willTakeBreak() {
		return this.will_take_break;
	}

	@Override
	public void dispose() {
		super.dispose();
		this.timeoutTimer.stop();
		this.countDownLatch.countDown();
	}
}
