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

package dev.benjaminguzman.gui.cv;

import dev.benjaminguzman.SpineWare;
import dev.benjaminguzman.gui.Colors;
import dev.benjaminguzman.gui.Fonts;
import dev.benjaminguzman.gui.Initializable;
import dev.benjaminguzman.prefs.cv.CVPrefsManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class RefreshRatePanel extends JPanel implements Initializable
{
	@NotNull
	private final Consumer<Integer> onSetRefreshRate;

	private final JSpinner refreshRateSpinner;

	public RefreshRatePanel(@NotNull final Consumer<Integer> onSetRefreshRate)
	{
		this.onSetRefreshRate = onSetRefreshRate;

		this.refreshRateSpinner = new JSpinner(
			new SpinnerNumberModel(CVPrefsManager.DEFAULT_REFRESH_RATE_MS, 100, 5_000, 1)
		);
	}

	@Override
	public void initComponents()
	{
		JLabel titleLabel = new JLabel(SpineWare.messagesBundle.getString("cv_refresh_rate_title"));
		titleLabel.setFont(Fonts.TITLE_FONT);

		JLabel descLabel = new JLabel(SpineWare.messagesBundle.getString("cv_refresh_rate_description"));
		descLabel.setFont(Fonts.FULL_DESCRIPTION_FONT);

		// add components
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 0, 5, 0);

		this.add(titleLabel, gbc);

		++gbc.gridy;
		this.add(descLabel, gbc);

		++gbc.gridy;
		this.add(this.createSliderPanel(), gbc);
	}

	/**
	 * Creates a panel with the given label and a slider
	 *
	 * @return the create panel panel
	 */
	private JPanel createSliderPanel()
	{
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		JLabel valueWarningLabel = new JLabel();
		valueWarningLabel.setForeground(Colors.YELLOW);

		panel.add(new JLabel(SpineWare.messagesBundle.getString("frequency")));
		panel.add(refreshRateSpinner);
		panel.add(new JLabel("ms"));
		panel.add(valueWarningLabel);

		String warnValueTooLow = SpineWare.messagesBundle.getString("refresh_rate_warn_too_low");
		String warnValueTooHigh = SpineWare.messagesBundle.getString("refresh_rate_warn_too_high");

		// add listeners
		refreshRateSpinner.addChangeListener(e -> {
			int selected_rate = (int) refreshRateSpinner.getValue();
			this.onSetRefreshRate.accept(selected_rate);

			if (selected_rate < 500) // it may slow down the computer
				valueWarningLabel.setText(warnValueTooLow);
			else if (selected_rate > 2_000) // functionality may not work as expected
				valueWarningLabel.setText(warnValueTooHigh);
			else // inside normal values, don't show warning
				valueWarningLabel.setText(null);

			CVPrefsManager.saveRefreshRate(selected_rate);
		});

		return panel;
	}

	/**
	 * Changes the value in the slider
	 *
	 * @param refresh_rate the new value
	 */
	public void setRefreshRate(int refresh_rate)
	{
		this.refreshRateSpinner.setValue(refresh_rate);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		this.refreshRateSpinner.setEnabled(enabled);
	}
}
