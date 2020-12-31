/*
 * Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
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

package org.fos.core;

import org.fos.SWMain;
import org.fos.timers.BreakSettings;
import org.fos.timers.Clock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

class TimersManagerTest
{
	@BeforeAll
	static void beforeAll() throws BackingStoreException
	{
		Preferences prefs = Preferences.userNodeForPackage(TimersManager.class);
		SWMain.changeMessagesBundle(Locale.ENGLISH);
		TimersManager.init();
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
		// test correct time info & audio stuff
		byte hours = 1, minutes = 2, seconds = 3;
		int hms = hours * 60 * 60 + minutes * 60 + seconds;

		String[] notificationAudioPaths = new String[]{"/tmp/hola.wav", "/tmp/hola.khe", "/tmp.mundo"};
		String[] soundsDirs = new String[]{"/tmp", "/root", "/something"};
		BreakType[] breakTypes = BreakType.values();
		TimersManager.saveBreaksSettings(Arrays.asList(
			new BreakSettings.Builder().workTimerSettings(new Clock(hours, minutes, seconds)).breakTimerSettings(new Clock(hours, minutes, seconds)).postponeTimerSettings(new Clock(hours, minutes, seconds)).breakType(breakTypes[0]).notificationAudioPath(notificationAudioPaths[0]).breakAudiosDirStr(soundsDirs[0]).enabled(true).createBreakSettings(),
			new BreakSettings.Builder().workTimerSettings(new Clock(hours, minutes, seconds)).breakTimerSettings(new Clock(hours, minutes, seconds)).postponeTimerSettings(new Clock(hours, minutes, seconds)).breakType(breakTypes[1]).notificationAudioPath(notificationAudioPaths[1]).breakAudiosDirStr(soundsDirs[1]).enabled(true).createBreakSettings(),
			new BreakSettings.Builder().workTimerSettings(new Clock(hours, minutes, seconds)).breakTimerSettings(new Clock(hours, minutes, seconds)).postponeTimerSettings(new Clock(hours, minutes, seconds)).breakType(breakTypes[2]).notificationAudioPath(notificationAudioPaths[2]).breakAudiosDirStr(soundsDirs[2]).enabled(true).createBreakSettings()
		));
		List<BreakSettings> breaksSettings = TimersManager.loadBreaksSettings();
		BreakSettings breakSettings;
		for (byte i = 0; i < breaksSettings.size(); ++i) {
			breakSettings = breaksSettings.get(i);

			assertEquals(breakSettings.getBreakType(), breakTypes[i]);

			// DAY BREAK should not have break timer
			if (breakSettings.getBreakType() != BreakType.DAY_BREAK)
				assertEquals(breakSettings.getBreakTimerSettings().getHMSAsSeconds(), hms);
			else
				assertNull(breakSettings.getBreakTimerSettings());

			// assert audio stuff
			assertEquals(breakSettings.getNotificationAudioPath(), notificationAudioPaths[i]);
			assertEquals(breakSettings.getBreakAudiosDirStr(), soundsDirs[i]);

			// assert timers
			assertEquals(breakSettings.getPostponeTimerSettings().getHMSAsSeconds(), hms);
			assertEquals(breakSettings.getWorkTimerSettings().getHMSAsSeconds(), hms);
			assertTrue(breakSettings.isEnabled());
		}
	}

	@Test
	void saveBreaksSettings()
	{
		this.loadBreakSettings(); // this method also test the saveBreaksSettings method
	}

	@Test
	void singletonInstance()
	{
		assertThrows(RuntimeException.class, TimersManager::init);
	}
}