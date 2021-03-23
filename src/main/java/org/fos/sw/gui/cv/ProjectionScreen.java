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

package org.fos.sw.gui.cv;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

public class ProjectionScreen extends Canvas
{
	private Image projectedImage;

	public ProjectionScreen()
	{

	}

	/**
	 * Updates the projected image and repaints it on the canvas
	 *
	 * @param frame the frame to be projected, it will be automatically converted from {@link Mat} to {@link Image}
	 */
	public void updateProjectedImage(Mat frame)
	{
		this.projectedImage = HighGui.toBufferedImage(frame);
		this.revalidate();
		this.repaint();
	}


	@Override
	public void paint(Graphics g)
	{
		super.paint(g);

		g.drawImage(projectedImage, 0, 0, this);
	}
}
