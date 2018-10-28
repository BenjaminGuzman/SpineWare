package titlebar;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import spineware.LF;

/**
 *
 * @author Mr. Robot
 */
public class TitleBar_Min_Close extends TitleBar{
    public TitleBar_Min_Close(JFrame jf){
        super(false, jf);
        this.add(createButtons(), BorderLayout.EAST);
    }
    public TitleBar_Min_Close(JDialog jd){
        super(jd);
        this.add(createButtons(), BorderLayout.EAST);
    }
    private JPanel createButtons(){
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(jf == null ? new Min(jd) : new Min(jf), BorderLayout.WEST);
        buttons.add(jf == null ? new Close(jd) : new Close(jf), BorderLayout.EAST);
        return buttons;
    }
    /*public TitleBar_Min_Close(boolean resizable, JFrame jf){
        super(resizable, jf);
        JButton JBTN_MIN = new JButton();
        this.add(createButtonsPanel(JBTN_MIN), gbc);
        listeners(JBTN_MIN);
    }
    private JPanel createButtonsPanel(JButton JBTN_MIN){
        JBTN_MIN.setIcon(new ImageIcon(this.getClass().getResource("minimize.png")));
        JBTN_MIN.setToolTipText("Minimizar");
        JBTN_MIN.setBorderPainted(false);
        JBTN_MIN.setFocusPainted(false);
        JBTN_MIN.setBackground(LF.NATIVE);
        JBTN_MIN.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 1));
        JBTN_MIN.addMouseListener(new Hover(jd, JBTN_MIN, (byte)2));
        
        JPanel BUTTONS = new JPanel(new GridLayout(1, 2));
        BUTTONS.add(JBTN_MIN);
        BUTTONS.add(JBTN_CLOSE);
        return BUTTONS;
    }
    private void listeners(JButton JBTN_MIN){
        JBTN_MIN.addMouseListener(new Hover(jd, JBTN_MIN, (byte)2));
        JBTN_MIN.addActionListener((ActionEvent evt) -> {
            jf.setState(JFrame.ICONIFIED);
        });
    }*/
}
