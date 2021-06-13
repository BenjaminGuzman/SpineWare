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

package org.fos.sw.gui;

import java.awt.Color;
import java.awt.Component;
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
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import org.fos.sw.SWMain;
import org.fos.sw.cv.CVManager;
import org.fos.sw.cv.CVPrefsManager;
import org.fos.sw.timers.TimersManager;
import org.fos.sw.timers.WallClock;
import org.fos.sw.timers.breaks.BreakToDo;
import org.fos.sw.timers.breaks.BreakType;

public class SysTrayMenu extends JDialog
{
	private final ActionListener onClickExitButton;
	private final ActionListener onClickOpenButton;

	private final List<JProgressBar> breaksProgressBars;
	private Timer statusTimer;

	private JLabel camNotCalibratedLabel;

	private JButton pauseTimersButton, stopCVButton;

	private String restartCVLoopStr, stopCVLoopStr;

	public SysTrayMenu(final JFrame owner, final ActionListener onClickExitButton, final ActionListener onClickOpenButton)
	{
		super(owner, "SpineWare");
		this.onClickExitButton = onClickExitButton;
		this.onClickOpenButton = onClickOpenButton;

		this.breaksProgressBars = new LinkedList<>();
		this.breaksProgressBars.add(new JProgressBar(0, 1)); // for small break
		this.breaksProgressBars.add(new JProgressBar(0, 1)); // for stretch break
		this.breaksProgressBars.add(new JProgressBar(0, 1)); // for day break

		this.setUndecorated(true);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
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
		ResourceBundle messagesBundle = SWMain.messagesBundle;

		ImageIcon swLogoImageIcon = null;
		try (InputStream inputStreamSWLogo = SWMain.getFileAsStream("/resources/media/SW_white.min.png")) {
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

		ImageIcon pauseIcon = SWMain.readAndScaleIcon("/resources/media/pause_white_18dp.png");
		ImageIcon continueIcon = SWMain.readAndScaleIcon("/resources/media/play_arrow_white_18dp.png");
		ImageIcon cvOnIcon = SWMain.readAndScaleIcon("/resources/media/visibility_white_18dp.png");
		ImageIcon cvOffIcon = SWMain.readAndScaleIcon("/resources/media/visibility_off_white_18dp.png");
		String pauseTimersStr = messagesBundle.getString("pause_timers");
		String continueTimersStr = messagesBundle.getString("resume_timers");
		stopCVLoopStr = messagesBundle.getString("stop_cv_loop");
		restartCVLoopStr = messagesBundle.getString("restart_cv_loop");

		JButton exitButton = new JButton(messagesBundle.getString("systray_exit"));
		JButton openButton = new JButton(messagesBundle.getString("systray_open"));

		pauseTimersButton = new JButton(pauseTimersStr);
		pauseTimersButton.setIcon(pauseIcon);
		pauseTimersButton.setToolTipText(messagesBundle.getString("pause_timers_tooltip"));

		stopCVButton = new JButton(CVManager.isCVLoopStopped() ? restartCVLoopStr : stopCVLoopStr);
		stopCVButton.setIcon(cvOffIcon);
		stopCVButton.setToolTipText(messagesBundle.getString("stop_restart_cv_loop_tooltip"));

		camNotCalibratedLabel = new JLabel(messagesBundle.getString("cam_not_calibrated"));

		exitButton.addActionListener((ActionEvent evt) -> {
			this.dispose();
			this.onClickExitButton.actionPerformed(evt);
		});
		openButton.addActionListener((ActionEvent evt) -> {
			this.setVisible(false);
			this.onClickOpenButton.actionPerformed(evt);
		});
		pauseTimersButton.addActionListener((ActionEvent evt) -> {
			boolean stop_main_loop = !TimersManager.isMainLoopStopped();
			TimersManager.setMainLoopStopped(stop_main_loop);
			//if (stop_main_loop)
			TimersManager.shutdownAllThreads();

			pauseTimersButton.setIcon(stop_main_loop ? continueIcon : pauseIcon);
			pauseTimersButton.setText(stop_main_loop ? continueTimersStr : pauseTimersStr);
		});
		stopCVButton.addActionListener((ActionEvent evt) -> {
			boolean stop_cv_loop = !CVManager.isCVLoopStopped();
			if (stop_cv_loop)
				CVManager.stopCVLoop();
			else
				CVManager.startCVLoop();

			stopCVButton.setIcon(stop_cv_loop ? cvOnIcon : cvOffIcon);
			stopCVButton.setText(stop_cv_loop ? restartCVLoopStr : stopCVLoopStr);
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.ipady = 10;
		gbc.gridx = 0;
		gbc.gridy = 0;

		gbc.gridwidth = 2;
		mainPanel.add(swLogoImageLabel, gbc);

		++gbc.gridy;
		mainPanel.add(exitButton, gbc);

		++gbc.gridy;
		mainPanel.add(openButton, gbc);

		++gbc.gridy;
		mainPanel.add(pauseTimersButton, gbc);

		++gbc.gridy;
		mainPanel.add(stopCVButton, gbc);

		if (!CVPrefsManager.isCamCalibrated()) {
			++gbc.gridy;
			camNotCalibratedLabel.setHorizontalAlignment(SwingConstants.CENTER);
			camNotCalibratedLabel.setForeground(Colors.YELLOW);
			camNotCalibratedLabel.setToolTipText(messagesBundle.getString("cam_not_calibrated_tooltip"));
			mainPanel.add(camNotCalibratedLabel, gbc);
		}

		gbc.gridwidth = 1;

		// small break progress bar
		gbc.gridx = 0;
		++gbc.gridy;
		mainPanel.add(new JLabel(messagesBundle.getString("small_breaks_title")), gbc);

		gbc.gridx = 1;
		mainPanel.add(this.breaksProgressBars.get(BreakType.SMALL_BREAK.getIndex()), gbc);

		// stretch break progress bar
		gbc.gridx = 0;
		++gbc.gridy;
		mainPanel.add(new JLabel(messagesBundle.getString("stretch_breaks_title")), gbc);

		gbc.gridx = 1;
		mainPanel.add(this.breaksProgressBars.get(BreakType.STRETCH_BREAK.getIndex()), gbc);

		// day break progress bar
		gbc.gridx = 0;
		++gbc.gridy;
		mainPanel.add(new JLabel(messagesBundle.getString("day_break_title")), gbc);

		gbc.gridx = 1;
		mainPanel.add(this.breaksProgressBars.get(BreakType.DAY_BREAK.getIndex()), gbc);

		return mainPanel;
	}

	/**
	 * Sets the system tray menu (panel) as visible or not
	 * If it is going to be visible, the progress bars are updated and a timer to update them every second is set
	 * If not, the timer (if exists) is stopped and the dialog is not visible
	 *
	 * @param visible whether the dialog should be visible or not
	 */
	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		ResourceBundle messagesBundle = SWMain.messagesBundle;

		if (!visible) {
			if (this.statusTimer != null)
				this.statusTimer.stop(); // if the user is not seeing the dialog, don't waste resources
			return;
		}

		// cv stuff
		camNotCalibratedLabel.setVisible(!CVPrefsManager.isCamCalibrated());
		stopCVButton.setVisible(CVPrefsManager.isFeatureEnabled());
		stopCVButton.setText(CVManager.isCVLoopStopped() ? restartCVLoopStr : stopCVLoopStr);

		// timers stuff
		AbstractMap<BreakType, BreakToDo> toDoList = TimersManager.getToDoList();

		long curr_s_since_epoch = System.currentTimeMillis() / 1_000;
		boolean set_timer = false;
		for (Map.Entry<BreakType, BreakToDo> entry : toDoList.entrySet()) {
			byte break_idx = entry.getKey().getIndex();

			JProgressBar progressBar = this.breaksProgressBars.get(break_idx);
			progressBar.setStringPainted(true);
			progressBar.setForeground(Colors.GREEN_DARK);
			progressBar.setBackground(Colors.RED_WINE);

			BreakToDo toDo = entry.getValue();

			if (!toDo.getBreakConfig().isEnabled()) {
				progressBar.setString(messagesBundle.getString("feature_disabled"));
				progressBar.setValue(0);
				progressBar.setEnabled(false);
				progressBar.setBackground(Color.DARK_GRAY);
				continue;
			} else if (toDo.remainingSecondsForExecution(curr_s_since_epoch) <= 0) { // the break notification or
				// the break count down is showing

				progressBar.setString(messagesBundle.getString("notification_is_showing"));
				progressBar.setValue(0);
				progressBar.setMaximum(toDo.getBreakConfig().getWorkWallClock().getHMSAsSeconds());
			}

			progressBar.setMaximum(toDo.getBreakConfig().getWorkWallClock().getHMSAsSeconds());

			set_timer = true;
		}

		// if there is no enabled timer
		if (!set_timer)
			return;

		// set timer to update progress bar value each second
		statusTimer = new Timer(1_000, (ActionEvent evt) -> {
			// if a break is happening right now, stop all count downs
			if (TimersManager.isBreakHappening()) {
				String notificationIsShowing = messagesBundle.getString("notification_is_showing");

				breaksProgressBars.stream()
					.filter(Component::isEnabled)
					.forEach(progressBar -> progressBar.setString(notificationIsShowing));

				pauseTimersButton.setEnabled(false);
				return;
			} else
				pauseTimersButton.setEnabled(true);

			long curr_s = System.currentTimeMillis() / 1_000;
			BreakType breakType;
			BreakToDo toDo;
			for (Map.Entry<BreakType, BreakToDo> entry : toDoList.entrySet()) {
				breakType = entry.getKey();
				toDo = entry.getValue();

				// skip the breaks that are not enabled
				if (!toDo.getBreakConfig().isEnabled())
					continue;

				//
				long remaining_seconds = toDo.remainingSecondsForExecution(curr_s);
				JProgressBar progressBar = this.breaksProgressBars.get(breakType.getIndex());

				progressBar.setString(WallClock.getHMSFromSecondsAsString(remaining_seconds));
				progressBar.setValue((int) remaining_seconds);
			}
		});
		this.statusTimer.setInitialDelay(0);
		this.statusTimer.start();
	}

	@Override
	public void dispose()
	{
		super.dispose();
		if (this.statusTimer != null)
			this.statusTimer.stop();
	}

	@Override
	public String toString()
	{
		return "SysTrayMenu{" +
			"onClickExitButton=" + onClickExitButton +
			", onClickOpenButton=" + onClickOpenButton +
			", breaksProgressBars=" + breaksProgressBars +
			", statusTimer=" + statusTimer +
			'}';
	}
}