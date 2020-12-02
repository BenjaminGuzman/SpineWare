/*
 * Copyright (c) 2020. Benjamín Guzmán
 * Author: Benjamín Guzmán <9benjaminguzman@gmail.com>
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

package org.fos;

import org.fos.timers.WorkingTimeTimer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.io.InputStream;

public class SysTrayMenu extends JDialog
{
	private final ActionListener onClickExitButton;
	private final ActionListener onClickOpenButton;

	public SysTrayMenu(final JFrame owner, final ActionListener onClickExitButton, final ActionListener onClickOpenButton)
	{
		super(owner, "SpineWare");
		this.onClickExitButton = onClickExitButton;
		this.onClickOpenButton = onClickOpenButton;

		this.setUndecorated(true);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.setModal(false);
		this.setType(Type.POPUP);
		this.setModalityType(ModalityType.MODELESS);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		JPanel mainPanel = this.initComponents();
		this.setContentPane(mainPanel);
		this.pack();

		this.addWindowFocusListener(new WindowFocusListener()
		{
			@Override
			public void windowGainedFocus(WindowEvent e)
			{
			}

			@Override
			public void windowLostFocus(WindowEvent e)
			{
				SysTrayMenu.this.setVisible(false);
			}
		});
	}

	/**
	 * Creates the menu components
	 * this method will handle all the GUI stuff
	 *
	 * @return the panel containing all the elements the menu should have
	 */
	public JPanel initComponents()
	{
		JPanel mainPanel = new JPanel(new GridBagLayout());

		InputStream inputStreamSWLogo = SWMain.getImageAsStream("/resources/media/SW_white.min.png");
		ImageIcon swLogoImageIcon = null;
		try {
			Image img = ImageIO.read(inputStreamSWLogo);
			img = img.getScaledInstance(50, 44, Image.SCALE_AREA_AVERAGING);
			swLogoImageIcon = new ImageIcon(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel swLogoImageLabel;
		if (swLogoImageIcon != null)
			swLogoImageLabel = new JLabel(swLogoImageIcon);
		else
			swLogoImageLabel = new JLabel("SW");

		JButton exitButton = new JButton(SWMain.messagesBundle.getString("systray_exit"));
		JButton openButton = new JButton(SWMain.messagesBundle.getString("systray_open"));

		exitButton.addActionListener((ActionEvent evt) -> {
			this.setVisible(false);
			this.onClickExitButton.actionPerformed(evt);
		});
		openButton.addActionListener((ActionEvent evt) -> {
			this.setVisible(false);
			this.onClickOpenButton.actionPerformed(evt);
		});

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.ipady = 10;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;

		mainPanel.add(swLogoImageLabel, gridBagConstraints);

		gridBagConstraints.gridy = 1;
		mainPanel.add(exitButton, gridBagConstraints);

		gridBagConstraints.gridy = 2;
		mainPanel.add(openButton, gridBagConstraints);

		// TODO: add 3 progress bars to show the remaining time for each break

		return mainPanel;
	}

	@Override
	public void setVisible(boolean b)
	{
		super.setVisible(b);

		WorkingTimeTimer smallBreakTimer = SWMain.timersManager.getSmallBreaksWorkingTimer();
		WorkingTimeTimer stretchBreakTimer = SWMain.timersManager.getStretchBreaksWorkingTimer();
		WorkingTimeTimer dayBreakTimer = SWMain.timersManager.getDayBreakWorkingTimer();

		if (smallBreakTimer != null) {
			long remaining_s_for_notification = smallBreakTimer.getNotificationShouldBeShownAt()
				- System.currentTimeMillis() / 1_000;
			if (remaining_s_for_notification <= 0) {
				// TODO: show that the notification is already shown
			} else {
				// TODO: show in the progress bar the remaining seconds
				// TODO: update the value each 2 seconds until the notification is shown
			}
		}

	}
}
