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

package org.fos.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import org.fos.SWMain;
import org.fos.timers.BreakConfig;
import org.fos.timers.Clock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimersManagerTest
{
	static OutputStream backupOS;
	static Path backupPath = Paths.get(System.getProperty("java.io.tmpdir"), "sw_prefs.bak");

	@BeforeAll
	static void beforeAll() throws BackingStoreException, IOException
	{
		backupOS = Files.newOutputStream(backupPath);

		Preferences prefs = Preferences.userNodeForPackage(TimersManager.class);
		SWMain.changeMessagesBundle(Locale.ENGLISH);
		TimersManager.init();
		prefs.exportSubtree(backupOS);
		prefs.clear();
	}

	@AfterAll
	static void afterAll() throws BackingStoreException, IOException, InvalidPreferencesFormatException
	{
		Preferences prefs = Preferences.userNodeForPackage(TimersManager.class);
		prefs.clear();

		Preferences.importPreferences(Files.newInputStream(backupPath));
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
			new BreakConfig.Builder().workTimerSettings(new Clock(hours, minutes, seconds)).breakTimerSettings(new Clock(hours, minutes, seconds)).postponeTimerSettings(new Clock(hours, minutes, seconds)).breakType(breakTypes[0]).enabled(true).createBreakSettings(),
			new BreakConfig.Builder().workTimerSettings(new Clock(hours, minutes, seconds)).breakTimerSettings(new Clock(hours, minutes, seconds)).postponeTimerSettings(new Clock(hours, minutes, seconds)).breakType(breakTypes[1]).enabled(true).createBreakSettings(),
			new BreakConfig.Builder().workTimerSettings(new Clock(hours, minutes, seconds)).breakTimerSettings(new Clock(hours, minutes, seconds)).postponeTimerSettings(new Clock(hours, minutes, seconds)).breakType(breakTypes[2]).enabled(true).createBreakSettings()
		));
		List<BreakConfig> breaksSettings = TimersManager.loadBreaksSettings();
		BreakConfig breakConfig;
		for (byte i = 0; i < breaksSettings.size(); ++i) {
			breakConfig = breaksSettings.get(i);

			assertEquals(breakConfig.getBreakType(), breakTypes[i]);

			// DAY BREAK should not have break timer
			if (breakConfig.getBreakType() != BreakType.DAY_BREAK)
				assertEquals(breakConfig.getBreakTimerSettings().getHMSAsSeconds(), hms);
			else
				assertNull(breakConfig.getBreakTimerSettings());

			// assert timers
			assertEquals(breakConfig.getPostponeTimerSettings().getHMSAsSeconds(), hms);
			assertEquals(breakConfig.getWorkTimerSettings().getHMSAsSeconds(), hms);
			assertTrue(breakConfig.isEnabled());
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