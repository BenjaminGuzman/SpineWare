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
package org.fos.timers;


import org.fos.Loggers;
import org.fos.timers.notifications.BreakCountDown;
import org.fos.timers.notifications.TakeABreakNotification;

import javax.swing.SwingUtilities;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class WorkingTimeTimer implements Runnable {
	private final TimerSettings workingTimerSettings;
	private final TimerSettings breakTimerSettings;
	private final String takeABreakMessage;
	private final String breakName;
	private final boolean add_take_break_option;

	private final AtomicReference<TakeABreakNotification> notificationRef = new AtomicReference<>();
	private final AtomicReference<BreakCountDown> breakCountDownRef = new AtomicReference<>();

	private CountDownLatch notificationCountDownLatch, breakCountDownLatch;

	private ScheduledExecutorService executorService;

	public WorkingTimeTimer(final TimerSettings workingTimerSettings, final TimerSettings breakTimerSettings, String takeABreakMessage, String breakName) {
		this(workingTimerSettings, breakTimerSettings, takeABreakMessage, breakName, true);
	}

	public WorkingTimeTimer(final TimerSettings workingTimerSettings, final TimerSettings breakTimerSettings, String takeABreakMessage, String breakName, boolean add_take_break_option) {
		this.workingTimerSettings = workingTimerSettings;
		this.breakTimerSettings = breakTimerSettings;
		this.takeABreakMessage = takeABreakMessage;
		this.breakName = breakName;
		this.add_take_break_option = add_take_break_option;
	}

	public void init() {
		this.executorService = Executors.newSingleThreadScheduledExecutor();
		this.scheduleWorkingTimeExecutor();
	}

	public void destroy() {
		this.executorService.shutdownNow();

		BreakCountDown breakCountDown = this.breakCountDownRef.get();
		if (breakCountDown != null)
			breakCountDown.dispose();

		TakeABreakNotification notification = this.notificationRef.get();
		if (notification != null)
			notification.dispose();
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
	public void run() {
		this.notificationCountDownLatch = new CountDownLatch(1);
		SwingUtilities.invokeLater(() -> notificationRef.set(
			new TakeABreakNotification(
				this.takeABreakMessage,
				this.notificationCountDownLatch,
				this.add_take_break_option
			)
		));

		try {
			this.notificationCountDownLatch.await();
		} catch (InterruptedException e) {
			Loggers.errorLogger.log(Level.WARNING, "The count down latch for the notification was interrupted", e);
		}

		if (!notificationRef.get().willTakeBreak()) {
			// the user will not take the break and will keep working, so start the working timer again
			this.scheduleWorkingTimeExecutor();
			return;
		}

		if (this.breakTimerSettings == null) { // the day break does not have break time
			this.scheduleWorkingTimeExecutor();
			return;
		}

		this.breakCountDownLatch = new CountDownLatch(1);
		SwingUtilities.invokeLater(() -> this.breakCountDownRef.set(
			new BreakCountDown(
				this.breakName,
				this.breakTimerSettings,
				this.breakCountDownLatch
			)
		));

		try {
			this.breakCountDownLatch.await();
		} catch (InterruptedException e) {
			Loggers.errorLogger.log(Level.WARNING, "The count down latch for the break count down was interrupted", e);
		}

		this.scheduleWorkingTimeExecutor();
	}

	private void scheduleWorkingTimeExecutor() {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		Loggers.debugLogger.info(
			"Break notification will appear in: "
				+ this.workingTimerSettings.getHMSAsString()
				+ " from now (" + dateTimeFormatter.format(now) + ")"
		);
		this.executorService.schedule(this, this.workingTimerSettings.getHMSAsSeconds(), TimeUnit.SECONDS);
	}
}