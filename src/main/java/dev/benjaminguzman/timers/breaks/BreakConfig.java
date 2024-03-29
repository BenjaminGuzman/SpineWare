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

package dev.benjaminguzman.timers.breaks;

import dev.benjaminguzman.hooks.BreakHooks;
import dev.benjaminguzman.timers.WallClock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

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
	private BreakHooks hooksConfig;

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
		final @Nullable BreakHooks hooksConfig,
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

	public @NotNull BreakType getBreakType()
	{
		return this.breakType;
	}

	public @NotNull WallClock getWorkWallClock()
	{
		return workWallClock;
	}

	public void setWorkTimerSettings(WallClock workWallClock)
	{
		this.workWallClock = workWallClock;
	}

	public Optional<WallClock> getBreakWallClock()
	{
		return Optional.ofNullable(breakWallClock);
	}

	public void setBreakTimerSettings(@Nullable WallClock breakWallClock)
	{
		this.breakWallClock = breakWallClock;
	}

	public @NotNull WallClock getPostponeWallClock()
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

	public @Nullable BreakHooks getHooksConfig()
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

		private BreakHooks hooksConfig;

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

		public Builder hooksConfig(BreakHooks hooksConfig)
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
