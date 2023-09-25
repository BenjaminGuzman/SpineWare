/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.net>
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

package dev.benjaminguzman.utils;

import dev.benjaminguzman.SpineWare;
import dev.benjaminguzman.core.Loggers;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;

import static javax.sound.sampled.LineEvent.Type.STOP;

/**
 * Class to play a single audio file
 * Supported audio files are the ones the jvm supports + mp3
 */
public class AudioPlayer implements Runnable
{
	private final Object mp3Lock = new Object();
	/**
	 * Java audio clip to play audio
	 * This is a "final" field because it is set within the constructor
	 */
	private final Clip audioClip;
	private Player mp3Player = null;
	private Consumer<Exception> onError;
	/**
	 * Indicates if the system can play audio.
	 * This is false if {@link AudioSystem#getClip()} failed, indicating the system cannot play audio
	 */
	private boolean can_play_audio = false;

	/**
	 * Executed when an audio stops playing
	 */
	private Runnable onAudioEnd;

	public AudioPlayer()
	{
		super();
		Clip audioClip;

		// initialize the audio clip
		try {
			audioClip = AudioSystem.getClip();
			this.can_play_audio = true;
		} catch (LineUnavailableException | SecurityException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Couldn't get system clip to play audio",
				e
			);
			SwingUtilities.invokeLater(
				() -> this.showErrorAlert(SpineWare.messagesBundle.getString("system_cannot_play_audio"))
			);
			audioClip = new DummyClip(); // create a dummy clip just to not have null and provoke a
			// null pointer exception when acquiring the lock on this object
		} catch (IllegalArgumentException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Your system does not have support for playing audio files",
				e
			);
			SwingUtilities.invokeLater(
				() -> this.showErrorAlert(SpineWare.messagesBundle.getString("system_cannot_play_audio"))
			);
			audioClip = new DummyClip(); // create a dummy clip just to not have null and provoke a
			// null pointer exception when acquiring the lock on this object
		}
		this.audioClip = audioClip;
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
		if (!can_play_audio)
			return;

		synchronized (this.audioClip) {
			this.audioClip.removeLineListener(this::lineListener);
			this.audioClip.close();
		}
		synchronized (this.mp3Lock) {
			this.mp3Player = null;
		}

		Loggers.getDebugLogger().log(
			Level.INFO,
			"Loading audio: " + audioFileAbsPath
		);

		// if the audio is an mp3, play it with java layer, because the native java API does not support mp3
		if (audioFileAbsPath.endsWith(".mp3")) {
			this.mp3Player = new Player(new FileInputStream(audioFileAbsPath));
			return;
		}

		synchronized (this.audioClip) {
			this.audioClip.open(AudioSystem.getAudioInputStream(
				new File(audioFileAbsPath).getAbsoluteFile()
			));
			this.audioClip.addLineListener(this::lineListener);
		}
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
		if (!can_play_audio)
			return;

		synchronized (this.audioClip) {
			this.audioClip.stop();
		}

		synchronized (this.mp3Lock) {
			if (this.mp3Player != null)
				this.mp3Player.close();
		}
	}

	/**
	 * stops playing audio & close resources
	 */
	public void shutdown()
	{
		Loggers.getDebugLogger().entering(this.getClass().getName(), "shutdown");

		if (!can_play_audio)
			return;

		synchronized (this.audioClip) {
			this.audioClip.removeLineListener(this::lineListener);
			this.audioClip.flush();
			this.audioClip.stop();
			this.audioClip.close();
		}

		synchronized (this.mp3Lock) {
			if (this.mp3Player != null)
				this.mp3Player.close();
			else
				Loggers.getDebugLogger().fine("MP3 player is closed");
			this.mp3Player = null;
		}
		Loggers.getDebugLogger().exiting(this.getClass().getName(), "shutdown");
	}

	/**
	 * Invoked when {@link Thread#start()} is invoked
	 * <p>
	 * This will start playing audio, therefore, be sure to call {@link #loadAudio(String)}
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
				synchronized (this.mp3Lock) {
					this.mp3Player = null;
				}
				return;
			}
		} catch (JavaLayerException e) {
			this.onError.accept(e);
			return;
		}

		this.audioClip.start();
	}

	/**
	 * @return true if the audio is currently being played
	 */
	public boolean isPlaying()
	{
		return can_play_audio && (this.mp3Player != null || this.audioClip.isRunning());
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
	 * Shows an error alert using a JOptionPane as in {@link SpineWare#showErrorAlert(String, String)}
	 * <p>
	 * Title for the message will be the message property "error_while_playing_audio"
	 *
	 * @param message the message for the JOptionPane
	 */
	private void showErrorAlert(String message)
	{
		SpineWare.showErrorAlert(message, SpineWare.messagesBundle.getString("error_while_playing_audio"));
	}

	/**
	 * Dummy class with no functionality
	 * Use it if the system clip could not be obtained but still you need a {@link Clip} object
	 */
	public static class DummyClip implements Clip
	{
		@Override
		public void open(AudioFormat format, byte[] data, int offset, int bufferSize) throws LineUnavailableException
		{
		}

		@Override
		public void open(AudioInputStream stream) throws LineUnavailableException, IOException
		{
		}

		@Override
		public int getFrameLength()
		{
			return 0;
		}

		@Override
		public long getMicrosecondLength()
		{
			return 0;
		}

		@Override
		public void setLoopPoints(int start, int end)
		{
		}

		@Override
		public void loop(int count)
		{
		}

		@Override
		public void drain()
		{
		}

		@Override
		public void flush()
		{
		}

		@Override
		public void start()
		{
		}

		@Override
		public void stop()
		{
		}

		@Override
		public boolean isRunning()
		{
			return false;
		}

		@Override
		public boolean isActive()
		{
			return false;
		}

		@Override
		public AudioFormat getFormat()
		{
			return null;
		}

		@Override
		public int getBufferSize()
		{
			return 0;
		}

		@Override
		public int available()
		{
			return 0;
		}

		@Override
		public int getFramePosition()
		{
			return 0;
		}

		@Override
		public void setFramePosition(int frames)
		{
		}

		@Override
		public long getLongFramePosition()
		{
			return 0;
		}

		@Override
		public long getMicrosecondPosition()
		{
			return 0;
		}

		@Override
		public void setMicrosecondPosition(long microseconds)
		{
		}

		@Override
		public float getLevel()
		{
			return 0;
		}

		@Override
		public Line.Info getLineInfo()
		{
			return null;
		}

		@Override
		public void open() throws LineUnavailableException
		{
		}

		@Override
		public void close()
		{
		}

		@Override
		public boolean isOpen()
		{
			return false;
		}

		@Override
		public Control[] getControls()
		{
			return new Control[0];
		}

		@Override
		public boolean isControlSupported(Control.Type control)
		{
			return false;
		}

		@Override
		public Control getControl(Control.Type control)
		{
			return null;
		}

		@Override
		public void addLineListener(LineListener listener)
		{
		}

		@Override
		public void removeLineListener(LineListener listener)
		{
		}
	}
}