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

package org.fos.sw.prefs.cv;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.stream.IntStream;
import org.fos.sw.cv.IdealFocalLengthMeasure;
import org.fos.sw.prefs.BackupPrefs;
import org.fos.sw.prefs.timers.HooksPrefsIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IdealFocalLengthPrefsIOTest
{
	static BackupPrefs backupPrefs = new BackupPrefs(
		Paths.get(System.getProperty("java.io.tmpdir"), "sw_focal_prefs.bak"),
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
	void saveIdealFocalLength()
	{
		IdealFocalLengthPrefsIO prefsIO = new IdealFocalLengthPrefsIO();

		IntStream.rangeClosed(1, 100).forEach(i -> {
			prefsIO.saveIdealFocalLength(new IdealFocalLengthMeasure(i, i * 10));
		});
	}

	@Test
	void loadIdealFocalLengths()
	{
		IdealFocalLengthPrefsIO prefsIO = new IdealFocalLengthPrefsIO();

		List<IdealFocalLengthMeasure> fLengths = prefsIO.loadIdealFocalLengths();

		assertEquals(fLengths.size(), 100);

		fLengths.forEach(fLength -> assertEquals(fLength.getDistance(), fLength.getIdealFocalLength() / 10));
	}
}