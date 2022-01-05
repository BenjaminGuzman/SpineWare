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

package net.benjaminguzman.gui.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.benjaminguzman.core.NotificationLocation;
import net.benjaminguzman.SWMain;
import net.benjaminguzman.prefs.NotificationPrefsIO;
import org.jetbrains.annotations.NotNull;

public class NotificationLocationComponent extends JPanel
{
	private final JComboBox<String> notificationLocationCombobox;
	//private JLabel savedLocationLabel;

	/**
	 * Hook invoked when the user selects an item from the combobox
	 * The consumer's argument is the notification location the user has selected
	 */
	@NotNull
	private final Consumer<NotificationLocation> onSelected;

	public NotificationLocationComponent(
		@NotNull Consumer<NotificationLocation> onSelected,
		NotificationPrefsIO.NotificationPreferenceType notificationType
	)
	{
		super(new GridBagLayout());

		this.onSelected = onSelected;

		// create the combobox
		String[] locationOptions = new String[]{
			SWMain.messagesBundle.getString("notification_location_bottom_right"),
			SWMain.messagesBundle.getString("notification_location_bottom_left"),
			SWMain.messagesBundle.getString("notification_location_top_right"),
			SWMain.messagesBundle.getString("notification_location_top_left")
		};
		this.notificationLocationCombobox = new JComboBox<>(locationOptions);
		this.notificationLocationCombobox.setSelectedIndex(
			NotificationPrefsIO.getNotificationPrefLocation(
				true,
				notificationType
			).getLocationIdx()
		);

		// create the label indicating the saved location
		/*JLabel label = new JLabel(SWMain.messagesBundle.getString("saved_notification_location"));
		this.savedLocationLabel = new JLabel((String)this.notificationLocationCombobox.getSelectedItem());
		this.savedLocationLabel.setFont(Fonts.SANS_SERIF_BOLD_12);*/

		// add the components
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 5;
		gbc.ipady = 5;
		gbc.gridx = 0;
		gbc.gridy = 0;

		this.add(new JLabel(SWMain.messagesBundle.getString("notification_location")), gbc);

		++gbc.gridx;
		this.add(this.notificationLocationCombobox, gbc);

		/*++gbc.gridy;
		gbc.gridx = 0;
		this.add(label, gbc);

		++gbc.gridx;
		this.add(this.savedLocationLabel, gbc);*/

		// add listeners
		this.notificationLocationCombobox.addActionListener((ActionEvent evt) -> {
			//this.savedLocationLabel.setText((String)this.notificationLocationCombobox.getSelectedItem());

			this.onSelected.accept(
				NotificationLocation.getInstance(
					this.notificationLocationCombobox.getSelectedIndex()
				).orElse(NotificationLocation.BOTTOM_RIGHT)
			);
		});
	}
}
