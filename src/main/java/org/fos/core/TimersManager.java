package org.fos.core;

import org.fos.Loggers;
import org.fos.timers.WorkingTimeTimer;
import org.fos.timers.SmallBreakTimer;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class TimersManager {
    public static String PREF_KEY_SMALL_BREAKS_DISABLED = "small breaks disabled";
    public static String PREF_KEY_SMALL_BREAKS_WORKING_TIME = "small breaks working time";
    public static String PREF_KEY_SMALL_BREAKS_BREAK_TIME = "small breaks break time";
    public static String PREF_KEY_STRETCH_BREAKS_DISABLED = "stretch breaks disabled";
    public static String PREF_KEY_STRETCH_BREAKS_WORKING_TIME = "stretch breaks working time";
    public static String PREF_KEY_STRETCH_BREAKS_BREAK_TIME = "stretch breaks break time";
    public static String PREF_KEY_DAY_BREAKS_DISABLED = "day breaks disabled";
    public static String PREF_KEY_DAY_BREAK_WORKING_TIME = "day break working time";

    private WorkingTimeTimer smallBreaksWorkingTimeTimer;
    private WorkingTimeTimer stretchBreaksWorkingTimeTimer;
    private WorkingTimeTimer dayBreakWorkingTimeTimer;

    public void createExecutorsFromPreferences() {
        Preferences preferences = Preferences.userNodeForPackage(this.getClass());
        try {
            preferences.sync(); // ensure we read updated values
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        boolean small_breaks_disabled = preferences.getBoolean(PREF_KEY_SMALL_BREAKS_DISABLED, true);
        boolean stretch_breaks_disabled = preferences.getBoolean(PREF_KEY_STRETCH_BREAKS_DISABLED, true);
        boolean day_breaks_disabled = preferences.getBoolean(PREF_KEY_DAY_BREAKS_DISABLED, true);

        if (!small_breaks_disabled) {
            int small_breaks_working_time = preferences.getInt(PREF_KEY_SMALL_BREAKS_WORKING_TIME, 5 * 60);
            int small_breaks_break_time = preferences.getInt(PREF_KEY_SMALL_BREAKS_BREAK_TIME, 60);

            this.smallBreaksWorkingTimeTimer = new WorkingTimeTimer(small_breaks_working_time, small_breaks_break_time, SmallBreakTimer.class);
            this.smallBreaksWorkingTimeTimer.init();
        }

        if (!stretch_breaks_disabled) {

        }

        if (!day_breaks_disabled) {

        }

    }

    /**
     * shutdowns all timers and resets the timers array
     * leaving the object as it were "brand-new" or recently instantiated
     */
    public void killAllTimers() {
        //for (ScheduledExecutorService scheduledExecutor : this.scheduledExecutors)
        //    scheduledExecutor.shutdown();
        //this.scheduledExecutors.clear();
        Loggers.debugLogger.exiting(TimersManager.class.getCanonicalName(), "killAllTimers");
    }
}
