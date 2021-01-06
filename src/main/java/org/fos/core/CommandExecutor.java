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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.fos.Loggers;
import org.fos.SWMain;

public class CommandExecutor extends Thread
{
	private static File hookSTDOUT, hookSTDERR;

	static {
		try {
			String tmpDir = System.getProperty("java.io.tmpdir");
			Path stdout = Paths.get(tmpDir, "SW_hooks_stdout.log");
			Path stderr = Paths.get(tmpDir, "SW_hooks_stderr.log");

			hookSTDOUT = Files.exists(stdout) ? stdout.toFile() : Files.createFile(stdout).toFile();
			hookSTDERR = Files.exists(stderr) ? stderr.toFile() : Files.createFile(stderr).toFile();
		} catch (IOException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Couldn't create temporary file to log hooks output",
				e
			);
		}
	}

	private Process process;
	private String cmd;
	private Consumer<Exception> onError;

	public CommandExecutor(String cmd)
	{
		super();
		this.cmd = cmd;
		this.setDaemon(true); // this thread should not be a user thread but a daemon thread
		this.setName("CommandExecutor-Thread"); // give it a name for easy-debugging
	}

	public CommandExecutor(String cmd, Consumer<Exception> onError)
	{
		this(cmd);
		this.onError = onError;
	}

	/**
	 * Sets the command to be executed
	 *
	 * @param cmd the command
	 */
	public void setCmd(String cmd)
	{
		this.cmd = cmd;
	}

	/**
	 * Set the {@link Consumer} object to execute if there is an error WHILE executing the command
	 *
	 * @param onError the action to run on error
	 */
	public void setOnError(Consumer<Exception> onError)
	{
		this.onError = onError;
	}

	/**
	 * Interrupts the process execution (if it is executing)
	 */
	@Override
	public void interrupt()
	{
		if (this.process != null)
			this.process.destroy();
	}


	/**
	 * This method will start the execution of the configured command
	 */
	@Override
	public void run()
	{
		ArrayList<String> executionCmd = new ArrayList<>(3);

		// add the shell
		// TODO: add support for other "shells" & test in various systems
		executionCmd.add(SWMain.IS_WINDOWS ? "cmd" : "sh");

		// add the argument for the shell to execute thegiven command
		executionCmd.add(SWMain.IS_WINDOWS ? "/c" : "-c");

		// add the command (or commands)
		executionCmd.add(cmd);

		try {
			this.process = Runtime.getRuntime().exec(executionCmd.toArray(new String[0]));

			int read;
			try (
				// stdout stuff
				BufferedInputStream processStdout =
					new BufferedInputStream(this.process.getInputStream());
				BufferedOutputStream stdoutBuff =
					new BufferedOutputStream(new FileOutputStream(hookSTDOUT, true));

				// stderr stuff
				BufferedInputStream processStderr =
					new BufferedInputStream(this.process.getErrorStream());
				BufferedOutputStream stderrBuff =
					new BufferedOutputStream(new FileOutputStream(hookSTDERR, true))
			) {
				String lineSeparator = System.lineSeparator();

				// write stdout output
				stdoutBuff.write(
					(lineSeparator + "--- EXECUTION STDOUT ---" + lineSeparator)
						.getBytes(StandardCharsets.UTF_8)
				);
				while ((read = processStdout.read()) != -1) {
					stdoutBuff.write(read);
					System.out.print((char) read);
				}

				// write stderr output
				stderrBuff.write(
					(lineSeparator + "--- EXECUTION STDERR ---" + lineSeparator)
						.getBytes(StandardCharsets.UTF_8)
				);
				while ((read = processStderr.read()) != -1)
					stderrBuff.write(read);
			}

			this.process.waitFor();
		} catch (IOException | SecurityException | IllegalArgumentException | InterruptedException e) {
			if (this.process != null)
				this.process.destroyForcibly();

			if (this.onError != null)
				this.onError.accept(e);
		}
	}
}
