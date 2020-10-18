package org.fos.timers.notifications;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;
import org.fos.Loggers;
import org.fos.SWMain;
import org.fos.controllers.TimeForABreakNotificationController;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

public class TakeABreakNotification extends JDialog {
    private final int WIDTH = 275, HEIGHT=125;

    private boolean will_take_break = true;

    // this countdown latch should be decremented when the dialog ends (its closed or "take a break" is clicked)
    private final CountDownLatch countDownLatch;

    private Timer closeTimer;

    private Scene jfxScene;
    private JFXPanel jfxPanel;
    private Parent jfxRoot;

    public TakeABreakNotification(final String timeForABreakMessage, final CountDownLatch countDownLatch) {
        super();
        //System.out.println(Thread.currentThread().getName() + " should be SWING");

        this.countDownLatch = countDownLatch;

        // load the FXML
        FXMLLoader loader = SWMain.loadFXML("/resources/views/TakeABreakNotification.fxml");
        if (loader == null)
            return;

        try {
            jfxRoot = loader.load();
        } catch (IOException e) {
            Loggers.errorLogger.log(Level.SEVERE, "Error while loading an FXML", e);
            return;
        }

        // configure the controller for the fxml
        TimeForABreakNotificationController controller = loader.getController();
        controller.setTimeForABreakMessage(timeForABreakMessage);
        controller.setOnDismissBreakAction(this::onDismissBreak);
        controller.setOnTakeBreakAction(this::onTakeBreak);

        this.jfxPanel = new JFXPanel();
        this.jfxScene = new Scene(this.jfxRoot);
        this.jfxPanel.setOpaque(false);

        this.setContentPane(jfxPanel);
        this.setPreferredSize(new Dimension(this.WIDTH, this.HEIGHT));
        this.setMinimumSize(new Dimension(this.WIDTH, this.HEIGHT));
        this.setModal(true);
        this.setModalityType(ModalityType.APPLICATION_MODAL);

        // set style
        this.setFocusable(false);
        this.setResizable(false);
        this.setUndecorated(true);
        this.getRootPane().setOpaque(false);
        this.setType(Type.POPUP);
        this.setAlwaysOnTop(true);

        this.pack();
    }

    /**
     * Shows the animation of the notification dialog
     * sliding from right to left at the bottom-right of the screen
     * This will call setVisible which will block until the dialog is disposed
     */
    public void showWithAnimation() {
        // put the dialog to the bottom right of the screen
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        final int x_pos = screenDimension.width - this.WIDTH;
        final int y_pos = screenDimension.height - 2 * this.HEIGHT;

        this.setLocation(x_pos, y_pos);

        Platform.runLater(() -> {
            this.jfxPanel.setScene(this.jfxScene);

            FadeTransition transition = new FadeTransition(Duration.seconds(1), this.jfxRoot);

            transition.setFromValue(0.75);
            transition.setToValue(1.0);
            transition.setCycleCount(5);
            transition.setAutoReverse(true);

            transition.play();
        });

        this.closeTimer = new Timer(true);
        this.closeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Loggers.debugLogger.fine("No response received, disposing the dialog");
                SwingUtilities.invokeLater(TakeABreakNotification.this::dispose);
            }
        }, 10_000); // close the dialog if no response is received after 10 seconds

        this.setVisible(true);
    }

    /**
     * Method to execute when the user clicks take a break
     * This method will be invoked in the javafx application thread
     * @param evt the click event
     */
    public void onTakeBreak(ActionEvent evt) {
        this.will_take_break = true;
        SwingUtilities.invokeLater(this::dispose);
    }

    /**
     * Method to execute when the user clicks dismiss break
     * This method will be invoked in the javafx application thread
     * @param evt the click event
     */
    public void onDismissBreak(ActionEvent evt) {
        this.will_take_break = false;
        SwingUtilities.invokeLater(this::dispose);
    }

    public boolean willUserTakeTheBreak() {
        return this.will_take_break;
    }

    @Override
    public void dispose() {
        super.dispose();

        if (this.closeTimer != null)
            this.closeTimer.cancel();

        this.countDownLatch.countDown();
        Loggers.debugLogger.fine("Disposing the dialog and counting down... Count down latch updated value: " + this.countDownLatch.getCount());
    }
}


/*public class TimeForABreakNotification extends Dialog<Boolean> {
    private final double WIDTH = 275, HEIGHT=125;

    private Timer closeTimer;

    public TimeForABreakNotification(final String timeForABreakMessage) {
        super();

        // load the FXML
        FXMLLoader loader = SWMain.loadFXML("/resources/views/TakeABreakNotification.fxml");
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
        TimeForABreakNotificationController controller = loader.getController();
        controller.setTimeForABreakMessage(timeForABreakMessage);

        DialogPane dialogPane = this.getDialogPane();

        // add buttons
        ButtonType takeBreak = new ButtonType("Take break", ButtonBar.ButtonData.APPLY); // TODO: i18n
        ButtonType dismissBreak = new ButtonType("Dismiss break", ButtonBar.ButtonData.CANCEL_CLOSE); // TODO: i18n
        dialogPane.getButtonTypes().addAll(takeBreak, dismissBreak);

        // set the contents
        dialogPane.setContent(root);

        // set custom styles
        dialogPane.getStylesheets().addAll("/resources/styles/general.css", "/resources/styles/notification.css");
        dialogPane.setBackground(Background.EMPTY);
        dialogPane.getScene().setFill(Color.TRANSPARENT);

        // set width and height
        dialogPane.setMinWidth(this.WIDTH);
        dialogPane.setMaxWidth(this.WIDTH);
        dialogPane.setMinHeight(this.HEIGHT);
        dialogPane.setMaxHeight(this.HEIGHT);

        // put the dialog to the bottom right of the screen
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        this.setX(screenBounds.getWidth() - this.WIDTH);
        this.setY(screenBounds.getHeight() - 2 * this.HEIGHT);

        this.setOnShowing((DialogEvent e) -> {this.showWithAnimation();});
        this.setOnCloseRequest((DialogEvent e) -> {
            if (this.closeTimer != null)
                this.closeTimer.cancel();
        }); // cancel the timer when the dialog is closed
        this.setResultConverter((ButtonType clickedButton) -> !clickedButton.getButtonData().isCancelButton());
    }

    /**
     * Shows the animation of the notification dialog
     * sliding from right to left at the bottom-right of the screen
     * The dialog pane will be animated, not the stage
     */
    /*public void showWithAnimation() {
        TranslateTransition transition = new TranslateTransition();
        transition.setFromX(this.WIDTH);
        transition.setToX(0);
        transition.setDuration(Duration.seconds(1));
        transition.setCycleCount(1);
        transition.setNode(this.getDialogPane());

        transition.play();

        this.closeTimer = new Timer(true);
        this.closeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(TimeForABreakNotification.this::close);
            }
        }, 7000); // close the dialog if no response is received after 7 seconds
    }
}
*/