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
package org.fos.sw;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.management.InstanceAlreadyExistsException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.fos.sw.core.Loggers;
import org.fos.sw.cv.CVManager;
import org.fos.sw.cv.CVUtils;
import org.fos.sw.gui.MainFrame;
import org.fos.sw.timers.TimersManager;

public class SWMain
{
	public static ResourceBundle messagesBundle;
	private static CVUtils cvUtils;
	private static Image swIcon;
	private static final String OS = System.getProperty("os.name").toLowerCase();
	public static boolean IS_WINDOWS = OS.contains("win");

	private static MainFrame mainFrame;

	private SWMain() throws InstantiationException
	{
		throw new InstantiationException("You cannot instantiate this class");
	}

	public static void main(String[] args)
	{
		Locale locale = Locale.getDefault();

		// parse cli options
		Options opts = new Options();
		opts.addOption("v", "version", false, "Show the SpineWare version and exit");
		opts.addOption("l", "lang", true, "Specify the GUI language as an ISO ISO 3166-1 alpha-2, e. g. " +
			"\"en\" to specify english, \"es\" to specify español");
		opts.addOption("r", "rm-lock", false, "Remove the SW lock before start. Use this option if " +
			"SpineWare does not start and says there is already an instance running.");

		CommandLineParser cliParser = new DefaultParser();
		try {
			CommandLine cli = cliParser.parse(opts, args);
			if (cli.hasOption('v')) {
				System.out.println("SpineWare version: " + SWMain.class.getPackage()
				                                                       .getImplementationVersion());
				return;
			}
			if (cli.hasOption('l')) {
				String lang = cli.getOptionValue('l');
				locale = new Locale(lang.toLowerCase());
			}
			if (cli.hasOption('r')) {
				String tmpDir = System.getProperty("java.io.tmpdir");
				if (Paths.get(tmpDir, "sw.lock").toFile().delete())
					System.out.println("Lock removed prior start.");
				else
					System.out.println("Lock could not be removed.");
			}
		} catch (ParseException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Error while parsing options",
				e
			);
		}

		if (!ensureSingleJVMInstance())
			return;

		// use font antialiasing
		System.setProperty("awt.useSystemAAFontSettings", "lcd");
		System.setProperty("swing.aatext", "true");

		SWMain.changeMessagesBundle(locale);

		com.formdev.flatlaf.FlatDarkLaf.install();

		// add shutdown hook for a clean shutdown
		// kill all timers, java should take care of the gui, you take care of the timers
		// this is probably not needed as the JVM GC should collect free resources on exit, including threads
		// but still it is good to have it
		Runtime.getRuntime().addShutdownHook(new Thread(SWMain::exit));

		try {
			Loggers.init();
			TimersManager.init();
			CVManager.init();
		} catch (TooManyListenersException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"You shouldn't be calling the init method on Loggers more than once",
				e
			);
		} catch (RuntimeException | InstantiationException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"You shouldn't be calling the init method on TimersManager more than once",
				e
			);
		}

		SwingUtilities.invokeLater(() -> mainFrame = new MainFrame());
	}

	/**
	 * Ensures there is a single jvm instance executing spineware
	 * <p>
	 * If there is another JVM instance, this method will show an alert to the user
	 *
	 * @return true if there is a single jvm instance running spineware, false otherwise
	 */
	private static boolean ensureSingleJVMInstance()
	{
		// see if another instance of SpineWare is already running
		String tmpDir = System.getProperty("java.io.tmpdir");

		Path lockFilePath = Paths.get(tmpDir, "sw.lock");
		if (lockFilePath.toFile().exists()) {
			SWMain.showErrorAlert("SpineWare is already running", "SpineWare is running");
			return false;
		}

		try {
			// even though this file has no permissions, it is possible to delete it, try with sudo
			Set<PosixFilePermission> noPermissionsToAnyone = PosixFilePermissions.fromString(
				"---------"
			);
			Files.createFile(
				lockFilePath,
				PosixFilePermissions.asFileAttribute(noPermissionsToAnyone)
			).toFile().deleteOnExit();
		} catch (IOException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Error while creating sw.lock file",
				e
			);
		} catch (UnsupportedOperationException e) {
			Loggers.getErrorLogger().log(
				Level.SEVERE,
				"Why are you using a system not POSIX-like/complaint? It is more difficult to write " +
					"software to your platform. SpineWare may not work well in your system",
				e
			);
		}

		return true;
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
		} catch (MissingResourceException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Could not load messages for locale: "
					+ locale + " (probably there are no translations yet?). Using default ENGLISH" +
					" locale",
				e
			);
			newBundle = ResourceBundle.getBundle("resources.bundles.messages", Locale.ENGLISH);
		}

		SWMain.messagesBundle = newBundle;
	}

	synchronized public static CVUtils getCVUtils()
	{
		if (cvUtils == null)
			try {
				cvUtils = new CVUtils();
			} catch (InstanceAlreadyExistsException e) {
				Loggers.getErrorLogger().log(Level.WARNING, "Error", e);
			}

		if (!cvUtils.getCamCapture().isOpened())
			cvUtils.open();

		return cvUtils;
	}

	/**
	 * Show a JOptionPane to the user with type {@link JOptionPane#ERROR_MESSAGE}
	 * If the SW could be loaded the alert will contain it
	 * <p>
	 * This will check if the method is invoked in the correct thread, if it is the alert is shown
	 * if doesn't, it will show the alert in the correct thread
	 *
	 * @param message the message for the JOptionPane
	 * @param title   the title for the JOptionPane
	 */
	public static void showErrorAlert(String message, String title)
	{
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(() -> _showErrorAlert(message, title));
		else
			_showErrorAlert(message, title);

	}

	/**
	 * Function to exit gracefully from the jvm
	 */
	public static void exit()
	{
		Loggers.getDebugLogger().log(Level.INFO, "Shutting down...");
		if (mainFrame != null)
			mainFrame.dispose(); // close the main JFrame
		TimersManager.shutdownAllThreads(); // shutdown all threads
		TimersManager.killAllTimers(); // stop all timers
		CVManager.stopCVLoop();
		if (cvUtils != null)
			cvUtils.close(); // close the web cam

		//System.exit(0); // this is not needed, when closing all windows and killing all timers, the JVM
		// should exit gracefully
	}

	/**
	 * The actual thread-safe version of {@link #showErrorAlert(String, String)}
	 *
	 * @param message same as in {@link #showErrorAlert(String, String)}
	 * @param title   same as in {@link #showErrorAlert(String, String)}
	 */
	private static void _showErrorAlert(String message, String title)
	{
		assert SwingUtilities.isEventDispatchThread();

		Image swImg = SWMain.getSWIcon();

		JOptionPane.showConfirmDialog(
			null,
			message,
			title,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE,
			swImg != null ? new ImageIcon(swImg) : null
		);
	}
}
