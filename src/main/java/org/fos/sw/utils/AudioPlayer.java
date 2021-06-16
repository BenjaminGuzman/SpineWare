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

package org.fos.sw.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.fos.sw.SWMain;
import org.fos.sw.core.Loggers;
import org.jetbrains.annotations.NotNull;
import static javax.sound.sampled.LineEvent.Type.STOP;

public class AudioPlayer implements Runnable
{
	private Clip audioClip;
	private Player mp3Player = null;
	private Consumer<Exception> onError;

	/**
	 * Executed when an audio stops playing
	 */
	private Runnable onAudioEnd;

	public AudioPlayer()
	{
		super();
	}

	public AudioPlayer(Consumer<Exception> onError)
	{
		this();
		this.onError = onError;
	}

	/**
	 * Set the hook to execute when the audio ends
	 * specifically this hook will be executed when
	 * {@link javax.sound.sampled.LineEvent.Type#STOP} or
	 * {@link javax.sound.sampled.LineEvent.Type#CLOSE}
	 * is received in the data line
	 *
	 * @param onAudioEnd the hook to execute
	 */
	public void onAudioEnd(Runnable onAudioEnd)
	{
		this.onAudioEnd = onAudioEnd;
	}

	/**
	 * Plays the given audio
	 * <p>
	 * This method will use the java sound API or the java layer library to play files
	 * Specifically it will use the java layer library if the audio is an mp3
	 *
	 * @param audioFileAbsPath the absolute path for the audio file to be played
	 * @throws NullPointerException if the audio clip has not been loaded
	 */
	public void loadAudio(@NotNull String audioFileAbsPath) throws UnsupportedAudioFileException,
		LineUnavailableException, IOException, JavaLayerException
	{
		this.audioClip.removeLineListener(this::lineListener);
		this.audioClip.close();
		Loggers.getDebugLogger().log(
			Level.INFO,
			"Loading audio: " + audioFileAbsPath
		);

		// if the audio is an mp3, play it with java layer, because the native java API does not support mp3
		if (audioFileAbsPath.endsWith(".mp3")) {
			this.mp3Player = new Player(new FileInputStream(audioFileAbsPath));
			return;
		}

		this.audioClip.open(AudioSystem.getAudioInputStream(
			new File(audioFileAbsPath).getAbsoluteFile()
		));
		this.audioClip.addLineListener(this::lineListener);
	}

	private void lineListener(LineEvent event)
	{
		LineEvent.Type evtType = event.getType();
		if (STOP.equals(evtType) && this.onAudioEnd != null)
			this.onAudioEnd.run();
	}

	/**
	 * Stops the audio clip
	 */
	public void stopAudio()
	{
		if (this.audioClip != null)
			this.audioClip.stop();
		if (this.mp3Player != null)
			this.mp3Player.close();
	}

	/**
	 * stops playing audio & close resources
	 */
	public void shutdown()
	{
		if (this.audioClip != null) {
			this.audioClip.removeLineListener(this::lineListener);
			this.audioClip.flush();
			this.audioClip.stop();
			this.audioClip.close();
		}
		if (this.mp3Player != null) {
			this.mp3Player.close();
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
		try {
			if (this.mp3Player != null) {
				this.mp3Player.play(); // this is a blocking call
				if (this.onAudioEnd != null)
					this.onAudioEnd.run();
				this.mp3Player = null;
				return;
			}
		} catch (JavaLayerException e) {
			this.onError.accept(e);
			return;
		}

		this.audioClip.start();
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
				() -> this.showErrorAlert(SWMain.messagesBundle.getString("system_cannot_play_audio"))
			);
			return false;
		} catch (IllegalArgumentException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Your system does not have support for playing audio files",
				e
			);
			SwingUtilities.invokeLater(
				() -> this.showErrorAlert(SWMain.messagesBundle.getString("system_cannot_play_audio"))
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
		SWMain.showErrorAlert(message, SWMain.messagesBundle.getString("error_while_playing_audio"));
	}
}