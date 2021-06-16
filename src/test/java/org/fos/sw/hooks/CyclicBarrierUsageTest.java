/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.dev>
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

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

public class CyclicBarrierUsageTest
{
	static boolean flag;

	@Test
	void testUsage()
	{
		// 2 parties should call await()
		// 1st party: this thread
		// 2nd party: the thread playing audio when the audio terminates
		CyclicBarrier barrier = new CyclicBarrier(2);
		Runnable barrierAwait = () -> {
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		};
		int i = 10;
		while (--i > 0) {

			// simulate task (e.g. playing audio)
			// this is the second party
			new Thread(() -> {
				if (flag)
					fail();

				flag = true;
				try {
					System.out.println("Playing audio... (or computing something)");
					Thread.sleep(100);
					barrierAwait.run();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				flag = false;
			}).start();

			// first party await() call
			barrierAwait.run();
			System.out.println("Done.");

			// at this point we know the audio has finished playing
			// because both parties called await()
			// reset the barrier for the next loop cycle
			barrier.reset();
		}
	}
}
