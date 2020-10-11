package org.fos.alerts;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import org.fos.SWMain;

public class SWAlert extends Alert {
    public SWAlert(AlertType alertType) {
        super(alertType);
        this.init();
    }

    public SWAlert(AlertType alertType, String contentText, ButtonType... buttons) {
        super(alertType, contentText, buttons);
        this.init();
    }

    /**
     * Loads a custom stylesheet to the dialog pane
     */
    private void init() {
        DialogPane dialogPane = this.getDialogPane();
        SWMain.addGeneralStyleSheet(dialogPane);

        if (SWMain.primaryStage != null) {
            this.initModality(Modality.APPLICATION_MODAL);
            this.initOwner(SWMain.primaryStage);
        }
    }
}
