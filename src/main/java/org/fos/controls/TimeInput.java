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
package org.fos.controls;
/*
import org.fos.I18nable;
import org.fos.SWMain;
import org.fos.controllers.BreaksController;

import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TimeInput extends GridPane implements Initializable, I18nable {
    @FXML
    private Spinner<Integer> hoursSpinner;
    @FXML
    private Spinner<Integer> minutesSpinner;
    @FXML
    private Spinner<Integer> secondsSpinner;

    @FXML
    private Label hoursSpinnerLabel;
    @FXML
    private Label minutesSpinnerLabel;
    @FXML
    private Label secondsSpinnerLabel;

    @FXML
    private Label inputLabel;
    @FXML
    private Label smallMessageLabel;

    private String defaultMessage = "";

    public static enum MESSAGE_TYPE {
        SUCCESS,
        ERROR,
        WARNING,
        NEUTRAL
    };

    public TimeInput() throws IOException {
        String fxmlPath = "/resources/controls/TimeInput.fxml";
        URL fxmlURL = this.getClass().getResource(fxmlPath);

        if (fxmlURL == null)
            throw new MissingResourceException("Resource " + fxmlPath + " is missing", this.getClass().getName(), fxmlPath);

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        fxmlLoader.load();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.setTextAccordingToLanguage();

        this.hoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 16, 0, 1));
        this.minutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 1));
        this.secondsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 1));
    }

    public void setInputLabelText(String label) {
        this.inputLabel.setText(label);
    }

    /**
     * Sets the default small message (the one shown below the input)
     * Besides setting the default small message
     * this method will also show the message
     * @param defaultMessage the default message
     *//*
    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
        this.setSmallMessageLabelToDefault();
    }

    /**
     * Sets the small message label
     * @param message the message to show
     * @param messageType the type of the message to show, the color depends on this
     *//*
    public void setSmallMessageLabelText(String message, MESSAGE_TYPE messageType) {
        this.smallMessageLabel.setText(message);

        this.smallMessageLabel.getStyleClass().removeAll("fail-text", "success-text", "warning-text");

        if (messageType == MESSAGE_TYPE.ERROR)
            this.smallMessageLabel.getStyleClass().add("fail-text");
        else if (messageType == MESSAGE_TYPE.SUCCESS)
            this.smallMessageLabel.getStyleClass().add("success-text");
        else if (messageType == MESSAGE_TYPE.WARNING)
            this.smallMessageLabel.getStyleClass().add("warning-text");
    }

    public void setSmallMessageLabelToDefault() {
        this.setSmallMessageLabelText(this.defaultMessage, MESSAGE_TYPE.NEUTRAL);
    }

    @Override
    public void setTextAccordingToLanguage() {
        this.hoursSpinnerLabel.setText(SWMain.messagesBundle.getString("hours"));
        this.minutesSpinnerLabel.setText(SWMain.messagesBundle.getString("minutes"));
        this.secondsSpinnerLabel.setText(SWMain.messagesBundle.getString("seconds"));
    }

    /**
     * Converts the given amount of seconds to hours, minutes and seconds
     * @param h_m_s_as_seconds the hours minutes and seconds as seconds
     * @return an array of ints of length 3, index 0 -> hours, index 1 -> minutes, index 2 -> seconds
     *//*
    public static int[] seconds2HoursMinutesSeconds(int h_m_s_as_seconds) {
        int seconds = h_m_s_as_seconds % 60;
        int hours_and_minutes_as_seconds = h_m_s_as_seconds - seconds;

        int minutes = (hours_and_minutes_as_seconds / 60) % 60;
        int hours_as_seconds = hours_and_minutes_as_seconds - minutes;

        int hours = hours_as_seconds / 60 / 60; // no modulus because it should be less than 24, also no need to call Math.floor

        return new int[] {hours, minutes, seconds};
    }

    public static String seconds2HoursMinutesSecondsAsString(int h_m_s_as_seconds) {
        int[] h_m_s_time = TimeInput.seconds2HoursMinutesSeconds(h_m_s_as_seconds);

        StringBuilder msgBuilder = new StringBuilder(30);

        if (h_m_s_time[0] != 0)
            msgBuilder.append(h_m_s_time[0]).append(' ').append("h"); // TODO: i18n
        if (h_m_s_time[1] != 0)
            msgBuilder.append(' ').append(h_m_s_time[1]).append(' ').append("m"); // TODO: i18n
        if (h_m_s_time[2] != 0)
            msgBuilder.append(' ').append(h_m_s_time[2]).append(' ').append("s"); // TODO: i18n

        return msgBuilder.toString().trim();
    }

    /**
     * Shows an ERROR if the time is not between the boundaries established in the BreaksController class
     * this method SHOULD be called after the isTimeOkWarning method or derivatives
     * @return true if the the time is between those boundaries
     *//*
    public boolean isTimeOk() {
        int time_as_seconds = this.getHoursMinutesSecondsAsSeconds();

        if (time_as_seconds < BreaksController.MIN_TIME_SECONDS) {
            // TODO: i18n
            this.setSmallMessageLabelText("Time must be greater than " + BreaksController.MIN_TIME_SECONDS + " seconds", TimeInput.MESSAGE_TYPE.ERROR);
            return false;
        } else if (time_as_seconds > BreaksController.MAX_TIME_SECONDS) {
            // TODO: i18n
            this.setSmallMessageLabelText("Time must be lesser than " + BreaksController.MIN_TIME_SECONDS + " seconds", TimeInput.MESSAGE_TYPE.ERROR);
            return false;
        }

        return true;
    }

    /**
     * Shows a WARNING if the time is not between the given boundaries
     * @param min_time_seconds the minimum value the input should have
     * @param max_time_seconds the maximum value the input should have
     * @return true if the time is between the given boundaries
     *//*
    public boolean isTimeOkWarning(int min_time_seconds, int max_time_seconds) {
        int time_as_seconds = this.getHoursMinutesSecondsAsSeconds();

        boolean is_ok = true;
        StringBuilder msgBuilder = new StringBuilder(50);

        if (time_as_seconds < min_time_seconds) {
            is_ok = false;
            msgBuilder.append("Time is recommended to be greater than or equal to"); // TODO: i18n
        } else if (time_as_seconds > max_time_seconds) {
            is_ok = false;
            msgBuilder.append("Time is recommended to be less than or equal to"); // TODO: i18n
        } else
            this.setSmallMessageLabelToDefault();

        if (is_ok)
            return true;

        String seconds_as_string = TimeInput.seconds2HoursMinutesSecondsAsString(time_as_seconds);
        msgBuilder.append(" ").append(seconds_as_string);

        this.setSmallMessageLabelText(msgBuilder.toString(), TimeInput.MESSAGE_TYPE.WARNING);

        return false;
    }

    /**
     * Enables or disables all inputs and labels
     * @param disabled a boolean
     *//*
    public void disabled(boolean disabled) {
        this.hoursSpinner.setDisable(disabled);
        this.minutesSpinner.setDisable(disabled);
        this.secondsSpinner.setDisable(disabled);

        this.smallMessageLabel.setDisable(disabled);
        this.inputLabel.setDisable(disabled);
    }

    // setters

    /**
     * Set the value for the Hours input
     * @param hours the hours
     *//*
    public void setHours(int hours) {
        this.hoursSpinner.getValueFactory().setValue(hours);
    }
    /**
     * Set the value for the Minutes input
     * @param minutes the hours
     *//*
    public void setMinutes(int minutes) {
        this.minutesSpinner.getValueFactory().setValue(minutes);
    }
    /**
     * Set the value for the Seconds input
     * @param seconds the hours
     *//*
    public void setSeconds(int seconds) {
        this.secondsSpinner.getValueFactory().setValue(seconds);
    }

    /**
     * Sets the Hours, Minutes and Seconds input values from a given amount of seconds
     * Such amount of seconds should include the number of hours, minutes and seconds
     * hours, minutes and seconds will be extracted automatically from that amount
     * @param h_m_s_as_seconds the hours, minutes and seconds as seconds, all in one
     *                         for example, for 2 hours, 3 minutes, 4 seconds, this param should be
     *                         2 * 60 * 60 + 3 * 60 + 4 = 7384
     *//*
    public void setHoursMinutesSecondsFromSeconds(int h_m_s_as_seconds) {
        int[] h_m_s = TimeInput.seconds2HoursMinutesSeconds(h_m_s_as_seconds);
        this.setHours(h_m_s[0]);
        this.setMinutes(h_m_s[1]);
        this.setSeconds(h_m_s[2]);
    }

    // getters
    /**
     * Get the value for the Hours input
     *//*
    public int getHours() {
        return this.hoursSpinner.getValue();
    }
    /**
     * Get the value for the Hours input
     *//*
    public int getMinutes() {
        return this.minutesSpinner.getValue();
    }
    /**
     * Get the value for the Hours input
     *//*
    public int getSeconds() {
        return this.secondsSpinner.getValue();
    }
    /**
     * Get the value for the Hours, Minutes and Seconds inputs
     * All in one
     * the value is returned as seconds
     * It converts the hours to seconds, minutes to seconds
     * and adds everything
     *//*
    public int getHoursMinutesSecondsAsSeconds() {
        int hours = this.getHours();
        int minutes = this.getMinutes();
        int seconds = this.getSeconds();

        int hours_as_seconds = hours * 60 * 60;
        int minutes_as_seconds = minutes * 60;

        return hours_as_seconds + minutes_as_seconds + seconds;
    }
}
*/