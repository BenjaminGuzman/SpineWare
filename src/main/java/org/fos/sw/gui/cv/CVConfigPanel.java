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
import org.fos.sw.gui.Hideable;
import org.fos.sw.gui.Showable;
import org.fos.sw.utils.DaemonThreadFactory;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class CVConfigPanel extends JPanel implements Hideable, Showable
{
	/**
	 * Number of frames per seconds to show in the projection (mirror) screen
	 */
	private static final int FPS = 20;

	private final ProjectionScreen projectionScreen;

	private final CVController cvController;

	private ScheduledExecutorService grabberService;

	public CVConfigPanel()
	{
		super();
		cvController = SWMain.getCVController();
		projectionScreen = new ProjectionScreen();
	}

	public void initComponents()
	{
		this.add(this.projectionScreen);


		Mat frame;
		if ((frame = cvController.captureFrame()) != null)
			projectionScreen.initComponents(frame);
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

		List<Rect> detectedFaces = cvController.detectFaces(frame);

		// show error message if no face was detected or more than 1 face was detected
		String errorMsg = null;
		if (detectedFaces.size() > 1)
			errorMsg = SWMain.getMessagesBundle().getString("too_many_faces");
		else if (detectedFaces.isEmpty())
			errorMsg = SWMain.getMessagesBundle().getString("no_faces_detected");

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
		for (Rect detectedFaceRect : detectedFaces)
			Imgproc.rectangle(frame, detectedFaceRect, new Scalar(0, 255, 0), 3);

		SwingUtilities.invokeLater(() -> projectionScreen.updateProjectedImage(frame));
	}

	/**
	 * Method invoked whenever the component is not visible anymore
	 */
	@Override
	public void onShown()
	{
		if (!SWMain.getCVController().getCamCapture().isOpened())
			SWMain.getCVController().open();

		grabberService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
		long period = (long) (1.0 / FPS * 1000);
		grabberService.scheduleAtFixedRate(this::showMirror, 0, period, TimeUnit.MILLISECONDS);
		Loggers.getDebugLogger().log(Level.INFO, "Updating the projection screen each " + period + "ms");
	}

	/**
	 * Method to be invoked when the section is shown
	 */
	@Override
	public void onHide()
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
}
