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

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimerSettingsTest {
	private final int N_TESTS = 100;

	@Test
	void getHMSAsSeconds() {
		Random random = new Random();
		TimerSettings obj;

		for (int i = 0; i < N_TESTS; ++i) {
			int hour = random.nextInt(60);
			int minutes = random.nextInt(60);
			int seconds = random.nextInt(60);
			obj = new TimerSettings((byte) hour, (byte) minutes, (byte) seconds);
			assertEquals(obj.getHMSAsSeconds(), hour * 60 * 60 + minutes * 60 + seconds);
		}
	}
}