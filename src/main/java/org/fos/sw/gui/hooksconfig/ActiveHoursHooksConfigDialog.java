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

package org.fos.sw.gui.hooksconfig;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import org.fos.sw.SWMain;
import org.fos.sw.hooks.HooksConfig;
import org.jetbrains.annotations.NotNull;


/**
 * Use this class to show a JDialog with all the options & elements for the user to configure
 * <p>
 * This will configure the hooks for a single break (micro break, stretch break, day break)
 */
public class ActiveHoursHooksConfigDialog extends AbstractHooksConfigDialog
{
	private HooksConfigPanel afterActiveHoursHooksPanel;
	private HooksConfigPanel beforeActiveHoursHooksPanel;

	/**
	 * Constructs the object
	 *
	 * @param owner the owner for the JDialog
	 */
	public ActiveHoursHooksConfigDialog(@NotNull Window owner)
	{
		super(owner);
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

		this.setTitle("SpineWare | Active Hours | " + SWMain.messagesBundle.getString("hooks_config"));
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
		HooksConfig beforeActiveHoursHooksConf = hooksPrefsIO.loadForActiveHours(false);
		HooksConfig afterActiveHoursHooksConf = hooksPrefsIO.loadForActiveHours(true);

		// on before active hours notification shown
		contentPanel.add(Box.createVerticalStrut(20));
		beforeActiveHoursHooksPanel = new HooksConfigPanel();
		beforeActiveHoursHooksPanel.initComponents(
			messagesBundle.getString("before_active_hours_hook_on_shown_notif_title"),
			playAudio,
			selectAudio,
			noAudio,
			cmd2Exec
		);
		beforeActiveHoursHooksPanel.setFeatureEnabled(beforeActiveHoursHooksConf.isStartEnabled());
		beforeActiveHoursHooksPanel.setSelectedAudio(beforeActiveHoursHooksConf.getOnStartAudioStr());
		beforeActiveHoursHooksPanel.setCmd(beforeActiveHoursHooksConf.getOnStartCmdStr());

		contentPanel.add(beforeActiveHoursHooksPanel);
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(new JSeparator());
		contentPanel.add(Box.createVerticalStrut(5));

		// on after active hours notification shown
		afterActiveHoursHooksPanel = new HooksConfigPanel();
		afterActiveHoursHooksPanel.initComponents(
			messagesBundle.getString("after_active_hours_hook_on_shown_notif_title"),
			playAudio,
			selectAudio,
			noAudio,
			cmd2Exec
		);
		afterActiveHoursHooksPanel.setFeatureEnabled(afterActiveHoursHooksConf.isStartEnabled());
		afterActiveHoursHooksPanel.setSelectedAudio(afterActiveHoursHooksConf.getOnStartAudioStr());
		afterActiveHoursHooksPanel.setCmd(afterActiveHoursHooksConf.getOnStartCmdStr());

		contentPanel.add(afterActiveHoursHooksPanel);
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(new JSeparator());
		contentPanel.add(Box.createVerticalStrut(10));

		scrollPane.setViewportView(contentPanel);

		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		return scrollPane;
	}

	@Override
	protected void onClickSaveConfig(ActionEvent evt)
	{
		save_configs = true;

		// create before active hours notification hooks
		/*
		THIS PREFERENCES ARE BEING SAVED ONCE THE DIALOG IS DISPOSED, SO IT IS OK IF WE DON'T SAVE THEM HERE
		hooksPrefsIO.saveActiveHoursHooks(
			new HooksConfig.Builder()
				.isNotificationHook(true)
				.startEnabled(beforeActiveHoursHooksPanel.isFeatureEnabled())
				.onStartAudioStr(beforeActiveHoursHooksPanel.getSelectedAudio())
				.onStartCmdStr(beforeActiveHoursHooksPanel.getCmd())
				.createHooksConfig(),
			false
		);

		// create after active hours notification hooks
		hooksPrefsIO.saveActiveHoursHooks(
			new HooksConfig.Builder()
				.isNotificationHook(true)
				.startEnabled(afterActiveHoursHooksPanel.isFeatureEnabled())
				.onStartAudioStr(afterActiveHoursHooksPanel.getSelectedAudio())
				.onStartCmdStr(afterActiveHoursHooksPanel.getCmd())
				.createHooksConfig(),
			true
		);*/

		this.dispose();
	}
}