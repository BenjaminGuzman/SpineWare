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

package org.fos.sw.gui.cv;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import org.fos.sw.SWMain;
import org.fos.sw.cv.CVPrefsManager;
import org.fos.sw.gui.Colors;
import org.fos.sw.gui.Fonts;
import org.fos.sw.gui.Initializable;
import org.jetbrains.annotations.NotNull;

public class RefreshRatePanel extends JPanel implements Initializable
{
	@NotNull
	private final Consumer<Integer> onSetRefreshRate;

	private final JSlider refreshRateSlider;

	public RefreshRatePanel(@NotNull final Consumer<Integer> onSetRefreshRate)
	{
		this.onSetRefreshRate = onSetRefreshRate;

		this.refreshRateSlider = new JSlider(100, 5_000);
	}

	@Override
	public void initComponents()
	{
		JLabel titleLabel = new JLabel(SWMain.messagesBundle.getString("cv_refresh_rate_title"));
		titleLabel.setFont(Fonts.TITLE_FONT);

		JLabel descLabel = new JLabel(SWMain.messagesBundle.getString("cv_refresh_rate_description"));
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
		JPanel panel = new JPanel();
		JLabel valueLabel = new JLabel();
		JLabel valueWarningLabel = new JLabel();
		valueWarningLabel.setForeground(Colors.YELLOW);

		refreshRateSlider.setPaintTicks(true);
		refreshRateSlider.setPaintLabels(true);
		refreshRateSlider.setPaintTrack(true);

		panel.add(new JLabel(SWMain.messagesBundle.getString("frequency")));
		panel.add(refreshRateSlider);
		panel.add(valueLabel);
		panel.add(valueWarningLabel);

		String warnValueTooLow = SWMain.messagesBundle.getString("refresh_rate_warn_too_low");
		String warnValueTooHigh = SWMain.messagesBundle.getString("refresh_rate_warn_too_high");

		// add listeners
		refreshRateSlider.addChangeListener(e -> {
			int selected_rate = refreshRateSlider.getValue();
			this.onSetRefreshRate.accept(selected_rate);

			valueLabel.setText(selected_rate + "ms");

			if (selected_rate < 500) // it may slow down the computer
				valueWarningLabel.setText(warnValueTooLow);
			else if (selected_rate > 2_000) // functionality may not work as expected
				valueWarningLabel.setText(warnValueTooHigh);
			else // inside normal values, don't show warning
				valueWarningLabel.setText(null);

			// improve performance with this if, dont save values on each change, just when the user has
			// set a value
			if (!refreshRateSlider.getValueIsAdjusting()) // if the scroll has been adjusted
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
		this.refreshRateSlider.setValue(refresh_rate);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		this.refreshRateSlider.setEnabled(enabled);
	}
}
