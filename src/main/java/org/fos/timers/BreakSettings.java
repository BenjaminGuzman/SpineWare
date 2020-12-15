/*
 * Copyright (c) 2020. Benjamín Guzmán
 * Author: Benjamín Guzmán <9benjaminguzman@gmail.com>
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

package org.fos.timers;

import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.core.BreakType;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

/**
 * Wrapper class containing information about the break settings including
 * working time settings
 * break settings
 * and a boolean value to indicate if the break is enabled or not
 */
public class BreakSettings
{
	private static final String[] defaultNotificationAudio = new String[]{
		"/resources/media/audio/small_breaks/notification.wav",
		"/resources/media/audio/stretch_breaks/notification.wav",
		"/resources/media/audio/day_break/notification.wav"
	};
	private final BreakType breakType;
	private TimerSettings workTimerSettings;
	private TimerSettings breakTimerSettings;
	private TimerSettings postponeTimerSettings;
	private boolean is_enabled;
	private String notificationAudioPath;
	private String breakAudiosDirStr;
	private Clip soundClip;
	private Thread soundThread;

	private BreakSettings(
		final TimerSettings workTimerSettings,
		final TimerSettings breakTimerSettings,
		final TimerSettings postponeTimerSettings,
		final BreakType breakType,
		final String notificationAudioPath,
		final String breakAudiosDirStr,
		final boolean is_enabled
	)
	{
		this.workTimerSettings = workTimerSettings;
		this.breakTimerSettings = breakTimerSettings;
		this.postponeTimerSettings = postponeTimerSettings;
		this.breakType = breakType;
		this.is_enabled = is_enabled;
		this.notificationAudioPath = notificationAudioPath;
		this.breakAudiosDirStr = breakAudiosDirStr;

		try {
			this.soundClip = AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Couldn't get system clip to play audio", e);
		}
	}

	public BreakType getBreakType()
	{
		return this.breakType;
	}

	public TimerSettings getWorkTimerSettings()
	{
		return workTimerSettings;
	}

	public void setWorkTimerSettings(TimerSettings workTimerSettings)
	{
		this.workTimerSettings = workTimerSettings;
	}

	public TimerSettings getBreakTimerSettings()
	{
		return breakTimerSettings;
	}

	public void setBreakTimerSettings(TimerSettings breakTimerSettings)
	{
		this.breakTimerSettings = breakTimerSettings;
	}

	public TimerSettings getPostponeTimerSettings()
	{
		return this.postponeTimerSettings;
	}

	public void setPostponeTimerSettings(TimerSettings postponeTimerSettings)
	{
		this.postponeTimerSettings = postponeTimerSettings;
	}

	public String getNotificationAudioPath()
	{
		return notificationAudioPath;
	}

	public void setNotificationAudioPath(String notificationAudioPath)
	{
		this.notificationAudioPath = notificationAudioPath;
	}

	public String getBreakAudiosDirStr()
	{
		return breakAudiosDirStr;
	}

	public void setBreakAudiosDirStr(String breakAudiosDirStr)
	{
		this.breakAudiosDirStr = breakAudiosDirStr;
	}

	public boolean isEnabled()
	{
		return this.is_enabled;
	}

	public void setEnabled(final boolean is_enabled)
	{
		this.is_enabled = is_enabled;
	}

	public void playBreakAudio()
	{
		this.stopAudio();
		this.soundThread = new Thread(this::_playBreakAudio);
		this.soundThread.start();
	}

	public void playNotificationAudio()
	{
		this.stopAudio();
		this.soundThread = new Thread(this::_playNotificationAudio);
		this.soundThread.start();
	}

