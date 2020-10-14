package org.fos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.fos.alerts.SWAlert;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class SWMain extends Application {

    public static ResourceBundle messagesBundle;
    public static Stage primaryStage = null;

    public static void main(String[] args) {
        Loggers.init();
        SWMain.changeMessagesBundle(Locale.US);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene mainScene = this.getMainScene();
        if (mainScene == null) { // the scene couldn't be loaded
            SWAlert alert = new SWAlert(Alert.AlertType.ERROR);

            alert.setTitle("Error");
            alert.setResizable(true);
            alert.setHeaderText(SWMain.messagesBundle.getString("couldnt_start"));
            alert.setContentText(SWMain.messagesBundle.getString("default_error_message"));
            alert.showAndWait();

            primaryStage.close();
            return;
        }

        SWMain.addGeneralStyleSheet(mainScene);

        primaryStage.setTitle("SpineWare");
        primaryStage.getIcons().add(new Image(SWMain.class.getResource("/resources/media/SW_white.min.png").toExternalForm()));

        primaryStage.setMinWidth(1000.0);
        primaryStage.setMinHeight(500.0);

        primaryStage.setScene(mainScene);
        primaryStage.show();

        SWMain.primaryStage = primaryStage;
    }

    /**
     * Tries to load the main scene
     * If the scene can not be loaded an error will be logged
     * @return the main scene, null if it couldn't be loaded
     */
    public Scene getMainScene() {
        String mainViewResourceLocation = "/resources/views/Main.fxml";
        URL mainViewURL = SWMain.class.getResource(mainViewResourceLocation);

        if (mainViewURL == null) {
            Loggers.errorLogger.log(Level.SEVERE, "Error while loading the main FXML view. Resource: " + mainViewResourceLocation + " does not exists");
            return null;
        }

        // this exception is unlikely to happen, but it is added because it's required
        try {
            return new Scene(FXMLLoader.load(mainViewURL));
        } catch (Exception e) {
            Loggers.errorLogger.log(Level.SEVERE, "Error while loading the main FXML view", e);
            e.printStackTrace();
        }

        return null;
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
