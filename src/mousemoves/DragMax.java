package mousemoves;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;

/**
 *
 * @author Healthynnovation
 */
public class DragMax extends MouseAdapter {
    private final JFrame JF;
    private Point coords;
    public DragMax(JFrame JF) {
        this.JF = JF;
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        coords = null;
    }
    @Override
    public void mousePressed(MouseEvent e) {
        coords = e.getPoint();
        if (e.getClickCount() == 2){
            if(JF.getExtendedState() == JFrame.MAXIMIZED_BOTH){
                JF.setSize(JF.getPreferredSize());
                JF.setExtendedState(JFrame.NORMAL);
            }
            else
                JF.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        if (currCoords.y < 10)
            JF.setExtendedState(JFrame.MAXIMIZED_BOTH);
        else if (JF.getExtendedState() == JFrame.MAXIMIZED_BOTH){
            JF.setSize(JF.getPreferredSize());
            JF.setExtendedState(JFrame.NORMAL);
        }
        else
            JF.setLocation(currCoords.x - coords.x, currCoords.y - coords.y);
    }
}
