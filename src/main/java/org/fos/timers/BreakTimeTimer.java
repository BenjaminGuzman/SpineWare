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
package org.fos.timers;
/*
import java.util.concurrent.CountDownLatch;

public abstract class BreakTimeTimer{
    protected final int break_time_s;

    public BreakTimeTimer(int break_time_s) {
        this.break_time_s = break_time_s;
    }

    protected abstract void init();

    /**
     * Creates a stage with style UTILITY so it does not appear in the taskbar
     * This stage is intended to be the owner of the dialog that will appear later
     * This is done because the dialog will have style TRANSPARENT which removes
     * OS default styling for the window but shows a new item in the taskbar, and we don't want that
     * @return the stage
     *//*
    protected Stage createContainerStage() {
        Stage stage = new Stage();
        stage.setOpacity(0);
        stage.setWidth(0);
        stage.setHeight(0);
        stage.setY(Double.MAX_VALUE);
        stage.setX(Double.MAX_VALUE);
        stage.setIconified(true);
        stage.hide();
        stage.toBack();
        stage.initStyle(StageStyle.UTILITY);
        stage.setScene(new Scene(new VBox())); // this is just to set a random scene so it won't be null later
        return stage;
    }
}
*/