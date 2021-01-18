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

package org.fos.gui.hooksconfig;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.core.BreakType;
import org.fos.hooks.HooksConfig;

/**
 * Use this class to show a JDialog with all the options & elements for the user to configure
 * <p>
 * This will configure the hooks for a single break (micro break, stretch break, day break)
 */
public class ConfigureHooksDialog extends JDialog
{
	// store the break type to know which configuration display (remember day break does not have break time)
	private final BreakType breakType;

	// arrays sto store start (index 0) / break (index 1) hooks panel
	private final HooksConfigPanel[] notificationHooksPanel;
	private final HooksConfigPanel[] breakHooksPanel;

	private boolean save_configs = false;

	/**
	 * Constructs the object
	 *
	 * @param owner     the owner for the JDialog
	 * @param breakType the break type, this is used to save the correct value in the correct preference while
	 *                  saving the user preferences
	 */
	public ConfigureHooksDialog(Window owner, BreakType breakType)
	{
		super(owner);
		this.notificationHooksPanel = new HooksConfigPanel[2];
		this.breakHooksPanel = new HooksConfigPanel[2];
		this.breakType = breakType;
	}

	/**
	 * Initiates the components for the JDialog content pane
	 * <p>
	 * this will call {@link #setVisible(boolean)} internally
	 * and also configure all the modality for the dialog
	 */
	public void initComponents()
	{
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(this.createConfigurationsPanel(), BorderLayout.CENTER);
		panel.add(this.createActionsPanel(), BorderLayout.SOUTH);

		this.setContentPane(panel);

		this.pack();
		this.setIconImage(SWMain.getSWIcon());
		this.setMinimumSize(new Dimension(500, 500)); // in case resizable is true
		this.setPreferredSize(new Dimension(600, 600));
		this.setMaximumSize(new Dimension(800, 800)); // in case resizable is true
		this.setModal(true);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setTitle("SpineWare | " + SWMain.getMessagesBundle().getString("hooks_config"));
		this.setLocationRelativeTo(this.getOwner());
		this.setVisible(true);
	}

	/**
	 * Creates the configuration panel
	 * <p>
	 * The created panel will contain all the configuration hooks for all the possible hooks
	 *
	 * @return a scrollpane containing all the sub-panels for the configuration hooks
	 */
	private JScrollPane createConfigurationsPanel()
	{
		JScrollPane scrollPane = new JScrollPane();
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		String playAudio = messagesBundle.getString("play_audio");
		String selectAudio = messagesBundle.getString("select_audio");
		String noAudio = messagesBundle.getString("no_audio");
		String cmd2Exec = messagesBundle.getString("cmd_to_execute");

		// load configurations
		HooksConfig notificationsHooksConf = null;
		try {
			notificationsHooksConf = HooksConfig.fromPrefs(this.breakType, true);
		} catch (InstantiationException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Error while loading notification hooks config",
				e
			);
		}

		// on notification shown hook
		contentPanel.add(Box.createVerticalStrut(20));
		this.notificationHooksPanel[0] = new HooksConfigPanel();
		this.notificationHooksPanel[0].initComponents(
			messagesBundle.getString("hook_on_shown_notif_title"),
			playAudio,
			selectAudio,
			noAudio,
			cmd2Exec
		);
		if (notificationsHooksConf != null) {
			this.notificationHooksPanel[0].setFeatureEnabled(notificationsHooksConf.isStartEnabled());
			this.notificationHooksPanel[0].setSelectedAudio(notificationsHooksConf.getOnStartAudioStr());
			this.notificationHooksPanel[0].setCmd(notificationsHooksConf.getOnStartCmdStr());
		}

		contentPanel.add(this.notificationHooksPanel[0]);
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(new JSeparator());
		contentPanel.add(Box.createVerticalStrut(5));

		// on notification closed hook
		this.notificationHooksPanel[1] = new HooksConfigPanel();
		this.notificationHooksPanel[1].initComponents(
			messagesBundle.getString("hook_on_closed_notif_title"),
			playAudio,
			selectAudio,
			noAudio,
			cmd2Exec
		);
		if (notificationsHooksConf != null) {
			this.notificationHooksPanel[1].setFeatureEnabled(notificationsHooksConf.isEndEnabled());
			this.notificationHooksPanel[1].setSelectedAudio(notificationsHooksConf.getOnEndAudioStr());
			this.notificationHooksPanel[1].setCmd(notificationsHooksConf.getOnEndCmdStr());
		}

		contentPanel.add(this.notificationHooksPanel[1]);
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(new JSeparator());
		contentPanel.add(Box.createVerticalStrut(10));

