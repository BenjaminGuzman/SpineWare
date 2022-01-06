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

package net.benjaminguzman;

import net.benjaminguzman.gui.MainFrame;
import net.benjaminguzman.utils.DaemonThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CLI implements Runnable
{
	private final MainFrame mainFrame;
	private final Thread t;

	public CLI(@NotNull MainFrame mainFrame)
	{
		t = new DaemonThreadFactory("CLI-Thread").newThread(this);
		this.mainFrame = mainFrame;
	}

	/**
	 * Starts executing the code that will read commands.
	 * This call is non-blocking because code will be executed in another thread
	 * To interrupt the thread see {@link #stop()}
	 * <p>
	 * This method should be called just once. Calling it more than once will have no effect
	 *
	 * @see #stop()
	 */
	public void start()
	{
		t.start();
	}

	/**
	 * Stops the execution of the thread reading commands
	 */
	public void stop()
	{
		try {
			// we need to close the underlying stream so the read() method (called from readLine())
			// stops (probably throwing an IOException)
			// we do this because just interrupting the thread is useless
			System.in.close();
		} catch (IOException ignored) {
		}
		t.interrupt();
	}

	@Override
	public void run()
	{
		String line;
		try {
			BufferedReader stdinBuff = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Starting reading commands");
			System.out.println("\texit | quit:             Exit the application");
			System.out.println("\tgc | free:               Run System.gc()");
			System.out.println("\tsystray | tray | menu:   Open the systray menu");
			while (!Thread.interrupted() && (line = stdinBuff.readLine()) != null) {
				line = line.toLowerCase().trim();

				switch (line) {
					case "exit":
					case "quit":
						SWMain.exit();
						return;
					case "free":
					case "gc":
						System.out.println("Running System.gc()");
						System.gc();
						break;
					case "systray":
					case "tray":
					case "menu":
						System.out.println("Opening systray menu");
						mainFrame.toggleSysTrayMenu();
						break;
					default:
						System.out.println("Command \"" + line + "\" was not understood");
				}
			}
		} catch (IOException ignored) {
		}
	}
}
