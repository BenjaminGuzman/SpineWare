/*
 * Copyright (c) 2020. Benjamín Guzmán
 * Author: Benjamín Guzmán <bg@benjaminguzman.dev>
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

import java.util.Arrays;
import java.util.List;
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
	void loadBreakSettings()
	{
		TimersManager timersManager = new TimersManager();
		timersManager.saveBreaksSettings(Arrays.asList(
			new BreakSettings.Builder().workTimerSettings(null).breakTimerSettings(null).postponeTimerSettings(null).breakType(BreakType.SMALL_BREAK).notificationAudioPath(null).breakAudiosDirStr(null).enabled(false).createBreakSettings(),
			new BreakSettings.Builder().workTimerSettings(null).breakTimerSettings(null).postponeTimerSettings(null).breakType(BreakType.STRETCH_BREAK).notificationAudioPath(null).breakAudiosDirStr(null).enabled(false).createBreakSettings(),
			new BreakSettings.Builder().workTimerSettings(null).breakTimerSettings(null).postponeTimerSettings(null).breakType(BreakType.DAY_BREAK).notificationAudioPath(null).breakAudiosDirStr(null).enabled(false).createBreakSettings()
		));
		List<BreakSettings> breaksSettings = timersManager.loadBreaksSettings();
		breaksSettings.forEach(breakSettings -> {
			assertNull(breakSettings.getBreakTimerSettings());
			assertNull(breakSettings.getWorkTimerSettings());
			assertFalse(breakSettings.isEnabled());
		});

		byte hours = 1, minutes = 2, seconds = 3;
		int hms = hours * 60 * 60 + minutes * 60 + seconds;
		timersManager.saveBreaksSettings(Arrays.asList(
			new BreakSettings.Builder().workTimerSettings(new TimerSettings(hours, minutes, seconds)).breakTimerSettings(new TimerSettings(hours, minutes, seconds)).postponeTimerSettings(new TimerSettings(hours, minutes, seconds)).breakType(BreakType.SMALL_BREAK).notificationAudioPath(null).breakAudiosDirStr(null).enabled(true).createBreakSettings(),
			new BreakSettings.Builder().workTimerSettings(new TimerSettings(hours, minutes, seconds)).breakTimerSettings(new TimerSettings(hours, minutes, seconds)).postponeTimerSettings(new TimerSettings(hours, minutes, seconds)).breakType(BreakType.SMALL_BREAK).notificationAudioPath(null).breakAudiosDirStr(null).enabled(true).createBreakSettings(),
			new BreakSettings.Builder().workTimerSettings(new TimerSettings(hours, minutes, seconds)).breakTimerSettings(new TimerSettings(hours, minutes, seconds)).postponeTimerSettings(new TimerSettings(hours, minutes, seconds)).breakType(BreakType.SMALL_BREAK).notificationAudioPath(null).breakAudiosDirStr(null).enabled(true).createBreakSettings()
		));
		breaksSettings = timersManager.loadBreaksSettings();
		breaksSettings.forEach(breakSettings -> {
			if (breakSettings.getBreakTimerSettings() != null) {
				assertEquals(breakSettings.getBreakTimerSettings().getHMSAsSeconds(), hms);
				assertEquals(breakSettings.getPostponeTimerSettings().getHMSAsSeconds(), hms);
			}

			assertEquals(breakSettings.getWorkTimerSettings().getHMSAsSeconds(), hms);
			assertTrue(breakSettings.isEnabled());
		});
	}

	@Test
	void saveBreaksSettings() {
		this.loadBreakSettings(); // this method also test the saveBreaksSettings method
	}
}