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

package net.benjaminguzman.gui.hooksconfig;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import net.benjaminguzman.SWMain;
import net.benjaminguzman.gui.Initializable;
import net.benjaminguzman.prefs.timers.HooksPrefsIO;
import net.benjaminguzman.timers.TimersManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractHooksConfigDialog extends JDialog implements Initializable
{
	@NotNull
	protected final HooksPrefsIO hooksPrefsIO;
	protected JPanel mainPanel;
	protected boolean save_configs = false;

	/**
	 * Constructs the object
	 *
	 * @param owner         the owner for the JDialog
	 * @param layoutManager the layout manager to use within the {@link #mainPanel}
	 */
	public AbstractHooksConfigDialog(@NotNull Window owner, @NotNull LayoutManager layoutManager)
	{
		super(owner);
		mainPanel = new JPanel(layoutManager);
		hooksPrefsIO = TimersManager.getPrefsIO().getHooksPrefsIO();
	}

	/**
	 * Constructs the object
	 *
	 * @param owner the owner for the JDialog
	 */
	public AbstractHooksConfigDialog(@NotNull Window owner)
	{
		this(owner, new BorderLayout());
	}

	public void configDialog()
	{
		this.setContentPane(mainPanel);

		this.setMinimumSize(new Dimension(600, 600)); // in case resizable is true
		this.setPreferredSize(new Dimension(900, 700));
		this.setMaximumSize(new Dimension(900, 800)); // in case resizable is true
		this.pack();
		this.setIconImage(SWMain.getSWIcon());
		this.setModal(true);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(this.getOwner());
	}

	/**
	 * Creates the actions panel
	 * <p>
	 * The panel will contain just two buttons, save and cancel
	 *
	 * @return the JPanel with the buttons added
	 */
	protected JPanel createActionsPanel()
	{
		JPanel panel = new JPanel();
		ResourceBundle messagesBundle = SWMain.messagesBundle;

		JButton saveBtn = new JButton(messagesBundle.getString("save_changes"));
		saveBtn.setToolTipText(messagesBundle.getString("save_changes_timers_warning"));

		JButton cancelBtn = new JButton(messagesBundle.getString("cancel"));

		panel.add(saveBtn);
		panel.add(cancelBtn);

		this.getRootPane().setDefaultButton(saveBtn);

		cancelBtn.addActionListener(this::onClickCancel);
		saveBtn.addActionListener(this::onClickSaveConfig);

		return panel;
	}

	protected abstract void onClickSaveConfig(ActionEvent evt);

	protected void onClickCancel(ActionEvent evt)
	{
		this.dispose();
	}

	/**
	 * Tells whether or not the user clicked the "save changes" button
	 * and therefore changes should be saved
	 *
	 * @return true if the user clicked the button
	 */
	public boolean shouldSaveChanges()
	{
		return save_configs;
	}
}
