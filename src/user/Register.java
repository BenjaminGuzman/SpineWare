package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import mousemoves.Hover;
import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import spineware.Detection;
import static spineware.Detection.grabber_height;
import static spineware.Detection.grabber_width;
import static spineware.Detection.isCamOn;
import spineware.JTextFieldLimit;
import spineware.LF;
import static spineware.LF.BG_BTN;
import static spineware.LF.FIELD;
import static spineware.LF.NATIVE;
import spineware.Loading;
import spineware.Principal;
import spineware.Spineware;
import spineware.Validate;
import titlebar.TitleBar_Min_Max_Close;

/**
 *
 * @author Mr. Robot
 */
public class Register extends JFrame{
    private Graphics graphics;
    private JPanel imagepanel;
    private final JTextField JTXT_NAME;
    private final JPasswordField JPSWD_PSWD, JPSWD_PSWD_REPEAT;
    private final JLabel JL_ERROR_NAME, JL_ERROR_PSWD, JL_ERROR_PSWD_REPEAT, JL_ERR, JL_STATE, JL_TAKE_PICS;
    private OpenCVFrameConverter.ToIplImage ipl_converter;
    private boolean alive;
    private volatile boolean dispose;
    private opencv_core.CvMemStorage storage;
    private Thread fd;
    private byte seconds;
    public Register(){
        Loading loading = new Loading();
        alive = true;
        dispose = false;
        if (!isCamOn)
            if (!Detection.start()){
                JOptionPane.showMessageDialog(null, "Algo ha salido mal al intentar abrir la cámara", "ERROR", JOptionPane.ERROR_MESSAGE);
                Detection.exit();
                System.exit(0);
            }
        this.setTitle("SpineWare - Login");
        JTXT_NAME = new JTextField();
        JPSWD_PSWD = new JPasswordField();
        JPSWD_PSWD_REPEAT = new JPasswordField();
        JL_ERROR_NAME = new JLabel();
        JL_ERROR_PSWD = new JLabel();
        JL_ERROR_PSWD_REPEAT = new JLabel();
        JL_ERR = new JLabel("");
        JL_STATE = new JLabel("");
        JL_TAKE_PICS = new JLabel("Temporizador");
        JButton JBTN_REGISTER = new JButton("Registrarme");
        JButton JBTN_PRINCIPAL = new JButton("Página Principal");
        JButton JBTN_HELP = new JButton("Ayuda");
        
        ipl_converter = new OpenCVFrameConverter.ToIplImage();
        this.setUndecorated(true);
        initComponents(JBTN_REGISTER, JBTN_PRINCIPAL, JBTN_HELP);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        loading.dispose();
        this.setVisible(true);
        this.setIconImage(Spineware.getIcon());
        listeners(JBTN_REGISTER, JBTN_PRINCIPAL, JBTN_HELP);
    }
    private void initComponents(JButton JBTN_REGISTER, JButton JBTN_PRINCIPAL, JButton JBTN_HELP){
        GridBagConstraints gbc = new GridBagConstraints();
        
        JPanel ROOT = new JPanel(new BorderLayout());
        JPanel REGISTER = new JPanel();
        JPanel MAIN = new JPanel();
        MAIN.setLayout(new BoxLayout(MAIN, BoxLayout.X_AXIS));
        imagepanel = new JPanel();
        
        REGISTER.setLayout(new BoxLayout(REGISTER, BoxLayout.Y_AXIS));
        REGISTER.add(createDataPanel());
        REGISTER.add(Box.createVerticalBox());
        REGISTER.add(createButtonsPanel(JBTN_REGISTER, JBTN_PRINCIPAL, JBTN_HELP, gbc));
        REGISTER.setBackground(Color.red);
        
        MAIN.add(createMirrorPanel());
        MAIN.add(REGISTER);
        
        ROOT.add(new TitleBar_Min_Max_Close(true, this), BorderLayout.NORTH);
        ROOT.add(MAIN, BorderLayout.CENTER);
        
        this.getRootPane().setDefaultButton(JBTN_REGISTER);
        ROOT.setBackground(NATIVE);
        this.getContentPane().add(ROOT);
        this.pack();
        graphics = imagepanel.getGraphics();
        fd = new Thread(new Capture());
        fd.start();
        //para que la interfaz no se desconfigure con las mayúsculas (número máximo de caracteres)
        JTXT_NAME.setPreferredSize(JTXT_NAME.getPreferredSize());
    }
    private JPanel createMirrorPanel(){
        JPanel mirror = new JPanel(new BorderLayout());
        imagepanel.setPreferredSize(new Dimension(grabber_width-20, grabber_height-20));//-20 para el margin
        imagepanel.setBackground(NATIVE);
        
        mirror.setPreferredSize(new Dimension(grabber_width+20, grabber_height+20));
        mirror.setBackground(NATIVE);
        mirror.add(imagepanel, BorderLayout.CENTER);
        return mirror;
    }
    private JPanel createDataPanel(){
        JPanel DATA = new JPanel();
        DATA.setLayout(new GridLayout(8, 1, 0, 10));
        DATA.setBackground(NATIVE);
        //label para los errores generales
        JL_ERR.setForeground(LF.ERROR);
        JL_ERR.setFont(LF.FONT_TXT);
        JL_ERR.setHorizontalAlignment(SwingUtilities.CENTER);
        JL_ERR.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        DATA.add(JL_ERR);
        //txt
        Border padding = BorderFactory.createEmptyBorder(5, 8, 5, 8);
        JTXT_NAME.setBackground(FIELD);
        JTXT_NAME.setFont(LF.FONT_TXT);
        JTXT_NAME.setBorder(padding);
        JTXT_NAME.setDocument(new JTextFieldLimit(35));
        DATA.add(JTXT_NAME);
        JTXT_NAME.setText("Ingrese su nombre de usuario");
        JTXT_NAME.setForeground(new Color(68, 68, 68));
        
        JL_ERROR_NAME.setForeground(LF.ERROR);
        JL_ERROR_NAME.setFont(LF.FONT_TXT);
        DATA.add(JL_ERROR_NAME);
        
        //pswd
        JPSWD_PSWD.setBackground(FIELD);
        JPSWD_PSWD.setFont(LF.FONT_TXT);
        JPSWD_PSWD.setBorder(padding);
        JPSWD_PSWD.setDocument(new JTextFieldLimit(35));
        DATA.add(JPSWD_PSWD);
        JPSWD_PSWD.setEchoChar('\u0000');
        JPSWD_PSWD.setText("Ingrese su contraseña");
        JPSWD_PSWD.setForeground(new Color(68, 68, 68));
        
        JL_ERROR_PSWD.setForeground(LF.ERROR);
        JL_ERROR_PSWD.setFont(LF.FONT_TXT);
        DATA.add(JL_ERROR_PSWD);
        
        JPSWD_PSWD_REPEAT.setBackground(FIELD);
        JPSWD_PSWD_REPEAT.setFont(LF.FONT_TXT);
        JPSWD_PSWD_REPEAT.setBorder(padding);
        JPSWD_PSWD_REPEAT.setDocument(new JTextFieldLimit(35));
        DATA.add(JPSWD_PSWD_REPEAT);
        JPSWD_PSWD_REPEAT.setEchoChar('\u0000');
        JPSWD_PSWD_REPEAT.setText("Repita su contraseña");
        JPSWD_PSWD_REPEAT.setForeground(new Color(68, 68, 68));
        
        JL_ERROR_PSWD_REPEAT.setForeground(LF.ERROR);
        JL_ERROR_PSWD_REPEAT.setFont(LF.FONT_TXT);
        DATA.add(JL_ERROR_PSWD_REPEAT);
        
        JL_STATE.setForeground(new Color(235, 235, 235));
        JL_STATE.setFont(new Font("Arial", Font.PLAIN, 20));
        JL_STATE.setHorizontalAlignment(SwingUtilities.CENTER);
        DATA.add(JL_STATE);
        DATA.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 20));
        return DATA;
    }
    private JPanel createButtonsPanel(JButton JBTN_REGISTER, JButton JBTN_PRINCIPAL, JButton JBTN_HELP, GridBagConstraints gbc){
        JPanel BUTTONS = new JPanel(new GridBagLayout());
        JBTN_REGISTER.setFont(LF.FONT_BTN);
        JBTN_REGISTER.setBackground(BG_BTN);
        JBTN_REGISTER.setForeground(new Color(20, 20, 20));
        JBTN_REGISTER.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));
        JBTN_REGISTER.setFocusPainted(false);
        JBTN_REGISTER.setEnabled(false);
        
        BUTTONS.setBackground(NATIVE);
        
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 30, 0);
        BUTTONS.add(JBTN_REGISTER, gbc);
        
        //panel de abajo a la derecha
        personalize(JBTN_PRINCIPAL);
        
        JL_TAKE_PICS.setFont(LF.FONT_BTN);
        JL_TAKE_PICS.setBackground(BG_BTN);
        JL_TAKE_PICS.setForeground(LF.FG_BTN);
        JL_TAKE_PICS.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JL_TAKE_PICS.setHorizontalAlignment(SwingUtilities.CENTER);
        
        personalize(JBTN_HELP);
        
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 15, 15, 15);
        BUTTONS.add(JBTN_PRINCIPAL, gbc);
        
        gbc.gridx = 1;
        BUTTONS.add(JL_TAKE_PICS, gbc);
        
        gbc.gridx = 2;
        BUTTONS.add(JBTN_HELP, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        return BUTTONS;
    }
    private void personalize(JButton btn){
        btn.setFont(LF.FONT_BTN);
        btn.setBackground(BG_BTN);
        btn.setForeground(LF.FG_BTN);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        btn.setFocusPainted(false);
    }
    private void listeners(JButton JBTN_REGISTER, JButton JBTN_PRINCIPAL, JButton JBTN_HELP){
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
        JTXT_NAME.addKeyListener(new KeyListener(){
            private boolean first = true;
            @Override
            public void keyTyped(KeyEvent ke) {
                if (JTXT_NAME.getText().length() == 35){
                    JL_ERROR_NAME.setText("Nombre menor a 35 caracteres");
                    JL_ERROR_NAME.setForeground(new Color(216, 180, 19));
                    ke.consume();
                    return;
                }
                if (!Character.isISOControl(ke.getKeyChar()))//que sea caracter imprimible
                    if (!Validate.name(String.valueOf(ke.getKeyChar()))){
                        JTXT_NAME.setText(JTXT_NAME.getText().replaceAll("[^a-zA-Z ]", ""));//reemplazar todo lo que no sea a-z-A-Z
                        JL_ERROR_NAME.setForeground(new Color(216, 180, 19));
                        JL_ERROR_NAME.setText("Sólo caracteres de la a-z y/o A-Z (sin ñ o Ñ)");
                        JBTN_REGISTER.setEnabled(false);
                        ke.consume();
                        return;
                    }
                if (!first){
                    if (JTXT_NAME.getText().length() < 7){
                        JL_ERROR_NAME.setText("Mínimo 8 caracteres");
                        JL_ERROR_NAME.setForeground(LF.ERROR);
                        JBTN_REGISTER.setEnabled(false);
                        return;
                    }
                } else if (JTXT_NAME.getText().length() == 8)
                    first = false;
                JL_ERROR_NAME.setText("");
                JBTN_REGISTER.setEnabled(true);
            }
            @Override
            public void keyPressed(KeyEvent ke) {}
            @Override
            public void keyReleased(KeyEvent ke) {}
        });     
        JPSWD_PSWD.addKeyListener(new KeyListener(){
            private boolean first = true;
            @Override
            public void keyTyped(KeyEvent ke) {
                if (JPSWD_PSWD.getPassword().length == 35){
                    JL_ERROR_PSWD.setText("Nombre menor a 35 caracteres");
                    JL_ERROR_PSWD.setForeground(new Color(216, 180, 19));
                    ke.consume();
                    return;
                }
                if (!first){
                    if (JPSWD_PSWD.getPassword().length < 7){
                        JL_ERROR_PSWD.setText("Mínimo 8 caracteres");
                        JL_ERROR_PSWD.setForeground(LF.ERROR);
                        JBTN_REGISTER.setEnabled(false);
                        return;
                    }
                } else if (JPSWD_PSWD.getPassword().length == 8)
                    first = false;
                JL_ERROR_PSWD.setText("");
                JBTN_REGISTER.setEnabled(true);
            }
            @Override
            public void keyPressed(KeyEvent ke) {}
            @Override
            public void keyReleased(KeyEvent ke) {}
        });
        JPSWD_PSWD_REPEAT.addKeyListener(new KeyListener(){
            private boolean first = true;
            @Override
            public void keyTyped(KeyEvent ke) {
                if (JPSWD_PSWD_REPEAT.getPassword().length == 35){
                    JL_ERROR_PSWD_REPEAT.setText("Nombre menor a 35 caracteres");
                    JL_ERROR_PSWD_REPEAT.setForeground(new Color(216, 180, 19));
                    ke.consume();
                    return;
                }
                if (!first){
                    if (JPSWD_PSWD_REPEAT.getPassword().length < 7){
                        JL_ERROR_PSWD_REPEAT.setText("Mínimo 8 caracteres");
                        JL_ERROR_PSWD_REPEAT.setForeground(LF.ERROR);
                        JBTN_REGISTER.setEnabled(false);
                        return;
                    }
                } else if (JPSWD_PSWD_REPEAT.getPassword().length == 8)
                    first = false;
                JL_ERROR_PSWD_REPEAT.setText("");
                JBTN_REGISTER.setEnabled(true);
            }
            @Override
            public void keyPressed(KeyEvent ke) {}
            @Override
            public void keyReleased(KeyEvent ke) {}
        });
        JPSWD_PSWD.addFocusListener(new FocusListener(){
            private boolean placeholder = true;
            private final Color FG_PLACEHOLDER = new Color(68, 68, 68), FG = new Color(10, 10, 10);
            @Override
            public void focusGained(FocusEvent fe) {
                if (placeholder){
                    JPSWD_PSWD.setText("");
                    JPSWD_PSWD.setForeground(FG);
                    //los usuarios prefieren que la contraseña se vea
                    JPSWD_PSWD.setEchoChar('\u25CF');
                }
            }
            @Override
            public void focusLost(FocusEvent fe) {
                if (JPSWD_PSWD.getPassword().length == 0){
                    //los usuarios prefieren que la contraseña se vea
                    //JPSWD_PSWD.setEchoChar('\u0000');
                    JPSWD_PSWD.setText("Ingrese su contraseña");
                    JPSWD_PSWD.setForeground(FG_PLACEHOLDER);
                    placeholder = true;
                } else placeholder = false;
            }
        });
        JPSWD_PSWD_REPEAT.addFocusListener(new FocusListener(){
             private boolean placeholder = true;
            private final Color FG_PLACEHOLDER = new Color(68, 68, 68), FG = new Color(10, 10, 10);
            @Override
            public void focusGained(FocusEvent fe) {
                if (placeholder){
                    JPSWD_PSWD_REPEAT.setText("");
                    JPSWD_PSWD_REPEAT.setForeground(FG);
                    //los usuarios prefieren que la contraseña se vea
                    JPSWD_PSWD_REPEAT.setEchoChar('\u25CF');
                }
            }
            @Override
            public void focusLost(FocusEvent fe) {
                if (JPSWD_PSWD_REPEAT.getPassword().length == 0){
                    //los usuarios prefieren que la contraseña se vea
                    //JPSWD_PSWD_REPEAT.setEchoChar('\u0000');
                    JPSWD_PSWD_REPEAT.setText("Ingrese su contraseña");
                    JPSWD_PSWD_REPEAT.setForeground(FG_PLACEHOLDER);
                    placeholder = true;
                } else placeholder = false;
            }
        });
        JBTN_REGISTER.addActionListener((ActionEvent evt) -> {
            if (valid()){
                Instructions inst = new Instructions(Register.this);
                inst = null;
                alive = false;
                fd = new Thread(new CaptureAndSave());
                fd.start();
                new User(JPSWD_PSWD.getPassword(), JTXT_NAME.getText());
                JBTN_PRINCIPAL.setEnabled(false);
                JBTN_HELP.setEnabled(false);
                JTXT_NAME.setEnabled(false);
                JPSWD_PSWD.setEnabled(false);
                JPSWD_PSWD_REPEAT.setEnabled(false);
                JBTN_REGISTER.setEnabled(false);
            }
        });
        JBTN_HELP.addActionListener((ActionEvent evt) -> {new Help(Register.this);});
        JBTN_PRINCIPAL.addActionListener((ActionEvent evt) -> {
            Register.this.dispose();
            new Principal();
        });
        JBTN_REGISTER.addMouseListener(new Hover(JBTN_REGISTER));
        JBTN_PRINCIPAL.addMouseListener(new Hover(JBTN_PRINCIPAL));
        JBTN_HELP.addMouseListener(new Hover(JBTN_HELP));
    }
    private boolean valid(){
        if (JTXT_NAME.getText().length() < 8){
            JL_ERROR_NAME.setText("Mínimo 8 caracteres");
            return false;
        } 
        if (JPSWD_PSWD.getPassword().length < 8){
            JL_ERROR_PSWD.setText("Mínimo 8 caracteres");
            return false;
        }
        if (JPSWD_PSWD_REPEAT.getPassword().length < 8){
            JL_ERROR_PSWD_REPEAT.setText("Mínimo 8 caracteres");
            return false;
        }
        if (!Validate.name(JTXT_NAME.getText())){
            JL_ERROR_NAME.setText("Sólo y caracteres de la a-z y/o A-Z");
                return false;
        }
        if (JTXT_NAME.getText().equals("Ingrese su nombre de usuario")){
            JL_ERR.setText("Por favor, introduzca su nombre de usuario");
            return false;
        }   
        final char[] cont = {'I', 'n', 'g', 'r', 'e', 's', 'e', ' ', 's', 'u', ' ', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a'};
        if (Arrays.equals(JPSWD_PSWD.getPassword(), cont)){
            JL_ERR.setText("Por favor, introduzca su contraseña");
            return false;
        }
        final char[] rep = {'R', 'e', 'p', 'i', 't', 'a', ' ', 's', 'u', ' ', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a'};
        if (Arrays.equals(JPSWD_PSWD_REPEAT.getPassword(), rep)){
            JL_ERR.setText("Por favor, repita su contraseña");
            return false;
        }
        if (!(Arrays.equals(JPSWD_PSWD_REPEAT.getPassword(), JPSWD_PSWD.getPassword()))){
            JL_ERR.setText("Las contraseñas no coinciden");
            return false;
        }
        JL_ERR.setText("");
        return true;
    }
    private class Capture extends Thread{
        private BufferedImage buff_img;
        private Java2DFrameConverter buff_img_converter;
        private byte seconds_multi, seconds_no, continuo;
        private boolean ok, multi, no;
        @Override
        public void run(){
            ok = multi = no = true;
            buff_img_converter = new Java2DFrameConverter();
            storage = opencv_core.CvMemStorage.create();
            while(alive){
                if (dispose){
                    try{
                        Detection.grabber.release();
                        isCamOn = false;
                    } catch(Exception e){
                        JOptionPane.showMessageDialog(Register.this, "Tuvimos problemas para liberar la cámara.\nSi no quieres que siga gastando recursos, cierra el programa", "ERROR LIBERANDO CÁMARA", 0);
                    }
                    break;
                }
                try{
                    detect(ipl_converter.convert(Detection.grabber.grab()));
                } catch(Exception e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Algo ha salido mal al intentar tomar la foto", "ERROR EN CÁMARA", 0);
                }
            }
        }
        private void detect(IplImage img){
            //opencv_core.CvSeq sign = cvHaarDetectObjects(img, Detection.CASCADE, storage, 1.5, 3, CV_HAAR_DO_CANNY_PRUNING);
            opencv_core.CvSeq sign = cvHaarDetectObjects(img, Detection.CASCADE, storage, 1.2, 4, CV_HAAR_DO_CANNY_PRUNING);
            cvClearMemStorage(storage);
            int total_faces = sign.total();
            Frame frame = ipl_converter.convert(img);
            try{//Underflow buffer
                buff_img = buff_img_converter.convert(frame);
            } catch(Exception e){}
            graphics.drawImage(buff_img, (imagepanel.getWidth()-buff_img.getWidth())/2, (imagepanel.getHeight()-buff_img.getHeight())/2, imagepanel);
            //los contadores no son segundos son iteraciones
            //if's para evitar falsos positivos o "fallos"
            //continuo: guardar cuántas veces se ha detectado la cara sin fallos
            //si van varias veces sin fallos y hay uno, contador a 0 y continuo a 0
            //si van varios fallos seguidos aumentar contador
            if (total_faces == 0){
                if (continuo < 35){
                    if (seconds_no == 20){
                        seconds_no = 0;
                        if (no){//"mejorar" rendimiento evitar llamar a setText y ponerle un texto que ya tiene
                            JL_STATE.setForeground(LF.WARNING);
                            JL_STATE.setText("No se encontró a alguien o el algoritmo está fallando");
                            no = false;
                        }
                        multi = true;
                        ok = true;
                    }
                    else seconds_no++;
                } else continuo = 0;
            } else if (total_faces > 1){
                if (continuo < 35){
                    if (seconds_multi == 20){
                        seconds_multi = 0;
                        if (multi){
                            JL_STATE.setForeground(LF.WARNING);
                            JL_STATE.setText("Se detectó más de una cara o el algoritmo está fallando");
                            multi = false;
                        }
                        no = true;
                        ok = true;
                    } else seconds_multi++;
                } else continuo = 0;
            } else{
                if (ok){
                    JL_STATE.setForeground(LF.GOOD);
                    JL_STATE.setText("Se está detectando una cara");
                    ok = false;
                }
                multi = true;
                no = true;
                continuo++;
                if (continuo > 35)
                    seconds_no = seconds_multi = 0;
            }
        }
    }
    private class CaptureAndSave extends Thread{
        private BufferedImage buff_img;
        private Java2DFrameConverter buff_img_converter;
        private byte seconds_multi, seconds_no, continuo;
        private boolean ok = true, multi = true, no = true;
        private File outputfile;
        private int filenumb = 0;
        @Override
        public void run(){
            JL_TAKE_PICS.setPreferredSize(JL_TAKE_PICS.getPreferredSize());
            buff_img_converter = new Java2DFrameConverter();
            Timer timer = new Timer();
            TimerTask timer_task = new TimerTask(){
                @Override
                public void run(){
                    JL_TAKE_PICS.setText(String.valueOf(30 - seconds <= -1 ? 0 : 30-seconds)+"s");//evitar el -1
                    seconds++;
                }
            };
            File userdata = new File(".userdata");
            if (!checkDir(userdata))
                return;
            timer.schedule(timer_task, 0, 1000);
            while(seconds < 31){
                if(dispose)break;
                try{
                    detect(ipl_converter.convert(Detection.grabber.grab()));
                } catch(BufferUnderflowException e){ 
                } catch(Exception e){
                    JOptionPane.showMessageDialog(null, "Algo ha salido mal al intentar tomar la foto", "ERROR EN CÁMARA", 0);
                }
            }
            releaseCam();
            timer.cancel();
            timer.purge();
            generateXml(userdata);
            fd = null;
            Register.this.dispose();
            new Principal();
        }
        private void releaseCam(){
            Detection.exit();
        }
        private void generateXml(File userdata){
            Loading loading = new Loading();
            FilenameFilter imgFilter = (File dir, String name1) -> name1.endsWith(".jpg");
            File[] imgs = userdata.listFiles(imgFilter);
            int total = imgs.length, i = 0;
            FaceRecognizer fr = createLBPHFaceRecognizer();
            MatVector images = new MatVector(total);
            Mat labels = new Mat(total, 1, CV_32SC1);
            System.out.println(total);
            IntBuffer labelsBuf = labels.createBuffer();
            Mat img;
            while(i < total){
                img = imread(imgs[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                imgs[i].delete();
                images.put(i, img);
                labelsBuf.put(i, 0); 
                i++;
            }
            try{
                Thread.sleep(1000);
                fr.train(images, labels);
            } catch(Exception e){
                JOptionPane.showMessageDialog(Register.this, "Lo sentimos, surgió un error al intentar entrenar el algoritmo, por favor regístrate otra vez", "Error al entrenar el algorimto", 0);
                dispose();
                new Register();
                return;
            }
            fr.save(new File("").getAbsolutePath()+File.separator+".userdata"+File.separator+JTXT_NAME.getText()+".xml");
            loading.dispose();
            JOptionPane.showMessageDialog(Register.this, "!Excelente! Ya estás registrado", "ÉXITO", JOptionPane.INFORMATION_MESSAGE);
        }
        private boolean checkDir(File userdata){
            if (!userdata.exists()){
                try{
                    userdata.mkdir();
                    if (System.getProperty("os.name").toLowerCase().contains("windows"))
                        Runtime.getRuntime().exec("attrib +H .userdata");
                } catch(SecurityException se){
                    JOptionPane.showMessageDialog(null, "No podemos guardar las imágenes", "ERROR DE SEGURIDAD", JOptionPane.WARNING_MESSAGE);
                    return false;
                } catch(IOException ex){
                    JOptionPane.showMessageDialog(null, "Ha ocurrido un error al intentar guardar las imágenes", "ERROR DE GUARDADO", JOptionPane.WARNING_MESSAGE);
                    return false;
                }       
            }
            return true;
        }
        private void detect(IplImage img){
            //opencv_core.CvSeq sign = cvHaarDetectObjects(img, Detection.CASCADE, storage, 1.5, 3, CV_HAAR_DO_CANNY_PRUNING);
            opencv_core.CvSeq sign = cvHaarDetectObjects(img, Detection.CASCADE, storage, 1.2, 4, CV_HAAR_DO_CANNY_PRUNING);
            cvClearMemStorage(storage);
            int total_faces = sign.total();
            Frame frame = ipl_converter.convert(img);
            buff_img = buff_img_converter.convert(frame);
            graphics.drawImage(buff_img, (imagepanel.getWidth()-buff_img.getWidth())/2, (imagepanel.getHeight()-buff_img.getHeight())/2, imagepanel);
            outputfile = new File(new File("").getAbsolutePath()+File.separator+".userdata"+File.separator+String.valueOf(filenumb++)+".jpg");
            try{
                Thread.sleep(500);
                ImageIO.write(buff_img, "jpg", outputfile);
            } catch(IOException e){}
            catch(NullPointerException e){
                JL_STATE.setForeground(LF.WARNING);
                JL_STATE.setText("No se encontró a alguien o el algoritmo está fallando");
            } catch(Exception e){}
            //los contadores no son segundos son iteraciones
            //if's para evitar falsos positivos o "fallos"
            //continuo: guardar cuántas veces se ha detectado la cara sin fallos
            //si van varias veces sin fallos y hay uno, contador a 0 y continuo a 0
            //si van varios fallos seguidos aumentar contador
            if (total_faces == 0){
                if (continuo < 35){
                    if (seconds_no == 20){
                        seconds_no = 0;
                        if (no){//"mejorar" rendimiento evitar llamar a setText y ponerle un texto que ya tiene
                            JL_STATE.setForeground(LF.WARNING);
                            JL_STATE.setText("No se encontró a alguien o el algoritmo está fallando");
                            no = false;
                        }
                        multi = true;
                        ok = true;
                    }
                    else seconds_no++;
                } else continuo = 0;
            } else if (total_faces > 1){
                if (continuo < 35){
                    if (seconds_multi == 20){
                        seconds_multi = 0;
                        if (multi){
                            JL_STATE.setForeground(LF.WARNING);
                            JL_STATE.setText("Se detectó más de una cara o el algoritmo está fallando");
                            multi = false;
                        }
                        no = true;
                        ok = true;
                    } else seconds_multi++;
                } else continuo = 0;
            } else{
                if (ok){
                    JL_STATE.setForeground(LF.GOOD);
                    JL_STATE.setText("Se está detectando una cara");
                    ok = false;
                }
                multi = true;
                no = true;
                continuo++;
                if (continuo > 35)
                    seconds_no = seconds_multi = 0;
            }
        }
    }
    @Override
    public void dispose(){
        dispose = true;
        super.dispose();
    }
}
