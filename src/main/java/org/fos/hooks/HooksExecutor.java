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

package org.fos.hooks;

import java.io.IOException;
import java.util.logging.Level;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.core.AudioPlayer;
import org.fos.core.CommandExecutor;

public class HooksExecutor
{
	private HooksConfig config;
	private AudioPlayer audioPlayer;
	private CommandExecutor cmdExecutor;

	public HooksExecutor()
	{
	}

	public HooksExecutor(HooksConfig config)
	{
		this();
		this.config = config;
	}

	synchronized public void setConfig(HooksConfig config)
	{
		this.config = config;
	}

	/**
	 * Runs the hooks configured to run on start
	 */
	synchronized public void runStart()
	{
		if (this.config != null)
			this.runHooks(this.config.getOnStartAudioStr(), this.config.getOnStartCmdStr());
	}

	/**
	 * Runs the hooks configured to run on termination
	 */
	synchronized public void runEnd()
	{
		if (this.config != null)
			this.runHooks(this.config.getOnEndAudioStr(), this.config.getOnEndCmdStr());
	}

	synchronized public void runHooks(String audioPath, String cmd)
	{
		this.stop(); // stop everything currently running

		if (audioPath != null) {
			this.audioPlayer = new AudioPlayer(this::onAudioError);
			try {
				this.audioPlayer.loadAudio(audioPath);
				this.audioPlayer.start(); // start the thread
			} catch (UnsupportedAudioFileException | LineUnavailableException e) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Error while playing audio",
					e
				);
			} catch (IOException e) {
				SWMain.showErrorAlert(
					SWMain.getMessagesBundle().getString("error_reading_file")
						+ " \"" + audioPath + "\" "
						+ SWMain.getMessagesBundle().getString("verify_the_file_exists"),
					SWMain.getMessagesBundle().getString("error_while_playing_audio")
				);
			}
		}

		if (cmd != null) {
			this.cmdExecutor = new CommandExecutor(cmd, this::onCMDError);
			this.cmdExecutor.start(); // start the thread
		}
	}

	/**
	 * Stops any execution currently happening
	 * <p>
	 * This will internally interrupt the executing threads (those created to play audio or execute a command)
	 */
	synchronized public void stop()
	{
		if (this.audioPlayer != null)
			this.audioPlayer.interrupt();
		if (this.cmdExecutor != null)
			this.cmdExecutor.interrupt();
	}

	public void onAudioError(Exception e)
	{
		// an interrupted exception is normal as all audio playing is stopped when its time to do
		// something else
		if (e instanceof InterruptedException)
			return;

		Loggers.getErrorLogger().log(Level.WARNING, "Audio error", e);
	}

	public void onCMDError(Exception e)
	{
		// an interrupted exception is normal as all commands executing are destroyed when its time to
		// execute something else
		if (e instanceof InterruptedException)
			return;

		Loggers.getErrorLogger().log(Level.WARNING, "Command execution error", e);
	}
}
