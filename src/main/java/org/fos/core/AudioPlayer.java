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

import org.fos.Loggers;
import org.fos.SWMain;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public class AudioPlayer extends Thread
{
	private Clip audioClip;

	public AudioPlayer()
	{
		super();
		this.setDaemon(true);
	}

	/**
	 * Gets the clip to reproduce audio files
	 * This method should be called before playing audio and using the this.soundClip object
	 * to ensure it isn't null
	 * If the sound clip couldn't be loaded, a warning message will be logged
	 *
	 * @return true if the soundClip could be loaded, false otherwise
	 */
	public boolean loadSoundClip()
	{
		if (this.audioClip != null)
			return true;

		try {
			this.audioClip = AudioSystem.getClip();
		} catch (LineUnavailableException | SecurityException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Couldn't get system clip to play audio",
				e
			);
			SwingUtilities.invokeLater(() -> this.showErrorAlert(SWMain.getMessagesBundle().getString("system_cannot_play_audio")));
			return false;
		} catch (IllegalArgumentException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Your system does not have support for playing audio files",
				e
			);
			SwingUtilities.invokeLater(() -> this.showErrorAlert(SWMain.getMessagesBundle().getString("system_cannot_play_audio")));
			return false;
		}
		return true;
	}

	/**
	 * Same as {@link #showErrorAlert(String, String)}
	 * But with default title set to the message property "error_while_playing_audio"
	 *
	 * @param message the message for the JOptionPane
	 */
	private void showErrorAlert(String message)
	{
		this.showErrorAlert(message, SWMain.getMessagesBundle().getString("error_while_playing_audio"));
	}

	/**
	 * Show a JOptionPane to the user with type {@link JOptionPane#ERROR_MESSAGE}
	 * If the SW could be loaded the alert will contain it
	 *
	 * @param message the message for the JOptionPane
	 * @param title   the title for the JOptionPane
	 */
	private void showErrorAlert(String message, String title)
	{
		Image swImg = SWMain.getSWIcon();

		JOptionPane.showConfirmDialog(
			null,
			message,
			title,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE,
			swImg != null ? new ImageIcon(swImg) : null
		);
	}
}