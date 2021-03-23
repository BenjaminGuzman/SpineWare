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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.management.InstanceAlreadyExistsException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.fos.sw.SWMain;
import org.fos.sw.gui.cv.CVConfigPanel;

public class PostureChecker extends AbstractSection
{
	private static boolean instantiated;

	private CVConfigPanel cvConfigPanel;

	public PostureChecker() throws InstanceAlreadyExistsException
	{
		super();
		if (instantiated)
			throw new InstanceAlreadyExistsException(
				"There must exist a single instance of " + AbstractSection.class.getName()
			);
		instantiated = true;

		// TODO: load preferred configs
	}

	@Override
	public void initComponents()
	{
		JPanel panel = new JPanel(new BorderLayout());

		// general feature config
		JPanel generalConfigPanel = createGeneralConfigPanel();

		// specific CVController feature config
		JPanel cvConfigPanel = createCVConfigPanel();

		// save / reset buttons
		JPanel actionsPanel = createActionsPanel();

		panel.add(generalConfigPanel, BorderLayout.NORTH);
		panel.add(cvConfigPanel, BorderLayout.CENTER);
		panel.add(actionsPanel, BorderLayout.SOUTH);

		this.setViewportView(panel);
		this.configScrollBar();
	}

	/**
	 * @return a panel containing all the actions the user can perform
	 * For example, save settings or put recommended values
	 */
	private JPanel createActionsPanel()
	{
		JPanel panel = new JPanel();

		JButton saveBtn = new JButton(SWMain.getMessagesBundle().getString("save_settings"));
		JButton setRecommendedValuesBtn = new JButton(
			SWMain.getMessagesBundle().getString("set_recommended_values")
		);

		panel.add(setRecommendedValuesBtn);
		panel.add(saveBtn);

		return panel;
	}

	/**
	 * @return a panel containing all the controllers to change the CVController configuration
	 * This includes a "mirror" where the user can see himself and inputs to configure the parameters of
	 * the posture checking algorithm
	 */
	private JPanel createCVConfigPanel()
	{
		JPanel panel = new JPanel();
		this.cvConfigPanel = new CVConfigPanel();
		panel.add(this.cvConfigPanel);
		panel.setBackground(Color.CYAN);
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
		JLabel titleLabel = new JLabel(SWMain.getMessagesBundle().getString("posture_checker_title"));
		titleLabel.setFont(TITLE_FONT);

		JLabel descriptionLabel = new JLabel(
			SWMain.getMessagesBundle().getString("posture_checker_description")
		);
		descriptionLabel.setFont(FULL_DESCRIPTION_FONT);

		// inputs creations
		JCheckBox featureEnabledCheckBox = new JCheckBox(
			SWMain.getMessagesBundle().getString("feature_enabled")
		);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.insets = new Insets(10, 10, 10, 10);

		panel.add(titleLabel, gridBagConstraints);

		++gridBagConstraints.gridx;
		panel.add(featureEnabledCheckBox, gridBagConstraints);

		--gridBagConstraints.gridx;
		++gridBagConstraints.gridy;
		panel.add(descriptionLabel, gridBagConstraints);

		return panel;
	}

	/**
	 * Method to be invoked when the section is shown
	 */
	@Override
	public void onShown()
	{
		this.cvConfigPanel.onShown();
	}

	/**
	 * Method invoked whenever the section is not visible anymore
	 */
	@Override
	public void onHide()
	{
		this.cvConfigPanel.onHide();
	}
}
