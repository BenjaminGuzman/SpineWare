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

package org.fos.sw.prefs;

import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.fos.sw.core.Loggers;

public abstract class PrefsIO
{
	protected Preferences prefs;

	/**
	 * Same as {@link Preferences#sync()} but this method will handle the exception by just logging a simple
	 * message and the exception
	 */
	protected void syncPrefs()
	{
		try {
			prefs.sync(); // ensure we read updated values
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Couldn't sync preferences for class " + this.getClass(),
				e
			);
		}
	}

	/**
	 * Same as {@link Preferences#flush()} but this method will handle the exception by just logging a simple
	 * message and the exception
	 */
	protected void flushPrefs()
	{
		try {
			prefs.flush(); // ensure preferences are saved
		} catch (BackingStoreException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Couldn't ensure preferences were saved",
				e
			);
		}
	}
}
