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

package org.fos.sw.hooks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jl.decoder.JavaLayerException;
import org.fos.sw.SWMain;
import org.fos.sw.core.Loggers;
import org.fos.sw.gui.hooksconfig.HooksConfigPanel;
import org.fos.sw.utils.AudioPlayer;
import org.fos.sw.utils.CommandExecutor;
import org.fos.sw.utils.DaemonAudioThreadFactory;
import org.fos.sw.utils.DaemonThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HooksExecutor
{
	@Nullable
	private HooksConfig config;

	private AudioPlayer audioPlayer;
	private CommandExecutor cmdExecutor;

	/**
	 * Thread used to start playing audio and avoid blocking
	 * Thanks to this, the audio and the given command can be executed in parallel
	 */
	private final ExecutorService audioStartThreadExecutor;

	/**
	 * Thread used to play audio
	 */
	private final ExecutorService audioThreadExecutor;
	private List<String> supportedAudioExtensions;
	private Future<?> audioPlayTask;  // future used to play audio in parallel
	private Future<?> audioStartTask; // future used to start audio in parallel
	private boolean wait_termination;

	public HooksExecutor()
	{
		this.audioStartThreadExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
		this.audioThreadExecutor = Executors.newSingleThreadExecutor(new DaemonAudioThreadFactory());
	}

	public HooksExecutor(@Nullable HooksConfig config)
	{
		this();
		this.config = config;
	}

	public void setConfig(@Nullable HooksConfig config)
	{
		this.config = config;
	}

	/**
	 * @param wait if true, when {@link #runStart()} or {@link #runEnd()} are executed, the current thread will be
	 *             blocked until the running hooks are terminated or interrupted
	 *             Obviously you'll need to set this value prior calling {@link #runStart()} or {@link #runEnd()}
	 */
	public void setWaitTermination(boolean wait)
	{
		this.wait_termination = wait;
	}

	/**
	 * Runs the hooks configured to run on start
	 */
	public void runStart()
	{
		if (config != null && config.isStartEnabled())
			runHooks(config.getOnStartAudioStr(), config.getOnStartCmdStr());
	}

	/**
	 * Runs the hooks configured to run on termination
	 */
	public void runEnd()
	{
		if (config != null && config.isEndEnabled())
			runHooks(config.getOnEndAudioStr(), config.getOnEndCmdStr());
	}

	/**
	 * Runs the audio and the command hooks
	 *
	 * @param audioPath the audio path for the file to be played
	 * @param cmd       the command to be executed
	 */
	private void runHooks(@Nullable String audioPath, @Nullable String cmd)
	{
		stop(); // stop everything currently running

		if (cmd != null) {
			this.cmdExecutor = new CommandExecutor(cmd, this::onCMDError);
			this.cmdExecutor.start();
		}

		// execute in other thread to avoid blocking the current thread
		if (audioPath != null)
			this.audioStartTask = this.audioStartThreadExecutor.submit(() -> this.playAudio(audioPath));

		if (wait_termination) {
			try {
				if (cmd != null)
					cmdExecutor.join();
				if (audioPath != null) {
					audioStartTask.get();
					audioPlayTask.get();
				}
			} catch (InterruptedException | ExecutionException | CancellationException e) {
				// if something went wrong or any thread was interrupted
				// ensure all hooks are terminated
				stop();
			}
		}
	}

	/**
	 * Plays the given audio
	 * If the audio is not a file but a directory, this will play all files in the directory
	 *
	 * @param audioPath the audio path or directory
	 */
	private void playAudio(@NotNull String audioPath)
	{
		if (this.audioPlayer == null)
			this.audioPlayer = new AudioPlayer(this::onAudioError);

		File audioFile = new File(audioPath);
		try {
			// play single file
			if (audioFile.isFile()) {
				this.audioPlayer.loadAudio(audioFile.getAbsolutePath());
				this.audioPlayTask = this.audioThreadExecutor.submit(this.audioPlayer);
				return;
			} else if (!audioFile.exists()) {
				Loggers.getErrorLogger().log(
					Level.WARNING,
					"Audio file: " + audioFile.getAbsolutePath() + " does not exists"
				);
				return;
			}
		} catch (UnsupportedAudioFileException | LineUnavailableException | IOException | JavaLayerException e) {
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

		File[] supportedAudioFiles = audioFile.listFiles(this::isAudioFileSupported);

		if (supportedAudioFiles == null || supportedAudioFiles.length == 0) {
			SWMain.showErrorAlert(
				"No playable audio files in directory: " + audioPath
					+ "\nAudio files should have one of the following formats: "
					+ this.supportedAudioExtensions,
				"No audio"
			);
			return;
		}

		// shuffle the files to reproduce them in random order
		List<File> tmp = Arrays.asList(supportedAudioFiles);
		Collections.shuffle(tmp);

		Deque<File> audioFiles = new ArrayDeque<>(tmp);

		// 2 parties should call await()
		// 1st party: this thread
		// 2nd party: the thread playing audio when the audio terminates
		CyclicBarrier barrier = new CyclicBarrier(2);
		File file;
		while (!this.audioStartTask.isCancelled() && !Thread.currentThread().isInterrupted()) {
			// select a random file from the deque
			file = audioFiles.pop();

			try {
				this.audioPlayer.loadAudio(file.getAbsolutePath());
			} catch (UnsupportedAudioFileException | LineUnavailableException | IOException | JavaLayerException e) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Error while playing audio: " + file.getAbsolutePath(),
					e
				);
			}
			this.audioPlayTask = this.audioThreadExecutor.submit(this.audioPlayer);

			// second party await() call
			this.audioPlayer.onAudioEnd(() -> {
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					// this is probably not needed because if the thread is interrupted
					// the first party (the code below) will be the first catching the exception
					this.audioPlayTask.cancel(true);
				}
			});

			// first party await() call
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				this.audioPlayer.shutdown();
				this.audioPlayTask.cancel(true);
				return;
			}

			// at this point we know the audio has finished playing
			// because both parties called await()
			// reset the barrier for the next loop cycle
			barrier.reset();

			// return the file to the deque so it can be played again
			audioFiles.addLast(file);
		}
	}

	private boolean isAudioFileSupported(File dir, String fileName)
	{
		int dot_pos;
		if ((dot_pos = fileName.lastIndexOf('.')) == -1)
			return false;

		// filter by file extension
		String extension = fileName.substring(dot_pos + 1).toLowerCase().trim();
		try {
			Path filePath = Paths.get(dir.getAbsolutePath(), fileName);

			// ensure it has a valid extension
			return this.supportedAudioExtensions.contains(extension)
				// ensure it is readable
				&& Files.isReadable(filePath)
				// ensure it is not a directory (a directory fileName music.mp3 is valid)
				// if it is a symlink, it will be followed
				&& Files.isRegularFile(filePath);
		} catch (SecurityException e) {
			return false;
		}
	}

	/**
	 * Stops any execution currently happening
	 * <p>
	 * This will internally interrupt the executing threads (those created to play audio or execute a command)
	 */
	public void stop()
	{
		Loggers.getDebugLogger().entering(this.getClass().getName(), "stop");
		if (audioPlayTask != null)
			audioPlayTask.cancel(true);
		if (audioStartTask != null)
			audioStartTask.cancel(true);
		if (audioPlayer != null)
			audioPlayer.shutdown();
		if (cmdExecutor != null)
			cmdExecutor.interrupt();
		Loggers.getDebugLogger().exiting(this.getClass().getName(), "stop");
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
