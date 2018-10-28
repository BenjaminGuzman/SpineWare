package titlebar;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JDialog;

/**
 *
 * @author Benjamín Guzmán
 */
public class HoverJDialog implements MouseListener{
    private final JButton btn;
    private final Color RED, YELLOW, GRAY, NATIVE;
    private final byte TYPE;
    private final JDialog jd;
    public HoverJDialog(JDialog jd, JButton jbtn, byte type){
        this.btn = jbtn;
        this.jd = jd;
        RED = new Color(179, 0, 0);
        YELLOW = new Color(204, 163, 0);
        GRAY = new Color(85, 85, 85);
        NATIVE = new Color(119, 119, 119);
        // 0 - close
        // 1 - max
        // 2 - min
        this.TYPE = type;
    }
    @Override
    public void mouseClicked(MouseEvent me) {
        if (TYPE == 0)
            jd.dispose();//destruirlo bien
        else if (TYPE == 1)
            System.out.println("MAX");
        else
            System.out.println("min");
    }
    @Override
    public void mousePressed(MouseEvent me) {}
    @Override
    public void mouseReleased(MouseEvent me) {}
    @Override
    public void mouseEntered(MouseEvent me) {
        if (TYPE == 0)
            btn.setBackground(RED);
        else if (TYPE == 1)
            btn.setBackground(GRAY);
        else 
            btn.setBackground(YELLOW);
    }
    @Override
    public void mouseExited(MouseEvent me) {
        btn.setBackground(NATIVE);
    }
}
