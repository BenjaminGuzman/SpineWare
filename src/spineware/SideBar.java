package spineware;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import mousemoves.Hover;

/**
 *
 * @author Healthynnovation
 */
public class SideBar extends JPanel{
    private final ArrayList<JButton> BUTTONS;
    public SideBar(ArrayList<JButton> btn, int top, int left, int bottom, int right){
        BUTTONS = btn;
        Iterator<JButton> i = btn.iterator();
        JButton temp;
        BoxLayout boxlayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(boxlayout);
        this.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        this.setBackground(LF.NATIVE);
        while(i.hasNext()){
            temp = i.next();
            personalize(temp);
            JPanel panel = new JPanel(new GridLayout());
            panel.add(temp);
            this.add(Box.createVerticalGlue());
            this.add(panel);
        }
    }
    public ArrayList<JButton> getButtons(){
        return BUTTONS;
    }
    private void personalize(JButton btn){
        btn.setFont(LF.FONT_BTN);
        btn.setBackground(LF.BG_BTN);
        btn.setForeground(LF.FG_BTN);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        btn.setFocusPainted(false);
        btn.addMouseListener(new Hover(btn));
    }
    public void clear(){
        BUTTONS.clear();
    }
}
