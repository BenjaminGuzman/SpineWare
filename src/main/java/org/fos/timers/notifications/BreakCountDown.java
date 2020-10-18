package org.fos.timers.notifications;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
