package mousemoves;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;

/**
 *
 * @author Healthynnovation
 */
public class Hover implements MouseListener{
    private final JButton BTN;
    private static final Color BG = new Color(80, 80, 80), FG = new Color(175, 175, 175), NBG = new Color(102, 102, 102);
    private static final Color NFG = new Color(34, 34, 34);
    public Hover(JButton btn){
        this.BTN = btn;
    }
    @Override
    public void mouseClicked(MouseEvent me) {}
    @Override
    public void mousePressed(MouseEvent me) {}
    @Override
    public void mouseReleased(MouseEvent me) {}

    @Override
    public void mouseEntered(MouseEvent me) {
        BTN.setBackground(BG);
        BTN.setForeground(FG);
    }

    @Override
    public void mouseExited(MouseEvent me) {
        BTN.setBackground(NBG);
        BTN.setForeground(NFG);
    }
}