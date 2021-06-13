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

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.fos.sw.SWMain;
import org.fos.sw.core.Loggers;
import org.fos.sw.gui.Hideable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

public class ProjectionScreen extends Canvas implements Hideable
{
	private Graphics graphics; // the graphics object for this Canvas

	private Image technicalDifficultiesImg;

	public ProjectionScreen()
	{

	}

	public void initComponents(@NotNull Mat frame)
	{
		this.setSize(frame.width(), frame.height());
	}

	/**
	 * Updates the projected image and repaints it on the canvas
	 *
	 * @param frame the frame to be projected, it will be automatically converted from {@link Mat} to {@link Image}
	 */
	public void updateProjectedImage(@Nullable Mat frame)
	{
		assert SwingUtilities.isEventDispatchThread();
		//System.out.println("Is AWT: " + SwingUtilities.isEventDispatchThread());

		if (graphics == null && ((graphics = this.getGraphics()) == null))
			return;

		if (frame == null || frame.empty()) {
			graphics.drawImage(
				this.getErrorImage(),
				0,
				0,
				this
			);
			return;
		}

		Image projectedImage = HighGui.toBufferedImage(frame);
		graphics.drawImage(projectedImage, 0, 0, this);
	}

	private Image getErrorImage()
	{
		if (this.technicalDifficultiesImg == null) {
			String imgPath = "/resources/media/technical_difficulties.jpg";
			try (InputStream imgIS =
				     SWMain.getFileAsStream("/resources/media/technical_difficulties.jpg")
			) {
				this.technicalDifficultiesImg = ImageIO.read(imgIS);
			} catch (IOException | IllegalArgumentException e) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Error while trying to read the error image!" + imgPath,
					e
				);
			}
		}

		return this.technicalDifficultiesImg;

	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		this.setVisible(enabled);
		this.revalidate();
		this.repaint();
	}

	/**
	 * Method to be invoked when the section is shown
	 */
	@Override
	public void onHide()
	{
		this.graphics = null;
	}
}
