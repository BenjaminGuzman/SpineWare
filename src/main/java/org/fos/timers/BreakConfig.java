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

import java.util.Objects;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.fos.Loggers;
import org.fos.core.BreakType;
import org.fos.hooks.BreakHooksConfig;
import org.fos.hooks.HooksConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper class containing information about the break settings including
 * working time settings
 * break settings
 * and a boolean value to indicate if the break is enabled or not
 */
public class BreakConfig
{
	@NotNull
	private BreakType breakType;

	@Nullable
	private BreakHooksConfig hooksConfig;

	@NotNull
	private WallClock workWallClock;
	/**
	 * This field may be null if {@link #breakType} is of type {@link BreakType#DAY_BREAK}
	 */
	@Nullable
	private WallClock breakWallClock;
	@NotNull
	private WallClock postponeWallClock;
	private boolean is_enabled;

	private BreakConfig(
		final @NotNull WallClock workWallClock,
		final @Nullable WallClock breakWallClock,
		final @NotNull WallClock postponeWallClock,
		final @NotNull BreakType breakType,
		final @Nullable BreakHooksConfig hooksConfig,
		final boolean is_enabled
	)
	{
		this.workWallClock = Objects.requireNonNull(workWallClock);
		this.breakWallClock = breakWallClock;
		this.postponeWallClock = Objects.requireNonNull(postponeWallClock);
		this.breakType = Objects.requireNonNull(breakType);
		this.is_enabled = is_enabled;
		this.hooksConfig = hooksConfig;
	}

	/**
	 * Saves the break settings in the user preferences
	 *
	 * @param prefs  the Preferences object that will be used to store the given settings
	 * @param config the configuration to be saved
	 */
	public static void saveBreakSettings(final Preferences prefs, final BreakConfig config)
	{
		String breakName = config.getBreakType().getName();

		prefs.putBoolean(breakName + " enabled", config.isEnabled());

		if (!config.isEnabled())
			return;

		// save timer settings
		prefs.putInt(
			breakName + " working time",
			config.getWorkTimerSettings().getHMSAsSeconds()
		);
		prefs.putInt(
			breakName + " postpone time",
			config.getPostponeTimerSettings().getHMSAsSeconds()
		);
		WallClock breakWallClock = config.getBreakTimerSettings();
		if (breakWallClock != null)
			prefs.putInt(breakName + " break time", breakWallClock.getHMSAsSeconds());

		// technically here the hook settings should be saved too
		// but they're being saved in the ConfigureHooksDialog, so no need to save them again here
	}

	/**
	 * Instantiates a {@link BreakConfig} object from the given preferences according to the given break type
	 *
	 * @param prefs the Preferences object that should store the break configuration
	 */
	public static BreakConfig fromPrefs(final Preferences prefs, final BreakType breakType)
	{
		String breakName = breakType.getName();
		boolean is_enabled = prefs.getBoolean(breakName + " enabled", false);

		// load timer settings
		int working_time = prefs.getInt(breakName + " working time", 0);
		int break_time = prefs.getInt(breakName + " break time", 0);
		int postpone_time = prefs.getInt(breakName + " postpone time", 0);

		// load hooks settings
		HooksConfig notifHooksConf;
		try {
			notifHooksConf = HooksConfig.fromPrefs(breakType, true);
		} catch (InstantiationException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Some preferences for the break: " + breakType
					+ " and notification hooks not saved correctly",
				e
			);
			return null;
		}

