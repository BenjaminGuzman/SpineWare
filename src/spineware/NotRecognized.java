package spineware;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mousemoves.Hover;
import titlebar.TitleBar_Close;
import user.Login;
import user.Register;

/**
 *
 * @author Mr. Robot
 */
public class NotRecognized extends JFrame{
        public NotRecognized(){
            Detection.exit();
            this.setTitle("SpineWare - No identificado");
            this.setUndecorated(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JButton JBTN_PRINCIPAL = new JButton("Página principal");
            JButton JBTN_LOGIN = new JButton("Iniciar sesión");
            JButton JBTN_REGISTER = new JButton("Registrarme");
            initComponents(JBTN_LOGIN, JBTN_REGISTER, JBTN_PRINCIPAL);
            this.setVisible(true);
            listeners(JBTN_LOGIN, JBTN_REGISTER, JBTN_PRINCIPAL);
            this.setIconImage(Spineware.getIcon());
        }
        private void initComponents(JButton JBTN_LOGIN, JButton JBTN_REGISTER, JButton JBTN_PRINCIPAL){
            Container root = this.getContentPane();
            
            JLabel NO_ID = new JLabel("<html>Hemos detectado una cara, sin embargo no la hemos podido identificar<br /><p style=\"text-align:center;\">¿Qué desea hacer?</p></html>");
            NO_ID.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            NO_ID.setForeground(Color.DARK_GRAY);
            NO_ID.setFont(LF.FONT_TXT);
            
            personalize(JBTN_LOGIN);
            personalize(JBTN_REGISTER);
            personalize(JBTN_PRINCIPAL);
            
            JPanel BUTTONS = new JPanel();
            
            root.setLayout(new BorderLayout());
            root.setBackground(LF.NATIVE);
            
            titlebar.TitleBar titbar = new TitleBar_Close(this);
            root.add(titbar, BorderLayout.NORTH);
            root.add(NO_ID, BorderLayout.CENTER);
            
            BUTTONS.setLayout(new GridLayout(1, 2, 20, 20));
            BUTTONS.setBackground(LF.NATIVE);
            BUTTONS.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
            BUTTONS.add(JBTN_LOGIN);
            BUTTONS.add(JBTN_PRINCIPAL);
            BUTTONS.add(JBTN_REGISTER);
            
            root.add(BUTTONS, BorderLayout.SOUTH);
            this.pack();
            this.setLocationRelativeTo(null);
        }
        private void listeners(JButton JBTN_LOGIN, JButton JBTN_REGISTER, JButton JBTN_PRINCIPAL){
            JBTN_REGISTER.addActionListener((ActionEvent evt)->{
                this.dispose();
                new Register();
            });
            JBTN_LOGIN.addActionListener((ActionEvent evt)->{
                this.dispose();
                new Login();
            });
            JBTN_PRINCIPAL.addActionListener((ActionEvent evt)->{
                this.dispose();
                new Principal();
            });
        }
        private void personalize(JButton btn){
            btn.setForeground(LF.FG_BTN);
            btn.setBackground(LF.BG_BTN);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.addMouseListener(new Hover(btn));
            btn.setFont(LF.FONT_BTN);
        }
    }
