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

package org.fos.core;

import java.util.concurrent.ThreadFactory;
import org.jetbrains.annotations.NotNull;

public class DaemonThreadFactory implements ThreadFactory
{
	protected String threadsName;

	public DaemonThreadFactory()
	{
		threadsName = "Daemon-Thread";
	}

	public DaemonThreadFactory(String threadsName)
	{
		this.threadsName = threadsName;
	}

	/**
	 * Constructs a new {@code Thread}.  Implementations may also initialize
	 * priority, name, daemon status, {@code ThreadGroup}, etc.
	 *
	 * @param r a runnable to be executed by new thread instance
	 * @return constructed thread, or {@code null} if the request to
	 * create a thread is rejected
	 */
	@Override
	public Thread newThread(@NotNull Runnable r)
	{
		Thread t = new Thread(r);
		t.setName(threadsName);
		t.setDaemon(true);
		return t;
	}
}
