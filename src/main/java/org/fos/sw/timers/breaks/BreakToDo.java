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

package org.fos.sw.timers.breaks;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.fos.sw.SWMain;
import org.fos.sw.gui.notifications.BreakCountDown;
import org.fos.sw.gui.notifications.TakeABreakNotification;
import org.fos.sw.hooks.BreakHooksConfig;
import org.fos.sw.timers.TimersManager;
import org.fos.sw.timers.WallClock;
import org.jetbrains.annotations.NotNull;

public class BreakToDo extends ToDo
{
	private final static byte MAX_N_DISMISSES = 3;
	private final static byte MAX_N_POSTPONED = 4;
	@NotNull
	private final BreakConfig breakConfig;
	private final String takeABreakMessage;
	private final String breakName;
	private final boolean add_take_break_option;

	// number of times the notification was dismissed
	private int n_dismisses;
	// number of times the notification was postponed
	private int n_postponed;

	@NotNull
	private WallClock postponeTime;

	public BreakToDo(
		String takeABreakMessage,
		String breakName,
		@NotNull BreakConfig breakConfig,
		boolean add_take_break_option
	)
	{
		this.breakConfig = breakConfig;
		this.takeABreakMessage = takeABreakMessage;
		this.breakName = breakName;
		this.add_take_break_option = add_take_break_option;
		postponeTime = breakConfig.getPostponeWallClock();
		if (breakConfig.isEnabled())
			updateExecutionTimes(breakConfig.getWorkWallClock());
	}

	/**
	 * Creates a {@link BreakToDo}
	 * from the given break configuration
	 *
	 * @param breakConfig the configuration for the break
	 * @return the create {@link BreakToDo}
	 */
	public static BreakToDo from(BreakConfig breakConfig)
	{
		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		if (breakConfig.getBreakType() == BreakType.DAY_BREAK)
			return new BreakToDo(
				messagesBundle.getString("time_for_a_day_break"),
				messagesBundle.getString("day_break_title"),
				breakConfig,
				false
			);

		// note at this time it is safe to call get on the optional, as the only case when it could return null
		// is when break time is day break
		return new BreakToDo(
			MessageFormat.format(
				messagesBundle.getString("notification_time_for_a_break"),
				breakConfig.getBreakWallClock().get().getHMSAsString()
			),
			messagesBundle.getString("time_for_a_small_break"),
			breakConfig,
			true
		);
	}

	/**
	 * Method to execute when it is time to take the break
	 * and also update some values within this class
	 */
	@Override
	public void run()
	{
		if (is_cancelled)
			return;

		if (!breakConfig.isEnabled())
			throw new RuntimeException("The run method was called inside a disabled BreakToDo object");

		if (Thread.currentThread().isInterrupted())
			return;

		postponeTime = breakConfig.getPostponeWallClock();
		BreakDecision breakDecision = showTakeBreakNotification(); // this is a blocking call
		if (breakDecision == null) {
			updateExecutionTimes(breakConfig.getWorkWallClock());
			return;
		}

		if (breakDecision != BreakDecision.TAKE_BREAK) {
			if (breakDecision == BreakDecision.POSTPONE)
				updateExecutionTimes(postponeTime);
			else if (breakDecision == BreakDecision.DISMISS)
				updateExecutionTimes(breakConfig.getWorkWallClock());
			return;
		}

		if (Thread.currentThread().isInterrupted())
			return;
		showBreakCountDown(); // this is a blocking call
		updateExecutionTimes(breakConfig.getWorkWallClock());
	}

	/**
	 * Recomputes {@link #next_execution_at} and {@link #last_execution_at} as if the object as just created
	 */
	public void reloadTimes()
	{
		this.updateExecutionTimes(breakConfig.getWorkWallClock());
	}

