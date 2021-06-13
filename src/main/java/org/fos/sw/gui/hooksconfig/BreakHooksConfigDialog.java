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

package org.fos.sw.gui.hooksconfig;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import org.fos.sw.SWMain;
import org.fos.sw.core.Loggers;
import org.fos.sw.hooks.SingleBreakHooksConfig;
import org.fos.sw.timers.breaks.BreakType;
import org.jetbrains.annotations.NotNull;

/**
 * Use this class to show a JDialog with all the options & elements for the user to configure
 * <p>
 * This will configure the hooks for a single break (micro break, stretch break, day break)
 */
public class BreakHooksConfigDialog extends AbstractHooksConfigDialog
{
	// store the break type to know which configuration display (remember day break does not have break time)
	private final BreakType breakType;

	// arrays to store start (index 0) and break (index 1) hooks panel
	private final HooksConfigPanel[] notificationHooksPanel;
	private final HooksConfigPanel[] breakHooksPanel;

	/**
	 * Constructs the object
	 *
	 * @param owner     the owner for the JDialog
	 * @param breakType the break type, this is used to save the correct value in the correct preference while
	 *                  saving the user preferences
	 */
	public BreakHooksConfigDialog(@NotNull Window owner, BreakType breakType)
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
	@Override
	public void initComponents()
	{
		mainPanel.add(createConfigurationsPanel(), BorderLayout.CENTER);
		mainPanel.add(createActionsPanel(), BorderLayout.SOUTH);

		super.configDialog();

		this.setTitle("SpineWare | Breaks | " + SWMain.messagesBundle.getString("hooks_config"));
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

		ResourceBundle messagesBundle = SWMain.messagesBundle;

		String playAudio = messagesBundle.getString("play_audio");
		String selectAudio = messagesBundle.getString("select_audio");
		String noAudio = messagesBundle.getString("no_audio");
		String cmd2Exec = messagesBundle.getString("cmd_to_execute");

		// load configurations
		SingleBreakHooksConfig notificationsHooksConf = null;
		try {
			notificationsHooksConf = hooksPrefsIO.loadForBreak(this.breakType, true);
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
			SingleBreakHooksConfig breakHooksConf = null;
			try {
				breakHooksConf = hooksPrefsIO.loadForBreak(this.breakType, false);
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
	 * Invoked when the user clicks the save config button
	 *
	 * @param evt event to be processed
	 */
	@Override
	protected void onClickSaveConfig(ActionEvent evt)
	{
		save_configs = true;

		// create notification hooks configuration & save preferences
		try {
			hooksPrefsIO.save(
				new SingleBreakHooksConfig.Builder()
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
			);
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
				hooksPrefsIO.save(
					new SingleBreakHooksConfig.Builder()
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
				);
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
}
