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

package org.fos.timers;

import java.util.Random;
import org.fos.sw.timers.WallClock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WallClockTest
{
	private final int N_TESTS = 100;

	@Test
	void getHMSAsSeconds()
	{
		Random random = new Random();
		WallClock obj;

		for (int i = 0; i < N_TESTS; ++i) {
			int hours = random.nextInt(60);
			int minutes = random.nextInt(60);
			int seconds = random.nextInt(60);
			obj = new WallClock((byte) hours, (byte) minutes, (byte) seconds);
			int hms_as_seconds = hours * 60 * 60 + minutes * 60 + seconds;
			// multiple asserts to test the cache effectiveness
			assertEquals(hms_as_seconds, obj.getHMSAsSeconds());
			assertEquals(hms_as_seconds, obj.getHMSAsSeconds());
			assertEquals(hms_as_seconds, obj.getHMSAsSeconds());
		}

		// try limit values 0
		obj = WallClock.from(0);
		assertEquals(0, obj.getHMSAsSeconds());

		// try limit values 60
		obj = new WallClock((byte) 24, (byte) 59, (byte) 59);
		assertEquals(24 * 60 * 60 + 59 * 60 + 59, obj.getHMSAsSeconds());
	}

	@Test
	void seconds2HoursMinutesSeconds()
	{
		Random random = new Random();
		WallClock obj = new WallClock((byte) 0, (byte) 0, (byte) 0);

		for (int i = 0; i < N_TESTS; ++i) {
			int hours = random.nextInt(60);
			int minutes = random.nextInt(60);
			int seconds = random.nextInt(60);
			int hms_as_seconds = hours * 60 * 60 + minutes * 60 + seconds;
			WallClock actual = WallClock.from(hms_as_seconds);

			assertEquals(hours, actual.getHours());
			assertEquals(minutes, actual.getMinutes());
			assertEquals(seconds, actual.getSeconds());
		}

		// try limit values 0
		WallClock actual = WallClock.from(0);
		assertEquals(0, actual.getHours());
		assertEquals(0, actual.getMinutes());
		assertEquals(0, actual.getSeconds());

		// try limit values 60
		actual = WallClock.from(24 * 60 * 60 + 59 * 60 + 59);
		assertEquals(24, actual.getHours());
		assertEquals(59, actual.getMinutes());
		assertEquals(59, actual.getSeconds());
	}

	@Test
	void getHMSAsString()
	{
		Random random = new Random();
		WallClock obj;
		String expectedStr;

		for (int i = 0; i < N_TESTS; ++i) {
			int hours = random.nextInt(60);
			int minutes = random.nextInt(60);
			int seconds = random.nextInt(60);
			obj = new WallClock((byte) hours, (byte) minutes, (byte) seconds);

			expectedStr = "";
			if (hours != 0)
				expectedStr += hours + "h ";
			if (minutes != 0)
				expectedStr += minutes + "m ";
			if (seconds != 0)
				expectedStr += seconds + "s";

			assertEquals(obj.getHMSAsString(), expectedStr.trim());
		}

		// try limit values 0
		obj = WallClock.from(0);
		assertEquals("", obj.getHMSAsString());

		// try limit values 60
		obj = new WallClock((byte) 24, (byte) 59, (byte) 59);
		assertEquals("24h 59m 59s", obj.getHMSAsString());
	}

	@Test
	void subtractSeconds()
	{
		int hms_as_seconds = 24 * 60 * 60 + 59 * 60 + 59;
		int remaining_seconds = hms_as_seconds;

		WallClock timer = WallClock.from(hms_as_seconds);

		while (remaining_seconds > 0) {
			// if the subtraction will result in a non-negative number, the method should return true
			assertTrue(timer.subtractSeconds((byte) 1));
			remaining_seconds -= 1;

			assertEquals(timer.getHMSAsSeconds(), remaining_seconds);
		}

		// if the subtraction will result in a negative number, the method should return false
		assertFalse(timer.subtractSeconds((byte) 1));
	}
}