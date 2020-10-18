package org.fos.controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.fos.I18nable;
import org.fos.SWMain;

import javax.swing.Action;
import java.net.URL;
import java.util.ResourceBundle;

public class TimeForABreakNotificationController implements Initializable, I18nable {
    @FXML
    public Button takeBreakButton;
    @FXML
    public Button dismissBreakButton;
    @FXML
    private Label timeForABreakLabel;

    public TimeForABreakNotificationController() {

    }

    public void setTimeForABreakMessage(final String message) {
        this.timeForABreakLabel.setText(message);
    }

    public void setOnTakeBreakAction(EventHandler<ActionEvent> action) {
        this.takeBreakButton.setOnAction(action);
    }

    public void setOnDismissBreakAction(EventHandler<ActionEvent> action) {
        this.dismissBreakButton.setOnAction(action);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.setTextAccordingToLanguage();
    }

    @Override
    public void setTextAccordingToLanguage() {
        this.takeBreakButton.setText(SWMain.messagesBundle.getString("notification_take_break"));
        this.dismissBreakButton.setText(SWMain.messagesBundle.getString("notification_dismiss_break"));
    }
}
