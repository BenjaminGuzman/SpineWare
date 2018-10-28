package titlebar;

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
public class Close extends JButton{
    public Close(JDialog jd){
        personalize();
        this.addMouseListener(new HoverJDialog(jd, this, (byte)0));
    }
    public Close(JFrame jf){
        personalize();
        this.addMouseListener(new HoverJFrame(jf, this, (byte)0));
    }
    private void personalize(){
        try{
            this.setIcon(new ImageIcon(Spineware.getDecodedFullPath("resources/close.png")));
        } catch(Exception e){}
        this.setToolTipText("Cerrar");
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setBackground(LF.NATIVE);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}
