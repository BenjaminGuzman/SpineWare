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

package net.benjaminguzman.cv;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

import net.benjaminguzman.core.Loggers;
import net.benjaminguzman.gui.notifications.PostureNotification;
import net.benjaminguzman.SWMain;
import net.benjaminguzman.prefs.cv.CVPrefsManager;
import net.benjaminguzman.utils.DaemonThreadFactory;
import org.jetbrains.annotations.NotNull;

public class CVManager
{
	/**
	 * Make this volatile to avoid visibility issues
	 * atomicity is not needed here (or at least it'd be an overkill)
	 */
	private static volatile boolean instantiated;
	private static ScheduledExecutorService cvLoopExecutor;

	private static final Object notificationLock = new Object();
	/**
	 * Notification currently showing or shown to the user.
	 */
	private static PostureNotification postureNotification;
	private static final CVLoop cvLoop = new CVLoop(
		CVManager::processUserPostureState,
		CVManager::onUserHasGoneAway,
		CVManager::onMultipleFacesDetected
	);

	private CVManager()
	{
		throw new RuntimeException(this.getClass().getName() + " cannot be instantiated");
	}

	public static void init()
	{
		if (instantiated)
			throw new RuntimeException("Don't invoke init() method more than once");

		instantiated = true;
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

		synchronized (cvLoop) {
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
				cvPrefs.refresh_rate,
				TimeUnit.MILLISECONDS
			);
			Loggers.getDebugLogger().log(
				Level.INFO,
				"CV loop started. Executing every " + cvPrefs.refresh_rate + "ms."
			);

			disposeNotification();
			SwingUtilities.invokeLater(() -> {
				synchronized (notificationLock) {
					postureNotification = new PostureNotification(cvPrefs.notifLocation);
				}
			});
		}
	}

	/**
	 * Stops the CV Loop (if running)
	 * If you need to "restart" the loop, just call {@link #startCVLoop()} after calling this method
	 * This method will also close the camera, if you don't want that use {@link #stopCVLoop(boolean)}
	 * This method will also close the notification (if any), if you don't want that use {@link #stopCVLoop(boolean, boolean)}
	 *
	 * @see #stopCVLoop(boolean)
	 */
	public static void stopCVLoop()
	{
		stopCVLoop(true);
	}

	/**
	 * Stops the CV Loop (if running)
	 * If you need to "restart" the loop, just call {@link #startCVLoop()} after calling this method
	 * This method will also dispose the notification (if it is showing), if you don't want this use {@link #stopCVLoop(boolean, boolean)}
	 *
	 * @param close_cam if true the cam will be closed
	 */
	public static void stopCVLoop(boolean close_cam)
	{
		stopCVLoop(close_cam, true);
	}

	/**
	 * Stops the CV loop (if running)
	 * If you need to "restart" the loop, just call {@link #startCVLoop()} after calling this method
	 *
	 * @param close_cam            if true the cam will be closed
	 * @param dispose_notification if true, the notification will be disposed
	 */
	public static void stopCVLoop(boolean close_cam, boolean dispose_notification)
	{
		Loggers.getDebugLogger().entering(CVManager.class.getName(), "stopCVLoop");

		synchronized (cvLoop) {
			if (dispose_notification)
				disposeNotification();

			if (cvLoopExecutor == null) // cv loop is not running
				return;

			if (close_cam)
				SWMain.getCVUtils().close();
			cvLoopExecutor.shutdownNow();
			cvLoopExecutor = null;
		}

		Loggers.getDebugLogger().exiting(CVManager.class.getName(), "stopCVLoop");
	}

	/**
	 * Removes any preconfigured onDispose hooks from the notification and disposes it
	 * In other words, no dispose hooks will be executed if you call this method.
	 * This method will block until the notification is disposed
	 */
	public static void disposeNotification()
	{
		synchronized (notificationLock) {
			if (postureNotification == null)
				return;

			postureNotification.setOnDisposed(null); // clear any custom callback. This method does not care
			// about it, we just want to dispose the notification

			if (!SwingUtilities.isEventDispatchThread())
				try {
					SwingUtilities.invokeAndWait(postureNotification::dispose);
				} catch (InterruptedException | InvocationTargetException e) {
					Loggers.getErrorLogger().log(
						Level.SEVERE,
						"Error while disposing cv notification",
						e
					);
				}
			else
				postureNotification.dispose();
		}
	}

	/**
	 * Callback invoked when no face was detected for a long time
	 */
	private static void onUserHasGoneAway()
	{
		// stop the loop but don't dispose the notification as it will be shown later (see below)
		stopCVLoop(true, false);

		synchronized (notificationLock) {
			postureNotification.setPostureStatus(PostureStatus.USER_IS_AWAY);
			postureNotification.setOnDisposed(() -> {
				// user has come back
				startCVLoop();
				if (postureNotification != null)
					postureNotification.setOnDisposed(null);
			});

			// don't synchronize inside swing thread
			// 🤞 nobody access the notification exactly when it is being showed
			SwingUtilities.invokeLater(postureNotification::showNotification);
		}
	}

	/**
	 * Callback invoked when multiple faces were detected for a long time
	 */
	private static void onMultipleFacesDetected()
	{
		synchronized (notificationLock) {
			postureNotification.setPostureStatus(PostureStatus.MULTIPLE_FACES);
			// don't synchronize inside swing thread
			// 🤞 nobody access the notification exactly when it is being showed
			SwingUtilities.invokeLater(postureNotification::showNotification);
		}
	}

	/**
	 * Callback invoked when a face was detected and the posture state of the user has being obtained and stored
	 * in the given arg
	 *
	 * @param status the computed user posture state
	 */
	private static void processUserPostureState(PostureAnalytics status)
	{
		double distance = status.getDistance();
		synchronized (notificationLock) {
			if (distance < CVUtils.SAFE_DISTANCE_CM && distance != -1) {
				postureNotification.setDistanceToCam(distance);
				postureNotification.setPostureStatus(PostureStatus.TOO_CLOSE);
			} else if (status.isToTheRight() || status.isToTheLeft() || status.isToTheTop() || status.isToTheBottom())
				postureNotification.setPostureStatus(PostureStatus.NOT_IN_CENTER);
			else { // posture is ok
				SwingUtilities.invokeLater(postureNotification::dispose);
				return;
			}
			// don't synchronize inside swing thread
			// 🤞 nobody access the notification exactly when it is being showed
			SwingUtilities.invokeLater(postureNotification::showNotification);
		}
	}

	/**
	 * Same as {@link #isCVLoopStopped()} but without synchronization.
	 * It is actually safe to call this method if you've already acquired the lock on {@link #cvLoop}
	 *
	 * @return true if the loop is not running, false otherwise
	 */
	private static boolean isCVLoopStoppedUnsafe()
	{
		return cvLoopExecutor == null || cvLoopExecutor.isShutdown();
	}

	/**
	 * Warning: If you have already acquired the lock and call this method a deadlock may be produced
	 * because this method also acquires the lock
	 *
	 * @return true if the loop is NOT running, false otherwise
	 */
	public static boolean isCVLoopStopped()
	{
		synchronized (cvLoop) {
			return isCVLoopStoppedUnsafe();
		}
	}
}
