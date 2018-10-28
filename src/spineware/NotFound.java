package spineware;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.net.URI;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import manuals.Manual;
import mousemoves.Hover;
import titlebar.TitleBar_Close;
import titlebar.TitleBar_Min_Close;
import user.Login;
import user.Register;

/**
 *
 * @author Healthynnovation
 */
public class NotFound extends JFrame{
    public NotFound(){
        Detection.exit();
        this.setTitle("SpineWare - No se a detectado rostro");
        this.setUndecorated(true);
        this.setResizable(false);
        JButton JBTN_OURS = new JButton("<html><u style=\"color:#2952D6\">nuestra página</u></html>");
        JButton JBTN_JAVACV = new JButton("<html><u style=\"color: #2952D6\">JavaCV</u></html>");
        
        JButton JBTN_LOGIN = new JButton("Iniciar sesión");
        JButton JBTN_REGISTER = new JButton("Registrarse");
        JButton JBTN_ADVICES_MANUAL = new JButton("Consejos");
        JButton JBTN_USER_MANUAL = new JButton("Manual de uso");
        initComponents(JBTN_OURS, JBTN_JAVACV, JBTN_LOGIN, JBTN_REGISTER, JBTN_ADVICES_MANUAL, JBTN_USER_MANUAL);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        listeners(JBTN_OURS, JBTN_JAVACV, JBTN_LOGIN, JBTN_REGISTER, JBTN_ADVICES_MANUAL, JBTN_USER_MANUAL);
        this.setIconImage(Spineware.getIcon());
    }
    private void initComponents(JButton JBTN_OURS, JButton JBTN_JAVACV, JButton JBTN_LOGIN, JButton JBTN_REGISTER, JButton JBTN_ADVICES_MANUAL, JButton JBTN_USER_MANUAL){
        JPanel ROOT = new JPanel(), JP_MSG = new JPanel(), JP_BTN = new JPanel(), JP_ERROR = new JPanel();
        JLabel JL_NOTFOUND = new JLabel("<html><p style=\"text-align: center\">La cámara no ha detectado algún rostro, por favor colócate de forma tal que esto no suceda.</p></html>"), JL_1 = new JLabel("Si crees que esto es un error, repórtalo en"), JL_2 = new JLabel("de GitHub o en la de");
        JBTN_JAVACV.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JBTN_JAVACV.setBorderPainted(false);
        JBTN_JAVACV.setFocusPainted(false);
        JBTN_JAVACV.setFont(LF.FONT_BTN);
        JBTN_JAVACV.setBackground(LF.NATIVE);
        JBTN_JAVACV.setToolTipText("https://github.com/bytedeco/javacv");
        
        JBTN_OURS.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JBTN_OURS.setBorderPainted(false);
        JBTN_OURS.setFocusPainted(false);
        JBTN_OURS.setFont(LF.FONT_BTN);
        JBTN_OURS.setBackground(LF.NATIVE);
        JBTN_OURS.setToolTipText("https://github.com/BenjaminGuzman");
        
        personalize(JBTN_LOGIN);
        JBTN_LOGIN.setToolTipText("Iniciar Sesión");
                
        personalize(JBTN_ADVICES_MANUAL);
        JBTN_ADVICES_MANUAL.setToolTipText("Manual de consejos");
                
        personalize(JBTN_USER_MANUAL);
        JBTN_USER_MANUAL.setToolTipText("Abrir manual de usuario");
                
        personalize(JBTN_REGISTER);
        JBTN_REGISTER.setToolTipText("Registrarte");
        
        JP_ERROR.setBackground(LF.NATIVE);
        JL_1.setFont(LF.FONT_TXT);
        JL_2.setFont(LF.FONT_TXT);
        JL_NOTFOUND.setFont(LF.FONT_TXT);
        
        JP_ERROR.add(JL_1);
        JP_ERROR.add(JBTN_OURS);
        JP_ERROR.add(JL_2);
        JP_ERROR.add(JBTN_JAVACV);
        Border border = BorderFactory.createEmptyBorder(5, 15, 15, 15);
        JP_ERROR.setBorder(border);
        
        JP_MSG.setLayout(new GridLayout(2, 1, 10, 10));
        JP_MSG.setBackground(LF.NATIVE);
        JP_MSG.add(JL_NOTFOUND);
        JP_MSG.add(JP_ERROR);
        JP_MSG.setBorder(border);
        
        JP_BTN.setLayout(new GridLayout(1, 4, 10, 10));
        JP_BTN.setBackground(LF.NATIVE);
        JP_BTN.add(JBTN_LOGIN);
        JP_BTN.add(JBTN_REGISTER);
        JP_BTN.add(JBTN_ADVICES_MANUAL);
        JP_BTN.add(JBTN_USER_MANUAL);
        JP_BTN.setBorder(border);
        
        ROOT.setLayout(new BorderLayout());
        ROOT.add(new TitleBar_Min_Close(this), BorderLayout.NORTH);
        ROOT.add(JP_MSG, BorderLayout.CENTER);
        ROOT.add(JP_BTN, BorderLayout.SOUTH);
        this.add(ROOT);
        this.pack();
    }
    private void personalize(JButton btn){
        btn.addMouseListener(new Hover(btn));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBackground(LF.BG_BTN);
        btn.setFont(LF.FONT_BTN);
        btn.setForeground(LF.FG_BTN);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    private void personalize(JTextField txt){
        txt.setHorizontalAlignment(SwingUtilities.CENTER);
        txt.setEditable(false);
        txt.setBorder(BorderFactory.createLineBorder(LF.NATIVE));
        txt.setBackground(LF.NATIVE);
        txt.setFont(LF.FONT_TXT);
    }
    protected void listeners(JButton JBTN_OURS, JButton JBTN_JAVACV, JButton JBTN_LOGIN, JButton JBTN_REGISTER, JButton JBTN_ADVICES_MANUAL, JButton JBTN_USER_MANUAL){
        JBTN_ADVICES_MANUAL.addActionListener((ActionEvent evt) -> {
            NotFound.super.dispose();
            new Manual();
        });
        JBTN_USER_MANUAL.addActionListener((ActionEvent evt) -> {
            Manual.openUserManual();
        });
        JBTN_JAVACV.addActionListener((ActionEvent evt) -> {
            if (Desktop.isDesktopSupported()){
                try{
                    Desktop.getDesktop().browse(new URI("https://github.com/bytedeco/javacv"));
                }catch(Exception e){
                    errorBrowser("<html>Parece que estamos teniendo problemas para abrir su navegador,<br />de cualquier forma puede ingresar a este link:</html>", "https://github.com/bytedeco/javacv");
                }
            } else {
                errorBrowser("<html>Parece que estamos teniendo problemas para abrir su navegador,<br />de cualquier forma puede ingresar a este link:</html>", "https://github.com/bytedeco/javacv");
            }
        });
        JBTN_OURS.addActionListener((ActionEvent evt) -> {
            if (Desktop.isDesktopSupported()){
                try{
                    Desktop.getDesktop().browse(new URI("https://github.com/BenjaminGuzman"));
                }catch(Exception e){
                    errorBrowser("<html>Parece que estamos teniendo problemas para abrir su navegador,<br />de cualquier forma puede ingresar a este link:</html>", "https://github.com/BenjaminGuzman");
                }
            } else {
                errorBrowser("<html>Parece que estamos teniendo problemas para abrir su navegador,<br />de cualquier forma puede ingresar a este link:</html>", "https://github.com/BenjaminGuzman");
            }
        });
        JBTN_LOGIN.addActionListener((ActionEvent evt) -> {
            NotFound.super.dispose();//cerrarlo normal
            new Login();
        });
        JBTN_REGISTER.addActionListener((ActionEvent evt) -> {
            NotFound.super.dispose();//cerrarlo normal
            new Register();
        });
    }
    private void errorBrowser(String msg, String link){
        JDialog jd = new JDialog(this, true);
        jd.setUndecorated(true);
        JPanel jp = new JPanel(new BorderLayout());
        JLabel jl = new JLabel(msg);
        jl.setFont(LF.FONT_TXT);
        jl.setForeground(LF.FIELD);
        JTextField jtxt = new JTextField(link);
        personalize(jtxt);
        JButton jbtn = new JButton("OK");
        personalize(jbtn);
        jbtn.addActionListener((ActionEvent evt)->{jd.dispose();});
        jbtn.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        jp.setBackground(LF.NATIVE);
        JPanel jp1 = new JPanel(new BorderLayout());
        jp1.setBackground(LF.NATIVE);
        jp1.add(jl, BorderLayout.NORTH);
        jp1.add(jtxt, BorderLayout.SOUTH);
        jp.add(new TitleBar_Close(jd), BorderLayout.NORTH);
        jp.add(jp1, BorderLayout.CENTER);
        jp.add(jbtn, BorderLayout.SOUTH);
        jd.getRootPane().setDefaultButton(jbtn);
        jd.getContentPane().add(jp);
        jd.pack();
        jd.setLocationRelativeTo(this);
        jd.setVisible(true);
    }
}
