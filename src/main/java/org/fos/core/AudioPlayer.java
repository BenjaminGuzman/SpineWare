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

package org.fos.core;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;
import org.fos.Loggers;
import org.fos.SWMain;

public class AudioPlayer extends Thread
{
	private Clip audioClip;
	private Consumer<Exception> onError;

	public AudioPlayer()
	{
		super();
		this.setDaemon(true);
	}

	public AudioPlayer(Consumer<Exception> onError)
	{
		this();
		this.onError = onError;
	}

	/**
	 * Plays the given audio
	 * <p>
	 * this should be invoked only if {@link #readyToPlayAudio()} is invoked
	 * otherwise, this method is likely to throw {@link NullPointerException}
	 *
	 * @param audioFileAbsPath the absolute path for the audio file to be played
	 * @throws NullPointerException if the audio clip has not been loaded
	 */
	public void loadAudio(String audioFileAbsPath) throws UnsupportedAudioFileException, LineUnavailableException, IOException
	{

		this.audioClip.open(AudioSystem.getAudioInputStream(new File(audioFileAbsPath).getAbsoluteFile()));
	}

	/**
	 * Stops the audio clip
	 */
	public void stopAudio()
	{
		this.audioClip.stop();
	}

	/**
	 * Same as {@link Clip#getMicrosecondPosition()}
	 *
	 * @return the number of microseconds of data processed since the line was opened (since audio started playing)
	 */
	public long getMicroSecondsPosition()
	{
		return this.audioClip.getMicrosecondPosition();
	}

	/**
	 * Interrupts this thread & stops playing audio
	 */
	@Override
	public void interrupt()
	{
		super.interrupt();
		if (this.audioClip != null) {
			this.audioClip.flush();
			this.audioClip.stop();
			this.audioClip.close();
		}
	}

	/**
	 * Invoked when {@link Thread#start()} is invoked
	 * <p>
	 * This will start playing audio, therefore, be sure to call {@link #init()}, {@link #loadAudio(String)}
	 * before calling {@link Thread#start()}
	 */
	@Override
	public void run()
	{
		super.run();
		try {
			this.audioClip.start();
		} catch (Exception e) {
			if (this.onError != null)
				this.onError.accept(e);
		}
	}

	/**
	 * Tells if the system audio clip has already loaded
	 * <p>
	 * If this methods returns false, you can try to load the system audio clip with {@link #init()}
	 *
	 * @return true if the system audio clip has been loaded and can be used to play audio, false otherwise
	 */
	public boolean readyToPlayAudio()
	{
		return this.audioClip != null;
	}

	/**
	 * Gets the clip to reproduce audio files
	 * This method should be called before playing audio and using the this.soundClip object
	 * to ensure it isn't null
	 * If the sound clip couldn't be loaded, a warning message will be logged
	 *
	 * @return true if the soundClip could be loaded, false otherwise
	 */
	public boolean init()
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
			SwingUtilities.invokeLater(
				() -> this.showErrorAlert(SWMain.getMessagesBundle().getString("system_cannot_play_audio"))
			);
			return false;
		} catch (IllegalArgumentException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Your system does not have support for playing audio files",
				e
			);
			SwingUtilities.invokeLater(
				() -> this.showErrorAlert(SWMain.getMessagesBundle().getString("system_cannot_play_audio"))
			);
			return false;
		}
		return true;
	}

	/**
	 * Set the {@link Consumer} object to execute if there is an error WHILE playing audio
	 * <p>
	 * The {@link Consumer#accept(Object)}} method will be invoked only when there is an error while playing audio, not if
	 * there is an error obtaining the system audio clip or similar errors
	 *
	 * @param onError the action to run
	 */
	public void setOnError(Consumer<Exception> onError)
	{
		this.onError = onError;
	}

	/**
	 * Shows an error alert using a JOptionPane as in {@link SWMain#showErrorAlert(String, String)}
	 *
	 * Title for the message will be the message property "error_while_playing_audio"
	 *
	 * @param message the message for the JOptionPane
	 */
	private void showErrorAlert(String message)
	{
		SWMain.showErrorAlert(message, SWMain.getMessagesBundle().getString("error_while_playing_audio"));
	}
}