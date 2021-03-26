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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.fos.sw.Loggers;
import org.fos.sw.SWMain;
import org.fos.sw.cv.CVController;
import org.fos.sw.cv.CVPrefsManager;
import org.fos.sw.gui.Hideable;
import org.fos.sw.gui.Initializable;
import org.fos.sw.gui.Showable;
import org.fos.sw.utils.DaemonThreadFactory;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class CVConfigPanel extends JPanel implements Hideable, Showable, Initializable
{
	/**
	 * Number of frames per seconds to show in the projection (mirror) screen
	 */
	private static final int FPS = 30;

	private final ProjectionScreen projectionScreen;
	private final CamCalibrationPanel camCalibrationPanel;
	private final CamMarginsPanel marginsPanel;

	private final CVController cvController;

	private ScheduledExecutorService grabberService;
	private final Scalar CV_RED = new Scalar(0, 0, 255), CV_BLUE = new Scalar(255, 0, 0); // BGR not RGB
	/**
	 * The ideal focal length
	 * It may be {@link CVController#INVALID_IDEAL_FOCAL_LENGTH}
	 */
	private double ideal_focal_length;
	private int margin_x = 10, margin_y = 10;
	private int min_acceptable_x, max_acceptable_x, min_acceptable_y, max_acceptable_y;
	private int frame_width, frame_height;
	private int compute_distance_countdown = 15;

	public CVConfigPanel()
	{
		super();
		cvController = SWMain.getCVController();
		projectionScreen = new ProjectionScreen();
		camCalibrationPanel = new CamCalibrationPanel(
			this::onHide,
			this::onShown,
			() -> setFocalLength(CVPrefsManager.getFocalLength())
		);
		marginsPanel = new CamMarginsPanel(this::onMarginXSet, this::onMarginYSet);
	}

	@Override
	public void initComponents()
	{
		this.setLayout(new GridBagLayout());

		camCalibrationPanel.initComponents();
		marginsPanel.initComponents();

		// add the components
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.insets = new Insets(15, 10, 15, 10);
		gbc.anchor = GridBagConstraints.CENTER;

		// add mirror
		this.add(this.projectionScreen, gbc);

		// add camera calibration
		++gbc.gridy;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1;
		this.add(camCalibrationPanel, gbc);

		// add cam margins configuration
		++gbc.gridy;
		this.add(marginsPanel, gbc);

		Mat frame;
		if ((frame = cvController.captureFrame()) != null) {
			projectionScreen.initComponents(frame);
			this.frame_width = frame.width();
			this.frame_height = frame.height();
		}

		setFocalLength(CVPrefsManager.getFocalLength());

		onMarginXSet(CVPrefsManager.getMargin(true));
		onMarginYSet(CVPrefsManager.getMargin(false));

		marginsPanel.setMargin(true, this.margin_x);
		marginsPanel.setMargin(false, this.margin_y);
	}

	/**
	 * Invoked when the margin X is set in the {@link CamMarginsPanel}
	 *
	 * @param percentage the percentage of the X margin
	 */
	synchronized private void onMarginXSet(int percentage)
	{
		this.margin_x = percentage <= 0 ? 10 : percentage;
		this.computeMargins();
	}

	/**
	 * Invoked when the margin Y is set in the {@link CamMarginsPanel}
	 *
	 * @param percentage the percentage of the Y margin
	 */
	synchronized private void onMarginYSet(int percentage)
	{
		this.margin_y = percentage <= 0 ? 10 : percentage;
		this.computeMargins();
	}

	private void computeMargins()
	{
		this.min_acceptable_x = (int) (this.margin_x / 100.0 * frame_width);
		this.max_acceptable_x = (int) ((1 - this.margin_x / 100.0) * frame_width);
		this.min_acceptable_y = (int) (this.margin_y / 100.0 * frame_height);
		this.max_acceptable_y = (int) ((1 - this.margin_y / 100.0) * frame_height);
	}

	/**
	 * Grabs a frame from the webcam and displays it in the projection screen
	 * This function is intended to be called repeatedly
	 */
	private void showMirror()
	{
		Mat frame = cvController.captureFrame();
		if ((frame == null || frame.empty()) && Thread.currentThread().isInterrupted())
			return;

		if (frame == null) {
			SwingUtilities.invokeLater(() -> projectionScreen.updateProjectedImage(null));
			return;
		}
		if (this.frame_width == 0 || this.frame_height == 0) {
			this.frame_width = frame.width();
			this.frame_height = frame.height();
		}

		List<Rect> detectedFaces = cvController.detectFaces(frame);
		boolean faces_were_detected = !detectedFaces.isEmpty();

		// show error message if no face was detected or more than 1 face was detected
		String errorMsg = null;
		if (detectedFaces.size() > 1)
			errorMsg = SWMain.messagesBundle.getString("too_many_faces");
		else if (!faces_were_detected)
			errorMsg = SWMain.messagesBundle.getString("no_face_detected");

		if (errorMsg != null)
			Imgproc.putText(
				frame,
				errorMsg,
				new Point(10, 30), // bottom left of the text
				Imgproc.FONT_HERSHEY_PLAIN,
				2.0,
				new Scalar(0, 0, 255) // BGR
			);

		// draw rectangles on the detected faces
		/*for (Rect detectedFaceRect : detectedFaces)
			Imgproc.rectangle(frame, detectedFaceRect, new Scalar(0, 255, 0), 3);*/

		Rect tmpFace = faces_were_detected ? detectedFaces.get(0) : null;

		if (faces_were_detected) // draw just the first detected face to improve performance
			Imgproc.rectangle(frame, tmpFace, new Scalar(0, 255, 0), 3);

		// draw margins
		Imgproc.line( // vertical margin left
			frame,
			new Point(min_acceptable_x, 0),
			new Point(min_acceptable_x, this.frame_height),
			faces_were_detected && tmpFace.x < min_acceptable_x ? CV_RED : CV_BLUE,
			2
		);
		Imgproc.line( // vertical margin right
			frame,
			new Point(max_acceptable_x, 0),
			new Point(max_acceptable_x, this.frame_height),
			faces_were_detected && tmpFace.x + tmpFace.width > max_acceptable_x ? CV_RED : CV_BLUE,
			2
		);
		Imgproc.line( // horizontal margin top
			frame,
			new Point(0, min_acceptable_y),
			new Point(this.frame_width, min_acceptable_y),
			faces_were_detected && tmpFace.y < min_acceptable_y ? CV_RED : CV_BLUE,
			2
		);
		Imgproc.line( // horizontal margin bottom
			frame,
			new Point(0, max_acceptable_y),
			new Point(this.frame_width, max_acceptable_y),
			faces_were_detected && tmpFace.y + tmpFace.height > max_acceptable_y ? CV_RED : CV_BLUE,
			2
		);

		SwingUtilities.invokeLater(() -> {
			projectionScreen.updateProjectedImage(frame); // update the mirror

			// wait compute_distance_countdown iterations to show the distance
			// this is done to avoid cluttering the screen (and also performing too much calculations)
			if (--compute_distance_countdown > 0)
				return;

			if (this.ideal_focal_length == CVController.INVALID_IDEAL_FOCAL_LENGTH) {
				camCalibrationPanel.updateDistance(0);
				return;
			} else if (detectedFaces.isEmpty()) {
				camCalibrationPanel.updateDistance(-1);
				return;
			}

			camCalibrationPanel.updateDistance(
				cvController.computeDistance(
					this.ideal_focal_length,
					CVController.ESTIMATED_FACE_HEIGHT_CM,
					detectedFaces.get(0).height
				)
			);

			compute_distance_countdown = 15;
		});
	}

	/**
	 * Invoked when the ideal focal length is changed
	 */
	synchronized public void setFocalLength(double new_focal_length)
	{
		Loggers.getDebugLogger().log(Level.INFO, "The new focal length is: " + new_focal_length);
		this.ideal_focal_length = new_focal_length;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if (enabled) {
			this.startMirror();
			this.projectionScreen.setEnabled(true);
			this.marginsPanel.setEnabled(true);
			this.camCalibrationPanel.setEnabled(true);
		} else {
			this.stopMirror();
			this.projectionScreen.setEnabled(false);
			this.marginsPanel.setEnabled(false);
			this.camCalibrationPanel.setEnabled(false);
		}
	}

	/**
	 * This will start and configure all the required stuff to show the mirror
	 *
	 * @see #stopMirror(), the analogous function (for each call to startMirror() a call must be made to
	 * stopMirror())
	 */
	private void startMirror()
	{
		if (!cvController.getCamCapture().isOpened())
			cvController.open();

		grabberService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
		long period = (long) (1.0 / FPS * 1000);
		grabberService.scheduleAtFixedRate(this::showMirror, 0, period, TimeUnit.MILLISECONDS);
		Loggers.getDebugLogger().log(Level.INFO, "Updating the projection screen each " + period + "ms");
	}

	/**
	 * Stops the mirror (projection screen)
	 */
	private void stopMirror()
	{
		if (grabberService != null) {
			grabberService.shutdownNow();
			Loggers.getDebugLogger().log(Level.INFO, "Stopping the frame capturing service");
		}

		try {
			SWMain.getCVController().close();
		} catch (Exception e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Error while closing the web cam", e);
		}

		projectionScreen.onHide();
	}

	/**
	 * Method invoked whenever the component is being displayed
	 */
	@Override
	public void onShown()
	{
		this.setEnabled(CVPrefsManager.isFeatureEnabled());
	}

	/**
	 * Method to be invoked when the section is not visible anymore
	 */
	@Override
	public void onHide()
	{
		this.stopMirror();
	}
}