/*
 * Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
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
package org.fos.panels;

import org.fos.Colors;
import org.fos.Fonts;
import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.core.BreakType;
import org.fos.timers.BreakSettings;
import org.fos.timers.TimerSettings;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;

public class BreaksPanel extends JScrollPane
{
	private final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 24);
	private final Font FULL_DESCRIPTION_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
	private final Font DESCRIPTION_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);

	private final JCheckBox[] featureEnabledCheckBoxes = new JCheckBox[3];
	private final TimeInputPanel[] workingTimeInputs = new TimeInputPanel[3];
	private final TimeInputPanel[] postponeTimeInputs = new TimeInputPanel[3];
	private final TimeInputPanel[] breaksTimeInputs = new TimeInputPanel[2];

	private final JLabel changesSavedStatusLabel;
	private final List<BreakSettings> preferredBreakSettings;
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
	private final String[] notificationAudioPaths = new String[3];
	private final String[] breakAudiosDirs = new String[2];
	private final TimerSettings minRequiredPostponeTime = new TimerSettings((byte) 0, (byte) 0, (byte) 6);
	private final TimerSettings maxRequiredPostponeTime = new TimerSettings((byte) 0, (byte) 30, (byte) 0);
	private Timer changesSavedStatusLabelTimer; // timer to hide the changes saved status label

	// configuration stuff
	private JComboBox<String> notificationLocationCombobox;

	public BreaksPanel()
	{
		super();

		this.changesSavedStatusLabel = new JLabel();
		this.changesSavedStatusLabel.setForeground(Color.GREEN);
		this.changesSavedStatusLabel.setFont(Fonts.SANS_SERIF_BOLD_12);

		// load preferred times
		this.preferredBreakSettings = SWMain.timersManager.loadBreaksSettings();

		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Audio files", "mp3", "ogg", "wav");

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createBreakPanel(BreakType.SMALL_BREAK, fileFilter));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createBreakPanel(BreakType.STRETCH_BREAK, fileFilter));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createBreakPanel(BreakType.DAY_BREAK, fileFilter));
		panel.add(Box.createVerticalStrut(10));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createOptionsPanel());
		panel.add(Box.createVerticalStrut(10));

		panel.add(Box.createVerticalStrut(5));
		panel.add(this.createActionsPanel());
		panel.add(Box.createVerticalStrut(15));

		this.setViewportView(panel);
		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.getHorizontalScrollBar().setUnitIncrement(16);
		this.getVerticalScrollBar().setUnitIncrement(16);
	}

	/**
	 * Creates a break panel
	 * This panel will display
	 * - name of the break
	 * - a small description of the break
	 * - working time input
	 * - break time input (if the break is not a day break, in which case this will not be displayed)
	 *
	 * @param breakType
	 * 	the break index, this is important to avoid writing so much code, each break has an index
	 * 	small break is 0
	 * 	stretch break is 1
	 * 	day break is 2
	 * 	with that index this method can customize the parameters of the inputs and text
	 *
	 * @return the JPanel containing all the elements mentioned above
	 */
	private JPanel createBreakPanel(final BreakType breakType, final FileNameExtensionFilter fileFilter)
	{
		String breakPrefix = breakType.getMessagesPrefix();
		byte break_idx = breakType.getIndex();

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
		this.featureEnabledCheckBoxes[break_idx].setSelected(this.preferredBreakSettings.get(break_idx).isEnabled());

		// audio configuration
		JButton notificationAudioButton = new JButton(SWMain.messagesBundle.getString("notification_select_audio_file"));
		notificationAudioButton.setToolTipText(SWMain.messagesBundle.getString("notification_select_audio_file_tooltip"));

		JLabel notificationAudioLabel = new JLabel("Default", SwingConstants.CENTER);
		notificationAudioButton.addActionListener((ActionEvent evt) -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(fileFilter);
			int selected_option = fileChooser.showOpenDialog(this);
			if (selected_option == JFileChooser.APPROVE_OPTION) {
				this.notificationAudioPaths[break_idx] = fileChooser.getSelectedFile().getAbsolutePath();
				notificationAudioLabel.setText(this.notificationAudioPaths[break_idx]);
			}
		});
		if (this.preferredBreakSettings.get(break_idx).getNotificationAudioPath() != null)
			notificationAudioLabel.setText(this.preferredBreakSettings.get(break_idx).getNotificationAudioPath());

		JButton notificationUseDefaultButton = new JButton(SWMain.messagesBundle.getString("notification_use_default_audio"));
		notificationUseDefaultButton.addActionListener((ActionEvent evt) -> {
			this.notificationAudioPaths[break_idx] = null;
			notificationAudioLabel.setText("Default");
		});

		JButton breakAudiosButton = new JButton(SWMain.messagesBundle.getString("break_select_audio_directory"));
		breakAudiosButton.setToolTipText(SWMain.messagesBundle.getString("break_select_audio_directory_tooltip"));
		JLabel breakAudiosLabel = new JLabel(SWMain.messagesBundle.getString("no_sound_will_be_played"), SwingConstants.CENTER);
		breakAudiosButton.addActionListener((ActionEvent evt) -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int selected_option = fileChooser.showOpenDialog(this);
			if (selected_option == JFileChooser.APPROVE_OPTION) {
				this.breakAudiosDirs[break_idx] = fileChooser.getSelectedFile().getAbsolutePath();
				breakAudiosLabel.setText(this.breakAudiosDirs[break_idx]);
			}
		});
		if (this.preferredBreakSettings.get(break_idx).getBreakAudiosDirStr() != null)
			breakAudiosLabel.setText(this.preferredBreakSettings.get(break_idx).getBreakAudiosDirStr());
		JButton breakNoAudioButton = new JButton(SWMain.messagesBundle.getString("break_without_audio"));
		breakNoAudioButton.addActionListener((ActionEvent evt) -> {
			this.breakAudiosDirs[break_idx] = null;
			breakAudiosLabel.setText(SWMain.messagesBundle.getString("no_sound_will_be_played"));
		});

		// second row working time input for hours, minutes, seconds
		JLabel workingTimeLabel = new JLabel(
			SWMain.messagesBundle.getString(breakPrefix + "_working_time_label"),
			SwingConstants.RIGHT
		);
		this.workingTimeInputs[break_idx] = new TimeInputPanel(
			this.minWorkRecommendedTimes[break_idx],
			this.maxWorkRecommendedTimes[break_idx],
			this.preferredBreakSettings.get(break_idx).getWorkTimerSettings()
		);
		this.workingTimeInputs[break_idx].setEnabled(this.preferredBreakSettings.get(break_idx).isEnabled());

		// third row break time input for hours, minutes, seconds
		JLabel breakTimeLabel = null;
		JLabel postponeTimeLabel;
		TimeInputPanel breakTimeInput = null;
		if (breakType != BreakType.DAY_BREAK) { // the day break doesn't have break time inputs, only working time inputs
			breakTimeLabel = new JLabel(
				SWMain.messagesBundle.getString("break_time_label"),
				SwingConstants.RIGHT
			);
			this.breaksTimeInputs[break_idx] = new TimeInputPanel(
				this.minBreakRecommendedTimes[break_idx],
				this.maxBreakRecommendedTimes[break_idx],
				this.preferredBreakSettings.get(break_idx).getBreakTimerSettings()
			);
			breakTimeInput = this.breaksTimeInputs[break_idx];
			breakTimeInput.setEnabled(this.preferredBreakSettings.get(break_idx).isEnabled());
		}

		postponeTimeLabel = new JLabel(
			SWMain.messagesBundle.getString("postpone_time_label"),
			SwingConstants.RIGHT
		);
		this.postponeTimeInputs[break_idx] = new TimeInputPanel(
			this.minRequiredPostponeTime,
			this.maxRequiredPostponeTime,
			this.preferredBreakSettings.get(break_idx).getPostponeTimerSettings(),
			true
		);
		this.postponeTimeInputs[break_idx].setEnabled(this.preferredBreakSettings.get(break_idx).isEnabled());

		// set listener for the checkbox
		this.featureEnabledCheckBoxes[break_idx].addActionListener(new OnActionCheckBoxListener(
			this.workingTimeInputs[break_idx],
			breakTimeInput,
			postponeTimeInputs[break_idx],
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

		// add third row, working time input
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
		if (breakType != BreakType.DAY_BREAK) {
			// add fourth row, break time input
			gridBagConstraints.gridx = 1;
			++gridBagConstraints.gridy;

			gridBagConstraints.gridwidth = 1;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			panel.add(breakTimeLabel, gridBagConstraints);

			gridBagConstraints.gridwidth = 3;
			++gridBagConstraints.gridx;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			panel.add(breakTimeInput, gridBagConstraints);
		}

		// add fifth row, break time input
		gridBagConstraints.gridx = 1;
		++gridBagConstraints.gridy;

		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		panel.add(postponeTimeLabel, gridBagConstraints);

		gridBagConstraints.gridwidth = 3;
		++gridBagConstraints.gridx;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		panel.add(this.postponeTimeInputs[break_idx], gridBagConstraints);

		// add audio files chooser, notification
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 0;
		++gridBagConstraints.gridy;

		panel.add(notificationUseDefaultButton, gridBagConstraints);

		++gridBagConstraints.gridx;
		panel.add(notificationAudioButton, gridBagConstraints);

		gridBagConstraints.gridx = 0;
		++gridBagConstraints.gridy;
		gridBagConstraints.gridwidth = 2;
		panel.add(notificationAudioLabel, gridBagConstraints);

		// add audio files chooser, break
		if (breakType != BreakType.DAY_BREAK) { // day break does not have break time
			gridBagConstraints.gridx = 2;
			gridBagConstraints.gridwidth = 1;
			--gridBagConstraints.gridy;
			panel.add(breakNoAudioButton, gridBagConstraints);

			++gridBagConstraints.gridx;
			panel.add(breakAudiosButton, gridBagConstraints);

			++gridBagConstraints.gridy;
			gridBagConstraints.gridx = 2;
			gridBagConstraints.gridwidth = 2;

			panel.add(breakAudiosLabel, gridBagConstraints);
		}

		return panel;
	}

	/**
	 * This will create the options panel
	 * currently containing only the notification location option
	 *
	 * @return the panel containing all elements for configuration
	 */
	private JPanel createOptionsPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());

		JLabel locationLabel = new JLabel(SWMain.messagesBundle.getString("notification_location"));

		String[] locationOptions = new String[]{
			SWMain.messagesBundle.getString("notification_location_bottom_right"),
			SWMain.messagesBundle.getString("notification_location_bottom_left"),
			SWMain.messagesBundle.getString("notification_location_top_right"),
			SWMain.messagesBundle.getString("notification_location_top_left")
		};
		this.notificationLocationCombobox = new JComboBox<>(locationOptions);
		this.notificationLocationCombobox.setSelectedIndex(
			SWMain.timersManager.getNotificationPrefLocation(true).getLocationIdx()
		);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.ipadx = 5;
		gridBagConstraints.ipady = 5;

		panel.add(locationLabel, gridBagConstraints);

		gridBagConstraints.gridx = 1;
		panel.add(this.notificationLocationCombobox, gridBagConstraints);

		return panel;
	}

	/**
	 * Creates a panel with all the actions the user can do
	 * Currently, this actions only include save changes
	 *
	 * @return the panel created
	 */
	private JPanel createActionsPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());

		// save button
		JButton saveButton = new JButton(SWMain.messagesBundle.getString("save_changes"));
		try (InputStream saveIconIS = SWMain.getFileAsStream("/resources/media/save_white_18dp.png")) {
			saveButton.setIcon(new ImageIcon(
				ImageIO.read(saveIconIS)
			));
		} catch (IOException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Error while reading save icon", e);
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
	 * @param evt
	 * 	event
	 */
	private void onClickSave(ActionEvent evt)
	{
		boolean is_valid = true;
		for (int i = 0; i < this.breaksTimeInputs.length; i++) {
			this.breaksTimeInputs[i].clearWarning();
			this.postponeTimeInputs[i].clearWarning();
			if (this.featureEnabledCheckBoxes[i].isSelected()) {
				is_valid = is_valid
					& this.breaksTimeInputs[i].checkInputValidity()
					& this.postponeTimeInputs[i].checkInputValidity();
			}
		}
		// check if the postpone time for the day limit is also valid
		this.postponeTimeInputs[2].clearWarning();
		if (this.featureEnabledCheckBoxes[2].isSelected())
			is_valid = is_valid & this.postponeTimeInputs[2].checkInputValidity();

		if (!is_valid) {
			this.showOnSaveMessage(SWMain.messagesBundle.getString("invalid_input_check_above"), Colors.RED);
			return;
		}

		for (int i = 0; i < this.workingTimeInputs.length; i++) {
			this.workingTimeInputs[i].clearWarning();
			if (this.featureEnabledCheckBoxes[i].isSelected())
				is_valid = is_valid & this.workingTimeInputs[i].checkInputValidity();
		}
		if (!is_valid) {
			this.showOnSaveMessage(SWMain.messagesBundle.getString("invalid_input_check_above"), Colors.RED);
			return;
		}

		// TODO: check for breaks that are equal, avoid collisions

		// update preferences for each break
		EnumSet.allOf(BreakType.class).forEach(this::updatePreferredBreakSettings);

		SWMain.timersManager.saveBreaksSettings(this.preferredBreakSettings);

		SWMain.timersManager.saveNotificationPrefLocation((byte) this.notificationLocationCombobox.getSelectedIndex());

		this.showOnSaveMessage(SWMain.messagesBundle.getString("changes_saved")
					       + ". " + SWMain.messagesBundle.getString("changes_saved_extra_text"), Color.GREEN);

		SWMain.timersManager.createExecutorsFromPreferences();
	}

	private void updatePreferredBreakSettings(BreakType breakType)
	{
		byte break_idx = breakType.getIndex();

		this.preferredBreakSettings.get(break_idx).setEnabled(this.featureEnabledCheckBoxes[break_idx].isSelected());

		this.preferredBreakSettings.get(break_idx).setWorkTimerSettings(new TimerSettings(
			this.workingTimeInputs[break_idx].getTime()
		));

		this.preferredBreakSettings.get(break_idx).setPostponeTimerSettings(new TimerSettings(
			this.postponeTimeInputs[break_idx].getTime()
		));

		this.preferredBreakSettings.get(break_idx).setNotificationAudioPath(this.notificationAudioPaths[break_idx]);

		if (breakType != BreakType.DAY_BREAK) {
			this.preferredBreakSettings.get(break_idx).setBreakTimerSettings(new TimerSettings(
				this.breaksTimeInputs[break_idx].getTime()
			));
			this.preferredBreakSettings.get(break_idx).setBreakAudiosDirStr(this.breakAudiosDirs[break_idx]);
		}
	}

	/**
	 * Show an error in the message label
	 * Naturally, this method will set the foreground color to red
	 * It will also create a timer to delete the reset the text in the label
	 *
	 * @param successMessage
	 * 	message to show in the label
	 * @param color
	 * 	color of the text
	 */
	private void showOnSaveMessage(String successMessage, Color color)
	{
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
	 * @param evt
	 * 	the event argument from the action listener
	 */
	private void onClickSetRecommendedValues(ActionEvent evt)
	{
		byte small_breaks_idx = BreakType.SMALL_BREAK.getIndex();
		this.workingTimeInputs[small_breaks_idx].setValues((byte) 0, (byte) 10, (byte) 0);
		this.breaksTimeInputs[small_breaks_idx].setValues((byte) 0, (byte) 0, (byte) 10);
		this.breaksTimeInputs[small_breaks_idx].setValues((byte) 0, (byte) 0, (byte) 10);
		if (!this.featureEnabledCheckBoxes[small_breaks_idx].isSelected())
			this.featureEnabledCheckBoxes[small_breaks_idx].doClick();

		byte stretch_breaks_idx = BreakType.STRETCH_BREAK.getIndex();
		this.workingTimeInputs[stretch_breaks_idx].setValues((byte) 2, (byte) 0, (byte) 0);
		this.breaksTimeInputs[stretch_breaks_idx].setValues((byte) 0, (byte) 30, (byte) 0);
		if (!this.featureEnabledCheckBoxes[stretch_breaks_idx].isSelected())
			this.featureEnabledCheckBoxes[stretch_breaks_idx].doClick();

		byte day_break_idx = BreakType.DAY_BREAK.getIndex();
		this.workingTimeInputs[day_break_idx].setValues((byte) 8, (byte) 0, (byte) 0);
		this.workingTimeInputs[day_break_idx].setValues((byte) 8, (byte) 0, (byte) 0);
		if (!this.featureEnabledCheckBoxes[day_break_idx].isSelected())
			this.featureEnabledCheckBoxes[day_break_idx].doClick();

		for (TimeInputPanel postponeTimeInput : this.postponeTimeInputs)
			postponeTimeInput.setValues(
				this.minRequiredPostponeTime.getHours(),
				this.minRequiredPostponeTime.getMinutes(),
				this.minRequiredPostponeTime.getSeconds()
			);

		this.showOnSaveMessage(SWMain.messagesBundle.getString("recommended_values_were_set"), Color.WHITE);
	}

	/**
	 * Class to enable or disable time inputs when checkbox is clicked
	 */
	private static class OnActionCheckBoxListener implements ActionListener
	{
		private final TimeInputPanel workingTimeInputPanel;
		private final TimeInputPanel breakTimeInputPanel;
		private final TimeInputPanel postponeTimeInputPanel;
		private boolean is_enabled;

		public OnActionCheckBoxListener(final TimeInputPanel workingTimeInputPanel,
						final TimeInputPanel breakTimeInputPanel,
						final TimeInputPanel postponeTimeInputPanel,
						final boolean is_enabled
		)
		{
			this.workingTimeInputPanel = workingTimeInputPanel;
			this.breakTimeInputPanel = breakTimeInputPanel;
			this.postponeTimeInputPanel = postponeTimeInputPanel;
			this.is_enabled = is_enabled;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			this.is_enabled = !this.is_enabled;

			this.workingTimeInputPanel.clearWarning();
			//this.workingTimeInputPanel.checkInputWarnings();
			this.workingTimeInputPanel.setEnabled(this.is_enabled);

			if (this.breakTimeInputPanel != null) {
				this.breakTimeInputPanel.clearWarning();
				//this.workingTimeInputPanel.checkInputWarnings();
				this.breakTimeInputPanel.setEnabled(this.is_enabled);
			}

			this.postponeTimeInputPanel.clearWarning();
			this.postponeTimeInputPanel.setEnabled(this.is_enabled);
		}
	}
}
