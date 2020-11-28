/*
 * Copyright © 2020 Benjamín Guzmán
 * Author: Benjamín Guzmán <bg@benjaminguzman.dev>
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
import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.alerts.SWAlert;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class MainController implements Initializable, I18nable {
    // Menu buttons
    @FXML
    private Button breaksMenuButton;
    @FXML
    private Button scheduleMenuButton;
    @FXML
    private Button postureMenuButton;
    @FXML
    private Button tipsMenuButton;
    @FXML
    private Button helpMenuButton;

    // root layout
    @FXML
    private BorderPane rootBorderPane;

    private Button activeMenuButton;
    private String activeViewStr;
    private final HashMap<String, Parent> viewsMap;

    public MainController() {
        this.viewsMap = new HashMap<String, Parent>();
        this.activeViewStr = "";
    }

    @FXML
    public void onClickChangeMainView(final ActionEvent evt) {
        final Button btn = (Button) evt.getSource();

        String newActiveViewStr = this.activeViewStr;

        if (btn == this.breaksMenuButton)
            newActiveViewStr = "Breaks";
        else if (btn == this.scheduleMenuButton)
            newActiveViewStr = "Schedule";
        else if (btn == this.postureMenuButton)
            newActiveViewStr = "Posture";
        else if (btn == this.tipsMenuButton)
            newActiveViewStr = "Tips";
        else if (btn == this.helpMenuButton)
            newActiveViewStr = "Help";

        if (this.changeMainView(newActiveViewStr))
            this.changeActiveMenuButton(btn);
    }

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        this.activeMenuButton = this.breaksMenuButton;

        if (this.changeMainView("Breaks"))
            this.changeActiveMenuButton(this.breaksMenuButton);

        this.setTextAccordingToLanguage();
    }

    /**
     * Changes the current active button for the newActiveButton
     * This method also changes css classes
     * @param newActiveButton the new button that is active
     *//*
    private void changeActiveMenuButton(Button newActiveButton) {
        this.activeMenuButton.getStyleClass().remove("active");
        newActiveButton.getStyleClass().add("active");

        this.activeMenuButton = newActiveButton;
    }

    /**
     * Changes the main view
     * @param newActiveViewStr, the name of the new view
     * @return true if the view was successfully changed, false otherwise
     *//*
    private boolean changeMainView(final String newActiveViewStr) {
        // if the clicked pane is already active, do nothing
        if (this.activeViewStr.equals(newActiveViewStr))
            return false;

        Parent newParent = this.getView(newActiveViewStr);
        if (newParent == null) {
            SWAlert alert = new SWAlert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error while loading the pane: " + newActiveViewStr);
            alert.setContentText(SWMain.messagesBundle.getString("default_error_message"));
            alert.setResizable(true);
            alert.showAndWait();
            return false;
        }

        this.rootBorderPane.setCenter(newParent);
        this.activeViewStr = newActiveViewStr;

        Loggers.debugLogger.fine("View was changed to: " + newActiveViewStr);

        return true;
    }

    /**
     * Checks if the view has been loaded, if it has, just returns the previously loaded pane
     * @param viewName the name of the view to get, JUST the name without extension
     * @return the view (parent)
     *//*
    private Parent getView(final String viewName) {
        // if the pane has not been loaded
        if (!this.viewsMap.containsKey(viewName))
            this.loadView(viewName);

        return this.viewsMap.get(viewName);
    }

    /**
     * Loads a view (parent) and stores a reference in the paneMap hashmap
     * @param viewName, the name of the view (parent) to load WITHOUT extension or path
     *                  e. g. if you wanna load the Help view, the argument must be "Help" only
     *//*
    private void loadView(final String viewName) {
        String viewURLStr = "/resources/views/" + viewName + ".fxml";
        URL viewURL = this.getClass().getResource(viewURLStr);

        if (viewURL == null) {
            Loggers.errorLogger.log(Level.SEVERE, viewURLStr + " FXML file does not exists");
            return;
        }

        try {
            Parent parent = FXMLLoader.load(viewURL);
            this.viewsMap.put(viewName, parent);
        } catch (IOException e) {
            Loggers.errorLogger.log(Level.SEVERE, "Error while loading FXML", e);
        }

        Loggers.debugLogger.fine("View " + viewName + " has been loaded from " + viewURLStr);
    }

    /**
     * Sets the buttons text with the loaded messages bundle (i18n)
     *//*
    @Override
    public void setTextAccordingToLanguage() {
        this.breaksMenuButton.setText(SWMain.messagesBundle.getString("menu_breaks"));
        this.scheduleMenuButton.setText(SWMain.messagesBundle.getString("menu_schedule"));
        this.postureMenuButton.setText(SWMain.messagesBundle.getString("menu_posture"));
        this.tipsMenuButton.setText(SWMain.messagesBundle.getString("menu_tips"));
        this.helpMenuButton.setText(SWMain.messagesBundle.getString("menu_help"));
    }
}*/
