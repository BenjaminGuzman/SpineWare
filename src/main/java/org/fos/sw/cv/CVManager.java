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

package org.fos.sw.cv;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.fos.sw.SWMain;
import org.fos.sw.core.Loggers;
import org.fos.sw.core.NotificationLocation;
import org.fos.sw.utils.DaemonThreadFactory;
import org.jetbrains.annotations.NotNull;

public class CVManager
{
	/**
	 * Make this volatile to avoid visibility issues
	 * atomicity is not needed here (or at least it'd be an overkill)
	 */
	private static volatile boolean instantiated;

	private static final Object cvLoopExecutorLock = new Object();

	private static CVLoop cvLoop;
	private static ScheduledExecutorService cvLoopExecutor;
	/**
	 * Volatile here is required because the value will be set from the thread calling {@link #startCVLoop()}
	 * while it will be retrieved/read from the CV-thread in functions like {@link #onUserHasGoneAway()}.
	 * <p>
	 * Mutex is not required because the probability of setting the value concurrently and having inconsistent
	 * state is very very low. It is almost guaranteed that {@link #startCVLoop()} (the only method that writes
	 * this value) will be called before calling methods like {@link #onUserHasGoneAway()} (the only methods that
	 * read this value)
	 */
	private static volatile NotificationLocation notifLocation;

	private CVManager()
	{
		throw new RuntimeException(this.getClass().getName() + " cannot be instantiated");
	}

	public static void init()
	{
		if (instantiated)
			throw new RuntimeException("Don't invoke init() method more than once");

		instantiated = true;
		cvLoop = new CVLoop(
			CVManager::processUserPostureState,
			CVManager::onUserHasGoneAway,
			CVManager::onMultipleFacesDetected
		);
		startCVLoop();
	}

	/**
	 * Starts the CV Loop with the previously saved preferences
	 * You can call this multiple times
	 * If the scheduled executor has been created, this method will not create a new one and CV feature will
	 * update the parameters based on the given preferences.
	 * <p>
	 * If there is any change in the preferences (e. g. in the GUI), just invoke this method again and
	 * preferences will be updated in the cv service too (of course, you'll need to save the preferences first
	 * within {@link CVPrefsManager})
	 *
	 * @see #startCVLoop(CVPrefs)
	 */
	public static void startCVLoop()
	{
		startCVLoop(CVPrefsManager.getCVPrefs());
	}

	/**
	 * Starts the CV Loop with the given preferences
	 * You can call this multiple times
	 * If the scheduled executor has been created, this method will not create a new one and CV feature will
	 * update the parameters based on the given preferences.
	 * <p>
	 * If there is any change in the preferences (e. g. in the GUI), just invoke this method again and
	 * preferences will be updated in the cv service too
	 *
	 * @param cvPrefs the CV preferences that will be used by all the CV features
	 */
	public static void startCVLoop(@NotNull CVPrefs cvPrefs)
	{
		if (!cvPrefs.is_enabled)
			return;

		Loggers.getDebugLogger().log(Level.INFO, "Starting CV Loop...");
		notifLocation = cvPrefs.notifLocation;

		synchronized (cvLoopExecutorLock) {
			// if the CV loop is already running, do nothing
			if (cvLoopExecutor != null && !cvLoopExecutor.isShutdown())
				return;

			if (!SWMain.getCVUtils().open()) {
				SWMain.showErrorAlert(
					SWMain.messagesBundle.getString("cam_open_error"),
					SWMain.messagesBundle.getString("cv_error")
				);
				return;
			}

			cvLoop.setCVPrefs(cvPrefs);

			cvLoopExecutor = Executors.newSingleThreadScheduledExecutor(
				// min priority is used because the computation is expensive an can slow down the
				// user's computer, and the CV features should not interfere with top priority threads
				new DaemonThreadFactory("CV-Loop-Thread", Thread.MIN_PRIORITY)
			);
			cvLoopExecutor.scheduleAtFixedRate(
				cvLoop,
				0,
				CVLoop.UPDATE_FREQUENCY_S,
				TimeUnit.SECONDS
			);
		}
	}

	/**
	 * Stops the CV Loop (if running)
	 * If you need to "restart" the loop, just call {@link #startCVLoop()}
	 * This method will also close the camera, if you don't want this use {@link #stopCVLoop(boolean)}
	 *
	 * @see #stopCVLoop(boolean)
	 */
	public static void stopCVLoop()
	{
		stopCVLoop(true);
	}

	/**
	 * Stops the CV Loop (if running)
	 * If you need to "restart" the loop, just call {@link #startCVLoop()}
	 *
	 * @param close_cam if true the cam will be closed
	 */
	public static void stopCVLoop(boolean close_cam)
	{
		synchronized (cvLoopExecutorLock) {
			if (cvLoopExecutor == null) // cv loop is not running
				return;

			Loggers.getDebugLogger().log(Level.INFO, "Stopping CV Loop...");

			cvLoopExecutor.shutdownNow();
			if (close_cam)
				SWMain.getCVUtils().close();
			cvLoopExecutor = null;
		}
	}

	/**
	 * Callback invoked when no face was detected for a long time
	 */
	private static void onUserHasGoneAway()
	{
		stopCVLoop();
		SWMain.getCVUtils().close();
		// TODO show notification
		System.out.println("User has gone");
		// TODO: restart the CV loop when the user comes back and presses continue
	}

	/**
	 * Callback invoked when multiple faces were detected for a long time
	 */
	private static void onMultipleFacesDetected()
	{
		// TODO show notification
	}

	/**
	 * Callback invoked when a face was detected and the posture state of the user has being obtained and stored
	 * in the given arg
	 *
	 * @param status the computed user posture state
	 */
	private static void processUserPostureState(PostureStatus status)
	{
		if (status.isPostureOk()) {
			// TODO remove notifications
			System.out.println("Remove notifications");
			return;
		}

		double distance = status.getDistance();
		if (distance > CVUtils.SAFE_DISTANCE_CM && distance != -1)
			// TODO show notification
			System.out.println("User is NOT at safe distance " + distance);
		else
			// TODO remove this
			System.out.println("User is at safe distance " + distance);

		if (status.isToTheRight() || status.isToTheLeft() || status.isToTheTop() || status.isToTheBottom())
			// TODO show notification
			System.out.println("User is not in the center of the screen");
	}

	/**
	 * @return true if the loop is NOT running, false otherwise
	 */
	public static boolean isCVLoopStopped()
	{
		synchronized (cvLoopExecutorLock) {
			return cvLoopExecutor == null || cvLoopExecutor.isShutdown();
		}
	}
}
