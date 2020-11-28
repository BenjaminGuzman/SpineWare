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
/*
import org.fos.Loggers;
import org.fos.controls.TimeInput;

import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class SmallBreakTimer extends BreakTimeTimer {
	public SmallBreakTimer(int break_time_s) {
		super(break_time_s);
	}

	/**
	 * When this method is called, a notification to take the break is shown
	 * when received a response from the notification, by either clicking "take break" or "dismiss break"
	 * this method will show the countdown for the latter and block until it ends
	 * or simple return for the former
	 *//*
	@Override
	protected void init() {
		// method is executing in the timer thread
		// we're going to show the take a break notification
		// therefore we need to run the dialog in the swing thread
		// and wait for a response <- this could be either the user dismissed the break or decided to take it

		final CountDownLatch notificationCountDownLatch = new CountDownLatch(1);

		// this reference is needed as the real object will be created in the swing thread
		AtomicReference<TakeABreakNotification> breakNotification = new AtomicReference<>();
		// show the take a break notification
		SwingUtilities.invokeLater(() -> {
			TakeABreakNotification notification = new TakeABreakNotification("Time for a " + TimeInput.seconds2HoursMinutesSecondsAsString(this.break_time_s) + " break", notificationCountDownLatch);
			breakNotification.set(notification);
			notification.showWithAnimation();
			notification.setVisible(true);
		});

		try {
			notificationCountDownLatch.await(); // wait in THIS thread, wait until the user clicks some button or the dialog disappears
		} catch (InterruptedException e) {
			Loggers.errorLogger.log(Level.SEVERE, "Error while waiting for a response from the take a break dialog", e);
		}

		boolean will_take_break = breakNotification.get().willUserTakeTheBreak();
		if (!will_take_break) {
			System.out.println("Not taking the break");
			return;
		}
		System.out.println("Taking the break, showing count down...");


		final CountDownLatch countDownLatch = new CountDownLatch(1);

		Platform.runLater(() -> {

		});

		try {
			System.out.println("Waiting for the break");
			countDownLatch.await();
			System.out.println("The break ended");
		} catch (InterruptedException e) {
			Loggers.errorLogger.log(Level.SEVERE, "Error while waiting for the break to complete", e);
		}
	}
}*/
