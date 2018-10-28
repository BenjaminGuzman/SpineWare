package user;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import spineware.LF;
import static spineware.LF.BG_BTN;
import spineware.Spineware;
import titlebar.TitleBar_Close;

/**
 *
 * @author SpineWare
 */
public class Help extends JDialog{
    private final Font FONT_HELP;
    public Help(JFrame parent){
        super(parent, true);
        FONT_HELP = new Font("Verdana", Font.PLAIN, 17);
        initComponents();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
        this.setIconImage(Spineware.getIcon());
    }
    private void initComponents(){
        JPanel ROOT = new JPanel();
        
        ROOT.setBackground(LF.NATIVE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        ROOT.setLayout(new GridBagLayout());
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.25;
        ROOT.add(new TitleBar_Close(this), gbc);
        
        JButton JBTN_OK = new JButton("OK");
        
        gbc.weightx = 2;
        gbc.weighty = 1;
        gbc.gridy = 1;
        ROOT.add(createDataPanel(JBTN_OK), gbc);
        
        this.getContentPane().add(ROOT);
        this.setUndecorated(true);
        this.pack();
        this.getRootPane().setDefaultButton(JBTN_OK);
    }
    private JPanel createDataPanel(JButton JBTN_OK){
        JPanel DATA = new JPanel(new GridLayout(8, 1, 10, 10));
        DATA.setBackground(LF.NATIVE);
        JLabel instructions[] = createInstructions();
        JLabel JL_IMG = new JLabel("<html>No se preocupe si la imagen se ve borrosa, es normal.</html>");
        JLabel JL_PSWD = new JLabel("<html>Su contraseña no se verá por motivos de seguridad.</html>");
        JLabel JL_REGISTER = new JLabel("<html>Dé click al botón \"Registrarme\" para proceder a la captura de imágenes de su cara.</html>");
        
        Border border_li = BorderFactory.createEmptyBorder(5, 25, 5, 10); 
        
        personalize(JL_IMG);
        personalize(JL_PSWD);
        personalize(JL_REGISTER);
        
        byte i = -1;
        while (i < 3)
            personalize(instructions[++i]);
        while (i > 0)
            instructions[i--].setBorder(border_li);        
        JBTN_OK.setHorizontalAlignment(SwingUtilities.CENTER);
        JBTN_OK.setFont(FONT_HELP);
        JBTN_OK.setBackground(BG_BTN);
        JBTN_OK.setForeground(LF.FIELD);
        JBTN_OK.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JBTN_OK.setFocusPainted(false);
        JPanel JPBTN = new JPanel();
        JPBTN.add(JBTN_OK);
        JPBTN.setBackground(LF.NATIVE);
        JBTN_OK.addActionListener((ActionEvent evt)->{Help.this.dispose();});
        
        DATA.add(JL_IMG);
        i = 0;
        while (i < 4)
            DATA.add(instructions[i++]);
        DATA.add(JL_PSWD);
        DATA.add(JL_REGISTER);
        DATA.add(JPBTN);
        return DATA;
    }
    private JLabel[] createInstructions(){
        JLabel instructions[] = new JLabel[4];
        instructions[0] = new JLabel("<html>El procedimiento para registrarse es el siguiente:</html>");
        instructions[1] = new JLabel("<html>\u2022 Ingrese su nombre de entre 8 y 35 caracteres (sólo se permiten caracteres de la 'a' a la 'z' mayúsculas, minúsculas y espacios).</html>");
        instructions[2] = new JLabel("<html>\u2022 Ingrese su contraseña de entre 16 y 35 caracteres (cualquier caracter está permitido).</html>");
        instructions[3] = new JLabel("<html>\u2022 Repita su contraseña, exactamente como la escribió anteriormente.</html>");
        return instructions;
    }
    private void personalize(JLabel jl){
        jl.setForeground(new Color(230, 230, 230));
        jl.setFont(FONT_HELP);
        jl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
}