		if (this.breakType != BreakType.DAY_BREAK) { // the day break should not have break hooks config
			HooksConfig breakHooksConf = null;
			try {
				breakHooksConf = HooksConfig.fromPrefs(this.breakType, false);
			} catch (InstantiationException e) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Error while loading notification hooks config",
					e
				);
			}

			// on break start hook
			this.breakHooksPanel[0] = new HooksConfigPanel(true);
			this.breakHooksPanel[0].initComponents(
				messagesBundle.getString("hook_on_break_start_title"),
				messagesBundle.getString("select_audio_dir_desc"),
				messagesBundle.getString("select_directory"),
				noAudio,
				cmd2Exec
			);
			if (breakHooksConf != null) {
				this.breakHooksPanel[0].setFeatureEnabled(breakHooksConf.isStartEnabled());
				this.breakHooksPanel[0].setSelectedAudio(breakHooksConf.getOnStartAudioStr());
				this.breakHooksPanel[0].setCmd(breakHooksConf.getOnStartCmdStr());
			}
			contentPanel.add(this.breakHooksPanel[0]);
			contentPanel.add(Box.createVerticalStrut(10));
			contentPanel.add(new JSeparator());
			contentPanel.add(Box.createVerticalStrut(5));

			// on break end hook
			this.breakHooksPanel[1] = new HooksConfigPanel();
			this.breakHooksPanel[1].initComponents(
				messagesBundle.getString("hook_on_break_end_title"),
				playAudio,
				selectAudio,
				noAudio,
				cmd2Exec
			);
			if (breakHooksConf != null) {
				this.breakHooksPanel[1].setFeatureEnabled(breakHooksConf.isEndEnabled());
				this.breakHooksPanel[1].setSelectedAudio(breakHooksConf.getOnEndAudioStr());
				this.breakHooksPanel[1].setCmd(breakHooksConf.getOnEndCmdStr());
			}
			contentPanel.add(this.breakHooksPanel[1]);
			contentPanel.add(Box.createVerticalStrut(20));
		}

		scrollPane.setViewportView(contentPanel);

		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		return scrollPane;
	}

	/**
	 * Creates the actions panel
	 * <p>
	 * The panel will contain just two buttons, save and cancel
	 *
	 * @return the JPanel with the buttons added
	 */
	public JPanel createActionsPanel()
	{
		JPanel panel = new JPanel();
		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		JButton saveBtn = new JButton(messagesBundle.getString("save_changes"));
		saveBtn.setToolTipText(messagesBundle.getString("save_changes_timers_warning"));

		JButton cancelBtn = new JButton(messagesBundle.getString("cancel"));

		panel.add(saveBtn);
		panel.add(cancelBtn);

		this.getRootPane().setDefaultButton(saveBtn);

		cancelBtn.addActionListener((ActionEvent evt) -> this.dispose());
		saveBtn.addActionListener(this::saveConfiguration);

		return panel;
	}

	/**
	 * Invoked when the user clicks the save config button
	 *
	 * @param evt event to be processed
	 */
	private void saveConfiguration(ActionEvent evt)
	{
		save_configs = true;

		// create notification hooks configuration & save preferences
		try {
			new HooksConfig.Builder()
				.isNotificationHook(true)
				.breakType(this.breakType)
				.startAudioIsDir(false)
				.startEnabled(this.notificationHooksPanel[0].isFeatureEnabled())
				.endEnabled(this.notificationHooksPanel[1].isFeatureEnabled())
				.onStartAudioStr(this.notificationHooksPanel[0].getSelectedAudio())
				.onEndAudioStr(this.notificationHooksPanel[1].getSelectedAudio())
				.onStartCmdStr(this.notificationHooksPanel[0].getCmd())
				.onEndCmdStr(this.notificationHooksPanel[1].getCmd())
				.createHooksConfig()
				.savePrefs();
		} catch (InstantiationException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Error while saving notification hooks config",
				e
			);
		}

		if (this.breakType != BreakType.DAY_BREAK) { // the day break should not have break hooks config

			// create break hooks configuration & save preferences
			try {
				new HooksConfig.Builder()
					.isNotificationHook(false)
					.breakType(this.breakType)
					.startAudioIsDir(true)
					.startEnabled(this.breakHooksPanel[0].isFeatureEnabled())
					.endEnabled(this.breakHooksPanel[1].isFeatureEnabled())
					.onStartAudioStr(this.breakHooksPanel[0].getSelectedAudio())
					.onEndAudioStr(this.breakHooksPanel[1].getSelectedAudio())
					.onStartCmdStr(this.breakHooksPanel[0].getCmd())
					.onEndCmdStr(this.breakHooksPanel[1].getCmd())
					.createHooksConfig()
					.savePrefs();
			} catch (InstantiationException e) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Error while loading notification hooks config",
					e
				);
			}
		}

		this.dispose();
	}

	/**
	 * Tells whether or not the user clicked the "save changes" button
	 * and therefore changes should be saved
	 *
	 * @return true if the user clicked the button
	 */
	public boolean shouldSaveChanges() {
		return save_configs;
	}
}
