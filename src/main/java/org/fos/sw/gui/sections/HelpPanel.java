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

package org.fos.sw.gui.sections;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import org.fos.sw.Loggers;
import org.fos.sw.SWMain;

public class HelpPanel extends JScrollPane
{
	public HelpPanel()
	{
		super();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());

		// spineware logo
		ImageIcon swLogoImageIcon = null;
		try (InputStream inputStreamSWLogo = SWMain.getFileAsStream("/resources/media/SpineWare_white.png")) {
			Image img = ImageIO.read(inputStreamSWLogo);
			img = img.getScaledInstance(400, 120, Image.SCALE_AREA_AVERAGING);
			swLogoImageIcon = new ImageIcon(img);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JLabel swLogoImageLabel;
		if (swLogoImageIcon != null)
			swLogoImageLabel = new JLabel(swLogoImageIcon);
		else
			swLogoImageLabel = new JLabel("SpineWare");

		// copyright message
		JLabel copyrightLabel = new JLabel("Copyright (c) 2020, 2021. Benjamín Guzmán <9benjaminguzman@gmail" +
			".com>");
		copyrightLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// GPLv3 license
		JLabel gplLicenseLabel = new JLabel("This software is licensed under the GNU GPLv3 license");
		gplLicenseLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		gplLicenseLabel.setHorizontalAlignment(SwingConstants.CENTER);

		try (InputStream inputStreamGPLLogo = SWMain.getFileAsStream("/resources/media/gplv3-136x68.png")) {
			Image img = ImageIO.read(inputStreamGPLLogo);
			gplLicenseLabel.setIcon(new ImageIcon(img));
			gplLicenseLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// source code, manuals & acknowledgements
		JEditorPane moreEditorPane = new JEditorPane();
		moreEditorPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
		moreEditorPane.setEditable(false);
		moreEditorPane.setText(SWMain.getMessagesBundle().getString("help_text"));
		moreEditorPane.addHyperlinkListener(this::onClickHyperLink);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.ipadx = 30;
		gridBagConstraints.ipady = 30;

		// add spineware logo
		gridBagConstraints.gridy = 0;
		mainPanel.add(swLogoImageLabel, gridBagConstraints);

		// add copyright
		++gridBagConstraints.gridy;
		mainPanel.add(copyrightLabel, gridBagConstraints);

		// add GPLv3 image
		++gridBagConstraints.gridy;
		mainPanel.add(gplLicenseLabel, gridBagConstraints);

		// add the editor panel
		++gridBagConstraints.gridy;
		mainPanel.add(moreEditorPane, gridBagConstraints);

		this.setViewportView(mainPanel);
		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.getHorizontalScrollBar().setUnitIncrement(16);
		this.getVerticalScrollBar().setUnitIncrement(16);
	}

	private void onClickHyperLink(HyperlinkEvent evt)
	{
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(evt.getURL().toURI());
				} catch (IOException | URISyntaxException | UnsupportedOperationException e) {
					JOptionPane.showMessageDialog(
						null,
						SWMain.getMessagesBundle().getString("error_couldnt_open_browser"),
						"Error",
						JOptionPane.ERROR_MESSAGE
					);
					Loggers.getErrorLogger().log(Level.SEVERE, "Couldn't open browser", e);
				}
			} else
				JOptionPane.showMessageDialog(
					null,
					SWMain.getMessagesBundle().getString("error_couldnt_open_browser"),
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
		}
	}
}
