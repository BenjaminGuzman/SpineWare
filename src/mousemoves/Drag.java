package mousemoves;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 *
 * @author Healthynnovation
 */
public class Drag extends MouseAdapter {
    private final JDialog JD;
    private final JFrame JF;
    private Point coords;
    public Drag(JDialog JD) {
        this.JD = JD;
        JF = null;
    }
    public Drag(JFrame JF){
        this.JF = JF;
        JD = null;
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        coords = null;
    }
    @Override
    public void mousePressed(MouseEvent e) {
        coords = e.getPoint();
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        if (JF == null)
            JD.setLocation(currCoords.x - coords.x, currCoords.y - coords.y);
        else
            JF.setLocation(currCoords.x - coords.x, currCoords.y - coords.y);
    }
}
