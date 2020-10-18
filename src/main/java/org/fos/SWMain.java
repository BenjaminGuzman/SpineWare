package org.fos;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.fos.alerts.SWAlert;
import org.fos.controllers.TimeForABreakNotificationController;
import org.fos.core.TimersManager;

import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class SWMain extends Application {

    public static ResourceBundle messagesBundle;
    public static Stage primaryStage = null;
    public static TimersManager timersManager;

    public static void main(String[] args) {
        Loggers.init();
        SWMain.changeMessagesBundle(Locale.getDefault());
        SWMain.timersManager = new TimersManager();
        SWMain.timersManager.createExecutorsFromPreferences();

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false); // do not exit when window is closed (to exit use systray)

        if (!this.configSysTray()) {
            System.exit(1);
            return;
        }

        // load the main scene
        Parent mainView = SWMain.loadParentView("/resources/views/Main.fxml");
        if (mainView == null) { // the scene couldn't be loaded
            SWAlert alert = new SWAlert(Alert.AlertType.ERROR);

            alert.setTitle("Error");
            alert.setResizable(true);
            alert.setHeaderText(SWMain.messagesBundle.getString("couldnt_start"));
            alert.setContentText(SWMain.messagesBundle.getString("default_error_message"));
            alert.showAndWait();

            primaryStage.close();
            System.exit(1);
            return;
        }

        SWMain.addGeneralStyleSheet(mainView);

        primaryStage.setTitle("SpineWare");
        primaryStage.getIcons().add(new Image(SWMain.class.getResource("/resources/media/SW_white.min.png").toExternalForm()));

        primaryStage.setMinWidth(1000.0);
        primaryStage.setMinHeight(500.0);

        primaryStage.setScene(new Scene(mainView));
        //primaryStage.show();

        SWMain.primaryStage = primaryStage;
    }

    /**
     * Tries to load given fxml
     * If the scene can not be loaded an error will be logged
     * @param fxmlResourceLocation location for the fxml
     * @return the scene loaded from the fxml, null if it couldn't be loaded
     */
    public static Parent loadParentView(final String fxmlResourceLocation) {
        FXMLLoader loader = SWMain.loadFXML(fxmlResourceLocation);

        if (loader == null)
            return null;

        try {
            return loader.load();
        } catch (Exception e) {
            Loggers.errorLogger.log(Level.SEVERE, "Error while loading a FXML view", e);
        }

        return null;
    }

    /**
     *
     */
    public static FXMLLoader loadFXML(final String fxmlResourceLocation) {
        URL resourceURL = SWMain.class.getResource(fxmlResourceLocation);

        if (resourceURL == null) {
            Loggers.errorLogger.log(Level.SEVERE, "Error while loading the main FXML view. Resource: " + fxmlResourceLocation + " does not exists");
            return null;
        }

        FXMLLoader loader = new FXMLLoader(resourceURL);

        try {
            return loader;
        } catch (Exception e) {
            Loggers.errorLogger.log(Level.SEVERE, "Couldn't load an FXML", e);
            return null;
        }
    }

    /**
     * Configures and set the system tray icon
     * TODO: work on the GUI, make it look nice
     * @return true if the sys tray icon was successfully added, false otherwise
     */
    private boolean configSysTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported. SpineWare needs access to the system tray");
            return false;
        }

        // create popup menu
        final PopupMenu popupMenu = new PopupMenu();
        popupMenu.setFont(Font.getFont(Font.SANS_SERIF));
        MenuItem openMenuItem = new MenuItem(SWMain.messagesBundle.getString("systray_open"));
        openMenuItem.addActionListener((java.awt.event.ActionEvent event) -> {
            Platform.runLater(this::showPrimaryStage);
        });

        MenuItem exitMenuItem = new MenuItem(SWMain.messagesBundle.getString("systray_exit"));
        exitMenuItem.addActionListener((java.awt.event.ActionEvent event) -> {
            Platform.runLater(this::exit);
        });

        popupMenu.add(openMenuItem);
        popupMenu.add(exitMenuItem);

        final TrayIcon trayIcon = new TrayIcon(new ImageIcon(SWMain.getSWIconURLAsString()).getImage(), "SpineWare", popupMenu);
        trayIcon.setToolTip("SpineWare");
        trayIcon.addActionListener((java.awt.event.ActionEvent event) -> {
            Platform.runLater(this::showPrimaryStage);
        });
        trayIcon.setPopupMenu(popupMenu);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            Loggers.errorLogger.log(Level.SEVERE, "Error while trying to add the system tray icon", e);
            return false;
        }

        return true;
    }

    public void showPrimaryStage() {
        primaryStage.show();
    }

    public void exit() {
        Platform.exit();
        // TODO: kill all timers
        System.exit(0);
    }

    public static String getSWIconURLAsString() {
        return SWMain.class.getResource("/resources/media/SW_white.min.png").toExternalForm();
    }

    public static void addGeneralStyleSheet(Parent parent) {
        parent.getStylesheets().add(SWMain.class.getResource("/resources/styles/general.css").toExternalForm());
    }

    public static void addGeneralStyleSheet(Scene scene) {
        scene.getStylesheets().add(SWMain.class.getResource("/resources/styles/general.css").toExternalForm());
    }

    public static void changeMessagesBundle(final Locale locale) {
        ResourceBundle newBundle;

        try {
            newBundle = ResourceBundle.getBundle("resources.bundles.messages", locale);
        } catch(Exception e) {
            Loggers.errorLogger.log(Level.SEVERE, "Could not load messages for locale: " + locale, e);
            return;
        }

        SWMain.messagesBundle = newBundle;

        Loggers.debugLogger.fine("The bundle with locale: " + locale + " has been loaded");
    }
}
