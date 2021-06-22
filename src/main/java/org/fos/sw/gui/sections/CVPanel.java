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

package org.fos.sw.gui.sections;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.fos.sw.SWMain;
import org.fos.sw.cv.CVManager;
import org.fos.sw.gui.Fonts;
import org.fos.sw.gui.cv.CVConfigPanel;
import org.fos.sw.prefs.cv.CVPrefsManager;

/**
 * Panel containing a "mirror" where the user can see the current status of his/her posture
 * It also contains all the configuration panels
 */
public class CVPanel extends AbstractSection
{
	private CVConfigPanel cvConfigPanel;
	private JCheckBox featureEnabledCheckBox;

	public CVPanel()
	{
		super();
	}

	@Override
	public void initComponents()
	{
		JPanel panel = new JPanel(new BorderLayout());

		// specific CVUtils feature config
		JPanel cvConfigPanel = createCVConfigPanel();

		// general feature config
		JPanel generalConfigPanel = createGeneralConfigPanel();

		panel.add(generalConfigPanel, BorderLayout.NORTH);
		panel.add(cvConfigPanel, BorderLayout.CENTER);

		this.setViewportView(panel);
		this.configScrollBar();
	}

	/**
	 * @return a panel containing all the controllers to change the CVUtils configuration
	 * This includes a "mirror" where the user can see himself and inputs to configure the parameters of
	 * the posture checking algorithm
	 */
	private JPanel createCVConfigPanel()
	{
		JPanel panel = new JPanel();
		cvConfigPanel = new CVConfigPanel();
		panel.add(this.cvConfigPanel);
		cvConfigPanel.initComponents();
		return panel;
	}

	/**
	 * @return a panel with a small description of the feature and
	 * all the controllers (buttons) relative to the general configuration
	 * The controllers may include checkboxes (e.g. is feature enabled?),
	 */
	private JPanel createGeneralConfigPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());

		// labels creation
		JLabel titleLabel = new JLabel(SWMain.messagesBundle.getString("posture_checker_title"));
		titleLabel.setFont(Fonts.TITLE_FONT);

		JLabel descriptionLabel = new JLabel(
			SWMain.messagesBundle.getString("posture_checker_description")
		);
		descriptionLabel.setFont(Fonts.FULL_DESCRIPTION_FONT);

		// inputs creations
		featureEnabledCheckBox = new JCheckBox(
			SWMain.messagesBundle.getString("feature_enabled")
		);
		featureEnabledCheckBox.setSelected(CVPrefsManager.isFeatureEnabled());

		// add listeners
		featureEnabledCheckBox.addActionListener(e -> {
			CVPrefsManager.setFeatureEnabled(featureEnabledCheckBox.isSelected());
			cvConfigPanel.setEnabled(featureEnabledCheckBox.isSelected());
			if (!featureEnabledCheckBox.isSelected())
				CVManager.stopCVLoop();
			// CVManager.startCVLoop(); will be invoked in the hook onHide
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(10, 10, 10, 10);

		panel.add(titleLabel, gbc);

		++gbc.gridx;
		panel.add(featureEnabledCheckBox, gbc);

		--gbc.gridx;
		++gbc.gridy;
		panel.add(descriptionLabel, gbc);

		return panel;
	}

	/**
	 * Method to be invoked when the section is shown
	 */
	@Override
	public void onShown()
	{
		CVManager.stopCVLoop(false);
		this.cvConfigPanel.onShown();
	}

	/**
	 * Method invoked whenever the section is not visible anymore
	 */
	@Override
	public void onHide()
	{
		this.cvConfigPanel.onHide();

		// if this method was called because the main frame lost focus and the cause was an inner dialog, the
		// CV loop must not be started
		if (!this.cvConfigPanel.hasVisibleDialog())
			CVManager.startCVLoop();
	}

	@Override
	public void onDispose()
	{
		this.cvConfigPanel.onHide();
		// when the jframe is being disposed is because the application will exit. Therefore, there is
		// no need to call CVManager.startCVLoop()
	}
}
