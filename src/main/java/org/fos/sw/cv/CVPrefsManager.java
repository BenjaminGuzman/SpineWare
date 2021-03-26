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

package org.fos.sw.cv;

import java.util.List;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.fos.sw.Loggers;
import org.fos.sw.prefs.cv.IdealFocalLengthPrefsIO;
import org.fos.sw.prefs.cv.MarginsPrefsIO;

public class CVPrefsManager
{
	private static final IdealFocalLengthPrefsIO fLengthPrefsIO = new IdealFocalLengthPrefsIO();
	private static final MarginsPrefsIO marginsPrefsIO = new MarginsPrefsIO();
	private static final Preferences cvPrefs = Preferences.userNodeForPackage(CVPrefsManager.class);
	private static final String FEATURE_ENABLED_KEY = "cv feature enabled";

	private CVPrefsManager()
	{
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
	 * @return the average focal length or {@link CVController#INVALID_IDEAL_FOCAL_LENGTH} if nothing has been saved
	 */
	public static double getFocalLength()
	{
		List<IdealFocalLengthMeasure> fLengths = fLengthPrefsIO.loadIdealFocalLengths();

		if (fLengths.isEmpty())
			return CVController.INVALID_IDEAL_FOCAL_LENGTH;

		OptionalDouble fLengthAvg = fLengths.stream()
			.mapToDouble(IdealFocalLengthMeasure::getIdealFocalLength)
			.average();

		return fLengthAvg.orElse(CVController.INVALID_IDEAL_FOCAL_LENGTH);
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
}
