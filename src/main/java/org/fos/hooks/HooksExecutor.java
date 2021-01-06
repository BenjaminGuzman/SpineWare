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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.core.AudioPlayer;
import org.fos.core.CommandExecutor;
import org.fos.core.DaemonAudioThreadFactory;
import org.fos.core.DaemonThreadFactory;
import org.fos.gui.hooksconfig.HooksConfigPanel;

public class HooksExecutor
{
	private HooksConfig config;
	private AudioPlayer audioPlayer;
	private CommandExecutor cmdExecutor;

	/**
	 * Thread used to start playing audio and not avoid blocking
	 * This way audio and the given command can be executed in parallel
	 */
	private final ExecutorService audioStartThreadExecutor;
	/**
	 * Thread used to actually play audio
	 */
	private final ExecutorService audioThreadExecutor;
	private List<String> supportedAudioExtensions;
	private Future<?> audioPlayTask;  // future used to play audio in parallel
	private Future<?> audioStartTask; // future used to start audio in parallel to command execution

	public HooksExecutor()
	{
		this.audioStartThreadExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
		this.audioThreadExecutor = Executors.newSingleThreadExecutor(new DaemonAudioThreadFactory());
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
		if (this.config != null && this.config.isStartEnabled())
			this.runHooks(this.config.getOnStartAudioStr(), this.config.getOnStartCmdStr());
	}

	/**
	 * Runs the hooks configured to run on termination
	 */
	synchronized public void runEnd()
	{
		if (this.config != null && this.config.isEndEnabled())
			this.runHooks(this.config.getOnEndAudioStr(), this.config.getOnEndCmdStr());
	}

	/**
	 * Runs the audio and the command hooks
	 *
	 * @param audioPath the audio path for the file to be played
	 * @param cmd       the command to be executed
	 */
	synchronized public void runHooks(String audioPath, String cmd)
	{
		this.stop(); // stop everything currently running

		if (cmd != null) {
			this.cmdExecutor = new CommandExecutor(cmd, this::onCMDError);
			this.cmdExecutor.setOnError(System.err::println);
			this.cmdExecutor.start();
		}

		// execute in other thread to avoid blocking the current thread
		if (audioPath != null)
			this.audioStartTask = this.audioStartThreadExecutor.submit(
				() -> this.playAudio(audioPath)
			);
	}

	/**
	 * Plays the given audio
	 * If the audio is not a file but a directory, this will play all files in the directory
	 *
	 * @param audioPath the audio path or directory
	 */
	private void playAudio(String audioPath)
	{
		if (this.audioPlayer == null) {
			this.audioPlayer = new AudioPlayer(this::onAudioError);
			if (!this.audioPlayer.init()) {
				Loggers.getErrorLogger().log(
					Level.WARNING,
					"Your system probably can't play audio"
				);
				return;
			}
		}

		File audioFile = new File(audioPath);
		try {
			// play single file
			if (audioFile.isFile()) {
				this.audioPlayer.loadAudio(audioFile.getAbsolutePath());
				this.audioPlayTask = this.audioThreadExecutor.submit(this.audioPlayer);
				return;
			}
		} catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Error while playing audio",
				e
			);
			return;
		}

		// play multiple audio files inside the directory
		if (this.supportedAudioExtensions == null)
			this.supportedAudioExtensions = Arrays.asList(
				HooksConfigPanel.getSupportedAudioFileExtensions()
			);

		File[] supportedAudioFiles = audioFile.listFiles((dir, name) -> {
			int dot_pos;
			if ((dot_pos = name.lastIndexOf('.')) == -1)
				return false;

			// filter by file extension
			String extension = name.substring(dot_pos + 1).toLowerCase().trim();
			return this.supportedAudioExtensions.contains(extension);
		});

		if (supportedAudioFiles == null) {
			SWMain.showErrorAlert(
				"No playable audio files in directory: " + audioPath
					+ "\nAudio files should have one of the following formats: "
					+ this.supportedAudioExtensions,
				"No audio"
			);
			return;
		}

		// shuffle the files to reproduce them in a random order
		List<File> tmp = Arrays.asList(supportedAudioFiles);
		Collections.shuffle(tmp);

		Deque<File> audioFiles = new LinkedList<>(tmp);

		File file;
		while (!this.audioStartTask.isCancelled()) {
			// select a random file from the deque
			file = audioFiles.pop();

			// TODO: find a more efficient way of doing this concurrency handling
			CountDownLatch latch = new CountDownLatch(1);

			try {
				this.audioPlayer.loadAudio(file.getAbsolutePath());
			} catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Error while playing audio: " + file.getAbsolutePath(),
					e
				);
			}
			this.audioPlayer.onAudioEnd(latch::countDown);

			this.audioPlayTask = this.audioThreadExecutor.submit(this.audioPlayer);

			// wait till the audio has been played entirely
			try {
				latch.await();
			} catch (InterruptedException e) {
				return;
			}

			// return the file to the deque so it can be played again
			audioFiles.addLast(file);
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
			this.audioPlayer.shutdown();
		if (this.cmdExecutor != null)
			this.cmdExecutor.interrupt();
		if (this.audioStartTask != null)
			this.audioStartTask.cancel(true);
		if (this.audioPlayTask != null)
			this.audioPlayTask.cancel(true);
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
