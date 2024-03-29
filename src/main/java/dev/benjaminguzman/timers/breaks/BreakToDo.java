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

package dev.benjaminguzman.timers.breaks;

import dev.benjaminguzman.SpineWare;
import dev.benjaminguzman.hooks.BreakHooks;
import dev.benjaminguzman.gui.notifications.CountDownDialog;
import dev.benjaminguzman.gui.notifications.TakeABreakNotification;
import dev.benjaminguzman.prefs.NotificationPrefsIO;
import dev.benjaminguzman.timers.WallClock;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class BreakToDo extends ToDo
{
	private final static byte MAX_N_DISMISSES = 3;
	private final static byte MAX_N_POSTPONED = 4;
	@NotNull
	private final BreakConfig breakConfig;
	private final String takeABreakMessage;
	private final String breakName;
	private final boolean add_take_break_option;

	// number of times the notification was dismissed
	private int n_dismisses;
	// number of times the notification was postponed
	private int n_postponed;

	@NotNull
	private WallClock postponeTime;

	public BreakToDo(
		String takeABreakMessage,
		String breakName,
		@NotNull BreakConfig breakConfig,
		boolean add_take_break_option
	)
	{
		this.breakConfig = breakConfig;
		this.takeABreakMessage = takeABreakMessage;
		this.breakName = breakName;
		this.add_take_break_option = add_take_break_option;
		postponeTime = breakConfig.getPostponeWallClock();
		if (breakConfig.isEnabled())
			updateExecutionTimes(breakConfig.getWorkWallClock());
	}

	/**
	 * Creates a {@link BreakToDo}
	 * from the given break configuration
	 *
	 * @param breakConfig the configuration for the break
	 * @return the create {@link BreakToDo}
	 */
	public static BreakToDo from(BreakConfig breakConfig)
	{
		ResourceBundle messagesBundle = SpineWare.messagesBundle;

		if (breakConfig.getBreakType() == BreakType.DAY_BREAK)
			return new BreakToDo(
				messagesBundle.getString("time_for_a_day_break"),
				messagesBundle.getString("day_break_title"),
				breakConfig,
				false
			);

		// note at this time it is safe to call get on the optional, as the only case when it could return null
		// is when break time is day break
		return new BreakToDo(
			MessageFormat.format(
				messagesBundle.getString("notification_time_for_a_break"),
				breakConfig.getBreakWallClock().get().getHMSAsString()
			),
			messagesBundle.getString("time_for_a_small_break"),
			breakConfig,
			true
		);
	}

	/**
	 * Method to execute when it is time to take the break
	 * and also update some values within this class
	 */
	@Override
	public void run()
	{
		if (is_cancelled)
			return;

		if (!breakConfig.isEnabled())
			throw new RuntimeException("The run method was called inside a disabled BreakToDo object");

		postponeTime = breakConfig.getPostponeWallClock();
		BreakDecision breakDecision = showTakeBreakNotification(); // this is a blocking call
		if (breakDecision == null) {
			updateExecutionTimes(breakConfig.getWorkWallClock());
			return;
		}

		if (breakDecision != BreakDecision.TAKE_BREAK) {
			if (breakDecision == BreakDecision.POSTPONE)
				updateExecutionTimes(postponeTime);
			else if (breakDecision == BreakDecision.DISMISS)
				updateExecutionTimes(breakConfig.getWorkWallClock());
			return;
		}

		showBreakCountDown(); // this is a blocking call
		updateExecutionTimes(breakConfig.getWorkWallClock());
	}

	/**
	 * Recomputes {@link #next_execution_at} and {@link #last_execution_at} as if the object as just created
	 */
	public void reloadTimes()
	{
		this.updateExecutionTimes(breakConfig.getWorkWallClock());
	}

	/**
	 * Show the "take a break notification"
	 * <p>
	 * This method will block till the notification is disposed/dismissed/postponed/accepted
	 *
	 * @return the corresponding {@link BreakDecision}. It returns null if the thread was interrupted
	 */
	private BreakDecision showTakeBreakNotification()
	{
		if (this.n_dismisses >= BreakToDo.MAX_N_DISMISSES || this.n_postponed >= BreakToDo.MAX_N_POSTPONED) {
			this.n_dismisses = 0;
			this.n_postponed = 0;

			if (!this.breakConfig.getBreakWallClock().isPresent()) { // in case of the day limit timer
				// this should almost never happen
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
					null,
					"You really need to stop working!",
					"That's enough",
					JOptionPane.WARNING_MESSAGE
				));
			}
			return BreakDecision.TAKE_BREAK;
		}

		BreakHooks hooksConfig = breakConfig.getHooksConfig();

		CountDownLatch notificationCountDownLatch = new CountDownLatch(1);
		AtomicReference<TakeABreakNotification> notificationRef = new AtomicReference<>();
		SwingUtilities.invokeLater(() -> notificationRef.set(
			new TakeABreakNotification(
				takeABreakMessage,
				notificationCountDownLatch,
				this.add_take_break_option,
				NotificationPrefsIO.getNotificationPrefLocation(
					NotificationPrefsIO.NotificationPreferenceType.TIMER_NOTIFICATION
				),
				hooksConfig != null ? hooksConfig::onStartNotificationHooks : null,
				hooksConfig != null ? hooksConfig::onEndNotificationHooks : null
			)
		));

		try {
			notificationCountDownLatch.await(); // wait till notification is dismissed
		} catch (InterruptedException e) {
			if (notificationRef.get() != null)
				SwingUtilities.invokeLater(() -> notificationRef.get().disposeNoHooks());
			return null;
		}

		if (notificationRef.get() != null) {
			TakeABreakNotification notification = notificationRef.get();
			BreakDecision decision = notification.getBreakDecision();
			switch (decision) {
				case POSTPONE:
					++n_postponed;
					if (notification.getPostponeTimeOverride() != null)
						postponeTime = notification.getPostponeTimeOverride();
					break;
				case DISMISS:
					++n_dismisses;
					break;
			}
			return decision;
		}

		return BreakDecision.DISMISS;
	}

	/**
	 * Shows the break countdown
	 * <p>
	 * This method will block until the countdown is done or the user click cancel
	 */
	private void showBreakCountDown()
	{
		BreakHooks hooksConfig = this.breakConfig.getHooksConfig();

		CountDownLatch breakCountDownLatch = new CountDownLatch(1);
		AtomicReference<CountDownDialog> breakCountDownRef = new AtomicReference<>();
		SwingUtilities.invokeLater(() -> breakCountDownRef.set(
			new CountDownDialog(
				this.breakName,
				this.breakConfig.getBreakWallClock().get(),
				breakCountDownLatch,
				hooksConfig != null ? hooksConfig::onStartBreakHooks : null,
				hooksConfig != null ? hooksConfig::onEndBreakHooks : null
			)
		));

		try {
			breakCountDownLatch.await();
		} catch (InterruptedException e) {
			if (breakCountDownRef.get() != null)
				SwingUtilities.invokeLater(() -> breakCountDownRef.get().disposeNoHooks());
		}
	}

	/////////////
	// Getters //
	////////////
	public @NotNull BreakConfig getBreakConfig()
	{
		return breakConfig;
	}

	@Override
	public String toString()
	{
		return "BreakToDo{" +
			"last_execution_at=" + last_execution_at +
			", next_execution_at=" + next_execution_at +
			", is_cancelled=" + is_cancelled +
			", breakConfig=" + breakConfig +
			", n_dismisses=" + n_dismisses +
			", n_postponed=" + n_postponed +
			", takeABreakMessage='" + takeABreakMessage + '\'' +
			", breakName='" + breakName + '\'' +
			", add_take_break_option=" + add_take_break_option +
			'}';
	}
}