	/**
	 * This method will read the file each time it is invoked
	 * This is the desired behaviour, we're trading memory vs efficiency
	 * Since efficiency is not critical here, we'll prefer to improve memory usage
	 */
	private void _playNotificationAudio()
	{
		if (this.soundClip == null)
			return;

		CountDownLatch countDownLatch = new CountDownLatch(1);
		LineListener onStopListener = (LineEvent evt) -> {
			if (evt.getType() == LineEvent.Type.STOP) {
				this.soundClip.close();
				countDownLatch.countDown();
			}
		};
		AudioInputStream audioInputStream = null;
		try {
			File notificationSoundFile = null;
			boolean notification_sound_file_is_good = this.notificationAudioPath != null
				&& (notificationSoundFile = new File(this.notificationAudioPath)).exists();

			if (notification_sound_file_is_good)
				audioInputStream = AudioSystem.getAudioInputStream(notificationSoundFile);
			else
				audioInputStream = AudioSystem.getAudioInputStream(
					new BufferedInputStream( // add mark/reset support, mark is just a pointer to the current position in the stream
								 SWMain.getFileAsStream(defaultNotificationAudio[this.breakType.getIndex()])
					)
				);

			this.soundClip.addLineListener(onStopListener);

			this.soundClip.open(audioInputStream);
			this.soundClip.start();

			countDownLatch.await(); // wait till the audio has finished playing back
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Couldn't play audio: " + this.notificationAudioPath, e);
			try {
				audioInputStream = AudioSystem.getAudioInputStream(
					new BufferedInputStream( // add mark/reset support, mark is just a pointer to the current position in the stream
								 SWMain.getFileAsStream(defaultNotificationAudio[this.breakType.getIndex()])
					)
				);
				this.soundClip.addLineListener(onStopListener);

				this.soundClip.open(audioInputStream);
				this.soundClip.start();

				countDownLatch.await(); // wait till the audio has finished playing back
			} catch (UnsupportedAudioFileException | InterruptedException | LineUnavailableException | IOException ex) {
				Loggers.getErrorLogger().log(Level.SEVERE, "Definitely audio couldn't be played", ex);
			}

		} catch (InterruptedException e) {
			// thread was interrupted, no exception handling is added here 'cause the main cause for
			// this exception to occur is the user exiting the application or cancelling something
		} finally {
			this.soundClip.removeLineListener(onStopListener);
			if (audioInputStream != null) {
				try {
					audioInputStream.close();
				} catch (IOException e) {
					Loggers.getErrorLogger().log(Level.WARNING, "Error while closing audio file", e);
				}
			}
		}
	}

