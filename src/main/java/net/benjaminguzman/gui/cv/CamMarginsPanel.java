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

package net.benjaminguzman.gui.cv;

import net.benjaminguzman.SWMain;
import net.benjaminguzman.gui.Fonts;
import net.benjaminguzman.gui.Initializable;
import net.benjaminguzman.prefs.cv.CVPrefsManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CamMarginsPanel extends JPanel implements Initializable
{
	@NotNull
	private final Consumer<Integer> onSetMarginX;

	@NotNull
	private final Consumer<Integer> onSetMarginY;

	private final JSlider marginXSlider;
	private final JSlider marginYSlider;

	public CamMarginsPanel(
		@NotNull final Consumer<Integer> onSetMarginX,
		@NotNull final Consumer<Integer> onSetMarginY
	)
	{
		this.onSetMarginX = onSetMarginX;
		this.onSetMarginY = onSetMarginY;

		this.marginXSlider = new JSlider(10, 40);
		this.marginYSlider = new JSlider(10, 40);
	}

	@Override
	public void initComponents()
	{
		JLabel titleLabel = new JLabel(SWMain.messagesBundle.getString("cam_margin_title"));
		titleLabel.setFont(Fonts.TITLE_FONT);

		JLabel descLabel = new JLabel(SWMain.messagesBundle.getString("cam_margin_description"));
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
		this.add(this.createMarginSliderPanel(true), gbc);

		++gbc.gridy;
		this.add(this.createMarginSliderPanel(false), gbc);
	}

	/**
	 * Creates a panel with an slider (and label) inside
	 *
	 * @param is_X if true, slider will be configured to set X values
	 * @return the panel containing the label and the slider
	 */
	private JPanel createMarginSliderPanel(boolean is_X)
	{
		JPanel panel = new JPanel();
		JSlider slider = is_X ? this.marginXSlider : this.marginYSlider;
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setPaintTrack(true);

		panel.add(new JLabel(SWMain.messagesBundle.getString(is_X ? "cam_margin_x" : "cam_margin_y")));
		panel.add(slider);

		// add listeners
		slider.addChangeListener(e -> {
			int selected_margin_percentage = slider.getValue();
			if (is_X)
				this.onSetMarginX.accept(selected_margin_percentage);
			else
				this.onSetMarginY.accept(selected_margin_percentage);

			// improve performance with this if, dont save values on each change, just when the user has
			// set a value
			if (!slider.getValueIsAdjusting()) // if the scroll has been adjusted
				CVPrefsManager.saveMargin(is_X, selected_margin_percentage);
		});

		return panel;
	}

	public void setMargin(boolean is_margin_X, int margin)
	{
		if (is_margin_X)
			this.marginXSlider.setValue(margin);
		else
			this.marginYSlider.setValue(margin);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		this.marginXSlider.setEnabled(enabled);
		this.marginYSlider.setEnabled(enabled);
	}
}
