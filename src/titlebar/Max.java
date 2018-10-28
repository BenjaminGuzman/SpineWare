package titlebar;

import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import spineware.LF;
import spineware.Spineware;

/**
 *
 * @author Benjamín Guzmán
 */
public class Max extends JButton{
    public Max(JFrame jf){
        this.setIcon(new ImageIcon(Spineware.getDecodedFullPath("resources/maximize.png")));
        this.setToolTipText("Cerrar");
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setBackground(LF.NATIVE);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.addMouseListener(new HoverJFrame(jf, this, (byte)1));
    }
}
