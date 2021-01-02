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

package org.fos.gui.notifications;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import org.fos.SWMain;
import org.fos.core.NotificationLocation;
import org.fos.gui.Colors;
import org.fos.gui.Fonts;

public class TakeABreakNotification extends Notification
{
	// Milliseconds for the notification timeout
	public static final int MS_NOTIFICATION_TIMEOUT = 15_000;

	// Milliseconds for the DAY notification timeout
	public static final int MS_DAY_NOTIFICATION_TIMEOUT = 60_000 * 30; // half an hour

	// this countdown latch should be decremented when the dialog ends (its closed or "take a break" is clicked)
	private final CountDownLatch countDownLatch;
	private final JProgressBar progressBarCountDown;
	private Timer countDownTimer;
	private boolean break_dismissed = false;
	private boolean break_postponed = true; // default behaviour is postponed
	private int remaining_seconds;

	public TakeABreakNotification(
		final String takeABreakMessage,
		final CountDownLatch countDownLatch,
		final boolean is_not_day_limit_notification,
		final NotificationLocation notificationLocation,
		final Runnable onShown,
		final Runnable onDisposed
	)
	{
		this(takeABreakMessage, countDownLatch, is_not_day_limit_notification, notificationLocation);
		this.onShown = onShown;
		this.onDisposed = onDisposed;

		if (this.onShown != null)
			this.onShown.run();
	}

	public TakeABreakNotification(
		final String takeABreakMessage,
		final CountDownLatch countDownLatch,
		final boolean is_not_day_limit_notification,
		final NotificationLocation notificationLocation
	)
	{
		super(
			is_not_day_limit_notification ? MS_NOTIFICATION_TIMEOUT : MS_DAY_NOTIFICATION_TIMEOUT,
			notificationLocation
		);

		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		this.countDownLatch = countDownLatch;

		this.remaining_seconds = super.getDisposeTimeout() / 1_000;

		// create take a break label
		JLabel takeABreakLabel = new JLabel(takeABreakMessage);
		takeABreakLabel.setFont(Fonts.SANS_SERIF_BOLD_15);

		// create buttons
		JPanel buttonsPanel = new JPanel();

		if (is_not_day_limit_notification) {
			JButton takeBreakButton = new JButton(messagesBundle.getString("notification_take_break"));
			takeBreakButton.setToolTipText(messagesBundle.getString("notification_take_break_tooltip"));
			takeBreakButton.addActionListener(this::onClickTakeBreak);
			buttonsPanel.add(takeBreakButton);
			this.getRootPane().setDefaultButton(takeBreakButton);
		}

		JButton postponeButton = new JButton(messagesBundle.getString("notification_postpone_break"));
		postponeButton.setToolTipText(messagesBundle.getString("notification_postpone_tooltip"));
		postponeButton.addActionListener(this::onClickPostpone);
		buttonsPanel.add(postponeButton);

		JButton dismissButton = new JButton(messagesBundle.getString("notification_dismiss_break"));
		dismissButton.setToolTipText(messagesBundle.getString("notification_dismiss_tooltip"));
		dismissButton.addActionListener(this::onClickDismiss);
		buttonsPanel.add(dismissButton);

		// create the progress bar
		this.progressBarCountDown = new JProgressBar(0, this.remaining_seconds);
		this.progressBarCountDown.setValue(this.remaining_seconds);
		this.progressBarCountDown.setString(this.remaining_seconds + "s");
		this.progressBarCountDown.setStringPainted(true);
		this.progressBarCountDown.setBorderPainted(false);
		this.progressBarCountDown.setBackground(Colors.RED_WINE);
		this.progressBarCountDown.setForeground(Colors.GREEN_DARK);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.ipadx = 5;
		gridBagConstraints.ipady = 5;

		// add SW icon
		gridBagConstraints.gridheight = 3;
		super.mainPanel.add(super.swIconLabel, gridBagConstraints);

		// add take a break label
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 1;
		super.mainPanel.add(takeABreakLabel, gridBagConstraints);

		// add buttons panel
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		super.mainPanel.add(buttonsPanel, gridBagConstraints);

		// add progress bar
		if (is_not_day_limit_notification) {
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 2;
			gridBagConstraints.weightx = 2;
			super.mainPanel.add(this.progressBarCountDown, gridBagConstraints);

			this.countDownTimer = new Timer(1_000, (ActionEvent evt) -> {
				if (this.remaining_seconds <= 0) {
					this.dispose();
					return;
				}

				String str = --this.remaining_seconds + "s";
				this.progressBarCountDown.setString(str);
				this.progressBarCountDown.setValue(this.remaining_seconds);
			});
			this.countDownTimer.start();
		}
		super.showJDialog();
	}

	/**
	 * Invoked when the user clicks the take break button
	 * This will set the break_dismissed and break_postponed properties to false
	 *
	 * @param evt event
	 */
	private void onClickTakeBreak(ActionEvent evt)
	{
		this.break_dismissed = false;
		this.break_postponed = false;
		this.dispose();
	}

	/**
	 * Invoked when the user clicks the dismiss button
	 * This will set the break_dismissed property to false
	 *
	 * @param evt event
	 */
	private void onClickDismiss(ActionEvent evt)
	{
		this.break_dismissed = true;
		this.dispose();
	}

	/**
	 * Invoked when the user clicks the postpone button
	 * This will set the break_postponed property to false
	 *
	 * @param evt event
	 */
	private void onClickPostpone(ActionEvent evt)
	{
		this.break_postponed = true;
		this.dispose();
	}

	/**
	 * @return true or false depending on what the user chose
	 * If this method is invoked before the user makes the choice, this method will return true
	 * That is the default behaviour, if the user does not decide, the notification will be dismissed
	 */
	public boolean breakWasDismissed()
	{
		return this.break_dismissed;
	}

	/**
	 * @return true or false depending on what the user chose
	 * If this method is invoked before the user makes the choice, this method has undefined behaviour
	 */
	public boolean breakWasPostponed()
	{
		return this.break_postponed;
	}

	@Override
	public void dispose()
	{
		super.dispose();

		if (this.onDisposed != null && this.break_dismissed)
			this.onDisposed.run();

		if (this.countDownTimer != null)
			this.countDownTimer.stop();

		this.countDownLatch.countDown();
	}

	@Override
	public String toString()
	{
		return "TakeABreakNotification{" +
			"countDownLatch=" + countDownLatch +
			", progressBarCountDown=" + progressBarCountDown +
			", countDownTimer=" + countDownTimer +
			", break_dismissed=" + break_dismissed +
			", break_postponed=" + break_postponed +
			", remaining_seconds=" + remaining_seconds +
			'}';
	}
}