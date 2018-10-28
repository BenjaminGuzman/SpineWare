package titlebar;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author Benjamín Guzmán
 */
public class HoverJFrame implements MouseListener{
        private final JButton btn;
        private final Color RED, YELLOW, GRAY, NATIVE;
        private final byte TYPE;
        private final JFrame jf;
        public HoverJFrame(JFrame jf, JButton jbtn, byte type){
            this.btn = jbtn;
            this.jf = jf;
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
                jf.dispose();//destruirlo bien
            else if (TYPE == 1){
                if (jf.getExtendedState() == JFrame.MAXIMIZED_BOTH){
                    jf.setSize(jf.getPreferredSize());
                    jf.setExtendedState(JFrame.NORMAL);
                    jf.setLocationRelativeTo(null);
                }
                else
                    jf.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            else
                jf.setState(JFrame.ICONIFIED);
        }
        @Override
        public void mousePressed(MouseEvent me) {
        }
        @Override
        public void mouseReleased(MouseEvent me) {
        }
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
