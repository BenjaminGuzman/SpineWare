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

package org.fos.sw.gui.notifications;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import org.fos.sw.Loggers;
import org.fos.sw.SWMain;
import org.fos.sw.gui.Fonts;
import org.fos.sw.timers.WallClock;
import org.jetbrains.annotations.Nullable;

public class BreakCountDown extends JDialog
{
	private static ImageIcon spineWareIcon; // static to avoid reading the image each time the dialog is shown

	private final WallClock breakTime;
	private final CountDownLatch countDownLatch;

	private final JLabel[] hmsRemainingTimeLabels;
	private final JProgressBar progressBar;

	private final Timer timerCountDown;

	@Nullable
	private Runnable onDisposed;

	public BreakCountDown(
		final String breakMessage,
		final WallClock breakTime,
		final CountDownLatch countDownLatch,
		final @Nullable Runnable onShown,
		final @Nullable Runnable onDisposed
	)
	{
		this(breakMessage, breakTime, countDownLatch);
		this.onDisposed = onDisposed;
		if (onShown != null)
			onShown.run();
	}

	public BreakCountDown(
		final String breakMessage,
		final WallClock breakTime,
		final CountDownLatch countDownLatch
	)
	{
		super((Window) null); // make this an unowned dialog
		assert SwingUtilities.isEventDispatchThread();

		ResourceBundle messagesBundle = SWMain.getMessagesBundle();

		this.breakTime = new WallClock(breakTime); // we need a copy because this class will modify it
		this.countDownLatch = countDownLatch;

		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// add SpineWare logo
		this.loadSpineWareIcon();
		JLabel spineWareIconLabel = new JLabel();
		if (BreakCountDown.spineWareIcon != null)
			spineWareIconLabel.setIcon(BreakCountDown.spineWareIcon);
		else
			spineWareIconLabel.setText("SW");

		// create break time message label
		JLabel breakMessageLabel = new JLabel(
			breakMessage + " (" + breakTime.getHMSAsString() + ")",
			SwingConstants.CENTER
		);
		breakMessageLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));

		// create remaining time message label
		JLabel remainingTimeLabel = new JLabel(messagesBundle.getString("remaining_break_time"));
		remainingTimeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

		// create real-time remaining time labels
		this.hmsRemainingTimeLabels = new JLabel[]{
			new JLabel(String.valueOf(this.breakTime.getHours())),
			new JLabel(String.valueOf(this.breakTime.getMinutes())),
			new JLabel(String.valueOf(this.breakTime.getSeconds()))
		};

		this.progressBar = new JProgressBar(0, this.breakTime.getHMSAsSeconds());
		this.progressBar.setValue(this.breakTime.getHMSAsSeconds());
		this.progressBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		// create cancel button
		JButton cancelButton = new JButton(messagesBundle.getString("cancel"));
		cancelButton.addActionListener((ActionEvent evt) -> this.dispose());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.ipady = 10;
		gridBagConstraints.ipadx = 10;

		// add spineware logo
		gridBagConstraints.gridwidth = 6;
		mainPanel.add(spineWareIconLabel, gridBagConstraints);

		// add break time message
		gridBagConstraints.gridy = 1;
		mainPanel.add(breakMessageLabel, gridBagConstraints);

		// add remaining time message
		gridBagConstraints.gridy = 2;
		mainPanel.add(remainingTimeLabel, gridBagConstraints);

		// add countdown indicators
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.weightx = 1;

		String[] hms = new String[]{
			messagesBundle.getString("hours"),
			messagesBundle.getString("minutes"),
			messagesBundle.getString("seconds")
		};
		byte i = 0;
		for (JLabel label : this.hmsRemainingTimeLabels) {
			label.setFont(Fonts.MONOSPACED_BOLD_24);
			label.setHorizontalAlignment(JLabel.RIGHT);
			mainPanel.add(label, gridBagConstraints); // add number label

			++gridBagConstraints.gridx;
			label.setHorizontalAlignment(JLabel.LEFT);
			mainPanel.add(new JLabel(hms[i]), gridBagConstraints); // add unit label

			++gridBagConstraints.gridx;
			++i;
		}

		// add progress bar
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(this.progressBar, gridBagConstraints);

		// add the cancel button
		++gridBagConstraints.gridy;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		mainPanel.add(cancelButton, gridBagConstraints);


		String iconPath = "/resources/media/SW_white.min.png";
		try (InputStream iconInputStream = SWMain.getFileAsStream(iconPath)) {
			this.setIconImage(ImageIO.read(iconInputStream));
		} catch (IOException e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Error while setting JFrame image icon", e);
		}

		this.setContentPane(mainPanel);

		// set default button
		this.getRootPane().setDefaultButton(cancelButton);

		this.setTitle(breakMessage);
		this.setResizable(false);
		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);

		this.timerCountDown = new Timer(1_000, (ActionEvent evt) -> this.countDown());
		this.timerCountDown.start();
	}

	public void countDown()
	{
		assert SwingUtilities.isEventDispatchThread();

		if (!this.breakTime.subtractSeconds((byte) 1)) { // if the subtraction could not be done because timer is at 0
			this.dispose();
			return;
		}

		this.hmsRemainingTimeLabels[0].setText(String.valueOf(this.breakTime.getHours()));
		this.hmsRemainingTimeLabels[1].setText(String.valueOf(this.breakTime.getMinutes()));
		this.hmsRemainingTimeLabels[2].setText(String.valueOf(this.breakTime.getSeconds()));
		this.progressBar.setValue(this.breakTime.getHMSAsSeconds());
	}

	/**
	 * If the image icon has been already loaded, this will simply do nothing
	 * If it hasn't been loaded, it tries to load it and stores it in the swIcon static member
	 */
	private void loadSpineWareIcon()
	{
		if (BreakCountDown.spineWareIcon != null)
			return;

		String iconImagePath = "/resources/media/SpineWare_white.png";
		Image icon;
		try (InputStream iconInputStream = SWMain.getFileAsStream(iconImagePath)) {
			icon = ImageIO.read(iconInputStream);
		} catch (IOException e) {
			Loggers.getErrorLogger().log(Level.SEVERE, "Error while reading SpineWare icon in path: " + iconImagePath, e);
			return;
		}
		icon = icon.getScaledInstance(400, 120, Image.SCALE_AREA_AVERAGING);

		BreakCountDown.spineWareIcon = new ImageIcon(icon);
	}

	/**
	 * Same as {@link JDialog#dispose}
	 * but this method will also count down the latch
	 * it will also stop the timer that handles the countdown in the GUI
	 */
	@Override
	public void dispose()
	{
		this.disposeNoHooks();
		if (this.onDisposed != null)
			this.onDisposed.run();
	}

	/**
	 * Same as {@link #dispose()}
	 * but this method will not run any hooks, e. g. {@link #onDisposed}
	 */
	public void disposeNoHooks()
	{
		super.dispose();
		this.timerCountDown.stop();
		this.countDownLatch.countDown();
	}

	@Override
	public String toString()
	{
		return "BreakCountDown{" +
			"breakSettings=" + breakTime +
			", countDownLatch=" + countDownLatch +
			", hmsRemainingTimeLabels=" + Arrays.toString(hmsRemainingTimeLabels) +
			", timerCountDown=" + timerCountDown +
			'}';
	}
}