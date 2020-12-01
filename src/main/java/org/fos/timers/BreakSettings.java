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

/**
 * Wrapper class containing information about the break settings including
 * working time settings
 * break settings
 * and a boolean value to indicate if the break is enabled or not
 */
public class BreakSettings {
	public TimerSettings workTimerSettings;
	public TimerSettings breakTimerSettings;
	private boolean is_enabled;

	public BreakSettings(final TimerSettings workTimerSettings, final TimerSettings breakTimerSettings, final boolean is_enabled) {
		this.workTimerSettings = workTimerSettings;
		this.breakTimerSettings = breakTimerSettings;
		this.is_enabled = is_enabled;
	}

	public BreakSettings(final TimerSettings workTimerSettings, final TimerSettings breakTimerSettings) {
		this(workTimerSettings, breakTimerSettings, true);
	}

	public boolean isEnabled() {
		return this.is_enabled;
	}

	public void setEnabled(final boolean is_enabled) {
		this.is_enabled = is_enabled;
	}
}
