/*
 * Copyright © 2020 Benjamín Guzmán
 * Author: Benjamín Guzmán <9benjaminguzman@gmail.com>
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
/*
package org.fos.timers.notifications;

import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.controllers.BreakCountDownController;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class BreakCountDown extends Dialog<Void> {

	private final int break_time_s;
	private int remaining_s;

	private Timer closeTimer;

	private BreakCountDownController controller;

	public BreakCountDown(final String breakTimeMessage, int break_time_s) {
		super();
		this.break_time_s = break_time_s;
		this.remaining_s = break_time_s;

		// load the FXML
		FXMLLoader loader = SWMain.loadFXML("/resources/views/BreakCountDown.fxml");
		if (loader == null)
			return;

		Parent root;
		try {
			root = loader.load();
		} catch (IOException e) {
			Loggers.errorLogger.log(Level.SEVERE, "Error while loading an FXML", e);
			return;
		}

		// configure the controller for the fxml
		this.controller = loader.getController();
		this.controller.setBreakTimeMessage(breakTimeMessage);

		DialogPane dialogPane = this.getDialogPane();

		// add buttons
		ButtonType dismissBreak = new ButtonType("Dismiss break", ButtonBar.ButtonData.CANCEL_CLOSE); // TODO: i18n
		dialogPane.getButtonTypes().add(dismissBreak);

		// set the contents
		dialogPane.setContent(root);

		// set custom styles
		this.showAndWait();
		dialogPane.getStylesheets().addAll("/resources/styles/general.css", "/resources/styles/notification.css");

		this.setOnShowing((DialogEvent evt) -> {
			this.closeTimer = new Timer();
			this.closeTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (BreakCountDown.this.remaining_s <= 0) {
						BreakCountDown.this.closeTimer.cancel();
						Platform.runLater(BreakCountDown.this::close);
						return;
					}

					BreakCountDown.this.remaining_s -= 1;
					Platform.runLater(() -> {
						BreakCountDown.this.controller.updateRemainingSeconds(BreakCountDown.this.remaining_s, BreakCountDown.this.remaining_s / (float)BreakCountDown.this.break_time_s);
					});
				}
			}, 0, 1000); // do the countdown each second
		});
		// cancel the timer when the dialog is closed
		this.setOnCloseRequest((DialogEvent e) -> {
			if (this.closeTimer != null)
				this.closeTimer.cancel();
		});
	}
}
*/