		HooksConfig breakHooksConf = null;
		try {
			breakHooksConf = HooksConfig.fromPrefs(breakType, false);
		} catch (InstantiationException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Some preferences for the break: " + breakType
					+ " and break hooks were not saved correctly",
				e
			);
		}

		BreakConfig.Builder builder = new Builder()
			.enabled(is_enabled)
			.workTimerSettings(WallClock.from(working_time))
			.postponeTimerSettings(WallClock.from(postpone_time))
			.hooksConfig(new BreakHooksConfig(notifHooksConf, breakHooksConf))
			.breakType(breakType);

		return breakType == BreakType.DAY_BREAK
			? builder.breakTimerSettings(null).createBreakSettings()
			: builder.breakTimerSettings(WallClock.from(break_time)).createBreakSettings();
	}

	public @NotNull BreakType getBreakType()
	{
		return this.breakType;
	}

	public WallClock getWorkTimerSettings()
	{
		return workWallClock;
	}

	public void setWorkTimerSettings(WallClock workWallClock)
	{
		this.workWallClock = workWallClock;
	}

	public WallClock getBreakTimerSettings()
	{
		return breakWallClock;
	}

	public void setBreakTimerSettings(WallClock breakWallClock)
	{
		this.breakWallClock = breakWallClock;
	}

	public WallClock getPostponeTimerSettings()
	{
		return this.postponeWallClock;
	}

	public void setPostponeTimerSettings(WallClock postponeWallClock)
	{
		this.postponeWallClock = postponeWallClock;
	}

	public boolean isEnabled()
	{
		return this.is_enabled;
	}

	public void setEnabled(final boolean is_enabled)
	{
		this.is_enabled = is_enabled;
	}

	public void updateAll(BreakConfig newConfig)
	{
		this.workWallClock.updateAll(
			newConfig.workWallClock.getHours(),
			newConfig.workWallClock.getMinutes(),
			newConfig.workWallClock.getSeconds()
		);
		if (newConfig.breakWallClock != null && breakWallClock != null) {
			this.breakWallClock.updateAll(
				newConfig.breakWallClock.getHours(),
				newConfig.breakWallClock.getMinutes(),
				newConfig.breakWallClock.getSeconds()
			);
		}
		this.postponeWallClock.updateAll(
			newConfig.postponeWallClock.getHours(),
			newConfig.postponeWallClock.getMinutes(),
			newConfig.postponeWallClock.getSeconds()
		);
		this.breakType = newConfig.breakType;
		this.is_enabled = newConfig.is_enabled;

		this.hooksConfig = newConfig.getHooksConfig();
	}

	public @Nullable BreakHooksConfig getHooksConfig()
	{
		return this.hooksConfig;
	}

	@Override
	public String toString()
	{
		return "BreakSettings{" +
			"breakType=" + breakType +
			", workWallClock=" + workWallClock +
			", breakWallClock=" + breakWallClock +
			", postponeWallClock=" + postponeWallClock +
			", is_enabled=" + is_enabled +
			'}';
	}

	/**
	 * Builder class for the BreakConfig
	 */
	public static class Builder
	{
		private WallClock workWallClock;
		private WallClock breakWallClock;
		private WallClock postponeWallClock;
		private BreakType breakType;
		private boolean is_enabled = true;

		private BreakHooksConfig hooksConfig;

		public Builder workTimerSettings(WallClock workWallClock)
		{
			this.workWallClock = workWallClock;
			return this;
		}

		public Builder breakTimerSettings(WallClock breakWallClock)
		{
			this.breakWallClock = breakWallClock;
			return this;
		}

		public Builder postponeTimerSettings(WallClock postponeWallClock)
		{
			this.postponeWallClock = postponeWallClock;
			return this;
		}

		public Builder breakType(BreakType breakType)
		{
			this.breakType = breakType;
			return this;
		}

		public Builder enabled(boolean is_enabled)
		{
			this.is_enabled = is_enabled;
			return this;
		}

		public Builder hooksConfig(BreakHooksConfig hooksConfig)
		{
			this.hooksConfig = hooksConfig;
			return this;
		}

		public BreakConfig createBreakSettings()
		{
			return new BreakConfig(
				this.workWallClock,
				this.breakWallClock,
				this.postponeWallClock,
				this.breakType,
				this.hooksConfig,
				this.is_enabled
			);
		}
	}
}
