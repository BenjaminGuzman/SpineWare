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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.fos.sw.SWMain;
import org.fos.sw.core.Loggers;

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
	 * Set the {@link Consumer} object to execute if there is an error during command execution
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
		super.interrupt();
		if (this.process != null)
			this.process.destroy();
	}


	/**
	 * This method will start the execution of the configured command
	 */
	@Override
	public void run()
	{
		// It is better to create this list of arguments than simply doing
		// Runtime.getRuntime().exec(cmd);
		// because the former does not allow multiple executions, for example
		// echo hello && echo world
		// would just execute the command "echo" with the argument "hello && echo world"
		ArrayList<String> executionCmd = new ArrayList<>(3);

		// add the shell
		// TODO: add support for other "shells" & test in various operative systems
		executionCmd.add(SWMain.IS_WINDOWS ? "cmd" : "sh");

		// add the argument for the shell to execute the given command
		executionCmd.add(SWMain.IS_WINDOWS ? "/c" : "-c");

		// add the command (or commands)
		executionCmd.add(cmd);

		try {
			this.process = Runtime.getRuntime().exec(executionCmd.toArray(new String[0]));

			Pipe stdoutPipe = new Pipe(
				new Pipe.Builder(
					this.process.getInputStream(),
					new FileOutputStream(hookSTDOUT, true)
				).prependStr("---BEGIN STDOUT @ "
					+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
					+ " for: " + this.cmd + "---\n")
				 .appendStr("---END STDOUT for: " + this.cmd + "---\n\n")
			);

			Pipe stderrPipe = new Pipe(
				new Pipe.Builder(
					this.process.getErrorStream(),
					new FileOutputStream(hookSTDERR, true)
				).prependStr("---BEGIN STDERR for: " + this.cmd + "---\n")
				 .appendStr("---END STDERR for: " + this.cmd + "---\n\n")
			);

			stdoutPipe.start();
			stderrPipe.start();

			this.process.waitFor();
		} catch (IOException | SecurityException | IllegalArgumentException | InterruptedException e) {
			if (this.process != null)
				this.process.destroyForcibly();

			if (this.onError != null)
				this.onError.accept(e);
		}
	}
}
