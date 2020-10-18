package org.fos.timers;

import javafx.application.Platform;
import org.fos.Loggers;
import org.fos.controls.TimeInput;
import org.fos.timers.notifications.TakeABreakNotification;

import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class SmallBreakTimer extends BreakTimeTimer {
    public SmallBreakTimer(int break_time_s) {
        super(break_time_s);
    }

    /**
     * When this method is called, a notification to take the break is shown
     * when received a response from the notification, by either clicking "take break" or "dismiss break"
     * this method will show the countdown for the latter and block until it ends
     * or simple return for the former
     */
    @Override
    protected void init() {
        // TODO: when this is executed, all other timers should be blocked/paused
        // method is executing in the timer thread
        // we're going to show the take a break notification
        // therefore we need to run the dialog in the swing thread
        // and wait for a response <- this could be either the user dismissed the break or decided to take it

        final CountDownLatch notificationCountDownLatch = new CountDownLatch(1);

        // this reference is needed as the real object will be created in the swing thread
        AtomicReference<TakeABreakNotification> breakNotification = new AtomicReference<>();
        // show the take a break notification
        SwingUtilities.invokeLater(() -> {
            TakeABreakNotification notification = new TakeABreakNotification("Time for a " + TimeInput.seconds2HoursMinutesSecondsAsString(this.break_time_s) + " break", notificationCountDownLatch);
            notification.showWithAnimation();
            breakNotification.set(notification);
        });

        try {
            notificationCountDownLatch.await(); // wait in THIS thread, wait until the user clicks some button or the dialog disappears
        } catch (InterruptedException e) {
            Loggers.errorLogger.log(Level.SEVERE, "Error while waiting for a response from the take a break dialog", e);
        }

        boolean will_take_break = breakNotification.get().willUserTakeTheBreak();
        if (!will_take_break) {
            Loggers.debugLogger.fine("Not taking the break");
            return;
        }
        Loggers.debugLogger.fine("Taking the break, showing count down...");


        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Platform.runLater(() -> {

        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Loggers.errorLogger.log(Level.SEVERE, "Error while waiting for the break to complete", e);
        }
    }
}
