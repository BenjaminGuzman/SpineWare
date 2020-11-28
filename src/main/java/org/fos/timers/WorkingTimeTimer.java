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
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class WorkingTimeTimer implements Runnable {
	private final int working_time_s;
	private final int break_time_s;

	private ScheduledExecutorService executorService;
	private BreakTimeTimer breakTimeTimer;

	private final Class<? extends BreakTimeTimer> BreakTimeTimerClass;

	public WorkingTimeTimer(final int working_time_s, int break_time_s, Class<? extends BreakTimeTimer> BreakTimeTimerClass) {
		this.working_time_s = working_time_s;
		this.break_time_s = break_time_s;
		this.BreakTimeTimerClass = BreakTimeTimerClass;
	}

	public void init() {
		this.executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.schedule(this, this.working_time_s, TimeUnit.SECONDS);
	}

	public void destroy() {
		this.executorService.shutdownNow();
		// TODO: also destroy the breakTimeTimer
		//this.breakTimeTimer.
	}

	/**
	 * This will start the break timer, wait for a response
	 * and set a timer for itself (to start working again)
	 *//*
	@Override
	public void run() {
		System.out.println("Entering the run method to create a new scheduler: " + Thread.currentThread().getName());
		// TODO: block other timers when this is executed
		// create the break timer and send count down latch to the break timer
		try {
			this.breakTimeTimer = this.BreakTimeTimerClass.getDeclaredConstructor(int.class).newInstance(this.break_time_s);
			this.breakTimeTimer.init();
		} catch (InstantiationException|IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
			Loggers.errorLogger.log(Level.SEVERE, "Error while creating an object instance of class: " + this.BreakTimeTimerClass.getCanonicalName(), e);
		}

		System.out.println("Creating a new executor!!! " + Thread.currentThread().getName());
		this.executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.schedule(this, working_time_s, TimeUnit.SECONDS); // once the break finished, get to work again
	}
}
*/