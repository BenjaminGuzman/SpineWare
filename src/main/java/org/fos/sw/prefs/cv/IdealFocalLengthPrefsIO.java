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

package org.fos.sw.prefs.cv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.fos.sw.Loggers;
import org.fos.sw.cv.CVUtils;
import org.fos.sw.cv.IdealFocalLengthMeasure;
import org.fos.sw.prefs.PrefsIO;
import org.jetbrains.annotations.NotNull;

public class IdealFocalLengthPrefsIO extends PrefsIO
{
	private static final String IDEAL_FOCAL_LENGTH_PREFIX = "ideal focal length";

	public IdealFocalLengthPrefsIO()
	{
		this.prefs = Preferences.userNodeForPackage(this.getClass());
	}

	/**
	 * Saves the given ideal focal length in the preferences
	 *
	 * @param fLengthMeasure the ideal focal length measured
	 */
	public void saveIdealFocalLength(@NotNull final IdealFocalLengthMeasure fLengthMeasure)
	{
		this.prefs.putDouble(
			IDEAL_FOCAL_LENGTH_PREFIX + fLengthMeasure.getDistance(),
			fLengthMeasure.getIdealFocalLength()
		);

		flushPrefs();
	}

	/**
	 * Loads all the saved ideal focal lengths
	 *
	 * @return a list with all the focal length measurements
	 */
	public List<IdealFocalLengthMeasure> loadIdealFocalLengths()
	{
		syncPrefs();

		ArrayList<IdealFocalLengthMeasure> fLengths = new ArrayList<>(4);

		try {
			for (String key : this.prefs.keys()) {
				if (!key.contains(IDEAL_FOCAL_LENGTH_PREFIX))
					continue;

				fLengths.add(new IdealFocalLengthMeasure(
					Double.parseDouble(key.replace(IDEAL_FOCAL_LENGTH_PREFIX, "").trim()),
					this.prefs.getDouble(key, CVUtils.INVALID_IDEAL_FOCAL_LENGTH)
				));
			}
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Error while reading the saved ideal focal lengths",
				e
			);
		}

		return fLengths;
	}

	/**
	 * Removes all focal lengths saved
	 */
	public void clear()
	{
		try {
			// remove only the focal length preferences
			Arrays.stream(this.prefs.keys())
				.filter(prefName -> prefName.contains(IDEAL_FOCAL_LENGTH_PREFIX))
				.forEach(prefs::remove);
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Could not clear preferences for the ideal focal lengths",
				e
			);
		}
	}
}
