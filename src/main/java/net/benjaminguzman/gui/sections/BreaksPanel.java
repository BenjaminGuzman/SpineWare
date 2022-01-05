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
package net.benjaminguzman.gui.sections;

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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import net.benjaminguzman.core.Loggers;
import net.benjaminguzman.core.NotificationLocation;
import net.benjaminguzman.gui.hooksconfig.AbstractHooksConfigDialog;
import net.benjaminguzman.gui.hooksconfig.ActiveHoursHooksConfigDialog;
import net.benjaminguzman.gui.hooksconfig.BreakHooksConfigDialog;
import net.benjaminguzman.hooks.BreakHooks;
import net.benjaminguzman.SWMain;
import net.benjaminguzman.gui.Colors;
import net.benjaminguzman.gui.Fonts;
import net.benjaminguzman.gui.util.NotificationLocationComponent;
import net.benjaminguzman.gui.util.TimeInputComponent;
import net.benjaminguzman.prefs.NotificationPrefsIO;
import net.benjaminguzman.prefs.timers.HooksPrefsIO;
import net.benjaminguzman.prefs.timers.TimersPrefsIO;
import net.benjaminguzman.timers.TimersManager;
import net.benjaminguzman.timers.WallClock;
import net.benjaminguzman.timers.breaks.ActiveHours;
import net.benjaminguzman.timers.breaks.BreakConfig;
import net.benjaminguzman.timers.breaks.BreakType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BreaksPanel extends AbstractSection
{
	private final List<BreakConfig> preferredBreakSettings;
	@NotNull
	private final ActiveHours preferredActiveHours;
	private final WallClock[] minWorkRecommendedTimes = new WallClock[]{
		new WallClock((byte) 0, (byte) 15, (byte) 0), // min work time for the small break
		new WallClock((byte) 0, (byte) 30, (byte) 0), // min work time for the stretch break
		new WallClock((byte) 8, (byte) 0, (byte) 0), // min work time for the day break
	};
	private final WallClock[] maxWorkRecommendedTimes = new WallClock[]{
		new WallClock((byte) 0, (byte) 30, (byte) 0), // max work time for the small break
		new WallClock((byte) 2, (byte) 0, (byte) 0), // max work time for the stretch break
		new WallClock((byte) 12, (byte) 0, (byte) 0), // max work time for the day break
	};
	private final WallClock[] minBreakRecommendedTimes = new WallClock[]{
		new WallClock((byte) 0, (byte) 0, (byte) 10), // min break time for the small break
		new WallClock((byte) 0, (byte) 10, (byte) 0), // min break time for the stretch break
	};
	private final WallClock[] maxBreakRecommendedTimes = new WallClock[]{
		new WallClock((byte) 0, (byte) 5, (byte) 0), // max break time for the small break
		new WallClock((byte) 1, (byte) 0, (byte) 0), // max break time for the stretch break
	};
	private final WallClock minRequiredPostponeTime = new WallClock((byte) 0, (byte) 0, (byte) 6);
	private final WallClock maxRequiredPostponeTime = new WallClock((byte) 0, (byte) 30, (byte) 0);

	public BreaksPanel()
	{
		super();

		// load preferred configurations
		TimersPrefsIO prefsIO = TimersManager.getPrefsIO();
		this.preferredBreakSettings = prefsIO.loadBreaksConfig();
		Optional<ActiveHours> prefActiveHours;
		if ((prefActiveHours = prefsIO.getActiveHoursPrefsIO()
		                              .loadActiveHours(prefsIO.getHooksPrefsIO())).isPresent())
			preferredActiveHours = prefActiveHours.get();
		else
			preferredActiveHours = new ActiveHours(WallClock.from(0), WallClock.from(0), false);
	}

	@Override
	public void initComponents()
	{
		// load icon images that will be used by the createBreakPanel method
		ImageIcon hooksConfigIcon = SWMain.readAndScaleIcon("/resources/media/task_white_18dp.png");
		ImageIcon saveConfigIcon = SWMain.readAndScaleIcon("/resources/media/save_white_18dp.png");

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(Box.createVerticalStrut(10));
		panel.add(createBreakPanel(
			BreakType.SMALL_BREAK,
			hooksConfigIcon,
			saveConfigIcon,
			new WallClock((byte) 0, (byte) 15, (byte) 0), // recommended value for working time
			new WallClock((byte) 0, (byte) 0, (byte) 10), // recommended value for break time
			new WallClock((byte) 0, (byte) 5, (byte) 0) // recommended value for postpone time
		));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(createBreakPanel(
			BreakType.STRETCH_BREAK,
			hooksConfigIcon,
			saveConfigIcon,
			new WallClock((byte) 2, (byte) 0, (byte) 0), // recommended value for working time
			new WallClock((byte) 0, (byte) 30, (byte) 0), // recommended value for break time
			new WallClock((byte) 0, (byte) 15, (byte) 0) // recommended value for postpone time
		));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(createBreakPanel(
			BreakType.DAY_BREAK,
			hooksConfigIcon,
			saveConfigIcon,
			new WallClock((byte) 8, (byte) 0, (byte) 0), // recommended value for working time
			null, // recommended value for break time
			new WallClock((byte) 0, (byte) 20, (byte) 0) // recommended value for postpone time
		));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(createActiveHoursPanel(hooksConfigIcon, saveConfigIcon));
		panel.add(Box.createVerticalStrut(10));

		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(Box.createVerticalStrut(10));
		panel.add(this.createOptionsPanel());
		panel.add(Box.createVerticalStrut(10));

		this.setViewportView(panel);
		this.configScrollBar();
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
	private JPanel createBreakPanel(
		BreakType breakType,
		ImageIcon hooksConfigIcon,
		ImageIcon saveConfigIcon,
		WallClock recommendedWorkingTime,
		WallClock recommendedBreakTime,
		WallClock recommendedPostponeTime
	)
	{
		ResourceBundle messagesBundle = SWMain.messagesBundle;

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

		breakTitleLabel.setFont(Fonts.TITLE_FONT);
		breakDescLabel.setFont(Fonts.DESCRIPTION_FONT);

		/*
		Second row:
			checkbox	full description
		 */
		JCheckBox featureEnabledCheckBox = new JCheckBox(messagesBundle.getString("feature_enabled"));
		featureEnabledCheckBox.setSelected(this.preferredBreakSettings.get(break_idx).isEnabled());

		JLabel breakFullDescLabel = new JLabel(breakFullDescription, JLabel.CENTER);
		breakFullDescLabel.setFont(Fonts.FULL_DESCRIPTION_FONT);

		/*
		Third row:
			hooks config	working time input
		 */
		JLabel workingTimeLabel = new JLabel(
			messagesBundle.getString(breakPrefix + "_working_time_label"),
			SwingConstants.RIGHT
		);
		TimeInputComponent workingTimeInput = new TimeInputComponent(
			this.minWorkRecommendedTimes[break_idx],
			this.maxWorkRecommendedTimes[break_idx],
			this.preferredBreakSettings.get(break_idx).getWorkWallClock()
		);
		workingTimeInput.setEnabled(this.preferredBreakSettings.get(break_idx).isEnabled());

		JButton hooksConfigBtn = new JButton(messagesBundle.getString("hooks_config"));
		hooksConfigBtn.setToolTipText(SWMain.messagesBundle.getString("hooks_config_tooltip"));
		hooksConfigBtn.setIcon(hooksConfigIcon);

		/*
		Fourth row:
			Set default values	Break time input

		If it is day break the time input will not be added
		 */
		JLabel breakTimeLabel = null;
		JLabel postponeTimeLabel;
		JButton setRecommendedValuesBtn = new JButton(messagesBundle.getString("set_recommended_values"));
		TimeInputComponent breakTimeInput = null;
		if (breakType != BreakType.DAY_BREAK) { // the day break doesn't have break time inputs, only working time inputs
			breakTimeLabel = new JLabel(
				messagesBundle.getString("break_time_label"),
				SwingConstants.RIGHT
			);
			breakTimeInput = new TimeInputComponent(
				this.minBreakRecommendedTimes[break_idx],
				this.maxBreakRecommendedTimes[break_idx],
				this.preferredBreakSettings.get(break_idx).getBreakWallClock().get()
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
		saveConfigBtn.setToolTipText(messagesBundle.getString("save_changes_timers_warning"));

		postponeTimeLabel = new JLabel(
			messagesBundle.getString("postpone_time_label"),
			SwingConstants.RIGHT
		);
		TimeInputComponent postponeTimeInput = new TimeInputComponent(
			this.minRequiredPostponeTime,
			this.maxRequiredPostponeTime,
			this.preferredBreakSettings.get(break_idx).getPostponeWallClock(),
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

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 0;
		gbc.gridx = 0;

		/*
		Add first row
			title	small description
		 */
		panel.add(breakTitleLabel, gbc);

		gbc.gridwidth = 4;
		++gbc.gridx;
		panel.add(breakDescLabel, gbc);

		/*
		Add second row
			checkbox	description
		 */
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(featureEnabledCheckBox, gbc);

		gbc.gridwidth = 4;
		gbc.gridx = 1;
		panel.add(breakFullDescLabel, gbc);

		/*
		Add third row
			hooks config	working time input
		 */
		gbc.gridy = 3;

		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(hooksConfigBtn, gbc);

		++gbc.gridx;
		addTimeInputAndLabel2Panel(panel, workingTimeLabel, workingTimeInput, gbc);

		/*
		Add fourth row:
			Set recommended vals	break time input

		ONLY if it is not a day break
		 */
		gbc.gridx = 0;
		++gbc.gridy;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(setRecommendedValuesBtn, gbc);
		if (breakType != BreakType.DAY_BREAK) {
			++gbc.gridx;
			addTimeInputAndLabel2Panel(panel, breakTimeLabel, breakTimeInput, gbc);
		}

		/*
		Add fifth row:
			save changes	postpone time
		 */
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		++gbc.gridy;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(saveConfigBtn, gbc);

		++gbc.gridx;
		addTimeInputAndLabel2Panel(panel, postponeTimeLabel, postponeTimeInput, gbc);

		/*
		Add sixth row:
			Status label (expanding through all the columns)
		 */
		++gbc.gridy;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(statusLabel, gbc);

		/*
		Add listeners to buttons
		 */
		FocusListener onFocusLostClearLabel = new OnFocusLostClearLabel(statusLabel);
		featureEnabledCheckBox.addActionListener(new OnActionCheckBoxListener(
			breakType == BreakType.DAY_BREAK
				? new TimeInputComponent[]{workingTimeInput, postponeTimeInput}
				: new TimeInputComponent[]{workingTimeInput, breakTimeInput, postponeTimeInput},
			featureEnabledCheckBox.isSelected(),
			breakType,
			statusLabel,
			saveConfigBtn,
			hooksConfigBtn,
			setRecommendedValuesBtn
		));
		featureEnabledCheckBox.addFocusListener(onFocusLostClearLabel);

		saveConfigBtn.addActionListener(new OnSaveConfigListener(
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

		OnClickHooksSettings onClickHooksSettings = new OnClickHooksSettings(super.owner, breakType);
		hooksConfigBtn.addActionListener((ActionEvent evt) -> {
			// show the modal dialog, this call will block
			onClickHooksSettings.actionPerformed(evt);

			if (onClickHooksSettings.shouldSaveChanges())
				// save the new configuration
				saveConfigBtn.doClick(60);
		});

		return panel;
	}

	/**
	 * Creates the active hours panel
	 *
	 * @return the JPanel
	 */
	private JPanel createActiveHoursPanel(
		ImageIcon hooksConfigIcon,
		ImageIcon saveConfigIcon
	)
	{
		JPanel panel = new JPanel(new GridBagLayout());

		ResourceBundle messagesBundle = SWMain.messagesBundle;

		String activeHoursTitle = messagesBundle.getString("active_hours_title");
		String activeHoursDesc = messagesBundle.getString("active_hours_description");
		String activeHoursFullDesc = messagesBundle.getString("active_hours_full_description");

		/*
		First row:
			Title	small description
		 */
		JLabel titleLabel = new JLabel(activeHoursTitle, JLabel.CENTER);
		JLabel descLabel = new JLabel(activeHoursDesc, JLabel.CENTER);

		titleLabel.setFont(Fonts.TITLE_FONT);
		descLabel.setFont(Fonts.DESCRIPTION_FONT);

		/*
		Second row:
			checkbox	full description
		 */
		JCheckBox featureEnabledCheckBox = new JCheckBox(messagesBundle.getString("feature_enabled"));
		featureEnabledCheckBox.setSelected(preferredActiveHours.isEnabled());

		JLabel fullDescLabel = new JLabel(activeHoursFullDesc, JLabel.CENTER);
		fullDescLabel.setFont(Fonts.FULL_DESCRIPTION_FONT);

		/*
		Third row:
			hooks config	start active hours at time input
		 */
		JLabel startTimeLabel = new JLabel(
			messagesBundle.getString("active_hours_start_label"),
			SwingConstants.RIGHT
		);
		TimeInputComponent startTimeInput = new TimeInputComponent(
			new WallClock((byte) 5, (byte) 0, (byte) 0),
			new WallClock((byte) 13, (byte) 0, (byte) 0),
			preferredActiveHours.getStart()
		);
		startTimeInput.setEnabled(preferredActiveHours.isEnabled());

		JButton hooksConfigBtn = new JButton(messagesBundle.getString("hooks_config"));
		hooksConfigBtn.setToolTipText(SWMain.messagesBundle.getString("hooks_config_tooltip"));
		hooksConfigBtn.setIcon(hooksConfigIcon);

		/*
		Fifth row:
			Save configuration	end active hours at time input
		 */
		JButton saveConfigBtn = new JButton(messagesBundle.getString("save_changes"));
		saveConfigBtn.setIcon(saveConfigIcon);
		saveConfigBtn.setFont(Fonts.SANS_SERIF_BOLD_15);
		saveConfigBtn.setToolTipText(messagesBundle.getString("save_changes_timers_warning"));

		JLabel endTimeLabel = new JLabel(
			messagesBundle.getString("active_hours_end_label"),
			SwingConstants.RIGHT
		);
		TimeInputComponent endTimeInput = new TimeInputComponent(
			new WallClock((byte) 18, (byte) 0, (byte) 0),
			new WallClock((byte) 23, (byte) 0, (byte) 0),
			preferredActiveHours.getEnd()
		);
		endTimeInput.setEnabled(preferredActiveHours.isEnabled());

		/*
		Sixth row:
			Status label (expanding through all the columns)
		 */
		JLabel statusLabel = new JLabel(" "); // put a space so the layout manager displays the label instead
		// of nothing
		statusLabel.setFont(Fonts.SANS_SERIF_BOLD_12);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 0;
		gbc.gridx = 0;

		/*
		Add first row
			title	small description
		 */
		panel.add(titleLabel, gbc);

		gbc.gridwidth = 4;
		++gbc.gridx;
		panel.add(descLabel, gbc);

		/*
		Add second row
			checkbox	description
		 */
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		++gbc.gridy;
		panel.add(featureEnabledCheckBox, gbc);

		gbc.gridwidth = 4;
		gbc.gridx = 1;
		panel.add(fullDescLabel, gbc);

		/*
		Add third row
			hooks config	working time input
		 */
		++gbc.gridy;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(hooksConfigBtn, gbc);

		++gbc.gridx;
		addTimeInputAndLabel2Panel(panel, startTimeLabel, startTimeInput, gbc);

		/*
		Add fifth row:
			save changes	end active hours time input
		 */
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		++gbc.gridy;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(saveConfigBtn, gbc);

		++gbc.gridx;
		addTimeInputAndLabel2Panel(panel, endTimeLabel, endTimeInput, gbc);

		/*
		Add sixth row:
			Status label (expanding through all the columns)
		 */
		++gbc.gridy;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(statusLabel, gbc);

		/*
		Add listeners to buttons
		 */
		FocusListener onFocusLostClearLabel = new OnFocusLostClearLabel(statusLabel);
		featureEnabledCheckBox.addActionListener(new OnActionCheckBoxListener(
			new TimeInputComponent[]{startTimeInput, endTimeInput},
			featureEnabledCheckBox.isSelected(),
			null,
			statusLabel,
			saveConfigBtn,
			hooksConfigBtn
		));
		featureEnabledCheckBox.addFocusListener(onFocusLostClearLabel);

		saveConfigBtn.addActionListener((ActionEvent evt) -> {
			int start_time = startTimeInput.getTime();
			int end_time = endTimeInput.getTime();

			if (end_time <= start_time) {
				statusLabel.setForeground(Colors.RED);
				statusLabel.setText(SWMain.messagesBundle.getString("active_hours_time_invalid"));
				return;
			}

			try {
				HooksPrefsIO hooksPrefsIO = TimersManager.getPrefsIO().getHooksPrefsIO();
				TimersManager.saveActiveHours(
					new ActiveHours(
						WallClock.from(start_time),
						WallClock.from(end_time),
						featureEnabledCheckBox.isSelected()
					).setAfterEndHooks(hooksPrefsIO.loadForActiveHours(true))
					 .setBeforeStartHooks(hooksPrefsIO.loadForActiveHours(false))
				);
				statusLabel.setForeground(Colors.GREEN);
				statusLabel.setText(SWMain.messagesBundle.getString("changes_saved"));
			} catch (InstantiationException e) {
				Loggers.getErrorLogger().log(Level.SEVERE, "Couldn't save preferences", e);
				statusLabel.setForeground(Colors.RED);
				statusLabel.setText(SWMain.messagesBundle.getString("error_while_saving_changes"));
			}
		});
		saveConfigBtn.addFocusListener(onFocusLostClearLabel);

		OnClickHooksSettings onClickHooksSettings = new OnClickHooksSettings(super.owner);
		hooksConfigBtn.addActionListener((ActionEvent evt) -> {
			// show the modal dialog, this call will block
			onClickHooksSettings.actionPerformed(evt);

			if (onClickHooksSettings.shouldSaveChanges())
				// save the new configuration
				saveConfigBtn.doClick(60);
		});

		return panel;
	}

	private void addTimeInputAndLabel2Panel(
		@NotNull JPanel panel,
		@NotNull JLabel timeInputLabel,
		@NotNull TimeInputComponent timeInput,
		@NotNull GridBagConstraints gbc
	)
	{
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.ipady = 15;
		panel.add(timeInputLabel, gbc);
		gbc.ipady = 0;

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		++gbc.gridx;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		panel.add(timeInput, gbc);
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

		NotificationLocation cvNotifLocation = NotificationPrefsIO.getNotificationPrefLocation(
			NotificationPrefsIO.NotificationPreferenceType.CV_NOTIFICATION
		);

		panel.add(new NotificationLocationComponent(
			(NotificationLocation selectedLocation) -> {
				if (selectedLocation == cvNotifLocation)
					SWMain.showErrorAlert(
						SWMain.messagesBundle.getString(
							"notification_location_collision_with_cv_alert"
						),
						SWMain.messagesBundle.getString(
							"notification_location_collision_with_cv_alert_title"
						)
					);

				NotificationPrefsIO.saveNotificationPrefLocation(
					selectedLocation,
					false
				);
			},
			NotificationPrefsIO.NotificationPreferenceType.TIMER_NOTIFICATION
		));

		return panel;
	}

	@Override
	public void onShown()
	{
	}

	@Override
	public void onHide()
	{
	}

	/**
	 * Class to enable or disable time inputs and buttons when checkbox is clicked
	 */
	private static class OnActionCheckBoxListener implements ActionListener
	{
		@NotNull
		private final TimeInputComponent[] relatedTimeInputComponents;
		@Nullable
		private final BreakType breakType;
		@NotNull
		private final JLabel statusLabel;
		@NotNull
		private final JButton[] relatedButtons;
		@NotNull
		private final JButton saveButton;
		private boolean is_enabled;

		/**
		 * Constructs a new object capable of handling checkbox click events as it implements the method
		 * {@link #actionPerformed(ActionEvent)}
		 *
		 * @param relatedTimeInputComponents the related time input panels, these panels will be enabled or
		 *                                   disabled if the check box is enabled or disabled
		 * @param is_enabled                 this is used to set the initial state for the checkbox
		 * @param breakType                  the break type associated to the checkbox, this will be used to
		 *                                   identify which break to enable/disable in the
		 *                                   preferences with
		 *                                   {@link TimersManager#setBreakEnabled(BreakType, boolean)}
		 * @param relatedButtons             the list of buttons that must be enabled/disabled if this checkbox is
		 *                                   enabled/disabled
		 */
		public OnActionCheckBoxListener(
			@NotNull TimeInputComponent[] relatedTimeInputComponents,
			final boolean is_enabled,
			@Nullable BreakType breakType,
			@NotNull JLabel statusLabel,
			@NotNull JButton saveButton,
			JButton... relatedButtons
		)
		{
			this.relatedTimeInputComponents = relatedTimeInputComponents;
			this.is_enabled = is_enabled;
			this.relatedButtons = relatedButtons;
			this.breakType = breakType;
			this.statusLabel = statusLabel;
			this.saveButton = saveButton;

			for (JButton btn : relatedButtons)
				btn.setEnabled(this.is_enabled);
			saveButton.setEnabled(this.is_enabled);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			is_enabled = !is_enabled;

			for (TimeInputComponent timeInputComponent : relatedTimeInputComponents) {
				timeInputComponent.clearWarning();
				timeInputComponent.setEnabled(is_enabled);
			}

			for (JButton btn : relatedButtons)
				btn.setEnabled(is_enabled);
			saveButton.setEnabled(is_enabled);

			statusLabel.setForeground(Colors.WHITE);
			statusLabel.setText(SWMain.messagesBundle.getString(
				is_enabled
					? "feature_successfully_enabled"
					: "feature_successfully_disabled"
			));

			// update the preference
			if (!is_enabled) {
				if (breakType != null)
					TimersManager.setBreakEnabled(breakType, false);
				else
					TimersManager.setActiveHoursEnabled(false);
			} else
				saveButton.doClick(60);
		}
	}

	private static class OnSaveConfigListener implements ActionListener
	{
		private final BreakType breakType;

		private final TimeInputComponent workingTimeInput;
		private final TimeInputComponent breakTimeInput;
		private final TimeInputComponent postponeTimeInput;

		private final JLabel statusLabel;
		private BreakConfig.Builder breakSettingsBuilder;

		public OnSaveConfigListener(
			final TimeInputComponent workingTimeInput,
			final TimeInputComponent breakTimeInput,
			final TimeInputComponent postponeTimeInput,
			final BreakType breakType,
			final JLabel statusLabel
		)
		{
			this.workingTimeInput = workingTimeInput;
			this.breakTimeInput = breakTimeInput;
			this.postponeTimeInput = postponeTimeInput;
			this.statusLabel = statusLabel;
			this.breakType = breakType;

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

			// check also some constraints
			if (postponeTimeInput.getTime() > workingTimeInput.getTime()) {
				statusLabel.setForeground(Colors.RED);
				statusLabel.setText(
					SWMain.messagesBundle.getString("working_time_greater_than_postpone_time")
				);
				return;
			} else if (breakTimeInput != null && breakTimeInput.getTime() > workingTimeInput.getTime()) {
				statusLabel.setForeground(Colors.RED);
				statusLabel.setText(
					SWMain.messagesBundle.getString("working_time_greater_than_break_time")
				);
				return;
			}

			// if everything went OK
			this.statusLabel.setText(
				SWMain.messagesBundle.getString("changes_saved")
					+ ". " + SWMain.messagesBundle.getString("changes_saved_extra_text")
			);
			this.statusLabel.setForeground(Colors.GREEN);

			if (this.breakTimeInput != null)
				this.breakSettingsBuilder.breakTimerSettings(WallClock.from(this.breakTimeInput.getTime()));

			// save the new break settings and reload the break
			breakSettingsBuilder = breakSettingsBuilder
				.workTimerSettings(WallClock.from(this.workingTimeInput.getTime()))
				.postponeTimerSettings(WallClock.from(this.postponeTimeInput.getTime()));
			try {
				HooksPrefsIO hooksPrefsIO = TimersManager.getPrefsIO().getHooksPrefsIO();
				breakSettingsBuilder = breakSettingsBuilder
					.hooksConfig(new BreakHooks(
						hooksPrefsIO.loadForBreak(breakType, true),
						hooksPrefsIO.loadForBreak(breakType, false)
					));
			} catch (InstantiationException ex) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Error while loading preferences for hook of break " + breakType,
					ex
				);
			}

			TimersManager.saveBreakConfig(breakSettingsBuilder.createBreakSettings());
		}
	}

	private static class OnSetRecommendedValues implements ActionListener
	{
		private final TimeInputComponent workingTimeInput;
		private final TimeInputComponent breakTimeInput;
		private final TimeInputComponent postponeTimeInput;

		private final WallClock workingRecommendedValues;
		private final WallClock breakRecommendedValues;
		private final WallClock postponeRecommendedValues;
		private final JLabel statusLabel;

		public OnSetRecommendedValues(
			final TimeInputComponent workingTimeInput,
			final TimeInputComponent breakTimeInput,
			final TimeInputComponent postponeTimeInput,
			final JLabel statusLabel,
			final WallClock recommendedWorkingTime,
			final WallClock recommendedBreakTime,
			final WallClock recommendedPostponeTime
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
			this.statusLabel.setText(SWMain.messagesBundle.getString("recommended_values_were_set"));
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
		@NotNull
		private final Window parentDialog;
		@Nullable
		private BreakType breakType;

		private boolean save_changes = false;

		public OnClickHooksSettings(@NotNull Window parentDialog)
		{
			this.parentDialog = parentDialog;
		}

		public OnClickHooksSettings(@NotNull Window parentDialog, @Nullable BreakType breakType)
		{
			this(parentDialog);
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
			AbstractHooksConfigDialog hooksConfigDialog = breakType != null ? new BreakHooksConfigDialog(
				parentDialog,
				breakType
			) : new ActiveHoursHooksConfigDialog(parentDialog);

			hooksConfigDialog.initComponents(); // this call will block
			save_changes = hooksConfigDialog.shouldSaveChanges();
		}

		public boolean shouldSaveChanges()
		{
			return save_changes;
		}
	}
}
