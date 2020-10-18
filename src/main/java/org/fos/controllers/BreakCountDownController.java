package org.fos.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class BreakCountDownController {
    @FXML
    public Label breakTimeLabel;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Label remainingTimeLabel;

    public BreakCountDownController() {

    }

    public void setBreakTimeMessage(final String message) {
        this.breakTimeLabel.setText(message);
    }

    public void updateRemainingSeconds(final int remaining_s, final double progress) {
        this.remainingTimeLabel.setText("Remaining time: " + remaining_s + " s");
        this.progressBar.setProgress(progress);
    }
}
