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

package net.benjaminguzman.prefs.timers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import net.benjaminguzman.hooks.HooksConfig;
import net.benjaminguzman.prefs.BackupPrefs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HooksPrefsIOTest
{
	static BackupPrefs backupPrefs = new BackupPrefs(
		Paths.get(System.getProperty("java.io.tmpdir"), "sw_hooks_prefs.bak"),
		HooksPrefsIO.class
	);

	@BeforeAll
	static void beforeAll() throws BackingStoreException, IOException
	{
		backupPrefs.backupAndClear();
	}

	@AfterAll
	static void afterAll() throws BackingStoreException, IOException, InvalidPreferencesFormatException
	{
		backupPrefs.restore();
	}

	@Test
	void saveActiveHoursHooks()
	{
		HooksPrefsIO prefsIO = new HooksPrefsIO(Preferences.userNodeForPackage(TimersPrefsIO.class));
		HooksConfig beforeActiveHours, afterActiveHours;
		prefsIO.saveActiveHoursHooks(
			(beforeActiveHours = new HooksConfig.Builder()
				.isNotificationHook(true)
				.startAudioIsDir(false)
				.startEnabled(true)
				.onStartAudioStr("some audio str")
				.onStartCmdStr("some command")
				.endEnabled(false) // the end is disabled 'cause there are no on disposed hooks
				.createHooksConfig()),
			false
		);
		prefsIO.saveActiveHoursHooks(
			(afterActiveHours = new HooksConfig.Builder()
				.isNotificationHook(true)
				.startAudioIsDir(false)
				.startEnabled(true)
				.onStartAudioStr("some audio str")
				.onStartCmdStr("some command")
				.endEnabled(false) // the end is disabled 'cause there are no on disposed hooks
				.createHooksConfig()),
			true
		);

		HooksConfig afterActiveHoursActual = prefsIO.loadForActiveHours(true);
		HooksConfig beforeActiveHoursActual = prefsIO.loadForActiveHours(false);

		assertEquals(afterActiveHours, afterActiveHoursActual);
		assertEquals(beforeActiveHours, beforeActiveHoursActual);
	}
}