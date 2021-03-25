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

package org.fos.sw.gui.cv;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.stream.IntStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.fos.sw.Loggers;
import org.fos.sw.SWMain;
import org.fos.sw.cv.CVController;
import org.fos.sw.gui.Fonts;
import org.fos.sw.gui.Initializable;
import org.fos.sw.gui.notifications.CountDownDialog;
import org.fos.sw.timers.WallClock;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;

public class CamCalibrationPanel extends JPanel implements Initializable
{
	private static final String DISTANCE_UNITS = "cm";
	/**
	 * Conversion factor from the {@link #DISTANCE_UNITS} to meters
	 * Multiply by this quantity to convert to meters
	 */
	private static final double DISTANCE_CONVERSION_FACTOR_2_M = 10e-2;
	/**
	 * Callback to execute when calibration is being performed
	 */
	@NotNull
	private final Runnable stopMirror;
	/**
	 * Callback to execute when calibration is done
	 */
	@NotNull
	private final Runnable resumeMirror;
	private JComboBox<String> distanceDropdown;
	private JComboBox<String> faceHeightDropdown;

	/**
	 * Stopping/resuming the mirror is required as the calibration algorithm will grab some frames.
	 * Since the mirror also grabs frames, bad things (or at least is not good)
	 * can happen if the resource is accessed twice
	 *
	 * @param stopMirror   Callback to execute when calibration is being performed
	 * @param resumeMirror Callback to execute when calibration is done
	 */
	public CamCalibrationPanel(@NotNull final Runnable stopMirror, @NotNull final Runnable resumeMirror)
	{
		this.stopMirror = stopMirror;
		this.resumeMirror = resumeMirror;
	}

	@Override
	public void initComponents()
	{
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 0, 5, 0);

		this.add(this.createDescriptionPanel(), gbc);

		++gbc.gridy;
		this.add(this.createActionsPanel(), gbc);
	}

	/**
	 * @return a panel with the title and the description (instructions) for the calibration
	 */
	private JPanel createDescriptionPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// create the title
		JLabel titleLabel = new JLabel(SWMain.messagesBundle.getString("calibration_title"));
		titleLabel.setFont(Fonts.TITLE_FONT);

		// create the description - instructions
		JLabel descLabel = new JLabel(SWMain.messagesBundle.getString("calibration_instructions"));
		descLabel.setFont(Fonts.FULL_DESCRIPTION_FONT);

		panel.add(titleLabel);
		panel.add(Box.createVerticalStrut(10));
		panel.add(descLabel);

		panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

		return panel;
	}

	/**
	 * @return a panel with inputs so the user can perform actions
	 */
	private JPanel createActionsPanel()
	{

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));

		// create the distance dropdown
		this.distanceDropdown = new JComboBox<>(
			IntStream.rangeClosed(2, 7)
				.map(i -> i * 10)
				.mapToObj(i -> i + DISTANCE_UNITS)
				.toArray(String[]::new)
		);

		// create the "take pic" button
		JButton calibrateBtn = new JButton(SWMain.messagesBundle.getString("calibrate"));

		// create label to tell if the cam has already been calibrated at that distance
		JLabel calibrationStatus = new JLabel();

		// add listeners
		calibrateBtn.addActionListener(this::onClickCalibrate);

		panel.add(this.distanceDropdown);
		panel.add(calibrateBtn);

		return panel;
	}

	/**
	 * Invoked when the calibrate button is clicked
	 */
	private void onClickCalibrate(ActionEvent evt)
	{
		// this.stopMirror.run(); // don't call this here, hte program will crash because in the MainFrame
		// class hooks to stop the mirror are already configured and will be triggered when the
		// CountDownDialog is shown

		// parse the distance
		String distanceStr = (String) this.distanceDropdown.getSelectedItem();
		if (distanceStr == null)
			return;
		int distance = Integer.parseInt(distanceStr.replace(DISTANCE_UNITS, "").trim());

		CountDownLatch latch = new CountDownLatch(1);

		// show countdown
		SwingUtilities.invokeLater(() -> new CountDownDialog(
			SWMain.messagesBundle.getString("performing_calibration") + " (@ " + distanceStr + ")",
			new WallClock(0, 0, 3),
			latch,
			SwingUtilities.windowForComponent(this)
		));

		// start calibration
		Thread calibrationThread = new Thread(() -> {
			Mat frame;
			CVController cvController = SWMain.getCVController();

			double avg_focal_length = 0;
			double n_focal_lengths = 0;
			double tmp_focal_length;

			// capture frames while the notification is showing
			while (latch.getCount() != 0 && !Thread.currentThread().isInterrupted()) {
				frame = SWMain.getCVController().captureFrame();
				if (frame == null || frame.empty())
					continue;

				if ((tmp_focal_length = cvController.getIdealFocalLength(distance, 12, frame)) == -1)
					continue;

				avg_focal_length += tmp_focal_length;
				++n_focal_lengths;
			}

			// if not enough frames were captured, notify the user
			if (n_focal_lengths <= 2) {
				SwingUtilities.invokeLater(() -> JOptionPane.showConfirmDialog(
					this,
					SWMain.messagesBundle.getString("no_face_detected") + ".\nTry again.",
					"Calibration Warning",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE
				));
			} else {
				avg_focal_length /= n_focal_lengths;

				Loggers.getDebugLogger().log(
					Level.INFO,
					"Average IDEAL focal length at distance " + distanceStr + " is: " + avg_focal_length
				);
			}

			// TODO: save computed ideal focal length in preferences

			// finally, when calibration is done or cancelled, resume the mirror
			SwingUtilities.invokeLater(this.resumeMirror);
		});
		calibrationThread.setDaemon(true);
		calibrationThread.start();
	}
}
