/*
 * Copyright (c) 2020. Benjamín Guzmán
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

package org.fos.core;

import org.fos.timers.BreakSettings;
import org.fos.timers.TimerSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimersManagerTest {

	@BeforeAll
	static void beforeAll() throws BackingStoreException {
		Preferences prefs = Preferences.userNodeForPackage(TimersManager.class);
		prefs.clear();
	}

	@AfterAll
	static void afterAll() throws BackingStoreException {
		Preferences prefs = Preferences.userNodeForPackage(TimersManager.class);
		prefs.clear();
	}

	@Test
	void loadBreakSettings() {
		TimersManager timersManager = new TimersManager();
		timersManager.saveBreaksSettings(new BreakSettings[]{
			new BreakSettings(null, null, false),
			new BreakSettings(null, null, false),
			new BreakSettings(null, null, false)
		});
		BreakSettings[] breaksSettings = timersManager.loadBreaksSettings();
		for (BreakSettings breakSettings : breaksSettings) {
			assertNull(breakSettings.breakTimerSettings);
			assertNull(breakSettings.workTimerSettings);
			assertFalse(breakSettings.isEnabled());
		}

		byte hours = 1, minutes = 2, seconds = 3;
		int hms = hours * 60 * 60 + minutes * 60 + seconds;
		timersManager.saveBreaksSettings(new BreakSettings[]{
			new BreakSettings(
				new TimerSettings(hours, minutes, seconds),
				new TimerSettings(hours, minutes, seconds), true),
			new BreakSettings(
				new TimerSettings(hours, minutes, seconds),
				new TimerSettings(hours, minutes, seconds), true),
			new BreakSettings(
				new TimerSettings(hours, minutes, seconds),
				new TimerSettings(hours, minutes, seconds), true)
		});
		breaksSettings = timersManager.loadBreaksSettings();
		for (BreakSettings breakSettings : breaksSettings) {
			if (breakSettings.breakTimerSettings != null)
				assertEquals(breakSettings.breakTimerSettings.getHMSAsSeconds(), hms);

			assertEquals(breakSettings.workTimerSettings.getHMSAsSeconds(), hms);
			assertTrue(breakSettings.isEnabled());
		}
	}

	@Test
	void saveBreaksSettings() {
		this.loadBreakSettings(); // this method also test the saveBreaksSettings method
	}
}