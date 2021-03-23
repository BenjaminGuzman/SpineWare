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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.fos.sw.Loggers;
import org.fos.sw.SWMain;
import org.fos.sw.gui.notifications.StartUpNotification;
import org.fos.sw.gui.sections.AbstractSection;
import org.fos.sw.gui.sections.BreaksPanel;
import org.fos.sw.gui.sections.HelpPanel;
import org.fos.sw.gui.sections.PostureChecker;
import org.fos.sw.timers.TimersManager;

public class MainFrame extends JFrame
{
	private static final short BREAKS_PANEL_CACHE_IDX = 0;
	private static final short POSTURE_PANEL_CACHE_IDX = 1;
	private static final short HELP_PANEL_CACHE_IDX = 2;

	private final AbstractSection[] mainPanelContentCaches = new AbstractSection[3];
	private JComponent activeContentPanel = null;
	private JPanel mainContentPanel = null;
	private TrayIcon trayIcon;

	public MainFrame()
	{
		super("SpineWare");
		this.configSysTray();

		this.setIconImage(SWMain.getSWIcon());

		this.setContentPane(this.createMainPanel());

		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		this.setMinimumSize(new Dimension(800, 600));
		this.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);

		// if this is the first time the user opens the application, show the jframe
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		boolean first_time_opened = prefs.getBoolean("first time opened", true);
		if (first_time_opened) {
			this.setVisible(true);
			prefs.putBoolean("first time opened", false);
		} else
			new StartUpNotification();
	}

	/**
	 * Creates the main panel, which contains to the left the main menu
	 * and to the right the current active panel
	 *
	 * @return the panel that contains both panels
	 */
	public JPanel createMainPanel()
	{
		JPanel rootContentPanel = new JPanel(new BorderLayout());
		this.mainContentPanel = new JPanel(new BorderLayout());

		// menu panel
		rootContentPanel.add(this.createMenuPanel(), BorderLayout.WEST);

		// main panel
		rootContentPanel.add(this.mainContentPanel, BorderLayout.CENTER);

		return rootContentPanel;
	}

	/**
	 * Creates the left menu panel shown in the main screen
	 * The panel contains the SpineWare logo and all the buttons
	 * The buttons already have the corresponding listener
	 *
	 * @return the JPanel that contains all this element
	 */
	public JPanel createMenuPanel()
	{
		final Font buttonFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

		JPanel menuPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(10, 5, 10, 5);
		gridBagConstraints.anchor = GridBagConstraints.NORTH;

		// SpineWare logo image
		ImageIcon swLogoImageIcon = null;
		try (InputStream inputStreamSWLogo = SWMain.getFileAsStream("/resources/media/SpineWare_white.png")) {
			Image img = ImageIO.read(inputStreamSWLogo);
			img = img.getScaledInstance(150, 45, Image.SCALE_AREA_AVERAGING);
			swLogoImageIcon = new ImageIcon(img);
		} catch (IOException e) {
			Loggers.getErrorLogger().log(Level.SEVERE, "Couldn't load spineware logo", e);
		}

		JLabel swLogoImageLabel;
		if (swLogoImageIcon != null)
			swLogoImageLabel = new JLabel(swLogoImageIcon);
		else
			swLogoImageLabel = new JLabel("SpineWare");
		menuPanel.add(swLogoImageLabel, gridBagConstraints);

		// add all buttons
		String[] buttonsLabels = new String[]{"menu_breaks", "menu_posture", "menu_help"};
		String[] buttonsIconsPaths = new String[]{
			"timer_white_18dp.png",
			"self_improvement_white_18dp.png",
			"help_white_18dp.png"
		};
		ActionListener[] buttonsListeners = new ActionListener[]{
			this::onClickBreaksMenu,
			this::onClickPostureMenu,
			this::onClickHelpMenu
		};

		JButton button;
		Insets buttonInsets = new Insets(10, 10, 10, 10);
		for (short i = 0; i < buttonsIconsPaths.length; ++i) {
			button = new JButton(SWMain.getMessagesBundle().getString(buttonsLabels[i]));
			button.setIcon(SWMain.readAndScaleIcon("/resources/media/" + buttonsIconsPaths[i]));

			button.setFont(buttonFont);

			++gridBagConstraints.gridy;
			button.setMargin(buttonInsets);
			menuPanel.add(button, gridBagConstraints);

			button.addActionListener(buttonsListeners[i]);
		}

		return menuPanel;
	}

	/**
	 * Method invoked when the user clicks the break button in the main menu
	 * This method will swap the main content panel
	 *
	 * @param evt the event
	 */
	public void onClickBreaksMenu(final ActionEvent evt)
	{
		this.changePanel(BREAKS_PANEL_CACHE_IDX, BreaksPanel.class);
	}

	/**
	 * Method invoked when the user clicks the help button in the main menu
	 * This method will swap the main content panel
	 *
	 * @param evt the event
	 */
	public void onClickHelpMenu(final ActionEvent evt)
	{
		this.changePanel(HELP_PANEL_CACHE_IDX, HelpPanel.class);
	}

	public void onClickPostureMenu(final ActionEvent evt)
	{
		this.changePanel(POSTURE_PANEL_CACHE_IDX, PostureChecker.class);
	}

	/**
	 * Changes the current active panel
	 * If the panel is not in the cache, a new instance will be created and added to the cache
	 * If the panel is already active, this method will simply do nothing
	 * This method will also handle the removal and aggregation of the old and new panel in the main panel
	 *
	 * @param panel_cache_idx the index in the array of panels (cache) where the panel should be
	 * @param panelClass      the class of the panel
	 * @param <T>             class of the panel
	 */
	public <T extends AbstractSection> void changePanel(final short panel_cache_idx, final Class<T> panelClass)
	{
		if (panel_cache_idx < 0 || panel_cache_idx >= this.mainPanelContentCaches.length)
			throw new IllegalArgumentException("The panel cache idx should be between [0, "
				+ (this.mainPanelContentCaches.length - 1) + "]");

		boolean invoke_init_components = false;
		// if the panel is not in the cache, create it
		if (this.mainPanelContentCaches[panel_cache_idx] == null) {
			try {
				this.mainPanelContentCaches[panel_cache_idx] = panelClass.getConstructor().newInstance();
				this.mainPanelContentCaches[panel_cache_idx].setOwner(this);
				invoke_init_components = true;
			} catch (InstantiationException | IllegalAccessException
				| InvocationTargetException | NoSuchMethodException e) {
				Loggers.getErrorLogger().log(Level.SEVERE, "Error while creating the main content panel", e);
			}
			Loggers.getDebugLogger().log(Level.INFO, "Loaded panel, created instance of: " + panelClass.getName());
		}

		// if the panel is the active one, do nothing
		if (panelClass.isInstance(this.activeContentPanel)) {
			Loggers.getDebugLogger().log(Level.INFO, "The panel with class "
				+ panelClass.getName() + " is already shown");
			return;
		}

		if (activeContentPanel != null)
			mainContentPanel.removeAll(); // clear the content panel
		activeContentPanel = mainPanelContentCaches[panel_cache_idx];
		mainContentPanel.add(activeContentPanel, BorderLayout.CENTER);
		if (invoke_init_components)
			mainPanelContentCaches[panel_cache_idx].initComponents();
		mainContentPanel.revalidate();
		mainContentPanel.repaint();
	}

	/**
	 * Configures and sets the system tray
	 * this method will also set the jframe icon once it was read
	 * therefore, if you call this method you will NOT need to read and set the JFrame icon image
	 */
	private void configSysTray()
	{
		if (!SystemTray.isSupported()) {
			Loggers.getErrorLogger().severe("System Tray is not supported :(\nExiting now...");
			System.exit(1);
			return;
		}

		Image iconImage;
		String iconPath = "/resources/media/SW_white_black.min.png";
		try (InputStream iconInputStream = SWMain.getFileAsStream(iconPath)) {
			iconImage = ImageIO.read(iconInputStream);
		} catch (IOException e) {
			Loggers.getErrorLogger().log(Level.SEVERE, "Couldn't read image file", e);
			return;
		}

		SystemTray sysTray = SystemTray.getSystemTray();
		trayIcon = new TrayIcon(iconImage, "SpineWare");
		trayIcon.setImageAutoSize(true);
		try {
			sysTray.add(trayIcon);
		} catch (AWTException e) {
			Loggers.getErrorLogger().log(Level.SEVERE, "Couldn't add icon to system tray", e);
		}

		SysTrayMenu sysTrayMenu = new SysTrayMenu(
			this,
			(java.awt.event.ActionEvent evt) -> SWMain.exit(),
			(java.awt.event.ActionEvent evt) -> {
				this.setAlwaysOnTop(true);
				if (!this.isVisible())
					this.setVisible(true);
				this.requestFocus();
				this.requestFocusInWindow();
				this.setAlwaysOnTop(false);
			}
		);

		trayIcon.addMouseListener(new MouseListener()
		{
			private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			private final Dimension menuSize = sysTrayMenu.getSize();
			private final Dimension menuLocationOffsetDim = new Dimension(5, 5);

			@Override
			public void mouseClicked(MouseEvent e)
			{
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				/// if the pop up menu is shown, hide it
				if (sysTrayMenu.isVisible()) {
					sysTrayMenu.setVisible(false);
					return;
				}

				Point menuLocation = new Point();

				Point point = e.getLocationOnScreen();
				menuLocation.x = point.x + menuLocationOffsetDim.width;

				if (point.x + this.menuSize.width >= screenSize.width) // this is indeed right, do the maths
					menuLocation.x = point.x - this.menuSize.width - menuLocationOffsetDim.width;

				menuLocation.y = point.y - this.menuSize.height - menuLocationOffsetDim.height;
				if (point.y - this.menuSize.height <= 0) // this is indeed right, do the maths
					menuLocation.y = point.y + menuLocationOffsetDim.height;

				sysTrayMenu.setLocation(menuLocation);
				sysTrayMenu.setVisible(true);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
			}
		});

		// start the timers
		TimersManager.startMainLoop();
	}

	/**
	 * Shows or hides this {@code Window} depending on the value of parameter
	 * {@code b}.
	 * <p>
	 * If the frame is not showing a {@link AbstractSection}, this method will automatically load the
	 * {@link BreaksPanel}
	 */
	@Override
	public void setVisible(boolean b)
	{
		super.setVisible(b);
		if (b && activeContentPanel == null)
			this.changePanel(BREAKS_PANEL_CACHE_IDX, BreaksPanel.class);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		if (SystemTray.isSupported()) // remove the systray
			SystemTray.getSystemTray().remove(this.trayIcon);
	}
}
