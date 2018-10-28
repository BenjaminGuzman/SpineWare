package titlebar;

import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import spineware.LF;
import spineware.Spineware;

/**
 *
 * @author Mr. Robot
 */
public class Min extends JButton{
    public Min(JDialog jd){
        personalize();
        this.addMouseListener(new HoverJDialog(jd, this, (byte)2));
    }
    public Min(JFrame jf){
        personalize();
        this.addMouseListener(new HoverJFrame(jf, this, (byte)2));
    }
    private void personalize(){
        this.setIcon(new ImageIcon(Spineware.getDecodedFullPath("resources/minimize.png")));
        this.setToolTipText("Minimizar");
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setBackground(LF.NATIVE);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}