	/**
	 * This method will loop trough all the supported audio in the breakAudiosDirStr directory
	 * And will play every file till the thread is interrupted
	 * To interrupt the thread call {@link #stopAudio()}
	 */
	public void _playBreakAudio()
	{
		if (this.breakAudiosDirStr == null || this.soundClip == null)
			return;

		if (this.breakType == BreakType.DAY_BREAK)
			return; // probably an exception should be thrown

		File breakAudiosDir = new File(this.breakAudiosDirStr);
		if (!breakAudiosDir.exists() || !breakAudiosDir.isDirectory() || !breakAudiosDir.canRead())
			return;

		// use an arraylist instead of an array to have access to the Collections API
		ArrayList<String> acceptedExtensions = new ArrayList<>(3);
		acceptedExtensions.add(".mp3");
		acceptedExtensions.add(".ogg");
		acceptedExtensions.add(".wav");

		File[] audioFiles = breakAudiosDir.listFiles((File file) -> {
			if (file.isDirectory())
				return false;

			String absPath = file.getAbsolutePath();
			return file.isFile() && acceptedExtensions.contains(
				absPath.substring(absPath.length() - 4)
			);
		});

		if (audioFiles == null || audioFiles.length == 0)
			return;

		Deque<File> pendingAudioFiles = new LinkedList<>(Arrays.asList(audioFiles));

		final long MAX_FILE_SIZE_MB = 100;

		AudioInputStream audioInputStream = null;
		LineListener onStopListener = null;
		while (!Thread.interrupted()) {
			File audioFile = pendingAudioFiles.poll(); // pop from the head of the deque
			pendingAudioFiles.addLast(audioFile); // insert to the tail of the deque

			if (audioFile == null) // something really weird happened, I believe this will never happen
				return;

			if (audioFile.length() / 1024 /* B -> kB */ / 1024 /* mB -> MB */ > 100) {
				Loggers.getErrorLogger().log(Level.WARNING, "The file: "
					+ audioFile.getAbsolutePath() + " is more than "
					+ MAX_FILE_SIZE_MB + "MB, skipping...");
				continue;
			}

			try {
				audioInputStream = AudioSystem.getAudioInputStream(audioFile.getAbsoluteFile());

				CountDownLatch countDownLatch = new CountDownLatch(1);
				onStopListener = (LineEvent evt) -> {
					if (evt.getType() == LineEvent.Type.STOP) {
						this.soundClip.close();
						countDownLatch.countDown();
					}
				};
				this.soundClip.addLineListener(onStopListener);

				this.soundClip.open(audioInputStream);
				this.soundClip.start();

				countDownLatch.await(); // wait till the audio has finished playing back
				audioInputStream.close();
				this.soundClip.removeLineListener(onStopListener);
			} catch (UnsupportedAudioFileException | IOException e) {
				Loggers.getErrorLogger().log(Level.SEVERE, "Something bad happened while trying to open: "
					+ audioFile.getAbsolutePath(), e);
			} catch (LineUnavailableException e) {
				Loggers.getErrorLogger().log(Level.SEVERE, "Something bad happened while trying to play file: "
					+ audioFile.getAbsolutePath(), e);
			} catch (InterruptedException e) {
				return;
			} finally {
				this.soundClip.removeLineListener(onStopListener);
				if (audioInputStream != null) {
					try {
						audioInputStream.close();
					} catch (IOException e) {
						Loggers.getErrorLogger().log(Level.WARNING, "Error while closing audio file", e);
					}
				}
			}
		}
	}

	/**
	 * Will stop the execution of the process that plays the sound
	 */
	public void stopAudio()
	{
		if (this.soundClip != null) {
			this.soundClip.stop();
			this.soundClip.close();
		}
		if (this.soundThread != null)
			this.soundThread.interrupt();

		this.soundThread = null;
	}

	public static class Builder
	{
		private TimerSettings workTimerSettings;
		private TimerSettings breakTimerSettings;
		private TimerSettings postponeTimerSettings;
		private BreakType breakType;
		private String notificationAudioPath;
		private String breakAudiosDirStr;
		private boolean is_enabled = true;

		public Builder workTimerSettings(TimerSettings workTimerSettings)
		{
			this.workTimerSettings = workTimerSettings;
			return this;
		}

		public Builder breakTimerSettings(TimerSettings breakTimerSettings)
		{
			this.breakTimerSettings = breakTimerSettings;
			return this;
		}

		public Builder postponeTimerSettings(TimerSettings postponeTimerSettings)
		{
			this.postponeTimerSettings = postponeTimerSettings;
			return this;
		}

		public Builder breakType(BreakType breakType)
		{
			this.breakType = breakType;
			return this;
		}

		public Builder notificationAudioPath(String notificationAudioPath)
		{
			this.notificationAudioPath = notificationAudioPath;
			return this;
		}

		public Builder breakAudiosDirStr(String breakAudiosDirStr)
		{
			this.breakAudiosDirStr = breakAudiosDirStr;
			return this;
		}

		public Builder enabled(boolean is_enabled)
		{
			this.is_enabled = is_enabled;
			return this;
		}

		public BreakSettings createBreakSettings()
		{
			return new BreakSettings(
				this.workTimerSettings,
				this.breakTimerSettings,
				this.postponeTimerSettings,
				this.breakType,
				this.notificationAudioPath,
				this.breakAudiosDirStr,
				this.is_enabled
			);
		}
	}
}
