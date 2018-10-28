package user;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import mousemoves.Hover;
import spineware.Detection;
import spineware.JTextFieldLimit;
import spineware.Monitoreo;
import spineware.Principal;
import spineware.Spineware;
import spineware.LF;
import titlebar.TitleBar_Min_Close;

/**
 *
 * @author Mr. Robot
 */
public class Login extends JFrame{
    private final JLabel JL_ERROR;
    private final JTextField JTXT_NAME;
    private final JPasswordField JPSWD_PSWD;
    private final JButton JBTN_LOGIN;
    public Login(){
        JButton JBTN_PRINCIPAL = new JButton("Página principal");
        JBTN_PRINCIPAL.requestFocus();

        JL_ERROR = new JLabel(" ");

        JTXT_NAME = new JTextField(20);
        JPSWD_PSWD = new JPasswordField();
        JBTN_LOGIN = new JButton("Iniciar sesión");

        this.getRootPane().setDefaultButton(JBTN_LOGIN);
        this.setUndecorated(true);
        initComponents(JBTN_PRINCIPAL);
        listeners(JBTN_PRINCIPAL);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
        this.setIconImage(Spineware.getIcon());
    }
    private void initComponents(JButton JBTN_PRINCIPAL){
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        
        JPanel ROOT = new JPanel();
        ROOT.setBackground(LF.NATIVE);

        //txt
        JTXT_NAME.setDocument(new JTextFieldLimit(38));
        JTXT_NAME.setBackground(LF.FIELD);
        JTXT_NAME.setFont(LF.FONT_TXT);
        JTXT_NAME.setText("Ingrese su nombre de usuario");
        JTXT_NAME.setForeground(new Color(68, 68, 68));
        JTXT_NAME.setBorder(BorderFactory.createLineBorder(LF.FIELD));

        //pswd
        JPSWD_PSWD.setDocument(new JTextFieldLimit(38));
        JPSWD_PSWD.setBackground(LF.FIELD);
        JPSWD_PSWD.setEchoChar('\u0000');
        JPSWD_PSWD.setText("Ingrese su contraseña");
        JPSWD_PSWD.setFont(LF.FONT_TXT);
        JPSWD_PSWD.setForeground(new Color(68, 68, 68));
        JPSWD_PSWD.setBorder(BorderFactory.createLineBorder(LF.FIELD));
        
        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        personalize(JBTN_PRINCIPAL, border);
        personalize(JBTN_LOGIN, border);

        //error
        JL_ERROR.setForeground(LF.ERROR);
        JL_ERROR.setFont(LF.FONT_TXT);

        ROOT.setLayout(gbl);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        ROOT.add(new TitleBar_Min_Close(this), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        ROOT.add(createMenuPanel(JBTN_PRINCIPAL), gbc);

        ROOT.add(createDataPanel(gbl, gbc), gbc);

        this.add(ROOT);
    }
    private void personalize(JButton btn, Border border){
        btn.setFont(LF.FONT_BTN);
        btn.setBackground(LF.BG_BTN);
        btn.setForeground(LF.FG_BTN);
        btn.setBorder(border);
        btn.setFocusPainted(false);
    }
    private JPanel createMenuPanel(JButton JBTN_PRINCIPAL){
        JPanel MENU = new JPanel();
        MENU.setBackground(LF.NATIVE);
        MENU.add(JBTN_PRINCIPAL);
        return MENU;
    }
    private JPanel createDataPanel(GridBagLayout gbl, GridBagConstraints gbc){
        JPanel DATA = new JPanel();
        DATA.setBorder(BorderFactory.createEmptyBorder(0, 40, 10, 40));
        DATA.setBackground(LF.NATIVE);
        DATA.setLayout(gbl);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 2, 5, 2);
        DATA.add(JTXT_NAME, gbc);

        gbc.gridy = 1;
        DATA.add(JPSWD_PSWD, gbc);

        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        DATA.add(JBTN_LOGIN, gbc);

        gbc.gridy = 3;
        DATA.add(JL_ERROR, gbc);
        
        gbc.gridheight = 5;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        return DATA;
    }
    private void listeners(JButton JBTN_PRINCIPAL){
        JTXT_NAME.addFocusListener(new FocusListener(){
            private boolean placeholder = true;
            private final Color FG_PLACEHOLDER = new Color(68, 68, 68), FG = new Color(10, 10, 10);
            @Override
            public void focusGained(FocusEvent fe) {
                if (placeholder){
                    JTXT_NAME.setText("");
                    JTXT_NAME.setForeground(FG);
                }
            }
            @Override
            public void focusLost(FocusEvent fe) {
                if (JTXT_NAME.getText().isEmpty()){
                    JTXT_NAME.setText("Ingrese su nombre de usuario");
                    JTXT_NAME.setForeground(FG_PLACEHOLDER);
                    placeholder = true;
                } else placeholder = false;
            }
        });
        JPSWD_PSWD.addFocusListener(new FocusListener(){
            private boolean placeholder = true;
            private final Color FG_PLACEHOLDER = new Color(68, 68, 68), FG = new Color(10, 10, 10);
            @Override
            public void focusGained(FocusEvent fe) {
                if (placeholder){
                    JPSWD_PSWD.setText("");
                    JPSWD_PSWD.setForeground(FG);
                    JPSWD_PSWD.setEchoChar(' ');
                }
            }
            @Override
            public void focusLost(FocusEvent fe) {
                if (JPSWD_PSWD.getPassword().length == 0){
                    JPSWD_PSWD.setEchoChar('\u0000');
                    JPSWD_PSWD.setText("Ingrese su contraseña");
                    JPSWD_PSWD.setForeground(FG_PLACEHOLDER);
                    placeholder = true;
                } else placeholder = false;
            }
        });
        JBTN_PRINCIPAL.addMouseListener(new Hover(JBTN_PRINCIPAL));
        JBTN_PRINCIPAL.addActionListener((ActionEvent evt) -> {
            Login.this.dispose();
            new Principal();
        });
        JBTN_LOGIN.addMouseListener(new Hover(JBTN_LOGIN));
        JBTN_LOGIN.addActionListener((ActionEvent e) -> {
            if(JTXT_NAME.getText().equals("Ingrese su nombre de usuario")){
                JL_ERROR.setText("Ingrese su nombre de usuario");
                return;
            }
            final char[] cont = {'I', 'n', 'g', 'r', 'e', 's', 'e', ' ', 's', 'u', ' ', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a'};
            if (Arrays.equals(JPSWD_PSWD.getPassword(), cont)){
                JL_ERROR.setText("Ingrese su contraseña");
                return;
            }
            JL_ERROR.setText(" ");
            User user = new User(JPSWD_PSWD.getPassword(), JTXT_NAME.getText().toCharArray());
            String file;
            try{
                file = search(user.enc);
            } catch(FileNotFoundException err){
                JOptionPane.showMessageDialog(this, "Tenemos problemas con los archivos", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            } catch(Exception err){
                err.printStackTrace();
                JOptionPane.showMessageDialog(this, "Algo ha salido mal", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (file == null)
                JL_ERROR.setText("Credenciales erróneas");
            else{
                super.dispose();
                recoverData(file);
            }
        });
    }
    private void recoverData(String file){
        Monitoreo approved = new Monitoreo();
        approved.GUI(file.replaceAll(".dat", ""));
        Detection.user = new User(new File(".userdata"+File.separator+file));
    }
    private String search(String encrypt) throws FileNotFoundException, IOException{
        FilenameFilter infodatFilter = (File file, String string) -> !string.startsWith("info-") && string.endsWith(".dat");
        File userdata = new File(".userdata");
        File[] users = userdata.listFiles(infodatFilter);
        int total = users.length;
        int i = 0;
        FileInputStream file;
        DataInputStream din;
        String line;
        while (i < total){  
            file = new FileInputStream(users[i]);
            din = new DataInputStream(file);
            line = din.readUTF();
            if (line.equals(encrypt))
                return users[i].getName(); 
            i++;
        }
        return null;
    }
    @Override
    public void dispose(){
        Detection.exit();
        super.dispose();
    }
}