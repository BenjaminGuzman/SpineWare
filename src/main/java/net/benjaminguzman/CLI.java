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

package net.benjaminguzman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.benjaminguzman.gui.MainFrame;
import net.benjaminguzman.utils.DaemonThreadFactory;
import org.jetbrains.annotations.NotNull;

public class CLI implements Runnable
{
	private final ExecutorService executor;
	private final MainFrame mainFrame;

	public CLI(@NotNull MainFrame mainFrame)
	{
		executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("CLI-Thread"));
		this.mainFrame = mainFrame;
	}

	/**
	 * Starts executing the code that will read commands.
	 * This call is non-blocking because code will be executed in another thread
	 * To interrupt the thread see {@link #stop()}
	 *
	 * @see #stop()
	 */
	public void start()
	{
		executor.execute(this);
	}

	/**
	 * Stops the execution of the thread reading commands
	 */
	public void stop()
	{
		executor.shutdownNow();
	}

	@Override
	public void run()
	{
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		try {
			System.out.println("Starting reading commands");
			System.out.println("\texit | quit:             Exit the application");
			System.out.println("\tgc | free:               Run System.gc()");
			System.out.println("\tsystray | tray | menu:   Open the systray menu");
			while ((line = bufferedReader.readLine()) != null) {
				line = line.toLowerCase().trim();

				switch (line) {
					case "exit":
					case "quit":
						SWMain.exit();
						return; // this will automatically stop the executor
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
				}
			}
		} catch (IOException e) {
		} // yes, don't report errors
	}
}
