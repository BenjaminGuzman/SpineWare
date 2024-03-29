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

package dev.benjaminguzman.prefs.cv;

import dev.benjaminguzman.core.Loggers;
import dev.benjaminguzman.cv.CVPrefs;
import dev.benjaminguzman.cv.CVUtils;
import dev.benjaminguzman.cv.IdealFocalLengthMeasure;
import dev.benjaminguzman.prefs.NotificationPrefsIO;

import java.util.List;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class CVPrefsManager
{
	private static final IdealFocalLengthPrefsIO fLengthPrefsIO = new IdealFocalLengthPrefsIO();
	private static final MarginsPrefsIO marginsPrefsIO = new MarginsPrefsIO();
	private static final Preferences cvPrefs = Preferences.userNodeForPackage(CVPrefsManager.class);
	private static final String FEATURE_ENABLED_KEY = "cv feature enabled";
	/**
	 * Default preferred refresh rate for the CV loop
	 */
	public static final int DEFAULT_REFRESH_RATE_MS = 700;
	private static final String REFRESH_RATE_KEY = "refresh rate";

	private CVPrefsManager() // prevent instantiation
	{
		throw new RuntimeException(this.getClass().getName() + " cannot be instantiated");
	}

	/**
	 * Saves the given measure for the ideal focal length
	 *
	 * @param fLengthMeasure the measured focal length
	 */
	public static void saveFocalLength(IdealFocalLengthMeasure fLengthMeasure)
	{
		fLengthPrefsIO.saveIdealFocalLength(fLengthMeasure);
	}


	/**
	 * Gets the average ideal focal length from the saved focal lengths
	 *
	 * @return the average focal length or {@link CVUtils#INVALID_IDEAL_FOCAL_LENGTH} if nothing has been saved
	 */
	public static double getFocalLength()
	{
		List<IdealFocalLengthMeasure> fLengths = fLengthPrefsIO.loadIdealFocalLengths();

		if (fLengths.isEmpty())
			return CVUtils.INVALID_IDEAL_FOCAL_LENGTH;

		OptionalDouble fLengthAvg = fLengths.stream()
			.mapToDouble(IdealFocalLengthMeasure::getIdealFocalLength)
			.average();

		return fLengthAvg.orElse(CVUtils.INVALID_IDEAL_FOCAL_LENGTH);
	}

	/**
	 * Removes all focal lengths saved
	 */
	public static void removeFocalLengths()
	{
		fLengthPrefsIO.clear();
	}

	public static void saveMargin(boolean is_margin_X, int margin_percentage)
	{
		marginsPrefsIO.saveMargin(is_margin_X, margin_percentage);
	}

	/**
	 * Gets the margin X or Y
	 * If the margin was not set 10 is returned
	 *
	 * @param is_margin_X if true the margin X is returned
	 * @return the margin
	 */
	public static int getMargin(boolean is_margin_X)
	{
		int margin = marginsPrefsIO.getMargin(is_margin_X);
		return margin == -1 ? 10 : margin;
	}

	/**
	 * @return true if the cv feature is enabled, false otherwise
	 */
	public static boolean isFeatureEnabled()
	{
		try {
			cvPrefs.sync();
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Error while syncing prefs", e);
		}
		return cvPrefs.getBoolean(FEATURE_ENABLED_KEY, false);
	}

	/**
	 * @param enabled if true the feature will be enabled
	 */
	public static void setFeatureEnabled(boolean enabled)
	{
		cvPrefs.putBoolean(FEATURE_ENABLED_KEY, enabled);
		try {
			cvPrefs.flush();
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Error while flushing prefs", e);
		}
	}

	/**
	 * Saves the refresh rate in the preferences
	 *
	 * @param refresh_rate the refresh rate in milliseconds to be saved
	 */
	public static void saveRefreshRate(int refresh_rate)
	{
		cvPrefs.putInt(REFRESH_RATE_KEY, refresh_rate);
		try {
			cvPrefs.flush();
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Error while flushing prefs", e);
		}
	}

	/**
	 * Gets the saved refresh rate in milliseconds
	 *
	 * @return the saved refresh rate or the default {@link #DEFAULT_REFRESH_RATE_MS}
	 */
	public static int getRefreshRate()
	{
		try {
			cvPrefs.sync();
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Error while syncing prefs", e);
		}
		return cvPrefs.getInt(REFRESH_RATE_KEY, DEFAULT_REFRESH_RATE_MS);
	}

	/**
	 * @return true if the camera has been calibrated (and the ideal focal length has been calculated)
	 */
	public static boolean isCamCalibrated()
	{
		return getFocalLength() != CVUtils.INVALID_IDEAL_FOCAL_LENGTH;
	}

	public static CVPrefs getCVPrefs()
	{
		return new CVPrefs(
			getMargin(true),
			getMargin(false),
			getFocalLength(),
			isFeatureEnabled(),
			getRefreshRate(),
			NotificationPrefsIO.getNotificationPrefLocation(
				true, // ignore cached values
				NotificationPrefsIO.NotificationPreferenceType.CV_NOTIFICATION
			)
		);
	}
}
