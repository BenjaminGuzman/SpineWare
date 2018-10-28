package titlebar;

import java.awt.BorderLayout;
import mousemoves.Drag;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mousemoves.DragMax;
import spineware.LF;
import spineware.Spineware;

/**
 *
 * @author Healthynnovation
 */
public class TitleBar extends JPanel{
    protected final JDialog jd;
    protected final JFrame jf;
    public TitleBar(JDialog jd){
        this.jd = jd;
        JLabel JL_SW = new JLabel(new ImageIcon(Spineware.getDecodedFullPath("resources/SW.png")));
        JLabel JL_SPINEWARE = new JLabel(new ImageIcon(Spineware.getDecodedFullPath("resources/Spineware.png")));
        Drag drag = new Drag(jd);
        this.addMouseMotionListener(drag);
        this.addMouseListener(drag);
        this.setBackground(LF.NATIVE);
        this.setLayout(new BorderLayout());
        this.add(JL_SW, BorderLayout.WEST);
        this.add(JL_SPINEWARE, BorderLayout.CENTER);
        jf = null;
    }
    public TitleBar(boolean resizable, JFrame jf){
        super();
        this.jf = jf;
        JLabel JL_SW = new JLabel(new ImageIcon(Spineware.getDecodedFullPath("resources/SW.png")));
        JLabel JL_SPINEWARE = new JLabel(new ImageIcon(Spineware.getDecodedFullPath("resources/Spineware.png")));
        setDrag(resizable);
        this.setBackground(LF.NATIVE);
        this.setLayout(new BorderLayout());
        this.add(JL_SW, BorderLayout.WEST);
        this.add(JL_SPINEWARE, BorderLayout.CENTER);
        jd = null;
    }
    private void setDrag(boolean r){
        if (r){
            DragMax drag = new DragMax(jf);
            this.addMouseMotionListener(drag);
            this.addMouseListener(drag);
        }
        else{
            Drag drag = jd == null ? new Drag(jf) : new Drag(jd);
            this.addMouseMotionListener(drag);
            this.addMouseListener(drag);
        }
    }
}
