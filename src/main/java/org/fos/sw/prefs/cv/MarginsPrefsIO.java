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

import java.util.prefs.Preferences;
import org.fos.sw.prefs.PrefsIO;

public class MarginsPrefsIO extends PrefsIO
{
	public MarginsPrefsIO()
	{
		this.prefs = Preferences.userNodeForPackage(this.getClass());
	}

	/**
	 * Saves the given margin
	 *
	 * @param is_margin_X       if true, the margin_percentage is margin X, if not is margin Y
	 * @param margin_percentage the percentage
	 */
	public void saveMargin(boolean is_margin_X, int margin_percentage)
	{
		this.prefs.putInt(
			getPrefMarginName(is_margin_X),
			margin_percentage
		);

		flushPrefs();
	}

	/**
	 * Gets the saved preference for a certain margin
	 *
	 * @param is_margin_X if true, the margin X preference will be returned, if false the margin Y will be returned
	 * @return the saved margin pref or -1 if there was no preference saved
	 */
	public int getMargin(boolean is_margin_X)
	{
		syncPrefs();
		return this.prefs.getInt(getPrefMarginName(is_margin_X), -1);
	}

	/**
	 * Returns the preference name depending on the is margin X param
	 *
	 * @param is_margin_X if true, the pref name will be the one assigned to the margin x
	 * @return the preference name
	 */
	private String getPrefMarginName(boolean is_margin_X)
	{
		return is_margin_X ? "margin X" : "margin Y";
	}
}
