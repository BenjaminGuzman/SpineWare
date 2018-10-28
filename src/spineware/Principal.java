package spineware;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import manuals.Manual;
import mousemoves.Hover;
import titlebar.TitleBar_Min_Max_Close;
import titlebar.TitleBar_Close;
import user.Login;
import user.Register;

/**
 *
 * @author Healthynnovation
 */
public class Principal extends JFrame{
    public Principal(){
        if (Detection.isCamOn)
            Detection.exit();
        ArrayList<JButton> buttons = initComponents();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(1037, 603));
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setIconImage(Spineware.getIcon());
        listeners(buttons);
    }
    private ArrayList<JButton> initComponents(){
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        
        JPanel ROOT = new JPanel(new BorderLayout());
        JPanel MAIN = new JPanel(gbl);
        
        ROOT.add(new TitleBar_Min_Max_Close(true, this), BorderLayout.NORTH);
        
        SideBar sidebar = createMenuPanel();
        ROOT.add(sidebar, BorderLayout.WEST);
        
        gbc.weightx = 7;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        MAIN.setBackground(LF.NATIVE);
        MAIN.add(createHelloPanel(gbl, gbc), gbc);
        ROOT.add(MAIN, BorderLayout.CENTER);
        
        ROOT.setBackground(LF.NATIVE);
        this.getContentPane().add(ROOT);
        this.setUndecorated(true);
        this.pack();
        return sidebar.getButtons();
    }
    private SideBar createMenuPanel(){
        ArrayList<JButton> buttons = new ArrayList<JButton>();
        buttons.add(new JButton("Manual de uso"));
        buttons.add(new JButton("Consejos"));
        buttons.add(new JButton("Registrarse"));
        buttons.add(new JButton("Iniciar sesión"));
        buttons.add(new JButton("JavaCV"));
        buttons.add(new JButton("SpineWare"));
        
        byte i = 0;
        while(i < 6)
            personalize(buttons.get(i++));
        return new SideBar(buttons, 10, 20, 50, 20);
    }
    private JPanel createHelloPanel(GridBagLayout gbl, GridBagConstraints gbc){
        JPanel HELLO = new JPanel(gbl);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel hola = new JLabel("<html><p style=\"text-align: center; font-size: 1.5em\">Bienvenido a SpineWare</p></html>");
        hola.setHorizontalAlignment(SwingUtilities.CENTER);
        HELLO.add(hola, gbc);
        
        JLabel sw = new JLabel();
        sw.setHorizontalAlignment(SwingUtilities.HORIZONTAL);
        sw.setIcon(new ImageIcon(Spineware.getDecodedFullPath("resources/SW_max.png")));
        gbc.gridy = 1;
        HELLO.add(sw, gbc);
        
        HELLO.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));
        HELLO.setBackground(LF.NATIVE);
        return HELLO;
    }
    private void personalize(JButton btn){
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.addMouseListener(new Hover(btn));
        btn.setBackground(LF.BG_BTN);
        btn.setForeground(LF.FG_BTN);
        btn.setFont(LF.FONT_BTN);
        btn.setFocusPainted(false);
    }
    private void listeners(ArrayList<JButton> buttons){
        //0 - Manual de uso
        //1 - Manual de consejos
        //2 - Registrarse
        //3 - Iniciar sesión
        //4 - javacv
        //5 - sw
        buttons.get(0).addActionListener((ActionEvent evt) -> {Manual.openUserManual();});
        buttons.get(1).addActionListener((ActionEvent evt) -> {
            Principal.this.dispose();
            new Manual();
        });
        buttons.get(2).addActionListener((ActionEvent evt) -> {
            Principal.this.dispose();
            new Register();
        });
        buttons.get(3).addActionListener((ActionEvent evt) -> {
            Principal.this.dispose();
            new Login();
        });
        buttons.get(4).addActionListener((ActionEvent evt) -> {
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
        buttons.get(5).addActionListener((ActionEvent evt) -> {
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
    }
    private void errorBrowser(String msg, String link){
        JDialog jd = new JDialog(this, true);
        jd.setUndecorated(true);
        JPanel jp = new JPanel(new BorderLayout());
        JLabel jl = new JLabel(msg);
        jl.setFont(LF.FONT_TXT);
        jl.setForeground(LF.FIELD);
        JTextField jtxt = new JTextField(link);
        jtxt.setHorizontalAlignment(SwingUtilities.CENTER);
        jtxt.setEditable(false);
        jtxt.setBorder(BorderFactory.createLineBorder(LF.NATIVE));
        jtxt.setBackground(LF.NATIVE);
        jtxt.setFont(LF.FONT_TXT);
        JButton jbtn = new JButton("OK");
        jbtn.addActionListener((ActionEvent evt)->{jd.dispose();});
        jbtn.addMouseListener(new Hover(jbtn));
        jbtn.setBorderPainted(false);
        jbtn.setFocusPainted(false);
        jbtn.setBackground(LF.BG_BTN);
        jbtn.setFont(LF.FONT_BTN);
        jbtn.setForeground(LF.FG_BTN);
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
    //probar fallo:
    //en manuals darle a vista y luego luego cambiar a principal
    @Override
    public void dispose(){
        Detection.exit();
        super.dispose();
        //System.exit(0);
    }
}