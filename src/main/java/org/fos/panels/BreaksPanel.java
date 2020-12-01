/*
 * Copyright © 2020 Benjamín Guzmán
 * Author: Benjamín Guzmán <bg@benjaminguzman.dev>
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
package org.fos.panels;

import org.fos.Fonts;
import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.timers.BreakSettings;
import org.fos.timers.TimerSettings;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;

public class BreaksPanel extends JScrollPane {
	private final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 24);
	private final Font FULL_DESCRIPTION_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
	private final Font DESCRIPTION_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);

	private final JCheckBox[] featureEnabledCheckBoxes = new JCheckBox[3];
	private final TimeInputPanel[] workingTimeInputs = new TimeInputPanel[3];
	private final TimeInputPanel[] breaksTimeInputs = new TimeInputPanel[2];

	private final JLabel changesSavedStatusLabel;
	private final BreakSettings[] preferredBreakSettings;
	private final TimerSettings[] minWorkRecommendedTimes = new TimerSettings[]{
		new TimerSettings((byte) 0, (byte) 5, (byte) 0), // min work time for the small break
		new TimerSettings((byte) 0, (byte) 30, (byte) 0), // min work time for the stretch break
		new TimerSettings((byte) 8, (byte) 0, (byte) 0), // min work time for the day break
	};
	private final TimerSettings[] maxWorkRecommendedTimes = new TimerSettings[]{
		new TimerSettings((byte) 0, (byte) 30, (byte) 0), // max work time for the small break
		new TimerSettings((byte) 2, (byte) 0, (byte) 0), // max work time for the stretch break
		new TimerSettings((byte) 12, (byte) 0, (byte) 0), // max work time for the day break
	};
	private final TimerSettings[] minBreakRecommendedTimes = new TimerSettings[]{
		new TimerSettings((byte) 0, (byte) 0, (byte) 10), // min break time for the small break
		new TimerSettings((byte) 0, (byte) 10, (byte) 0), // min break time for the stretch break
	};
	private final TimerSettings[] maxBreakRecommendedTimes = new TimerSettings[]{
		new TimerSettings((byte) 0, (byte) 5, (byte) 0), // max break time for the small break
		new TimerSettings((byte) 1, (byte) 0, (byte) 0), // max break time for the stretch break
	};
	private final byte SMALL_BREAKS_IDX = 0;
	private final byte STRETCH_BREAKS_IDX = 1;
	private final byte DAY_BREAK_IDX = 2;
	private Timer changesSavedStatusLabelTimer;

	public BreaksPanel() {
		super();

		this.changesSavedStatusLabel = new JLabel();
		this.changesSavedStatusLabel.setForeground(Color.GREEN);
		this.changesSavedStatusLabel.setFont(Fonts.SANS_SERIF_BOLD_12);

		// load preferred times
		this.preferredBreakSettings = SWMain.timersManager.loadBreaksSettings();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createBreakPanel(this.SMALL_BREAKS_IDX));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createBreakPanel(this.STRETCH_BREAKS_IDX));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createBreakPanel(this.DAY_BREAK_IDX));
		panel.add(Box.createVerticalStrut(10));

		panel.add(Box.createVerticalStrut(5));
		panel.add(this.createActionsPanel());
		panel.add(Box.createVerticalStrut(15));

		this.setViewportView(panel);
		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}

	/**
	 * Creates a break panel
	 * This panel will display
	 * - name of the break
	 * - a small description of the break
	 * - working time input
	 * - break time input (if the break is not a day break, in which case this will not be displayed)
	 *
	 * @param break_idx the break index, this is important to avoid writing so much code, each break has an index
	 *                  small break is 0
	 *                  stretch break is 1
	 *                  day break is 2
	 *                  with that index this method can customize the parameters of the inputs and text
	 *
	 * @return the JPanel containing all the elements mentioned above
	 */
	private JPanel createBreakPanel(final byte break_idx) {
		String breakPrefix;
		if (break_idx == this.SMALL_BREAKS_IDX)
			breakPrefix = "small_breaks";
		else if (break_idx == this.STRETCH_BREAKS_IDX)
			breakPrefix = "stretch_breaks";
		else if (break_idx == this.DAY_BREAK_IDX)
			breakPrefix = "day_break";
		else
			throw new IllegalArgumentException("Argument break_idx is not in range");

		String breakTitle = SWMain.messagesBundle.getString(breakPrefix + "_title");
		String breakFullDescription = SWMain.messagesBundle.getString(breakPrefix + "_full_description");
		String breakDescription = SWMain.messagesBundle.getString(breakPrefix + "_description");
		JPanel panel = new JPanel(new GridBagLayout());

		// first row, title, description & checkbox
		JLabel breakTitleLabel = new JLabel(breakTitle, JLabel.CENTER);
		JLabel breakDescLabel = new JLabel(breakDescription, JLabel.CENTER);
		JLabel breakFullDescLabel = new JLabel(breakFullDescription, JLabel.CENTER);
		breakTitleLabel.setFont(this.TITLE_FONT);
		breakDescLabel.setFont(this.DESCRIPTION_FONT);
		breakFullDescLabel.setFont(this.FULL_DESCRIPTION_FONT);

		this.featureEnabledCheckBoxes[break_idx] = new JCheckBox(SWMain.messagesBundle.getString("feature_enabled"));
		this.featureEnabledCheckBoxes[break_idx].setSelected(this.preferredBreakSettings[break_idx].isEnabled());

		// second row working time input for hours, minutes, seconds
		JLabel workingTimeLabel = new JLabel(SWMain.messagesBundle.getString(breakPrefix + "_working_time_label"),
			SwingConstants.RIGHT);
		this.workingTimeInputs[break_idx] = new TimeInputPanel(
			this.minWorkRecommendedTimes[break_idx],
			this.maxWorkRecommendedTimes[break_idx],
			this.preferredBreakSettings[break_idx].workTimerSettings
		);
		this.workingTimeInputs[break_idx].setEnabled(this.preferredBreakSettings[break_idx].isEnabled());

		// third row break time input for hours, minutes, seconds
		JLabel breakTimeLabel = null;
		TimeInputPanel breakTimeInput = null;
		if (break_idx != this.DAY_BREAK_IDX) { // the day break doesn't have break time inputs, only working time inputs
			breakTimeLabel = new JLabel(SWMain.messagesBundle.getString(breakPrefix + "_break_time_label"),
				SwingConstants.RIGHT);
			this.breaksTimeInputs[break_idx] = new TimeInputPanel(
				this.minBreakRecommendedTimes[break_idx],
				this.maxBreakRecommendedTimes[break_idx],
				this.preferredBreakSettings[break_idx].breakTimerSettings
			);
			breakTimeInput = this.breaksTimeInputs[break_idx];
			breakTimeInput.setEnabled(this.preferredBreakSettings[break_idx].isEnabled());
		}

		// set listener for the checkbox
		this.featureEnabledCheckBoxes[break_idx].addActionListener(new OnActionCheckBoxListener(
			this.workingTimeInputs[break_idx],
			breakTimeInput,
			this.featureEnabledCheckBoxes[break_idx].isSelected()
		));


		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;

		// add first row, break description
		panel.add(breakTitleLabel, gridBagConstraints);

		gridBagConstraints.gridwidth = 4;
		++gridBagConstraints.gridx;
		panel.add(breakDescLabel, gridBagConstraints);

		// add second row, feature enabled checkbox and small description
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		panel.add(this.featureEnabledCheckBoxes[break_idx], gridBagConstraints);

		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.gridx = 1;
		panel.add(breakFullDescLabel, gridBagConstraints);

		// add second row, working time input
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;

		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		panel.add(workingTimeLabel, gridBagConstraints);

		gridBagConstraints.gridwidth = 3;
		++gridBagConstraints.gridx;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		panel.add(this.workingTimeInputs[break_idx], gridBagConstraints);

		// the day break does not have break time, only working time
		if (break_idx == this.DAY_BREAK_IDX)
			return panel;

		// add fourth row, break time input
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;

		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		panel.add(breakTimeLabel, gridBagConstraints);

		gridBagConstraints.gridwidth = 3;
		++gridBagConstraints.gridx;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		panel.add(breakTimeInput, gridBagConstraints);

		return panel;
	}

	/**
	 * Creates a panel with all the actions the user can do
	 * Currently, this actions only include save changes
	 *
	 * @return the panel created
	 */
	private JPanel createActionsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());

		// save button
		JButton saveButton = new JButton(SWMain.messagesBundle.getString("save_changes"));
		try {
			saveButton.setIcon(new ImageIcon(
				ImageIO.read(SWMain.getImageAsStream("/resources/media/save_white_18dp.png"))
			));
		} catch (IOException e) {
			Loggers.errorLogger.log(Level.WARNING, "Error while reading save icon", e);
		}
		saveButton.setFont(Fonts.SANS_SERIF_BOLD_15);

		// set recommended values
		JButton setRecommendedValuesButton = new JButton(SWMain.messagesBundle.getString("set_recommended_values"));

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(10, 10, 10, 10);
		panel.add(setRecommendedValuesButton, gridBagConstraints);

		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weightx = 2;
		panel.add(saveButton, gridBagConstraints);

		gridBagConstraints.gridx += 2;
		gridBagConstraints.gridwidth = 1;
		panel.add(this.changesSavedStatusLabel, gridBagConstraints);

		saveButton.addActionListener(this::onClickSave);
		setRecommendedValuesButton.addActionListener(this::onClickSetRecommendedValues);

		return panel;
	}

	/**
	 * Method invoked when the users clicks the save button
	 *
	 * @param evt event
	 */
	private void onClickSave(ActionEvent evt) {
		boolean is_valid = true;
		for (int i = 0; i < this.breaksTimeInputs.length; i++) {
			this.breaksTimeInputs[i].clearWarning();
			if (this.featureEnabledCheckBoxes[i].isSelected())
				is_valid = is_valid & this.breaksTimeInputs[i].checkInputValidity();
		}
		if (!is_valid) {
			this.showOnSaveMessage(SWMain.messagesBundle.getString("invalid_input_check_above"), Color.RED);
			return;
		}

		for (int i = 0; i < this.workingTimeInputs.length; i++) {
			this.workingTimeInputs[i].clearWarning();
			if (this.featureEnabledCheckBoxes[i].isSelected())
				is_valid = is_valid & this.workingTimeInputs[i].checkInputValidity();
		}
		if (!is_valid) {
			this.showOnSaveMessage(SWMain.messagesBundle.getString("invalid_input_check_above"), Color.RED);
			return;
		}

		int[] breaks_idxs = new int[]{this.SMALL_BREAKS_IDX, this.STRETCH_BREAKS_IDX, this.DAY_BREAK_IDX};
		for (int break_idx : breaks_idxs) {
			this.preferredBreakSettings[break_idx].setEnabled(this.featureEnabledCheckBoxes[break_idx].isSelected());

			this.preferredBreakSettings[break_idx].workTimerSettings = new TimerSettings(
				this.workingTimeInputs[break_idx].getTime()
			);

			if (break_idx != this.DAY_BREAK_IDX)
				this.preferredBreakSettings[break_idx].breakTimerSettings = new TimerSettings(
					this.breaksTimeInputs[break_idx].getTime()
				);
		}
		SWMain.timersManager.saveBreaksSettings(this.preferredBreakSettings);

		this.showOnSaveMessage(SWMain.messagesBundle.getString("changes_saved")
			+ ". " + SWMain.messagesBundle.getString("changes_saved_extra_text"), Color.GREEN);

		SWMain.timersManager.createExecutorsFromPreferences();
	}

	/**
	 * Show an error in the message label
	 * Naturally, this method will set the foreground color to red
	 * It will also create a timer to delete the reset the text in the label
	 *
	 * @param successMessage message to show in the label
	 * @param color          color of the text
	 */
	private void showOnSaveMessage(String successMessage, Color color) {
		this.changesSavedStatusLabel.setForeground(color);
		this.changesSavedStatusLabel.setText(successMessage);

		if (this.changesSavedStatusLabelTimer != null && this.changesSavedStatusLabelTimer.isRunning())
			this.changesSavedStatusLabelTimer.stop();

		this.changesSavedStatusLabelTimer = new Timer(10_000, (ActionEvent evt) ->
			this.changesSavedStatusLabel.setText(null)
		);

		this.changesSavedStatusLabelTimer.start();
	}

	/**
	 * Sets the recommended values
	 *
	 * @param evt the event argument from the action listener
	 */
	private void onClickSetRecommendedValues(ActionEvent evt) {
		this.workingTimeInputs[this.SMALL_BREAKS_IDX].setValues((byte) 0, (byte) 10, (byte) 0);
		this.breaksTimeInputs[this.SMALL_BREAKS_IDX].setValues((byte) 0, (byte) 0, (byte) 10);

		this.workingTimeInputs[this.STRETCH_BREAKS_IDX].setValues((byte) 2, (byte) 0, (byte) 0);
		this.breaksTimeInputs[this.STRETCH_BREAKS_IDX].setValues((byte) 0, (byte) 30, (byte) 0);

		this.workingTimeInputs[this.DAY_BREAK_IDX].setValues((byte) 8, (byte) 0, (byte) 0);
	}

	/**
	 * Class to enable or disable time inputs when checkbox is clicked
	 */
	private static class OnActionCheckBoxListener implements ActionListener {
		private final TimeInputPanel workingTimeInputPanel;
		private final TimeInputPanel breakTimeInputPanel;
		private boolean is_enabled;

		public OnActionCheckBoxListener(final TimeInputPanel workingTimeInputPanel,
						final TimeInputPanel breakTimeInputPanel,
						final boolean is_enabled
		) {
			this.workingTimeInputPanel = workingTimeInputPanel;
			this.breakTimeInputPanel = breakTimeInputPanel;
			this.is_enabled = is_enabled;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.is_enabled = !this.is_enabled;

			this.workingTimeInputPanel.clearWarning();
			//this.workingTimeInputPanel.checkInputWarnings();
			this.workingTimeInputPanel.setEnabled(this.is_enabled);

			if (this.breakTimeInputPanel != null) {
				this.breakTimeInputPanel.clearWarning();
				//this.workingTimeInputPanel.checkInputWarnings();
				this.breakTimeInputPanel.setEnabled(this.is_enabled);
			}

		}
	}
}