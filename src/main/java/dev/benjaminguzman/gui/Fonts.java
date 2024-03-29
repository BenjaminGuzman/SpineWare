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

package dev.benjaminguzman.gui;

import java.awt.*;

public class Fonts
{
	public static final Font SANS_SERIF_BOLD_12 = new Font(Font.SANS_SERIF, Font.BOLD, 12);
	public static final Font SANS_SERIF_BOLD_15 = new Font(Font.SANS_SERIF, Font.BOLD, 15);
	public static final Font MONOSPACED_BOLD_12 = new Font(Font.MONOSPACED, Font.BOLD, 12);
	public static final Font MONOSPACED_BOLD_24 = new Font(Font.MONOSPACED, Font.BOLD, 24);

	public static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 24);
	public static final Font FULL_DESCRIPTION_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
	public static final Font DESCRIPTION_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);

	/*static {
		String fontPath = "/resources/fonts/Timeburner/Timeburner.ttf";
		try {
			SANS_SERIF_BOLD_12 = Font.createFont(Font.TRUETYPE_FONT, SWMain.getFileAsStream(fontPath));
		} catch (FontFormatException | IOException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Couldn't load font: " + fontPath,
				e
			);
		}
	}*/
}
