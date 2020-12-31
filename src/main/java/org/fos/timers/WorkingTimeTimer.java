/*
 * Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
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
package org.fos.timers;


import org.fos.Loggers;
import org.fos.core.TimersManager;
import org.fos.timers.notifications.BreakCountDown;
import org.fos.timers.notifications.TakeABreakNotification;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class WorkingTimeTimer implements Runnable
{
	private final static byte MAX_N_DISMISSES = 3;
	private final static byte MAX_N_POSTPONED = 4;

	// this variable is volatile so other breaks (threads) can always see the updated value
	private static final AtomicBoolean is_break_happening = new AtomicBoolean(false); // sentinel value to avoid
	// collisions between breaks

	private final BreakSettings breakSettings;
	private final String takeABreakMessage;
	private final String breakName;
	private final boolean add_take_break_option;
	private final AtomicReference<TakeABreakNotification> notificationRef = new AtomicReference<>();
	private final AtomicReference<BreakCountDown> breakCountDownRef = new AtomicReference<>();
	private long last_time_timer_was_set_s;
	private long notification_should_be_shown_at_s;
	private byte n_dismisses = 0;
	private byte n_postponed = 0;
	private CountDownLatch notificationCountDownLatch, breakCountDownLatch;

	private ScheduledExecutorService executorService;

	public WorkingTimeTimer(
		final BreakSettings breakSettings,
		final String takeABreakMessage,
		final String breakName
	)
	{
		this(
			breakSettings,
			takeABreakMessage,
			breakName,
			true
		);
	}

	public WorkingTimeTimer(
		final BreakSettings breakSettings,
		final String takeABreakMessage,
		final String breakName,
		final boolean add_take_break_option
	)
	{
		this.breakSettings = breakSettings;
		this.takeABreakMessage = takeABreakMessage;
		this.breakName = breakName;
		this.add_take_break_option = add_take_break_option;
	}

	/**
	 * Initiates the executor (the timer)
	 */
	public void init()
	{
		this.executorService = Executors.newSingleThreadScheduledExecutor();
		this.scheduleWorkingTimeExecutor();
	}

	/**
	 * Shutdowns the executor (timer)
	 * and disposes all active notification or count down
	 */
	public void destroy()
	{
		if (this.executorService == null)
			return;

		this.executorService.shutdownNow();

		BreakCountDown breakCountDown = this.breakCountDownRef.get();
		if (breakCountDown != null)
			breakCountDown.dispose();

		TakeABreakNotification notification = this.notificationRef.get();
		if (notification != null)
			notification.dispose();

		this.breakSettings.stopHooks();
	}

	/**
	 * This will start the break timer, wait for a response
	 * and set a timer for itself (to start working again)
	 * This method is meant to be executed once the working time has passed
	 * <p>
	 * This method will show a notification to the user indicating it is time to take a break
	 * If the user decides to take the break, a countdown will start
	 * If the user decides to dismiss the notification, no countdown will start
	 * <p>
	 * Either way, at the end, the executorService will restart to repeat the process
	 */
	@Override
	public void run()
	{
		if (WorkingTimeTimer.is_break_happening.get()) {
			this.schedulePostponedExecutor();
			WorkingTimeTimer.is_break_happening.set(true);
			return;
		}
		WorkingTimeTimer.is_break_happening.set(true);

		if (this.n_dismisses >= WorkingTimeTimer.MAX_N_DISMISSES
			|| this.n_postponed >= WorkingTimeTimer.MAX_N_POSTPONED) {
			this.n_dismisses = 0;
			this.n_postponed = 0;

			if (this.breakSettings.getBreakTimerSettings() == null) { // in case of the day limit timer
				// this should almost never happen
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(
						null,
						"You really need to stop working!",
						"That's enough",
						JOptionPane.WARNING_MESSAGE
					);
					this.scheduleWorkingTimeExecutor();
				});
				return;
			}
			this.showBreakCountDown();
			return;
		}

		this.notificationCountDownLatch = new CountDownLatch(1);
		SwingUtilities.invokeLater(() -> notificationRef.set(
			new TakeABreakNotification(
				this.takeABreakMessage,
				this.notificationCountDownLatch,
				this.add_take_break_option,
				TimersManager.getNotificationPrefLocation(),
				this.breakSettings::startNotificationHooks,
				this.breakSettings::stopNotificationHooks
			)
		));

		try {
			this.notificationCountDownLatch.await();
		} catch (InterruptedException e) {
			Loggers.getErrorLogger().log(
				Level.INFO,
				"The count down latch for the notification was interrupted",
				e
			);
		}

		if (notificationRef.get().breakWasDismissed()) {
			++this.n_dismisses;
			// the user will not take the break and will keep working, so start the working timer again
			this.scheduleWorkingTimeExecutor();
			return;
		} else if (notificationRef.get().breakWasPostponed()) {
			++this.n_postponed;
			// the user has postponed the break, wait the postponed time to show the notification again
			this.schedulePostponedExecutor();
			return;
		}

		if (this.breakSettings.getBreakTimerSettings() == null) { // the day break does not have break time
			this.scheduleWorkingTimeExecutor();
			return;
		}

		this.showBreakCountDown();
	}

	/**
	 * Instantiates the break countdown and waits till this countdown finishes
	 * this will use the breakCountDownLatch
	 * This method WILL ALSO SCHEDULE AGAIN THE WORKING TIMER
	 * so you don't have to call scheduleWorkingTimeExecutor
	 */
	private void showBreakCountDown()
	{
		this.breakCountDownLatch = new CountDownLatch(1);
		SwingUtilities.invokeLater(() -> this.breakCountDownRef.set(
			new BreakCountDown(
				this.breakName,
				this.breakSettings.getBreakTimerSettings(),
				this.breakCountDownLatch,
				this.breakSettings::startBreakHooks,
				this.breakSettings::stopBreakHooks
			)
		));

		try {
			this.breakCountDownLatch.await();
		} catch (InterruptedException e) {
			Loggers.getErrorLogger().log(
				Level.INFO,
				"The count down latch for the break count down was interrupted",
				e
			);
		}

		this.scheduleWorkingTimeExecutor();
	}

	/**
	 * Shcedules the executor to run the method {@link #run()}
	 *
	 * @param seconds_timeout the seconds timeout (number of seconds to wait to execute {@link #run()})
	 */
	private void scheduleExecutor(int seconds_timeout)
	{
		WorkingTimeTimer.is_break_happening.set(false);

		this.breakSettings.stopHooks();

		this.executorService.schedule(this, seconds_timeout, TimeUnit.SECONDS);

		this.last_time_timer_was_set_s = System.currentTimeMillis() / 1_000;
		this.notification_should_be_shown_at_s = this.last_time_timer_was_set_s + seconds_timeout;
	}

	private void schedulePostponedExecutor()
	{
		this.scheduleExecutor(this.breakSettings.getPostponeTimerSettings().getHMSAsSeconds());
	}

	private void scheduleWorkingTimeExecutor()
	{
		this.scheduleExecutor(this.breakSettings.getWorkTimerSettings().getHMSAsSeconds());
	}

	/**
	 * @return the last time in seconds the working timer was set
	 */
	public long getLastTimeTimerWasSet()
	{
		return this.last_time_timer_was_set_s;
	}

	/**
	 * @return the time in seconds the notification should be shown
	 * if the time is in the past, then the notification was already shown or is been shown
	 */
	public long getNotificationShouldBeShownAt()
	{
		return this.notification_should_be_shown_at_s;
	}

	/**
	 * @return the remaining seconds to show the notification. If this value is negative, then the notification
	 * has already been shown
	 */
	public int getRemainingSToShowNotif()
	{
		/*
		When the notification has not been shown yet
		this.notification_should_be_shown_at_s
		should be in the future
		 */
		return (int) (this.notification_should_be_shown_at_s - System.currentTimeMillis() / 1_000);
	}

	/**
	 * @return the working timer time as seconds
	 */
	public int getWorkingTimeSeconds()
	{
		return this.breakSettings.getWorkTimerSettings().getHMSAsSeconds();
	}

	@Override
	public String toString()
	{
		return "WorkingTimeTimer{" +
			"breakSettings=" + breakSettings +
			", takeABreakMessage='" + takeABreakMessage + '\'' +
			", breakName='" + breakName + '\'' +
			", add_take_break_option=" + add_take_break_option +
			", notificationRef=" + notificationRef +
			", breakCountDownRef=" + breakCountDownRef +
			", last_time_timer_was_set_s=" + last_time_timer_was_set_s +
			", notification_should_be_shown_at_s=" + notification_should_be_shown_at_s +
			", n_dismisses=" + n_dismisses +
			", n_postponed=" + n_postponed +
			", notificationCountDownLatch=" + notificationCountDownLatch +
			", breakCountDownLatch=" + breakCountDownLatch +
			", executorService=" + executorService +
			'}';
	}
}