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

package org.fos.sw.timers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.fos.sw.SWMain;
import org.fos.sw.hooks.BreakHooks;
import org.fos.sw.hooks.SingleBreakHooksConfig;
import org.fos.sw.timers.breaks.BreakConfig;
import org.fos.sw.timers.breaks.BreakToDo;
import org.fos.sw.timers.breaks.BreakType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTimerLoopTest
{
	private static final List<BreakConfig> configs;
	private static MainTimerLoop mainLoop;

	static {
		SWMain.changeMessagesBundle(Locale.ENGLISH);
		try {
			TimersManager.init(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		configs = new ArrayList<>();

		configs.add(new BreakConfig.Builder()
			.breakTimerSettings(WallClock.from(5))
			.workTimerSettings(WallClock.from(5))
			.postponeTimerSettings(WallClock.from(5))
			.hooksConfig(null)
			.breakType(BreakType.SMALL_BREAK)
			.enabled(true)
			.createBreakSettings());
		try {
			configs.add(new BreakConfig.Builder()
				.breakTimerSettings(WallClock.from(5))
				.workTimerSettings(WallClock.from(5))
				.postponeTimerSettings(WallClock.from(5))
				.hooksConfig(new BreakHooks(new SingleBreakHooksConfig(
					false, null, null,
					null, null, false,
					false, false, BreakType.STRETCH_BREAK
				), null
				))
				.breakType(BreakType.STRETCH_BREAK)
				.enabled(true)
				.createBreakSettings());
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		configs.add(new BreakConfig.Builder()
			.workTimerSettings(WallClock.from(5))
			.postponeTimerSettings(WallClock.from(5))
			.hooksConfig(null)
			.breakType(BreakType.DAY_BREAK)
			.enabled(true)
			.createBreakSettings());

		try {
			mainLoop = MainTimerLoop.createMainTimer(
				configs.stream()
					.map(BreakToDo::from)
					.collect(Collectors.toList())
			);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}

	@Test
	void setStopped() throws InterruptedException
	{
		mainLoop.updateBreakToDo(BreakToDo.from(
			new BreakConfig.Builder()
				.breakTimerSettings(WallClock.from(15))
				.workTimerSettings(WallClock.from(25))
				.postponeTimerSettings(WallClock.from(35))
				.breakType(BreakType.SMALL_BREAK)
				.createBreakSettings()
		));

		mainLoop.setStopped(true);
		Executors
			.newSingleThreadScheduledExecutor()
			.scheduleAtFixedRate(mainLoop, 0, 1, TimeUnit.SECONDS);

		Executors.newSingleThreadScheduledExecutor()
			.schedule(() -> {
				assertFalse(mainLoop.isBreakHappening());
				mainLoop.setStopped(false);
				System.out.println("Hola");
			}, 1, TimeUnit.SECONDS);

		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.schedule(() -> assertTrue(mainLoop.isBreakHappening()), 2, TimeUnit.SECONDS);

		executor.awaitTermination(10, TimeUnit.SECONDS);
	}

	@Test
	void updateBreakToDo()
	{
		BreakToDo updated = new BreakToDo(
			"Some message", "hola",
			new BreakConfig.Builder()
				.breakType(BreakType.SMALL_BREAK)
				.breakTimerSettings(WallClock.from(4))
				.workTimerSettings(WallClock.from(4))
				.postponeTimerSettings(WallClock.from(4))
				.createBreakSettings(),
			false
		);
		mainLoop.updateBreakToDo(updated);

		assertEquals(updated, mainLoop.getBreaksToDoList().get(BreakType.SMALL_BREAK));

		updated = new BreakToDo(
			"Some message", "hola mundo",
			new BreakConfig.Builder()
				.breakType(BreakType.STRETCH_BREAK)
				.breakTimerSettings(WallClock.from(4))
				.workTimerSettings(WallClock.from(4))
				.postponeTimerSettings(WallClock.from(4))
				.createBreakSettings(),
			false
		);
		mainLoop.updateBreakToDo(updated);

		assertEquals(updated, mainLoop.getBreaksToDoList().get(BreakType.STRETCH_BREAK));

		updated = new BreakToDo(
			"Some message", "hola",
			new BreakConfig.Builder()
				.breakType(BreakType.DAY_BREAK)
				.breakTimerSettings(WallClock.from(4))
				.workTimerSettings(WallClock.from(4))
				.postponeTimerSettings(WallClock.from(4))
				.createBreakSettings(),
			false
		);
		mainLoop.updateBreakToDo(updated);

		assertEquals(updated, mainLoop.getBreaksToDoList().get(BreakType.DAY_BREAK));
	}

	@Test
	void setBreakEnabled()
	{
		boolean[] values = {true, false};
		for (boolean value : values) {
			mainLoop.setBreakEnabled(BreakType.SMALL_BREAK, value);
			mainLoop.setBreakEnabled(BreakType.STRETCH_BREAK, value);
			mainLoop.setBreakEnabled(BreakType.DAY_BREAK, value);

			assertEquals(
				mainLoop.getBreaksToDoList().get(BreakType.SMALL_BREAK).getBreakConfig().isEnabled(),
				value
			);
			assertEquals(
				mainLoop.getBreaksToDoList().get(BreakType.STRETCH_BREAK).getBreakConfig().isEnabled(),
				value
			);
			assertEquals(
				mainLoop.getBreaksToDoList().get(BreakType.DAY_BREAK).getBreakConfig().isEnabled(),
				value
			);
		}
	}

	@Test
	void isBreakHappening() throws InterruptedException
	{
		mainLoop.updateBreakToDo(BreakToDo.from(
			new BreakConfig.Builder()
				.breakTimerSettings(WallClock.from(15))
				.workTimerSettings(WallClock.from(25))
				.postponeTimerSettings(WallClock.from(35))
				.breakType(BreakType.SMALL_BREAK)
				.createBreakSettings()
		));

		Executors.newSingleThreadScheduledExecutor()
			.scheduleAtFixedRate(mainLoop, 0, 1, TimeUnit.SECONDS);

		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.schedule(() -> assertTrue(mainLoop.isBreakHappening()), 1, TimeUnit.SECONDS);
		executor.awaitTermination(10, TimeUnit.SECONDS);
	}
}