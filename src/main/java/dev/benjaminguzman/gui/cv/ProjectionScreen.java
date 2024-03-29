/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.net>
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

package dev.benjaminguzman.gui.cv;

import dev.benjaminguzman.SpineWare;
import dev.benjaminguzman.core.Loggers;
import dev.benjaminguzman.gui.Hideable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

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
	 * @param frame the frame to be projected, it will be automatically converted from {@link Mat} to {@link Image}.
	 *              The frame will be released with {@link Mat#release()} so you'd better not use it after this
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
		projectedImage.flush();
	}

	private Image getErrorImage()
	{
		if (this.technicalDifficultiesImg == null) {
			String imgPath = "/resources/media/technical_difficulties.jpg";
			try (InputStream imgIS =
				     SpineWare.getFileAsStream("/resources/media/technical_difficulties.jpg")
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
