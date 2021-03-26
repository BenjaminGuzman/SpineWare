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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import org.fos.sw.SWMain;
import org.jetbrains.annotations.NotNull;

public class BackupPrefs
{
	@NotNull
	private final Class<? extends PrefsIO> c;

	@NotNull
	private final Path backupPath;

	public BackupPrefs(@NotNull Path backupPath, @NotNull Class<? extends PrefsIO> c)
	{
		this.c = c;
		this.backupPath = backupPath;
	}

	public void backupAndClear() throws IOException, BackingStoreException
	{
		if (!backupPath.toFile().exists())
			Files.createFile(backupPath);
		OutputStream backupOS = Files.newOutputStream(backupPath);

		Preferences prefs = Preferences.userNodeForPackage(c);
		SWMain.changeMessagesBundle(Locale.ENGLISH);
		prefs.exportSubtree(backupOS);
		prefs.clear();
	}

	public void restore() throws BackingStoreException, IOException, InvalidPreferencesFormatException
	{
		Preferences prefs = Preferences.userNodeForPackage(this.c);
		prefs.clear();

		Preferences.importPreferences(Files.newInputStream(backupPath));
	}
}
