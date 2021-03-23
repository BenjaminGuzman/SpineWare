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

import java.awt.LayoutManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.JPanel;
import org.fos.sw.Loggers;
import org.fos.sw.SWMain;
import org.fos.sw.cv.CVController;
import org.fos.sw.utils.DaemonThreadFactory;
import org.opencv.core.Mat;

public class CVConfigPanel extends JPanel
{
	/**
	 * Number of frames per seconds to show in the projection (mirror) screen
	 */
	private static final int FPS = 30;

	private final ProjectionScreen projectionScreen;

	private final CVController cvController;

	public CVConfigPanel()
	{
		super();
		cvController = SWMain.getCVController();
		projectionScreen = new ProjectionScreen();
	}

	public CVConfigPanel(LayoutManager layoutManager)
	{
		super(layoutManager);
		cvController = SWMain.getCVController();
		projectionScreen = new ProjectionScreen();
	}

	public void initComponents()
	{
		this.add(this.projectionScreen);
		this.projectionScreen.setSize(640, 720);

		ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
		long period = (long) (1.0 / FPS * 1000);
		timer.scheduleAtFixedRate(this::showMirror, 0, period, TimeUnit.MILLISECONDS);
		Loggers.getDebugLogger().log(Level.INFO, "Updating the projection screen each " + period + "ms");
	}

	/**
	 * Grabs a frame from the webcam and displays it in the projection screen
	 * This function is intended to be called repeatedly
	 */
	private void showMirror()
	{
		Mat frame = cvController.captureFrame();
		if (frame == null || frame.empty()) {
			Loggers.getErrorLogger().log(Level.WARNING, "Error capturing frame");
			return;
		}

		projectionScreen.updateProjectedImage(frame);
		System.out.println(frame);
	}
}
