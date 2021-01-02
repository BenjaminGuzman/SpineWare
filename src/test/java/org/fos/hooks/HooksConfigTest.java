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

package org.fos.hooks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import org.fos.core.BreakType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HooksConfigTest
{
	static OutputStream backupOS;
	static Path backupPath = Paths.get("/tmp", "sw_prefs.bak");

	@BeforeAll
	public static void beforeAll() throws IOException, BackingStoreException
	{
		backupOS = Files.newOutputStream(backupPath);

		Preferences prefs = Preferences.userNodeForPackage(BreakHooksConfig.class);
		prefs.exportSubtree(backupOS);
		prefs.clear();
	}

	@AfterAll
	public static void afterAll() throws BackingStoreException, IOException, InvalidPreferencesFormatException
	{
		Preferences.userNodeForPackage(BreakHooksConfig.class).clear();
		Preferences.importPreferences(Files.newInputStream(backupPath));
	}

	void testConfig(HooksConfig expectedConf) throws InstantiationException
	{
		// save real config
		expectedConf.savePrefs();

		// load dummy config
		HooksConfig actualConf = HooksConfig.fromPrefs(expectedConf.getBreakType(), expectedConf.isNotificationHook());

		assertEquals(expectedConf.isNotificationHook(), actualConf.isNotificationHook());
		assertEquals(expectedConf.isStartEnabled(), actualConf.isStartEnabled());
		assertEquals(expectedConf.isEndEnabled(), actualConf.isEndEnabled());
		assertEquals(expectedConf.getBreakType(), actualConf.getBreakType());
		assertEquals(expectedConf.getOnStartAudioStr(), actualConf.getOnStartAudioStr());
		assertEquals(expectedConf.getOnEndAudioStr(), actualConf.getOnEndAudioStr());
		assertEquals(expectedConf.getOnStartCmdStr(), actualConf.getOnStartCmdStr());
		assertEquals(expectedConf.getOnEndCmdStr(), actualConf.getOnEndCmdStr());
	}

	@Test
	void smallBreakConfCreation() throws InstantiationException
	{
		// 4 tests
		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(true)
				.startEnabled(true)
				.endEnabled(true)
				.breakType(BreakType.SMALL_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);

		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(true)
				.startEnabled(true)
				.endEnabled(false)
				.breakType(BreakType.SMALL_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);

		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(true)
				.startEnabled(false)
				.endEnabled(false)
				.breakType(BreakType.SMALL_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);

		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(true)
				.startEnabled(false)
				.endEnabled(true)
				.breakType(BreakType.SMALL_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);

		// 4 tests
		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(false)
				.startEnabled(true)
				.endEnabled(true)
				.breakType(BreakType.SMALL_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);

		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(false)
				.startEnabled(true)
				.endEnabled(false)
				.breakType(BreakType.SMALL_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);

		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(false)
				.startEnabled(false)
				.endEnabled(false)
				.breakType(BreakType.SMALL_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);

		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(false)
				.startEnabled(false)
				.endEnabled(true)
				.breakType(BreakType.SMALL_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);
	}

	@Test
	void stretchBreakConfCreation() throws InstantiationException
	{
		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(true)
				.startEnabled(true)
				.endEnabled(true)
				.breakType(BreakType.STRETCH_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);

		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(false)
				.startEnabled(false)
				.endEnabled(false)
				.breakType(BreakType.STRETCH_BREAK)
				.onStartAudioStr("start audio")
				.onEndAudioStr("end audio")
				.onStartCmdStr("start command")
				.onEndCmdStr("end command")
				.createHooksConfig()
		);
	}

	@Test
	void dayBreakConfCreation() throws InstantiationException
	{
		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(true)
				.startEnabled(true)
				.endEnabled(true)
				.breakType(BreakType.DAY_BREAK)
				.onStartAudioStr("start audio")
				.onStartCmdStr("start command")
				.createHooksConfig()
		);

		testConfig(
			new HooksConfig.Builder()
				.isNotificationHook(false)
				.startEnabled(false)
				.endEnabled(false)
				.breakType(BreakType.DAY_BREAK)
				.onStartAudioStr("start audio")
				.onStartCmdStr("start command")
				.createHooksConfig()
		);
	}
}