	/**
	 * Show the "take a break notification"
	 * <p>
	 * This method will block till the notification is disposed/dismissed/postponed/accepted
	 *
	 * @return the corresponding {@link BreakDecision}. It returns null if the thread was interrupted
	 */
	private BreakDecision showTakeBreakNotification()
	{
		if (this.n_dismisses >= BreakToDo.MAX_N_DISMISSES || this.n_postponed >= BreakToDo.MAX_N_POSTPONED) {
			this.n_dismisses = 0;
			this.n_postponed = 0;

			if (!this.breakConfig.getBreakWallClock().isPresent()) { // in case of the day limit timer
				// this should almost never happen
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
					null,
					"You really need to stop working!",
					"That's enough",
					JOptionPane.WARNING_MESSAGE
				));
			}
			return BreakDecision.TAKE_BREAK;
		}

		BreakHooksConfig hooksConfig = breakConfig.getHooksConfig();

		CountDownLatch notificationCountDownLatch = new CountDownLatch(1);
		AtomicReference<TakeABreakNotification> notificationRef = new AtomicReference<>();
		SwingUtilities.invokeLater(() -> notificationRef.set(
			new TakeABreakNotification(
				takeABreakMessage,
				notificationCountDownLatch,
				this.add_take_break_option,
				TimersManager.getPrefsIO().getNotificationPrefsIO().getNotificationPrefLocation(),
				hooksConfig != null ? hooksConfig::onStartNotificationHooks : null,
				hooksConfig != null ? hooksConfig::onEndNotificationHooks : null
			)
		));

		try {
			notificationCountDownLatch.await(); // wait till notification is dismissed
		} catch (InterruptedException e) {
			if (notificationRef.get() != null)
				notificationRef.get().disposeNoHooks();
			return null;
		} finally { // shutdown the hooks
			if (hooksConfig != null)
				hooksConfig.shutdown();
		}

		if (notificationRef.get() != null) {
			TakeABreakNotification notification = notificationRef.get();
			BreakDecision decision = notification.getBreakDecision();
			switch (decision) {
				case POSTPONE:
					++n_postponed;
					if (notification.getPostponeTimeOverride() != null)
						postponeTime = notification.getPostponeTimeOverride();
					break;
				case DISMISS:
					++n_dismisses;
					break;
			}
			return decision;
		}

		return BreakDecision.DISMISS;
	}

	/**
	 * Shows the break countdown
	 * <p>
	 * This method will block until the countdown is done or the user click cancel
	 */
	private void showBreakCountDown()
	{
		BreakHooksConfig hooksConfig = this.breakConfig.getHooksConfig();

		CountDownLatch breakCountDownLatch = new CountDownLatch(1);
		AtomicReference<BreakCountDown> breakCountDownRef = new AtomicReference<>();
		SwingUtilities.invokeLater(() -> breakCountDownRef.set(
			new BreakCountDown(
				this.breakName,
				this.breakConfig.getBreakWallClock().get(),
				breakCountDownLatch,
				hooksConfig != null ? hooksConfig::onStartBreakHooks : null,
				hooksConfig != null ? hooksConfig::onEndBreakHooks : null
			)
		));

		try {
			breakCountDownLatch.await();
		} catch (InterruptedException e) {
			if (breakCountDownRef.get() != null)
				breakCountDownRef.get().disposeNoHooks();
		} finally { // shutdown the hooks
			if (hooksConfig != null)
				hooksConfig.shutdown();
		}
	}

	/////////////
	// Getters //
	////////////
	public @NotNull BreakConfig getBreakConfig()
	{
		return breakConfig;
	}

	@Override
	public String toString()
	{
		return "BreakToDo{" +
			"last_execution_at=" + last_execution_at +
			", next_execution_at=" + next_execution_at +
			", is_cancelled=" + is_cancelled +
			", breakConfig=" + breakConfig +
			", n_dismisses=" + n_dismisses +
			", n_postponed=" + n_postponed +
			", takeABreakMessage='" + takeABreakMessage + '\'' +
			", breakName='" + breakName + '\'' +
			", add_take_break_option=" + add_take_break_option +
			'}';
	}
}
