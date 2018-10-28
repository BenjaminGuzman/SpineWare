package user;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import spineware.Spineware;

/**
 *
 * @author Mr. Robot
 */
public class ProgressBar extends JDialog{
    private final JProgressBar progress;
    private int max;
    public ProgressBar(JDialog jd){
        super(jd, true);
        progress = new JProgressBar();
        progress.setStringPainted(true);
        progress.setBorder(BorderFactory.createTitledBorder("Generando xml..."));
        this.getContentPane().add(progress, BorderLayout.CENTER);
        this.setLocationRelativeTo(jd);
        this.setUndecorated(true);
        this.pack();
        this.setVisible(true);
        this.setIconImage(Spineware.getIcon());
    }
    public void setVal(int value){
        progress.setValue(value*100/max);
    }
    public void setMaxVal(int max){
        this.max = max;
    }
}
