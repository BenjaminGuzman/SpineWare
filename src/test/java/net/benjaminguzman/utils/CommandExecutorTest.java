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

package net.benjaminguzman.utils;

import java.util.Timer;
import java.util.TimerTask;
import org.junit.jupiter.api.Test;

class CommandExecutorTest
{
	@Test
	void run() throws InterruptedException
	{
		CommandExecutor cmdExecutor = new CommandExecutor("echo \"hello world\" && echo goodbye && echo " +
			"multiple & >&2 echo should go to stderr");
		Thread t = new Thread(cmdExecutor);
		t.start();

		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
			private int count = 0;

			@Override
			public void run()
			{
				if (count == 4)
					cmdExecutor.interrupt();
				else
					System.out.println("The JVM is not closing because there is a user-thread " +
						"still active, elapsed time: " + count + "s");
				++count;
			}
		}, 0, 1_000); // terminate the command execution after 1 second
		// the JVM should not terminate before that second!!

		t.join(); // wait for the thread to stop
	}
}