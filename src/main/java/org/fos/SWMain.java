/*
 * Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
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
package org.fos;

import org.fos.core.TimersManager;
import org.fos.panels.BreaksPanel;
import org.fos.panels.HelpPanel;
import org.fos.timers.notifications.StartUpNotification;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.prefs.Preferences;

public class SWMain extends JFrame
{
	private static final short BREAKS_PANEL_CACHE_IDX = 0;
	private static final short HELP_PANEL_CACHE_IDX = 1;
	private static volatile ResourceBundle messagesBundle;
	private static volatile Image swIcon;

	private final JComponent[] mainPanelContentCaches = new JComponent[2];
	private JComponent activeContentPanel = null;
	private JPanel mainContentPanel = null;

	public SWMain()
	{
		super("SpineWare");
		this.configSysTray();

		// set the JFrame icon
		String iconPath = "/resources/media/SW_white.min.png";

		this.setIconImage(SWMain.getSWIcon());

		this.setContentPane(this.createMainPanel());

		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		this.setMinimumSize(new Dimension(600, 600));

		// if this is the first time the user opens the application, show the jframe
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		boolean first_time_opened = prefs.getBoolean("first time opened", true);
		if (first_time_opened) {
			this.setVisible(true); // do not show the jframe on start
			prefs.putBoolean("first time opened", false);
		} else
			new StartUpNotification();
	}

	public static void main(String[] args)
	{
		SWMain.changeMessagesBundle(Locale.getDefault());

		try {
			Loggers.init();
			TimersManager.init();
		} catch (TooManyListenersException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"You shouldn't be calling the init method on Loggers more than once",
				e
			);
		} catch (RuntimeException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"You shouldn't be calling the init method on TimersManager more than once",
				e
			);
		}

		com.formdev.flatlaf.FlatDarkLaf.install();

		SwingUtilities.invokeLater(SWMain::new);
		System.setProperty("awt.useSystemAAFontSettings", "on"); // use font antialiasing
	}

	/**
	 * Same method as {@link Class#getResourceAsStream(String) getResourceAsStream(String)}
	 * but, if the resource can't be loaded, a warning message is logged
	 *
	 * @param filePath the path of the image or file you want to read
	 * @return the stream if it can be loaded, null otherwise
	 */
	public static InputStream getFileAsStream(final String filePath)
	{
		InputStream inStream = SWMain.class.getResourceAsStream(filePath);
		if (inStream == null)
			Loggers.getErrorLogger().warning("Couldn't read file: " + filePath + " (probably doesn't exists)");
		return inStream;
	}

	/**
	 * Loads an image file using {@link #getFileAsStream(String)} and scales it to a size of 20x20
	 * (the size all icons inside this application must have)
	 *
	 * @param imgPath the path for the icon
	 * @return the loaded icon, or null if the resource couldn't be loaded
	 */
	public static ImageIcon readAndScaleIcon(final String imgPath)
	{
		try (InputStream iconIS = SWMain.getFileAsStream(imgPath)) {
			Image img = ImageIO.read(iconIS);
			img = img.getScaledInstance(20, 20, Image.SCALE_AREA_AVERAGING);
			return new ImageIcon(img);
		} catch (IOException | IllegalArgumentException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Error while trying to read & scale icon: " + imgPath,
				e
			);
		}
		return null;
	}

	/**
	 * Load the SW icon
	 *
	 * @return the loaded icon, if it couldn't be loaded, this method will return null
	 */
	synchronized public static Image getSWIcon()
	{
		if (SWMain.swIcon != null)
			return SWMain.swIcon;

		try (InputStream swIS = SWMain.getFileAsStream("/resources/media/SW_white.min.png")) {
			return (SWMain.swIcon = ImageIO.read(swIS));
		} catch (IOException | IllegalArgumentException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Error while trying to read the SW icon",
				e
			);
		}
		return null;
	}

	synchronized public static void changeMessagesBundle(final Locale locale)
	{
		ResourceBundle newBundle;

		try {
			newBundle = ResourceBundle.getBundle("resources.bundles.messages", locale);
		} catch (Exception e) {
			Loggers.getErrorLogger().log(Level.WARNING, "Could not load messages for locale: "
				+ locale + ". Using default US locale", e);
			newBundle = ResourceBundle.getBundle("resources.bundles.messages", Locale.US);
		}

		SWMain.messagesBundle = newBundle;
	}

	public static ResourceBundle getMessagesBundle()
	{
		return SWMain.messagesBundle;
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
		String[] buttonsLabels = new String[]{"menu_breaks", "menu_help"};
		String[] buttonsIconsPaths = new String[]{"timer_white_18dp.png", "help_white_18dp.png"};
		ActionListener[] buttonsListeners = new ActionListener[]{this::onClickBreaksMenu, this::onClickHelpMenu};

		JButton button;
		Insets buttonInsets = new Insets(10, 10, 10, 10);
		for (short i = 0; i < buttonsIconsPaths.length; ++i) {
			button = new JButton(messagesBundle.getString(buttonsLabels[i]));
			button.setIcon(SWMain.readAndScaleIcon("/resources/media/" + buttonsIconsPaths[i]));

			button.setFont(buttonFont);

			++gridBagConstraints.gridy;
			button.setMargin(buttonInsets);
			menuPanel.add(button, gridBagConstraints);

			button.addActionListener(buttonsListeners[i]);
		}

		this.changePanel(SWMain.BREAKS_PANEL_CACHE_IDX, BreaksPanel.class);

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
		this.changePanel(SWMain.BREAKS_PANEL_CACHE_IDX, BreaksPanel.class);
	}

	/**
	 * Method invoked when the user clicks the help button in the main menu
	 * This method will swap the main content panel
	 *
	 * @param evt the event
	 */
	public void onClickHelpMenu(final ActionEvent evt)
	{
		this.changePanel(SWMain.HELP_PANEL_CACHE_IDX, HelpPanel.class);
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
	public <T extends JComponent> void changePanel(final short panel_cache_idx, final Class<T> panelClass)
	{
		if (panel_cache_idx < 0 || panel_cache_idx >= this.mainPanelContentCaches.length)
			throw new IllegalArgumentException("The panel cache idx should be between [0, "
				+ (this.mainPanelContentCaches.length - 1) + "]");
		// if the panel is not in the cache, create it
		if (this.mainPanelContentCaches[panel_cache_idx] == null) {
			try {
				this.mainPanelContentCaches[panel_cache_idx] = panelClass.getConstructor().newInstance();
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

		if (this.activeContentPanel != null)
			this.mainContentPanel.removeAll(); // clear the content panel
		this.activeContentPanel = this.mainPanelContentCaches[panel_cache_idx];
		this.mainContentPanel.add(this.activeContentPanel, BorderLayout.CENTER);
		this.mainContentPanel.revalidate();
		this.mainContentPanel.repaint();
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
		TrayIcon trayIcon = new TrayIcon(iconImage, "SpineWare");
		trayIcon.setImageAutoSize(true);
		try {
			sysTray.add(trayIcon);
		} catch (AWTException e) {
			Loggers.getErrorLogger().log(Level.SEVERE, "Couldn't add icon to system tray", e);
		}

		SysTrayMenu sysTrayMenu = new SysTrayMenu(
			this,
			(java.awt.event.ActionEvent evt) -> {
				sysTray.remove(trayIcon);
				this.exit();
			},
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
		TimersManager.createExecutorsFromPrefs();
	}

	public void exit()
	{
		Loggers.getDebugLogger().log(Level.INFO, "Shutting down...");
		this.dispose(); // close the main JFrame
		TimersManager.killAllTimers(); // stop all timers

		//System.exit(0); // this is not needed, when closing all windows and killing all timers, the JVM
		// should exit gracefully
	}
}
