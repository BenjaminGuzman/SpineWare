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

package net.benjaminguzman.gui.notifications;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import net.benjaminguzman.core.Loggers;
import net.benjaminguzman.core.NotificationLocation;
import net.benjaminguzman.SWMain;
import net.benjaminguzman.gui.Initializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNotification extends JDialog implements Initializable
{
	protected static ImageIcon swIcon; // static to avoid reading the image each time the notification is shown
	private final int dispose_timeout_ms;

	@NotNull
	protected final JLabel swIconLabel; // icon that contains the SW image
	@NotNull
	protected final JButton closeBtn; // button to close the notification
	@NotNull
	protected final JPanel mainPanel; // panel that will contain everything in the JDialog
	@NotNull
	private final NotificationLocation notificationLocation;

	/**
	 * runnable to execute when the notification is shown
	 * this class will call {@link Runnable#run()} when {@link #showJDialog()} is called
	 */
	@Nullable
	protected Runnable onShown;

	/**
	 * runnable to execute when the notification is disposed
	 * this class will NOT call the {@link Runnable#run()} when {@link #dispose()} is called
	 */
	@Nullable
	protected Runnable onDisposed;

	@Nullable
	private Timer timeoutTimer; // timer to automatically dispose the dialog

	public AbstractNotification()
	{
		this(20_000, NotificationLocation.BOTTOM_RIGHT);
	}

	public AbstractNotification(NotificationLocation notificationLocation)
	{
		this(20_000, notificationLocation);
	}

	/**
	 * Creates the notification with the given dispose timeout
	 * this will set the main panel with a new grid bag layout and new empty border
	 * this will also load the SW icon
	 *
	 * @param dispose_timeout_ms   number of milliseconds to wait before the dialog is automatically disposed
	 *                             if this is less than or equal to 0, the notification will be never disposed
	 * @param notificationLocation this tells where to put the notification, check this class static constants for details
	 */
	public AbstractNotification(int dispose_timeout_ms, @NotNull NotificationLocation notificationLocation)
	{
		super((Window) null); // pass null to create an unowned JDialog
		assert SwingUtilities.isEventDispatchThread();

		this.dispose_timeout_ms = dispose_timeout_ms;
		this.notificationLocation = notificationLocation;

		this.mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// create SW icon
		this.loadSWIcon();
		this.swIconLabel = new JLabel();
		if (AbstractNotification.swIcon != null)
			swIconLabel.setIcon(AbstractNotification.swIcon);
		else
			swIconLabel.setText("SW");

		// add close button
		this.closeBtn = new JButton("X");
		this.closeBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		this.closeBtn.setBorderPainted(false);
		this.closeBtn.setContentAreaFilled(false);
		this.closeBtn.setFocusPainted(false);
		this.closeBtn.setOpaque(false);

		// configure the JDialog
		this.setContentPane(this.mainPanel);
		this.setUndecorated(true);
		this.setResizable(false);
		this.setType(Type.POPUP);
		this.setAlwaysOnTop(true);
		this.setAutoRequestFocus(false); // if you're working, this alert should not make you loose your focus in whatever you're doing
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // even though this is not likely to happen, is a good practice to have it
	}

	/**
	 * Use this function to initialize and add the components to the {@link #mainPanel}
	 */
	public abstract void initComponents();

	/**
	 * Method all derived classes should call once they've added content to the main panel
	 * this method will pack, show the notification and set the timer to automatically dismiss the notification
	 */
	protected void showJDialog()
	{
		assert SwingUtilities.isEventDispatchThread();
		if (onShown != null)
			onShown.run();

		this.pack();

		Point notificationLocation = this.getNotificationPointLocation();

		this.setLocation(notificationLocation);
		this.setVisible(true); // TODO: add an sliding animation to the notification

		// create timer to automatically dismiss the notification after some time
		if (this.dispose_timeout_ms > 0) {
			this.timeoutTimer = new Timer(this.dispose_timeout_ms, (ActionEvent evt) -> this.dispose());
			this.timeoutTimer.start();
		}
	}

	/**
	 * Load the SW icon, if it is loaded, this method will do nothing
	 */
	private void loadSWIcon()
	{
		if (AbstractNotification.swIcon != null)
			return;

		String iconImagePath = "/resources/media/SW_white.png";
		Image icon;
		try (InputStream iconInputStream = SWMain.getFileAsStream(iconImagePath)) {
			icon = ImageIO.read(iconInputStream);
		} catch (IOException e) {
			Loggers.getErrorLogger().log(Level.SEVERE, "Error while reading SW icon in path: " + iconImagePath, e);
			return;
		}
		icon = icon.getScaledInstance(67, 59, Image.SCALE_AREA_AVERAGING);

		AbstractNotification.swIcon = new ImageIcon(icon);
	}

	private Point getNotificationPointLocation()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension notificationSize = this.getSize();

		int x = 0, y = 50;

		// if the preferred location is at the bottom
		if (this.notificationLocation == NotificationLocation.BOTTOM_RIGHT
			|| this.notificationLocation == NotificationLocation.BOTTOM_LEFT)
			y = screenSize.height - notificationSize.height - 50;

		// if the preferred location is to the right
		if (this.notificationLocation == NotificationLocation.TOP_RIGHT
			|| this.notificationLocation == NotificationLocation.BOTTOM_RIGHT)
			x = screenSize.width - notificationSize.width;

		return new Point(x, y);
	}

	/**
	 * Get the dispose timeout of this notification
	 * The dispose timeout is in milliseconds
	 * After that time, the notification will be automatically disposed
	 *
	 * @return the dispose timeout
	 */
	public int getDisposeTimeout()
	{
		return this.dispose_timeout_ms;
	}


	public AbstractNotification setOnShown(@Nullable Runnable onShown)
	{
		this.onShown = onShown;
		return this;
	}

	public void setOnDisposed(@Nullable Runnable onDisposed)
	{
		this.onDisposed = onDisposed;
	}

	@Override
	public void dispose()
	{
		assert SwingUtilities.isEventDispatchThread();
		super.dispose();
		if (this.timeoutTimer != null)
			this.timeoutTimer.stop();
	}

	@Override
	public String toString()
	{
		return "AbstractNotification{" +
			"dispose_timeout_ms=" + dispose_timeout_ms +
			", notificationLocation=" + notificationLocation +
			", swIconLabel=" + swIconLabel +
			", mainPanel=" + mainPanel +
			", timeoutTimer=" + timeoutTimer +
			'}';
	}
}
