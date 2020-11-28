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
package org.fos.controllers;

/*
import org.fos.I18nable;
import org.fos.SWMain;
import org.fos.alerts.SWAlert;
import org.fos.controls.TimeInput;
import org.fos.core.TimersManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class BreaksController implements I18nable, Initializable {
    public static short MIN_TIME_SECONDS = 10;
    public static int MAX_TIME_SECONDS = 16 * 60 * 60;

    // small break stuff
    @FXML private Label smallBreaksTitleLabel;
    @FXML private Label smallBreaksDescriptionLabel;
    @FXML private CheckBox smallBreaksEnabledCheckBox;
    @FXML private Label smallBreaksFullDescriptionLabel;
    @FXML private TimeInput smallBreaksWorkingTimeInput;
    @FXML private TimeInput smallBreaksBreakTimeInput;

    // stretch break stuff
    @FXML private Label stretchBreaksTitleLabel;
    @FXML private Label stretchBreaksDescriptionLabel;
    @FXML private CheckBox stretchBreaksEnabledCheckBox;
    @FXML private Label stretchBreaksFullDescriptionLabel;
    @FXML private TimeInput stretchBreaksWorkingTimeInput;
    @FXML private TimeInput stretchBreaksBreakTimeInput;

    // day break stuff
    @FXML private Label dayBreakTitleLabel;
    @FXML private Label dayBreakDescriptionLabel;
    @FXML private CheckBox dayBreakEnabledCheckBox;
    @FXML private Label dayBreakFullDescriptionLabel;
    @FXML private TimeInput dayBreakWorkingTimeInput;

    // save changes button stuff
    @FXML private Button saveChangesButton;

    /**
     * preferences keys
     * small breaks disabled: boolean
     * stretch breaks disabled: boolean
     * day break disabled: boolean
     *
     * small breaks working time: int (number of seconds for the working time)
     * small breaks break time: int (number of seconds for the break time)
     *
     * stretch breaks working time: int
     * stretch breaks break time: int
     *
     * day break working time: int
     *//*
    private final Preferences preferences;

    public BreaksController() {
        this.preferences = Preferences.userNodeForPackage(TimersManager.class);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.setTextAccordingToLanguage();
        this.setInputsDefaults();
    }

    /**
     * Sets the input defaults, first it checks the user preferences
     * if they exist, those values are set
     * if not, the default values are set
     * Timer inputs are modified as well as checkboxs
     *//*
    private void setInputsDefaults() {
        ///////////////////////
        // small break stuff //
        //////////////////////
        int small_breaks_working_time_s = this.preferences.getInt(TimersManager.PREF_KEY_SMALL_BREAKS_WORKING_TIME, 0);
        this.smallBreaksWorkingTimeInput.setHoursMinutesSecondsFromSeconds(small_breaks_working_time_s);

        int small_breaks_break_time_s = this.preferences.getInt(TimersManager.PREF_KEY_SMALL_BREAKS_BREAK_TIME, 0);
        this.smallBreaksBreakTimeInput.setHoursMinutesSecondsFromSeconds(small_breaks_break_time_s);

        boolean small_breaks_enabled = !this.preferences.getBoolean(TimersManager.PREF_KEY_SMALL_BREAKS_DISABLED, false);
        this.smallBreaksEnabledCheckBox.setSelected(small_breaks_enabled);
        this.smallBreaksWorkingTimeInput.setDisable(!small_breaks_enabled);
        this.smallBreaksBreakTimeInput.setDisable(!small_breaks_enabled);

        /////////////////////////
        // stretch break stuff //
        /////////////////////////
        int stretch_breaks_working_time_s = this.preferences.getInt(TimersManager.PREF_KEY_STRETCH_BREAKS_WORKING_TIME, 0);
        this.stretchBreaksWorkingTimeInput.setHoursMinutesSecondsFromSeconds(stretch_breaks_working_time_s);

        int stretch_breaks_break_time_s = this.preferences.getInt(TimersManager.PREF_KEY_STRETCH_BREAKS_BREAK_TIME, 0);
        this.stretchBreaksBreakTimeInput.setHoursMinutesSecondsFromSeconds(stretch_breaks_break_time_s);

        boolean stretch_breaks_enabled = !this.preferences.getBoolean(TimersManager.PREF_KEY_STRETCH_BREAKS_DISABLED, false);
        this.stretchBreaksEnabledCheckBox.setSelected(stretch_breaks_enabled);
        this.stretchBreaksBreakTimeInput.setDisable(!stretch_breaks_enabled);
        this.stretchBreaksWorkingTimeInput.setDisable(!stretch_breaks_enabled);

        /////////////////////////
        // stretch break stuff //
        /////////////////////////
        int day_break_working_time_s = this.preferences.getInt(TimersManager.PREF_KEY_DAY_BREAK_WORKING_TIME, 0);
        this.dayBreakWorkingTimeInput.setHoursMinutesSecondsFromSeconds(day_break_working_time_s);

        boolean day_break_enabled = !this.preferences.getBoolean(TimersManager.PREF_KEY_DAY_BREAKS_DISABLED, false);
        this.dayBreakEnabledCheckBox.setSelected(day_break_enabled);
        this.dayBreakWorkingTimeInput.setDisable(!day_break_enabled);
    }

    @Override
    public void setTextAccordingToLanguage() {
        ///////////////////////
        // small break stuff //
        //////////////////////
        // title & descriptions
        this.smallBreaksTitleLabel.setText(SWMain.messagesBundle.getString("small_breaks_title"));
        this.smallBreaksDescriptionLabel.setText(SWMain.messagesBundle.getString("small_breaks_description"));
        this.smallBreaksFullDescriptionLabel.setText(SWMain.messagesBundle.getString("small_breaks_full_description"));
        // time inputs
        this.smallBreaksBreakTimeInput.setInputLabelText(SWMain.messagesBundle.getString("small_breaks_break_time_label"));
        this.smallBreaksBreakTimeInput.setDefaultMessage(SWMain.messagesBundle.getString("small_breaks_break_time_recommended"));
        this.smallBreaksWorkingTimeInput.setInputLabelText(SWMain.messagesBundle.getString("small_breaks_working_time_label"));
        this.smallBreaksWorkingTimeInput.setDefaultMessage(SWMain.messagesBundle.getString("small_breaks_working_time_recommended"));
        // enabled/disabled feature
        this.smallBreaksEnabledCheckBox.setText(SWMain.messagesBundle.getString("feature_enabled"));

        /////////////////////////
        // stretch break stuff //
        ////////////////////////
        // title & descriptions
        this.stretchBreaksTitleLabel.setText(SWMain.messagesBundle.getString("stretch_breaks_title"));
        this.stretchBreaksDescriptionLabel.setText(SWMain.messagesBundle.getString("stretch_breaks_description"));
        this.stretchBreaksFullDescriptionLabel.setText(SWMain.messagesBundle.getString("stretch_breaks_full_description"));
        // time inputs
        this.stretchBreaksBreakTimeInput.setInputLabelText(SWMain.messagesBundle.getString("stretch_breaks_break_time_label"));
        this.stretchBreaksBreakTimeInput.setDefaultMessage(SWMain.messagesBundle.getString("stretch_breaks_break_time_recommended"));
        this.stretchBreaksWorkingTimeInput.setInputLabelText(SWMain.messagesBundle.getString("stretch_breaks_working_time_label"));
        this.stretchBreaksWorkingTimeInput.setDefaultMessage(SWMain.messagesBundle.getString("stretch_breaks_working_time_recommended"));
        // enabled/disabled feature
        this.stretchBreaksEnabledCheckBox.setText(SWMain.messagesBundle.getString("feature_enabled"));

        /////////////////////
        // day break stuff //
        /////////////////////
        // title & descriptions
        this.dayBreakTitleLabel.setText(SWMain.messagesBundle.getString("day_break_title"));
        this.dayBreakDescriptionLabel.setText(SWMain.messagesBundle.getString("day_break_description"));
        this.dayBreakFullDescriptionLabel.setText(SWMain.messagesBundle.getString("day_break_full_description"));
        // time inputs
        this.dayBreakWorkingTimeInput.setInputLabelText(SWMain.messagesBundle.getString("day_break_working_time_label"));
        this.dayBreakWorkingTimeInput.setDefaultMessage(SWMain.messagesBundle.getString("day_break_working_time_recommended"));
        // enabled/disabled feature
        this.dayBreakEnabledCheckBox.setText(SWMain.messagesBundle.getString("feature_enabled"));

        // save button
        this.saveChangesButton.setText(SWMain.messagesBundle.getString("save_changes"));
    }

    /**
     * Saves a time preference
     * @param prefKey the preference key for the preferences object
     * @param timeInput the time input form which the time will be obtained
     * @return true if changes were successfully saved
     *//*
    private boolean saveTimePref(String prefKey, TimeInput timeInput) {
        int time_as_seconds = timeInput.getHoursMinutesSecondsAsSeconds();

        this.preferences.putInt(prefKey, time_as_seconds);
        return true;
    }

    /**
     * Saves the preferences
     * But, before doing that it checks all times are ok
     *//*
    @FXML
    private void onClickSaveChanges() {
        boolean all_times_are_ok = true;
        boolean is_time_ok = true;

        // small breaks stuff
        if (this.smallBreaksEnabledCheckBox.isSelected()) {
            this.smallBreaksWorkingTimeInput.isTimeOkWarning(2 * 60 /* 2 minutes *//*, 15 * 60 /* 15 minutes *//*);*/
            /*this.smallBreaksBreakTimeInput.isTimeOkWarning(5 /* 5 s *//*, 60 /* 60 s *//*);

            is_time_ok = is_time_ok & this.smallBreaksWorkingTimeInput.isTimeOk();
            is_time_ok = is_time_ok & this.smallBreaksBreakTimeInput.isTimeOk();

            if (is_time_ok) {
                this.saveTimePref(TimersManager.PREF_KEY_SMALL_BREAKS_WORKING_TIME, this.smallBreaksBreakTimeInput);
                this.saveTimePref(TimersManager.PREF_KEY_SMALL_BREAKS_BREAK_TIME, this.smallBreaksBreakTimeInput);
            }

            this.preferences.putBoolean(TimersManager.PREF_KEY_SMALL_BREAKS_DISABLED, false);
        } else
            this.preferences.putBoolean(TimersManager.PREF_KEY_SMALL_BREAKS_DISABLED, true);

        all_times_are_ok = all_times_are_ok & is_time_ok;
        is_time_ok = true;

        // stretch breaks stuff
        if (this.stretchBreaksEnabledCheckBox.isSelected()) {
            this.stretchBreaksWorkingTimeInput.isTimeOkWarning(30 * 60 /* 30 m *//*, 5 * 60 * 60 /* 5 h *//*);
            this.stretchBreaksBreakTimeInput.isTimeOkWarning(15 * 60 /* 15 m *//*, 50 * 60 /* 50 m *//*);

            is_time_ok = is_time_ok & this.stretchBreaksWorkingTimeInput.isTimeOk();
            is_time_ok = is_time_ok & this.stretchBreaksBreakTimeInput.isTimeOk();

            if (is_time_ok) {
                this.saveTimePref(TimersManager.PREF_KEY_STRETCH_BREAKS_WORKING_TIME, this.stretchBreaksWorkingTimeInput);
                this.saveTimePref(TimersManager.PREF_KEY_STRETCH_BREAKS_BREAK_TIME, this.stretchBreaksBreakTimeInput);
            }
            this.preferences.putBoolean(TimersManager.PREF_KEY_STRETCH_BREAKS_DISABLED, false);
        } else
            this.preferences.putBoolean(TimersManager.PREF_KEY_STRETCH_BREAKS_DISABLED, true);

        all_times_are_ok = all_times_are_ok & is_time_ok;

        // day break stuff
        if (this.dayBreakEnabledCheckBox.isSelected()) {
            this.dayBreakWorkingTimeInput.isTimeOkWarning(8 * 60 * 60 /* 8 hours *//*, 16 * 60 * 60 /* 16 hours *//*);

            if (all_times_are_ok = all_times_are_ok & this.dayBreakWorkingTimeInput.isTimeOk())
                this.saveTimePref(TimersManager.PREF_KEY_DAY_BREAK_WORKING_TIME, this.dayBreakWorkingTimeInput);

            this.preferences.putBoolean(TimersManager.PREF_KEY_DAY_BREAKS_DISABLED, false);
        } else
            this.preferences.putBoolean(TimersManager.PREF_KEY_DAY_BREAKS_DISABLED, true);

        if (all_times_are_ok) {
            SWAlert alert = new SWAlert(Alert.AlertType.INFORMATION, SWMain.messagesBundle.getString("changes_saved_extra_text"), ButtonType.OK);
            alert.setHeaderText(SWMain.messagesBundle.getString("changes_saved"));
            alert.showAndWait();
            SWMain.timersManager.killAllTimers();
            SWMain.timersManager.createExecutorsFromPreferences(); // preferences should now be updated
        }
    }

    @FXML
    private void onClickEnable(ActionEvent event) {
        CheckBox checkBox = (CheckBox) event.getSource();

        if (checkBox == this.smallBreaksEnabledCheckBox) {
            this.smallBreaksWorkingTimeInput.setDisable(!checkBox.isSelected());
            this.smallBreaksBreakTimeInput.setDisable(!checkBox.isSelected());
        } else if (checkBox == this.stretchBreaksEnabledCheckBox) {
            this.stretchBreaksWorkingTimeInput.setDisable(!checkBox.isSelected());
            this.stretchBreaksBreakTimeInput.setDisable(!checkBox.isSelected());
        } else if (checkBox == this.dayBreakEnabledCheckBox) {
            this.dayBreakWorkingTimeInput.setDisable(!checkBox.isSelected());
        }
    }
}
*/