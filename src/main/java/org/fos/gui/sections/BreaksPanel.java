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
package org.fos.gui.sections;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.ResourceBundle;
import javax.management.InstanceAlreadyExistsException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.fos.SWMain;
import org.fos.core.BreakType;
import org.fos.core.TimersManager;
import org.fos.gui.Colors;
import org.fos.gui.Fonts;
import org.fos.gui.hooksconfig.ConfigureHooksDialog;
import org.fos.gui.util.TimeInputPanel;
import org.fos.timers.BreakConfig;
import org.fos.timers.Clock;

public class BreaksPanel extends JScrollPane
{
	private static boolean instantiated;

	private final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 24);
	private final Font FULL_DESCRIPTION_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
	private final Font DESCRIPTION_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);

	private final List<BreakConfig> preferredBreakSettings;
	private final Clock[] minWorkRecommendedTimes = new Clock[]{
		new Clock((byte) 0, (byte) 5, (byte) 0), // min work time for the small break
		new Clock((byte) 0, (byte) 30, (byte) 0), // min work time for the stretch break
		new Clock((byte) 8, (byte) 0, (byte) 0), // min work time for the day break
	};
	private final Clock[] maxWorkRecommendedTimes = new Clock[]{
		new Clock((byte) 0, (byte) 30, (byte) 0), // max work time for the small break
		new Clock((byte) 2, (byte) 0, (byte) 0), // max work time for the stretch break
		new Clock((byte) 12, (byte) 0, (byte) 0), // max work time for the day break
	};
	private final Clock[] minBreakRecommendedTimes = new Clock[]{
		new Clock((byte) 0, (byte) 0, (byte) 10), // min break time for the small break
		new Clock((byte) 0, (byte) 10, (byte) 0), // min break time for the stretch break
	};
	private final Clock[] maxBreakRecommendedTimes = new Clock[]{
		new Clock((byte) 0, (byte) 5, (byte) 0), // max break time for the small break
		new Clock((byte) 1, (byte) 0, (byte) 0), // max break time for the stretch break
	};
	private final Clock minRequiredPostponeTime = new Clock((byte) 0, (byte) 0, (byte) 6);
	private final Clock maxRequiredPostponeTime = new Clock((byte) 0, (byte) 30, (byte) 0);

	// configuration stuff
	private JComboBox<String> notificationLocationCombobox;

	public BreaksPanel() throws InstanceAlreadyExistsException
	{
		super();

		if (BreaksPanel.instantiated)
			throw new InstanceAlreadyExistsException("There must exist a single instance of " + BreaksPanel.class.getName());

		BreaksPanel.instantiated = true;

		// load preferred times
		this.preferredBreakSettings = TimersManager.loadBreaksSettings();

		// load icon images that will be used by the createBreakPanel method
		ImageIcon hooksConfigIcon = SWMain.readAndScaleIcon("/resources/media/task_white_18dp.png");
		ImageIcon saveConfigIcon = SWMain.readAndScaleIcon("/resources/media/save_white_18dp.png");

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createBreakPanel(
			BreakType.SMALL_BREAK,
			hooksConfigIcon,
			saveConfigIcon,
			new Clock((byte) 0, (byte) 10, (byte) 0), // recommended value for working time
			new Clock((byte) 0, (byte) 0, (byte) 10), // recommended value for break time
			new Clock((byte) 0, (byte) 0, (byte) 10) // recommended value for postpone time
		));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createBreakPanel(
			BreakType.STRETCH_BREAK,
			hooksConfigIcon,
			saveConfigIcon,
			new Clock((byte) 2, (byte) 0, (byte) 0), // recommended value for working time
			new Clock((byte) 0, (byte) 30, (byte) 0), // recommended value for break time
			new Clock((byte) 0, (byte) 0, (byte) 10) // recommended value for postpone time)
		));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createBreakPanel(
			BreakType.DAY_BREAK,
			hooksConfigIcon,
			saveConfigIcon,
			new Clock((byte) 8, (byte) 0, (byte) 0), // recommended value for working time
			null, // recommended value for break time
			new Clock((byte) 0, (byte) 0, (byte) 10) // recommended value for postpone time)
		));
		panel.add(Box.createVerticalStrut(10));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createOptionsPanel());
		panel.add(Box.createVerticalStrut(10));

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
	 * @param breakType       the break index, this is important to avoid writing so much code
	 * @param hooksConfigIcon the icon to display in the hooks configuration button
	 * @param saveConfigIcon  the icon to display in the save configurations button
	 * @return the JPanel containing all the elements mentioned above
	 */
	private JPanel createBreakPanel(final BreakType breakType, final ImageIcon hooksConfigIcon,
	                                final ImageIcon saveConfigIcon, final Clock recommendedWorkingTime,
	                                final Clock recommendedBreakTime, final Clock recommendedPostponeTime)
	{
		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		String breakPrefix = breakType.getMessagesPrefix();
		byte break_idx = breakType.getIndex();

		String breakTitle = messagesBundle.getString(breakPrefix + "_title");
		String breakFullDescription = messagesBundle.getString(breakPrefix + "_full_description");
		String breakDescription = messagesBundle.getString(breakPrefix + "_description");
		JPanel panel = new JPanel(new GridBagLayout());

		/*
		First row:
			Title	small description
		 */
		JLabel breakTitleLabel = new JLabel(breakTitle, JLabel.CENTER);
		JLabel breakDescLabel = new JLabel(breakDescription, JLabel.CENTER);

		breakTitleLabel.setFont(this.TITLE_FONT);
		breakDescLabel.setFont(this.DESCRIPTION_FONT);

		/*
		Second row:
			checkbox	full description
		 */
		JCheckBox featureEnabledCheckBox = new JCheckBox(messagesBundle.getString("feature_enabled"));
		featureEnabledCheckBox.setSelected(this.preferredBreakSettings.get(break_idx).isEnabled());

		JLabel breakFullDescLabel = new JLabel(breakFullDescription, JLabel.CENTER);
		breakFullDescLabel.setFont(this.FULL_DESCRIPTION_FONT);

		/*
		Third row:
			hooks config	working time input
		 */
		JLabel workingTimeLabel = new JLabel(
			messagesBundle.getString(breakPrefix + "_working_time_label"),
			SwingConstants.RIGHT
		);
		TimeInputPanel workingTimeInput = new TimeInputPanel(
			this.minWorkRecommendedTimes[break_idx],
			this.maxWorkRecommendedTimes[break_idx],
			this.preferredBreakSettings.get(break_idx).getWorkTimerSettings()
		);
		workingTimeInput.setEnabled(this.preferredBreakSettings.get(break_idx).isEnabled());

		JButton hooksConfigBtn = new JButton(messagesBundle.getString("hooks_config"));
		hooksConfigBtn.setToolTipText(SWMain.getMessagesBundle().getString("hooks_config_tooltip"));
		hooksConfigBtn.setIcon(hooksConfigIcon);

		/*
		Fourth row:
			Set default values	Break time input

		If it is day break the time input will not be added
		 */
		JLabel breakTimeLabel = null;
		JLabel postponeTimeLabel;
		JButton setRecommendedValuesBtn = new JButton(messagesBundle.getString("set_recommended_values"));
		TimeInputPanel breakTimeInput = null;
		if (breakType != BreakType.DAY_BREAK) { // the day break doesn't have break time inputs, only working time inputs
			breakTimeLabel = new JLabel(
				messagesBundle.getString("break_time_label"),
				SwingConstants.RIGHT
			);
			breakTimeInput = new TimeInputPanel(
				this.minBreakRecommendedTimes[break_idx],
				this.maxBreakRecommendedTimes[break_idx],
				this.preferredBreakSettings.get(break_idx).getBreakTimerSettings()
			);
			breakTimeInput.setEnabled(this.preferredBreakSettings.get(break_idx).isEnabled());
		}

		/*
		Fifth row:
			Save configuration	Postpone time input
		 */
		JButton saveConfigBtn = new JButton(messagesBundle.getString("save_changes"));
		saveConfigBtn.setIcon(saveConfigIcon);
		saveConfigBtn.setFont(Fonts.SANS_SERIF_BOLD_15);

		postponeTimeLabel = new JLabel(
			messagesBundle.getString("postpone_time_label"),
			SwingConstants.RIGHT
		);
		TimeInputPanel postponeTimeInput = new TimeInputPanel(
			this.minRequiredPostponeTime,
			this.maxRequiredPostponeTime,
			this.preferredBreakSettings.get(break_idx).getPostponeTimerSettings(),
			true
		);
		postponeTimeInput.setEnabled(this.preferredBreakSettings.get(break_idx).isEnabled());

		/*
		Sixth row:
			Status label (expanding through all the columns)
		 */
		JLabel statusLabel = new JLabel(" "); // put a space so the layout manager displays the label instead
		// of nothing
		statusLabel.setFont(Fonts.SANS_SERIF_BOLD_12);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;

		/*
		Add first row
			title	small description
		 */
		panel.add(breakTitleLabel, gridBagConstraints);

		gridBagConstraints.gridwidth = 4;
		++gridBagConstraints.gridx;
		panel.add(breakDescLabel, gridBagConstraints);

		/*
		Add second row
			checkbox	description
		 */
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		panel.add(featureEnabledCheckBox, gridBagConstraints);

		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.gridx = 1;
		panel.add(breakFullDescLabel, gridBagConstraints);

		/*
		Add third row
			hooks config	working time input
		 */
		gridBagConstraints.gridy = 3;

		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(hooksConfigBtn, gridBagConstraints);

		++gridBagConstraints.gridx;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		panel.add(workingTimeLabel, gridBagConstraints);

		gridBagConstraints.gridwidth = 3;
		++gridBagConstraints.gridx;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		panel.add(workingTimeInput, gridBagConstraints);

		/*
		Add fourth row:
			Set recommended vals	break time input

		ONLY if it is not a day break
		 */
		gridBagConstraints.gridx = 0;
		++gridBagConstraints.gridy;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(setRecommendedValuesBtn, gridBagConstraints);
		if (breakType != BreakType.DAY_BREAK) {
			gridBagConstraints.gridx = 1;
			gridBagConstraints.anchor = GridBagConstraints.CENTER;
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			panel.add(breakTimeLabel, gridBagConstraints);

			gridBagConstraints.gridwidth = 3;
			++gridBagConstraints.gridx;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			panel.add(breakTimeInput, gridBagConstraints);
		}

		/*
		Add fifth row:
			save changes	postpone time
		 */
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = 1;
		++gridBagConstraints.gridy;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(saveConfigBtn, gridBagConstraints);

		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		panel.add(postponeTimeLabel, gridBagConstraints);

		gridBagConstraints.gridwidth = 3;
		++gridBagConstraints.gridx;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		panel.add(postponeTimeInput, gridBagConstraints);

		/*
		Add sixth row:
			Status label (expanding through all the columns)
		 */
		++gridBagConstraints.gridy;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(statusLabel, gridBagConstraints);

		/*
		Add listeners to buttons
		 */
		FocusListener onFocusLostClearLabel = new OnFocusLostClearLabel(statusLabel);
		featureEnabledCheckBox.addActionListener(new OnActionCheckBoxListener(
			workingTimeInput,
			breakTimeInput,
			postponeTimeInput,
			featureEnabledCheckBox.isSelected(),
			breakType,
			statusLabel,
			hooksConfigBtn,
			setRecommendedValuesBtn,
			saveConfigBtn
		));
		featureEnabledCheckBox.addFocusListener(onFocusLostClearLabel);

		saveConfigBtn.addActionListener(new OnSaveSettingsListener(
			workingTimeInput,
			breakType != BreakType.DAY_BREAK ? breakTimeInput : null,
			postponeTimeInput,
			breakType,
			statusLabel
		));
		saveConfigBtn.addFocusListener(onFocusLostClearLabel);

		setRecommendedValuesBtn.addActionListener(new OnSetRecommendedValues(
			workingTimeInput,
			breakType != BreakType.DAY_BREAK ? breakTimeInput : null,
			postponeTimeInput,
			statusLabel,
			recommendedWorkingTime,
			recommendedBreakTime,
			recommendedPostponeTime
		));
		setRecommendedValuesBtn.addFocusListener(onFocusLostClearLabel);

		hooksConfigBtn.addActionListener(new OnClickHooksSettings(
			SwingUtilities.getWindowAncestor(this),
			breakType
		));

		return panel;
	}

	/**
	 * This will create the options panel
	 * currently the panel contains only the notification location option
	 *
	 * @return the panel containing all elements for configuration
	 */
	private JPanel createOptionsPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		JLabel locationLabel = new JLabel(messagesBundle.getString("notification_location"));

		String[] locationOptions = new String[]{
			messagesBundle.getString("notification_location_bottom_right"),
			messagesBundle.getString("notification_location_bottom_left"),
			messagesBundle.getString("notification_location_top_right"),
			messagesBundle.getString("notification_location_top_left")
		};
		this.notificationLocationCombobox = new JComboBox<>(locationOptions);
		this.notificationLocationCombobox.setSelectedIndex(
			TimersManager.getNotificationPrefLocation(true).getLocationIdx()
		);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.ipadx = 5;
		gridBagConstraints.ipady = 5;

		panel.add(locationLabel, gridBagConstraints);

		gridBagConstraints.gridx = 1;
		panel.add(this.notificationLocationCombobox, gridBagConstraints);

		// set the listener to automatically save config on changes
		this.notificationLocationCombobox.addActionListener((ActionEvent evt) ->
			TimersManager.saveNotificationPrefLocation(
				(byte) this.notificationLocationCombobox.getSelectedIndex()
			)
		);

		return panel;
	}

	/**
	 * Class to enable or disable time inputs and buttons when checkbox is clicked
	 */
	private static class OnActionCheckBoxListener implements ActionListener
	{
		private final TimeInputPanel workingTimeInputPanel;
		private final TimeInputPanel breakTimeInputPanel;
		private final TimeInputPanel postponeTimeInputPanel;
		private final BreakType breakType;
		private final JLabel statusLabel;
		private final JButton[] relatedButtons;
		private boolean is_enabled;

		/**
		 * Constructs a new object capable of handling checkbox click events as it implements the method
		 * {@link #actionPerformed(ActionEvent)}
		 *
		 * @param workingTimeInputPanel  the working time input to enable/disable
		 * @param breakTimeInputPanel    the break time input to enable/disable
		 * @param postponeTimeInputPanel the postpone time input to enable/disable
		 * @param is_enabled             this is used to set the initial state for the checkbox
		 * @param breakType              the break type associated to the checkbox, this will be used to
		 *                               identify which break to enable/disable in the
		 *                               preferences with
		 *                               {@link TimersManager#setBreakEnabled(BreakType, boolean)}
		 * @param relatedButtons         the list of buttons that must be enabled/disabled if this checkbox is
		 *                               enabled/disabled
		 */
		public OnActionCheckBoxListener(
			final TimeInputPanel workingTimeInputPanel,
			final TimeInputPanel breakTimeInputPanel,
			final TimeInputPanel postponeTimeInputPanel,
			final boolean is_enabled,
			final BreakType breakType,
			JLabel statusLabel,
			JButton... relatedButtons
		)
		{
			this.workingTimeInputPanel = workingTimeInputPanel;
			this.breakTimeInputPanel = breakTimeInputPanel;
			this.postponeTimeInputPanel = postponeTimeInputPanel;
			this.is_enabled = is_enabled;
			this.relatedButtons = relatedButtons;
			this.breakType = breakType;
			this.statusLabel = statusLabel;

			for (JButton btn : relatedButtons)
				btn.setEnabled(this.is_enabled);
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

			for (JButton btn : relatedButtons)
				btn.setEnabled(this.is_enabled);

			// if the user disables the checkbox, save that preference and stop the timer
			if (!this.is_enabled)
				TimersManager.setBreakEnabled(this.breakType, false);

			this.statusLabel.setText(SWMain.getMessagesBundle().getString(
				this.is_enabled
					? "break_successfully_enabled"
					: "break_successfully_disabled"
			));
		}
	}

	private static class OnSaveSettingsListener implements ActionListener
	{
		private final BreakConfig.Builder breakSettingsBuilder;

		private final TimeInputPanel workingTimeInput;
		private final TimeInputPanel breakTimeInput;
		private final TimeInputPanel postponeTimeInput;

		private final JLabel statusLabel;

		public OnSaveSettingsListener(
			final TimeInputPanel workingTimeInput,
			final TimeInputPanel breakTimeInput,
			final TimeInputPanel postponeTimeInput,
			final BreakType breakType,
			final JLabel statusLabel
		)
		{
			this.workingTimeInput = workingTimeInput;
			this.breakTimeInput = breakTimeInput;
			this.postponeTimeInput = postponeTimeInput;
			this.statusLabel = statusLabel;

			this.breakSettingsBuilder = new BreakConfig.Builder().breakType(breakType);

		}

		/**
		 * Invoked when an action occurs.
		 *
		 * @param e the event to be processed
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// clear all warnings
			this.workingTimeInput.clearWarning();
			if (this.breakTimeInput != null) this.breakTimeInput.clearWarning();
			this.postponeTimeInput.clearWarning();

			// first check if everything is valid
			boolean is_valid = this.workingTimeInput.checkInputValidity()
				& (this.breakTimeInput == null || this.breakTimeInput.checkInputValidity())
				& this.postponeTimeInput.checkInputValidity();

			if (!is_valid)
				return;

			// if everything went OK
			this.statusLabel.setText(SWMain.getMessagesBundle().getString("changes_saved")
				+ ". " + SWMain.getMessagesBundle().getString("changes_saved_extra_text"));
			this.statusLabel.setForeground(Colors.GREEN);

			if (this.breakTimeInput != null)
				this.breakSettingsBuilder.breakTimerSettings(Clock.from(this.breakTimeInput.getTime()));

			// save the nw break settings and reload the break
			BreakConfig newBreakConfig = this.breakSettingsBuilder
				.workTimerSettings(Clock.from(this.workingTimeInput.getTime()))
				.postponeTimerSettings(Clock.from(this.postponeTimeInput.getTime()))
				// TODO: add config for hooks
				.createBreakSettings();

			TimersManager.saveBreakSettings(newBreakConfig);
		}
	}

	private static class OnSetRecommendedValues implements ActionListener
	{
		private final TimeInputPanel workingTimeInput;
		private final TimeInputPanel breakTimeInput;
		private final TimeInputPanel postponeTimeInput;

		private final Clock workingRecommendedValues;
		private final Clock breakRecommendedValues;
		private final Clock postponeRecommendedValues;
		private final JLabel statusLabel;

		public OnSetRecommendedValues(
			final TimeInputPanel workingTimeInput,
			final TimeInputPanel breakTimeInput,
			final TimeInputPanel postponeTimeInput,
			final JLabel statusLabel,
			final Clock recommendedWorkingTime,
			final Clock recommendedBreakTime,
			final Clock recommendedPostponeTime
		)
		{
			this.workingTimeInput = workingTimeInput;
			this.breakTimeInput = breakTimeInput;
			this.postponeTimeInput = postponeTimeInput;
			this.statusLabel = statusLabel;

			this.workingRecommendedValues = recommendedWorkingTime;
			this.breakRecommendedValues = recommendedBreakTime;
			this.postponeRecommendedValues = recommendedPostponeTime;
		}

		/**
		 * Invoked when an action occurs.
		 *
		 * @param e the event to be processed
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// clear all warnings
			this.workingTimeInput.clearWarning();
			if (this.breakTimeInput != null) this.breakTimeInput.clearWarning();
			this.postponeTimeInput.clearWarning();

			// set recommended values
			this.workingTimeInput.setValues(this.workingRecommendedValues);
			if (this.breakTimeInput != null) this.breakTimeInput.setValues(this.breakRecommendedValues);
			this.postponeTimeInput.setValues(this.postponeRecommendedValues);

			// notify the user
			this.statusLabel.setText(SWMain.getMessagesBundle().getString("recommended_values_were_set"));
			this.statusLabel.setForeground(Colors.WHITE);
		}
	}

	private static class OnFocusLostClearLabel extends FocusAdapter
	{
		private final JLabel label;

		public OnFocusLostClearLabel(final JLabel label)
		{
			this.label = label;
		}

		/**
		 * Invoked when a component loses the keyboard focus.
		 *
		 * @param e the focus event to be processed
		 */
		@Override
		public void focusLost(FocusEvent e)
		{
			super.focusLost(e);
			this.label.setText(" "); // write a space so the JLabel doesn't disappear from the layout
		}
	}

	private static class OnClickHooksSettings implements ActionListener
	{
		private final Window parentDialogWindow;
		private final BreakType breakType;

		public OnClickHooksSettings(Window parentDialogWindow, BreakType breakType)
		{
			this.parentDialogWindow = parentDialogWindow;
			this.breakType = breakType;
		}

		/**
		 * Invoked when an action occurs.
		 *
		 * @param e the event to be processed
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			new ConfigureHooksDialog(this.parentDialogWindow, this.breakType).initComponents();
		}
	}
}
