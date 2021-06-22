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

package org.fos.sw.cv;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import org.fos.sw.prefs.BackupPrefs;
import org.fos.sw.prefs.cv.CVPrefsManager;
import org.fos.sw.prefs.cv.IdealFocalLengthPrefsIO;
import org.fos.sw.prefs.cv.MarginsPrefsIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CVPrefsManagerTest
{
	static BackupPrefs marginsBackupPrefs = new BackupPrefs(
		Paths.get(System.getProperty("java.io.tmpdir"), "sw_cv_margin_prefs.bak"),
		MarginsPrefsIO.class
	);

	static BackupPrefs fBackupPrefs = new BackupPrefs(
		Paths.get(System.getProperty("java.io.tmpdir"), "sw_cv_flen_prefs.bak"),
		IdealFocalLengthPrefsIO.class
	);
	private final int margin_x = 20, margin_y = 50;
	private final IdealFocalLengthMeasure measurement = new IdealFocalLengthMeasure(20, 700);

	@BeforeAll
	static void beforeAll() throws BackingStoreException, IOException
	{
		marginsBackupPrefs.backupAndClear();
		fBackupPrefs.backupAndClear();
	}

	@AfterAll
	static void afterAll() throws BackingStoreException, IOException, InvalidPreferencesFormatException
	{
		marginsBackupPrefs.restore();
		fBackupPrefs.restore();
	}

	@Test
	void saveFocalLength()
	{
		CVPrefsManager.saveFocalLength(measurement);
		assertEquals(CVPrefsManager.getFocalLength(), measurement.getIdealFocalLength());
	}

	@Test
	void saveMargin()
	{
		CVPrefsManager.saveMargin(true, margin_x);
		assertEquals(CVPrefsManager.getMargin(true), margin_x);

		CVPrefsManager.saveMargin(false, margin_y);
		assertEquals(CVPrefsManager.getMargin(false), margin_y);
	}

	@Test
	void setFeatureEnabled()
	{
		CVPrefsManager.setFeatureEnabled(false);
		assertFalse(CVPrefsManager.isFeatureEnabled());

		CVPrefsManager.setFeatureEnabled(true);
		assertTrue(CVPrefsManager.isFeatureEnabled());
	}

	@Test
	void getCVPrefs()
	{
		CVPrefs prefs = CVPrefsManager.getCVPrefs();
		assertEquals(prefs.ideal_f_length, measurement.getIdealFocalLength());
		assertEquals(prefs.margin_x, margin_x);
		assertEquals(prefs.margin_y, margin_y);
		assertTrue(prefs.is_enabled);

		CVPrefsManager.setFeatureEnabled(false);
		assertFalse(CVPrefsManager.getCVPrefs().is_enabled);
	}
}