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

package net.benjaminguzman.utils;

import org.junit.jupiter.api.Test;

class CommandExecutorTest
{
	@Test
	void run() throws InterruptedException
	{
		CommandExecutor cmdExecutor = new CommandExecutor("sleep 1 && echo \"hello world\" && echo goodbye && " +
			"echo " +
			"multiple & >&2 echo should go to stderr");
		Thread t = new Thread(cmdExecutor);
		t.start();

		// the JVM should not terminate before that second!!
		t.join(); // wait for the thread to finish
		// now go to the tmp folder and see the log
	}
}