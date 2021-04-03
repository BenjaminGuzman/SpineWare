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
import org.fos.sw.Loggers;
import org.fos.sw.SWMain;
import org.fos.sw.utils.DaemonThreadFactory;

public class CVManager
{
	/**
	 * Make this volatile to avoid visibility issues
	 * atomicity is not needed here (or at least it'd be an overkill)
	 */
	private static volatile boolean instantiated;

	private static CVLoop cvLoop;
	private static ScheduledExecutorService cvLoopExecutor;

	private CVManager()
	{
		throw new RuntimeException(this.getClass().getName() + " cannot be instantiated");
	}

	public static void init()
	{
		if (instantiated)
			throw new RuntimeException("Don't invoke init() method more than once");

		instantiated = true;
		cvLoop = new CVLoop(CVManager::onUserGoneAway, CVManager::processUserPostureState);
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
	synchronized public static void startCVLoop(CVPrefs cvPrefs)
	{
		Loggers.getDebugLogger().log(Level.INFO, "Starting CV Loop...");

		if (!cvPrefs.is_enabled)
			return;

		if (cvPrefs.ideal_f_length == CVUtils.INVALID_IDEAL_FOCAL_LENGTH) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				SWMain.messagesBundle.getString("cam_not_calibrated_warning_title")
					+ SWMain.messagesBundle.getString("cam_not_calibrated_warning")
			);

			// showing an alert may be a little intrusive and annoying
			/*SWMain.showErrorAlert(
				SWMain.messagesBundle.getString("cam_not_calibrated_warning"),
				SWMain.messagesBundle.getString("cam_not_calibrated_warning_title")
			);*/
		}

		SWMain.getCVUtils().open();
		cvLoop.setCVPrefs(cvPrefs);

		if (cvLoopExecutor == null) {
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
	synchronized public static void stopCVLoop()
	{
		stopCVLoop(true);
	}

	/**
	 * Stops the CV Loop (if running)
	 * If you need to "restart" the loop, just call {@link #startCVLoop()}
	 *
	 * @param close_cam if true the cam will be closed
	 */
	synchronized public static void stopCVLoop(boolean close_cam)
	{
		Loggers.getDebugLogger().log(Level.INFO, "Stopping CV Loop...");
		if (cvLoopExecutor != null) {
			cvLoopExecutor.shutdownNow();
			if (close_cam)
				SWMain.getCVUtils().close();
		}
		cvLoopExecutor = null;
	}

	/**
	 * Callback invoked when no face was detected for a long time
	 */
	private static void onUserGoneAway()
	{
		Loggers.getDebugLogger().log(Level.INFO, "User is gone");
		stopCVLoop();
		SWMain.getCVUtils().close();
		// TODO: restart the CV loop when the user comes back and presses continue
	}

	/**
	 * Callback invoked when a face was detected and the posture state of the user has being obtained and stored
	 * in the given arg
	 *
	 * @param postureState the computed user posture state
	 */
	private static void processUserPostureState(PostureState postureState)
	{
		if (postureState.getDistance() > CVUtils.SAFE_DISTANCE_CM)
			Loggers.getDebugLogger().log(Level.INFO, "User is NOT at safe distance " + postureState.getDistance());
		else
			Loggers.getDebugLogger().log(Level.INFO, "User is at safe distance " + postureState.getDistance());

		if (postureState.isToTheRight() || postureState.isToTheLeft() || postureState.isToTheTop() || postureState.isToTheBottom())
			Loggers.getDebugLogger().log(Level.INFO, "User is not in the center of the screen");
	}
}
