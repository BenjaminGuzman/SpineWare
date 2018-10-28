package titlebar;

import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Mr. Robot
 */
public class TitleBar_Close extends TitleBar{
    public TitleBar_Close(JFrame jf){
        super(false, jf);
        this.add(createButtons(), BorderLayout.EAST);
    }
    public TitleBar_Close(JDialog jd){
        super(jd);
        this.add(createButtons(), BorderLayout.EAST);
    }
    private JPanel createButtons(){
        JPanel button = new JPanel(new BorderLayout());
        button.add(jf == null ? new Close(jd) : new Close(jf), BorderLayout.CENTER);
        return button;
    }
}