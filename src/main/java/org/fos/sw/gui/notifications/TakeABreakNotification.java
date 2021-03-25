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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.fos.sw.SWMain;
import org.fos.sw.core.NotificationLocation;
import org.fos.sw.gui.Colors;
import org.fos.sw.gui.Fonts;
import org.fos.sw.timers.WallClock;
import org.fos.sw.timers.breaks.BreakDecision;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TakeABreakNotification extends AbstractNotification
{
	// Milliseconds for the notification timeout
	public static final int MS_NOTIFICATION_TIMEOUT = 30_000;

	// Milliseconds for the DAY notification timeout
	public static final int MS_DAY_NOTIFICATION_TIMEOUT = 60_000 * 30; // half an hour

	/**
	 * this countdown latch should be decremented when the dialog ends (its closed or "take a break" is clicked)
	 */
	@NotNull
	private final CountDownLatch countDownLatch;
	@NotNull
	private final JProgressBar progressBarCountDown;
	@Nullable
	private Timer countDownTimer; // may be null if break type is day
	private boolean break_dismissed = false;
	private boolean break_postponed = true; // default behaviour is postponed
	private int remaining_seconds;

	@Nullable
	private WallClock postponeTimeOverride;

	@NotNull
	private final String takeABreakMessage;
	private final boolean is_not_day_limit_notification;
	private boolean pause_countdown;


	public TakeABreakNotification(
		final @NotNull String takeABreakMessage,
		final CountDownLatch countDownLatch,
		boolean is_not_day_limit_notification,
		final NotificationLocation notificationLocation,
		final @Nullable Runnable onShown,
		final @Nullable Runnable onDisposed
	)
	{
		this(takeABreakMessage, countDownLatch, is_not_day_limit_notification, notificationLocation);
		super.onShown = onShown;
		super.onDisposed = onDisposed;

		initComponents();
	}

	public TakeABreakNotification(
		final @NotNull String takeABreakMessage,
		final @NotNull CountDownLatch countDownLatch,
		boolean is_not_day_limit_notification,
		final NotificationLocation notificationLocation
	)
	{
		super(
			is_not_day_limit_notification ? -1 : MS_DAY_NOTIFICATION_TIMEOUT,
			notificationLocation
		);

		this.countDownLatch = countDownLatch;
		this.takeABreakMessage = takeABreakMessage;
		this.is_not_day_limit_notification = is_not_day_limit_notification;
		remaining_seconds = MS_NOTIFICATION_TIMEOUT / 1_000;
		progressBarCountDown = new JProgressBar(0, remaining_seconds);
	}

	/**
	 * Use this function to initialize and add the components to the {@link #mainPanel}
	 */
	@Override
	public void initComponents()
	{
		ResourceBundle messagesBundle = SWMain.messagesBundle;

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
		postponeButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				super.mouseClicked(e);
				TakeABreakNotification.this.onClickPostpone(e);
			}
		});
		buttonsPanel.add(postponeButton);

		JButton dismissButton = new JButton(messagesBundle.getString("notification_dismiss_break"));
		dismissButton.setToolTipText(messagesBundle.getString("notification_dismiss_tooltip"));
		dismissButton.addActionListener(this::onClickDismiss);
		buttonsPanel.add(dismissButton);

		// create the progress bar
		progressBarCountDown.setValue(remaining_seconds);
		progressBarCountDown.setString(remaining_seconds + "s");
		progressBarCountDown.setStringPainted(true);
		progressBarCountDown.setBorderPainted(false);
		progressBarCountDown.setBackground(Colors.RED_WINE);
		progressBarCountDown.setForeground(Colors.GREEN_DARK);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 5;
		gbc.ipady = 5;

		// add SW icon
		gbc.gridheight = 3;
		super.mainPanel.add(super.swIconLabel, gbc);

		// add take a break label
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		super.mainPanel.add(takeABreakLabel, gbc);

		// add buttons panel
		gbc.gridx = 1;
		gbc.gridy = 1;
		super.mainPanel.add(buttonsPanel, gbc);

		// add progress bar
		if (is_not_day_limit_notification) {
			gbc.gridx = 1;
			gbc.gridy = 2;
			gbc.weightx = 2;
			super.mainPanel.add(progressBarCountDown, gbc);

			countDownTimer = new Timer(1_000, (ActionEvent evt) -> {
				if (pause_countdown)
					return;

				if (remaining_seconds <= 0) {
					dispose();
					return;
				}

				String str = --remaining_seconds + "s";
				progressBarCountDown.setString(str);
				progressBarCountDown.setValue(remaining_seconds);
			});
			countDownTimer.start();
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
	private void onClickPostpone(MouseEvent evt)
	{
		if (SwingUtilities.isRightMouseButton(evt)) {
			pause_countdown = true;
			this.setAlwaysOnTop(false);
			PostponeTimeDialog postponeTimeDialog = new PostponeTimeDialog(this); // this will block
			this.setAlwaysOnTop(true);
			pause_countdown = false;

			if (postponeTimeDialog.wasCancelled())
				return;

			postponeTimeOverride = postponeTimeDialog.getPostponeTime();
		} else
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
	 * @return a value of the enum {@link BreakDecision} depending on what the user chose
	 */
	public BreakDecision getBreakDecision()
	{
		if (this.break_dismissed)
			return BreakDecision.DISMISS;
		if (this.break_postponed)
			return BreakDecision.POSTPONE;
		return BreakDecision.TAKE_BREAK;
	}

	/**
	 * @return true or false depending on what the user chose
	 * If this method is invoked before the user makes the choice, this method has undefined behaviour
	 */
	public boolean breakWasPostponed()
	{
		return this.break_postponed;
	}

	/**
	 * @return the new postpone time the user chose
	 * This may be null if {@link #getBreakDecision()} does not return {@link BreakDecision#POSTPONE}
	 */
	public @Nullable WallClock getPostponeTimeOverride()
	{
		return postponeTimeOverride;
	}

	/**
	 * Disposes the jdialog and executes the configured hooks (e. g. {@link #onDisposed})
	 */
	@Override
	public void dispose()
	{
		this.disposeNoHooks();

		if (this.onDisposed != null && (this.break_dismissed || this.break_postponed))
			this.onDisposed.run();
	}

	/**
	 * Disposes the jdialog but executes no hooks (e. g. {@link #onDisposed})
	 */
	public void disposeNoHooks()
	{
		super.dispose();

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
