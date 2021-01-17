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

package org.fos.gui.util;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.fos.SWMain;
import org.fos.gui.Colors;
import org.fos.gui.Fonts;
import org.fos.timers.WallClock;

public class TimeInputPanel extends JPanel
{
	private final JSpinner hoursSpinner;
	private final JSpinner minutesSpinner;
	private final JSpinner secondsSpinner;

	private final JLabel warningLabel;

	private final WallClock minRecommendedTime;
	private final WallClock maxRecommendedTime;
	private final boolean use_hard_limits; // check the constructor for details

	public TimeInputPanel(
		final WallClock minRecommendedTime,
		final WallClock maxRecommendedTime,
		final WallClock preferredTime
	)
	{
		this(minRecommendedTime, maxRecommendedTime, preferredTime, false);
	}

	/**
	 * Constructs the time input panel by adding the corresponding inputs
	 * (three spinners)
	 * and adding a label to each input
	 *
	 * @param minRecommendedTime the minimum recommended time for this input
	 * @param maxRecommendedTime the maximum recommended time for this input
	 * @param preferredTime      the preferred time for this input, this is obtained from the user preferences
	 *                           if this is null, the preferences does not exists
	 * @param use_hard_limits    if true, the min and max recommended times will not be treated as a nice to have
	 *                           but as a MUST have, therefore, if this is true error messages will appear instead
	 *                           of warning messages
	 */
	public TimeInputPanel(
		final WallClock minRecommendedTime,
		final WallClock maxRecommendedTime,
		final WallClock preferredTime,
		final boolean use_hard_limits
	)
	{
		super();

		int[] preferred_hms = new int[]{0, 0, 0};
		if (preferredTime != null) {
			preferred_hms[0] = preferredTime.getHours();
			preferred_hms[1] = preferredTime.getMinutes();
			preferred_hms[2] = preferredTime.getSeconds();
		}

		this.use_hard_limits = use_hard_limits;
		this.minRecommendedTime = minRecommendedTime;
		this.maxRecommendedTime = maxRecommendedTime;

		this.hoursSpinner = new JSpinner(new SpinnerNumberModel(preferred_hms[0], 0, 16, 1));
		this.minutesSpinner = new JSpinner(new SpinnerNumberModel(preferred_hms[1], 0, 59, 1));
		this.secondsSpinner = new JSpinner(new SpinnerNumberModel(preferred_hms[2], 0, 59, 1));

		this.warningLabel = new JLabel();
		this.warningLabel.setFont(Fonts.SANS_SERIF_BOLD_12);

		this.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new GridBagConstraints();

		// add spinners
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);

		this.add(this.hoursSpinner, gridBagConstraints);

		++gridBagConstraints.gridx;
		this.add(this.minutesSpinner, gridBagConstraints);

		++gridBagConstraints.gridx;
		this.add(this.secondsSpinner, gridBagConstraints);

		// add labels for spinners
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;

		ResourceBundle messagesBundle = SWMain.getMessagesBundle();
		this.add(new JLabel(messagesBundle.getString("hours")), gridBagConstraints);

		++gridBagConstraints.gridx;
		this.add(new JLabel(messagesBundle.getString("minutes")), gridBagConstraints);

		++gridBagConstraints.gridx;
		this.add(new JLabel(messagesBundle.getString("seconds")), gridBagConstraints);

		// add warning / error label
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;

		this.add(this.warningLabel);
		this.warningLabel.setForeground(Color.YELLOW);
	}

	/**
	 * Checks if the current values of the input is inside the given bounds
	 * if not, a warning is shown to the user as a label
	 */
	public boolean checkInputValidity()
	{
		int max_recommended_value = this.maxRecommendedTime.getHMSAsSeconds();
		int min_recommended_value = this.minRecommendedTime.getHMSAsSeconds();
		int selected_value = this.getTime();

		if (selected_value <= 5) {
			this.warningLabel.setForeground(Colors.RED);
			this.warningLabel.setText(SWMain.getMessagesBundle().getString("must_be_greater_than"));
			return false;
		}

		if (this.use_hard_limits) {
			if (selected_value > max_recommended_value)
				this.warningLabel.setText(this.maxRecommendedTime.getHMSAsString()
					+ " " + SWMain.getMessagesBundle().getString("is_the_maximum"));
			else if (selected_value < min_recommended_value)
				this.warningLabel.setText(this.minRecommendedTime.getHMSAsString()
					+ " " + SWMain.getMessagesBundle().getString("is_the_minimum"));
			else
				return true; // if the value is between min and max
			this.warningLabel.setForeground(Colors.RED);
			return false;
		}

		if (selected_value > max_recommended_value)
			this.showRecommendedValueWarning(true);
		else if (selected_value < min_recommended_value)
			this.showRecommendedValueWarning(false);

		return true;
	}

	/**
	 * Sets the enabled param on all the inputs
	 *
	 * @param enabled same param that will be passed to javax.swing.JComponent#setEnabled
	 */
	public void setEnabled(boolean enabled)
	{
		this.hoursSpinner.setEnabled(enabled);
		this.minutesSpinner.setEnabled(enabled);
		this.secondsSpinner.setEnabled(enabled);
	}

	/**
	 * Sets the given values as params in the corresponding spinners
	 *
	 * @param hours   hours to put in the hoursSpinner
	 * @param minutes minutes to put in the minutesSpinner
	 * @param seconds seconds to put in the secondsSpinner
	 */
	public void setValues(final byte hours, final byte minutes, final byte seconds)
	{
		this.hoursSpinner.setValue((int) hours); // the spinner expects integers, that's why cast is needed
		this.minutesSpinner.setValue((int) minutes);
		this.secondsSpinner.setValue((int) seconds);
	}

	public void setValues(final WallClock wallClock)
	{
		if (wallClock == null)
			return;
		this.setValues(wallClock.getHours(), wallClock.getMinutes(), wallClock.getSeconds());
	}

	/**
	 * Sets the text for the warningLabel
	 *
	 * @param show_upper_bound if true, the warning will show the preferred upper bound
	 */
	public void showRecommendedValueWarning(boolean show_upper_bound)
	{
		WallClock wallClock = show_upper_bound ? this.maxRecommendedTime : this.minRecommendedTime;
		this.warningLabel.setText(wallClock.getHMSAsString() + " " + SWMain.getMessagesBundle().getString("is_recommended"));
	}

	/**
	 * sets the warning label to null
	 */
	public void clearWarning()
	{
		this.warningLabel.setForeground(Color.YELLOW);
		this.warningLabel.setText(null);
	}

	/**
	 * Gets the hours minutes and seconds selected all in one
	 * all represented as seconds
	 *
	 * @return the number of seconds
	 */
	public int getTime()
	{
		return this.getSeconds() + this.getMinutes() * 60 + this.getHours() * 60 * 60;
	}

	/**
	 * @return the value as int of the hoursSpinner at the time this method is invoked
	 */
	public int getHours()
	{
		return (int) this.hoursSpinner.getModel().getValue();
	}

	/**
	 * @return the value as int of the minutesSpinner at the time this method is invoked
	 */
	public int getMinutes()
	{
		return (int) this.minutesSpinner.getModel().getValue();
	}

	/**
	 * @return the value as int of the secondsSpinner at the time this method is invoked
	 */
	public int getSeconds()
	{
		return (int) this.secondsSpinner.getModel().getValue();
	}
}
