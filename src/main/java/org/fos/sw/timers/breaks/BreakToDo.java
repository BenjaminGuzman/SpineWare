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

package org.fos.sw.timers.breaks;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.fos.sw.SWMain;
import org.fos.sw.gui.notifications.BreakCountDown;
import org.fos.sw.gui.notifications.TakeABreakNotification;
import org.fos.sw.hooks.BreakHooksConfig;
import org.fos.sw.timers.TimersManager;
import org.fos.sw.timers.WallClock;
import org.jetbrains.annotations.NotNull;

public class BreakToDo implements Comparable<BreakToDo>, Runnable
{
	private final static byte MAX_N_DISMISSES = 3;
	private final static byte MAX_N_POSTPONED = 4;
	@NotNull
	private final BreakConfig breakConfig;
	private final String takeABreakMessage;
	private final String breakName;
	private final boolean add_take_break_option;
	/**
	 * Number of seconds since unix epoch when the to do
	 * was executed
	 * <p>
	 * This value should be always in the past
	 */
	private long last_execution_at;
	/**
	 * Time in seconds since the unix epoch when the to do
	 * should be executed
	 * <p>
	 * This value should be most of the time in the future
	 * It may be in the past only if the {@link #run()} is executing
	 */
	private long next_execution_at;
	/**
	 * Tells if the to do has been cancelled
	 * and therefore {@link #run()} should not be executed
	 */
	private boolean is_cancelled;
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
		postponeTime = breakConfig.getPostponeTimerSettings();
		if (breakConfig.isEnabled())
			updateExecutionTimes(breakConfig.getWorkTimerSettings());
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
		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

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
				breakConfig.getBreakTimerSettings().get().getHMSAsString()
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

		if (Thread.currentThread().isInterrupted())
			return;

		postponeTime = breakConfig.getPostponeTimerSettings();
		BreakDecision breakDecision = showTakeBreakNotification(); // this is a blocking call
		if (breakDecision == null) {
			updateExecutionTimes(breakConfig.getWorkTimerSettings());
			return;
		}

		if (breakDecision != BreakDecision.TAKE_BREAK) {
			if (breakDecision == BreakDecision.POSTPONE)
				updateExecutionTimes(postponeTime);
			else if (breakDecision == BreakDecision.DISMISS)
				updateExecutionTimes(breakConfig.getWorkTimerSettings());
			return;
		}

		if (Thread.currentThread().isInterrupted())
			return;
		showBreakCountDown(); // this is a blocking call
		updateExecutionTimes(breakConfig.getWorkTimerSettings());
	}

	/**
	 * Updates the execution times in the class
	 *
	 * @param timeoutTime The timeout configuration used to set the {@link #next_execution_at} value
	 */
	synchronized private void updateExecutionTimes(@NotNull WallClock timeoutTime)
	{
		last_execution_at = System.currentTimeMillis() / 1_000;
		next_execution_at = last_execution_at + timeoutTime.getHMSAsSeconds();
	}

	/**
	 * Recomputes {@link #next_execution_at} and {@link #last_execution_at} as if the object as just created
	 */
	public void reloadTimes()
	{
		this.updateExecutionTimes(breakConfig.getWorkTimerSettings());
	}

	/**
	 * Postpone the execution of this to do by a given amount of seconds
	 *
	 * @param postponed_seconds the number of seconds to postpone the to do
	 */
	public void postponeExecution(int postponed_seconds)
	{
		last_execution_at += postponed_seconds;
		next_execution_at += postponed_seconds;
	}

	/**
	 * Tell if the {@link #run()} should be executed or not
	 * <p>
	 * This method will take into account if the to do is cancelled
	 *
	 * @param now the number of seconds since unix epoch at the time this function is called
	 * @return true if the runnable should be executed, false otherwise
	 */
	public boolean shouldExecuteNow(long now)
	{
		return !is_cancelled && remainingSecondsForExecution(now) <= 0;
	}

	/**
	 * @param now the number of seconds since unix epoch at the time this function is called
	 * @return the number of seconds remaining for the {@link #run()} to be executed
	 */
	public long remainingSecondsForExecution(long now)
	{
		return next_execution_at - now;
	}

	/**
	 * @return true if the To Do is cancelled and therefore should not be executed
	 */
	public boolean isCancelled()
	{
		return is_cancelled;
	}

	/**
	 * Sets whether or not the to do should be cancelled
	 *
	 * @param cancelled true if the to do should be cancelled
	 */
	public void setCancelled(boolean cancelled)
	{
		this.is_cancelled = cancelled;
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

			if (!this.breakConfig.getBreakTimerSettings().isPresent()) { // in case of the day limit timer
				// this should almost never happen
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(
						null,
						"You really need to stop working!",
						"That's enough",
						JOptionPane.WARNING_MESSAGE
					);
				});
			}
			return BreakDecision.TAKE_BREAK;
		}

		BreakHooksConfig hooksConfig = breakConfig.getHooksConfig();

		CountDownLatch notificationCountDownLatch = new CountDownLatch(1);
		AtomicReference<TakeABreakNotification> notificationRef = new AtomicReference<>();
		SwingUtilities.invokeLater(() -> notificationRef.set(
			new TakeABreakNotification(
				takeABreakMessage,
				notificationCountDownLatch,
				this.add_take_break_option,
				TimersManager.getPrefsIO().getNotificationPrefsIO().getNotificationPrefLocation(),
				hooksConfig != null ? hooksConfig::onStartNotificationHooks : null,
				hooksConfig != null ? hooksConfig::onEndNotificationHooks : null
			)
		));

		try {
			notificationCountDownLatch.await(); // wait till notification is dismissed
		} catch (InterruptedException e) {
			if (notificationRef.get() != null)
				notificationRef.get().disposeNoHooks();
			return null;
		} finally { // shutdown the hooks
			if (hooksConfig != null)
				hooksConfig.shutdown();
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
		BreakHooksConfig hooksConfig = this.breakConfig.getHooksConfig();

		CountDownLatch breakCountDownLatch = new CountDownLatch(1);
		AtomicReference<BreakCountDown> breakCountDownRef = new AtomicReference<>();
		SwingUtilities.invokeLater(() -> breakCountDownRef.set(
			new BreakCountDown(
				this.breakName,
				this.breakConfig.getBreakTimerSettings().get(),
				breakCountDownLatch,
				hooksConfig != null ? hooksConfig::onStartBreakHooks : null,
				hooksConfig != null ? hooksConfig::onEndBreakHooks : null
			)
		));

		try {
			breakCountDownLatch.await();
		} catch (InterruptedException e) {
			if (breakCountDownRef.get() != null)
				breakCountDownRef.get().disposeNoHooks();
		} finally { // shutdown the hooks
			if (hooksConfig != null)
				hooksConfig.shutdown();
		}
	}

	/////////////
	// Getters //
	////////////
	public @NotNull BreakConfig getBreakConfig()
	{
		return breakConfig;
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * <p>
	 * A {@link BreakToDo} object is less than other object if the {@link #next_execution_at} is
	 * less than the {@link #next_execution_at} of the other object
	 *
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                              from being compared to this object.
	 */
	@Override
	public int compareTo(@NotNull BreakToDo o)
	{
		long diff = this.next_execution_at - o.next_execution_at;
		try {
			return Math.toIntExact(diff);
		} catch (ArithmeticException ex) {
			return diff > 0 ? 1
				: diff == 0 ? 0 : -1;
		}
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
