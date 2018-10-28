/*package manuals;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import spineware.Detection;
import titlebar.TitleBar;
import titlebar.TitleBar_Close;

/**
 *
 * @author Healthynnovation
 */
/*public class Manual extends JDialog implements ActionListener {
    
    JLabel IMG_VISTA,IMG_POSTURA;
    JButton JBTN_POSTURA, JBTN_VISTA, JBTN_PDF,JBTN_BACK;
    JPanel MAIN,ROOT,MENU,principal;
    JTextArea TXT_superior,TXT_inferior;
    BorderLayout borderLayout1 = new BorderLayout();
    private ImageIcon vista_cool;
    private ImageIcon Postura;
    private Vista vista;
    private Videofx video;
       Container cont =getContentPane();
     
    public Manual (JFrame jf){
        super(jf, true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initcomponents();
        this.setUndecorated(true);
        this.setResizable(false);
        this.setSize(800,600);
        this.setLocation(300, 70);
        this.setVisible(true);
        this.setIconImage(new ImageIcon(".\\resources\\icon.png").getImage());
    }   
    public Manual(JDialog jd){
        super(jd, true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initcomponents();
        this.setUndecorated(true);
        this.setResizable(false);
        this.setSize(800,600);
        this.setLocation(300, 70);
        this.setVisible(true);
        this.setIconImage(new ImageIcon(".\\resources\\icon.png").getImage());
    }
    public void initcomponents(){  
        TitleBar titlebar = new TitleBar_Close(this);
        titlebar.setBounds(0, 0, 800, 60);
        this.add(titlebar);
         setLayout(null);  
       
        this.setTitle("Manual");
        this.setBounds(100,100,700,500);
 
  
    
    JBTN_POSTURA=new JButton();
    JBTN_POSTURA.setLabel("POSTURA");
    JBTN_POSTURA.setBounds(5,260,90,40);
    JBTN_POSTURA.setBackground(new Color(115, 115, 115));
    JBTN_POSTURA.addActionListener(this);
    cont.add(JBTN_POSTURA);
    
    
    JBTN_VISTA=new JButton();
    JBTN_VISTA.setLabel("VISTA");
    JBTN_VISTA.setBounds(5,310,90,40);
    JBTN_VISTA.setBackground(new Color(115, 115, 115));
    JBTN_VISTA.addActionListener(this);
    cont.add(JBTN_VISTA);
    
    
    JBTN_BACK=new JButton();
    JBTN_BACK.setLabel("manual");
    //JBTN_BACK.setBounds(5,260,90,40);
    JBTN_BACK.setBackground(new Color(115, 115, 115));
    JBTN_BACK.addActionListener(this);
   // cont.add(JBTN_BACK);
    
    
    JBTN_PDF=new JButton();
    JBTN_PDF.setLabel("PDF");
    JBTN_PDF.setBounds(5,210,90,40);
    JBTN_PDF.setBackground(new Color(115,115,115));
    JBTN_PDF.addActionListener(this);
    cont.add(JBTN_PDF);
    
    
    TXT_superior=new JTextArea();
    TXT_superior.setFont(new Font("Arial",1,12));
    TXT_superior.setText("Bienvenido al manual de consejos, las opciones se encuentran del lado izquierdo");
    TXT_superior.setEditable(false);
    TXT_superior.setBounds(200, 70,590, 20);
    TXT_superior.setBackground(new Color(130, 130, 130));
    TXT_superior.setLayout(borderLayout1);
    cont.add(TXT_superior, BorderLayout.CENTER);
    
      
    TXT_inferior=new JTextArea();
    TXT_inferior.setFont(new Font("Arial",1,12));
    TXT_inferior.setText("Recuerda que...\n\n\n"
            + "La postura corporal correcta, en definitiva, implica la alineación simétrica y proporcional de los segmentos corporales\n" 
            + "alrededores del eje de la gravedad. De este modo, el sujeto no exagera la curva lumbar, dorsal o cervical,sino que \n"
            + "conserva las curvas fisiológicas normales de la columna vertebral.");
    TXT_inferior.setEditable(false);
    TXT_inferior.setBounds(110,350,800, 800);
    TXT_inferior.setBackground(new Color(130, 130, 130));
    cont.add(TXT_inferior);
    
    
    MAIN=new JPanel();
    MAIN.setBackground(new Color(130, 130, 130));
    MAIN.setBounds(100, 60 , 800, 740);
    MAIN.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90)));
   // cont.setLayout(borderLayout1);//borrar todo de borderloyaut
    
   IMG_VISTA = new JLabel();
   MAIN.add(IMG_VISTA);
   //IMG_VISTA.setBounds(400, 100, 200, 250);
   IMG_VISTA.setBackground(Color.red);
   IMG_VISTA.setLayout(borderLayout1);
     
    vista_cool = new ImageIcon(new ImageIcon(getClass().getResource("vision.png")).getImage());
    IMG_VISTA.setIcon(vista_cool);
   
    cont.add(MAIN);    
    
     
    
    IMG_POSTURA = new JLabel();
    IMG_POSTURA.setBounds(200, 300, 300, 400);
    IMG_POSTURA.setLayout(borderLayout1);
    
    Postura = new ImageIcon(new ImageIcon(getClass().getResource("lol.png")).getImage());
    IMG_POSTURA.setIcon(Postura);
    
    MAIN.add(IMG_POSTURA, BorderLayout.WEST);
   
     
    ROOT=new JPanel();
    ROOT.setBackground(new Color(130, 130, 130));
    ROOT.setBounds(0,0,800,60);
    ROOT.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90)));
    cont.add(ROOT);
    
    MENU=new JPanel();
    MENU.setBackground(new Color(130, 130, 130));
    MENU.setBounds(0, 60 , 250, 700);
    MENU.setLayout(borderLayout1);
    MENU.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90)));
    cont.add(MENU);
   
    
    }
    
    public static void openUserManual(){
        if (Desktop.isDesktopSupported())
            try{
                Desktop.getDesktop().open(new File("resources\\user manual - SpineWare.pdf"));
            } catch(IllegalArgumentException e){
                JOptionPane.showMessageDialog(null, "Falta el archivo \"user manual - SpineWare.pdf\" por favor no modifiques la carpeta \"resources\".", "Falta: \"user manual - SpineWare.pdf\"", JOptionPane.WARNING_MESSAGE);
            } catch(Exception e){
                JOptionPane.showMessageDialog(null, "Ocurrió un error al intentar abrir el archivo", "Error al abrir: \"user manual - SpineWare.pdf\"", JOptionPane.WARNING_MESSAGE);
            }
        else
            JOptionPane.showMessageDialog(null, "Lo sentimos no puedes visualizar \"user manual - SpineWare.pdf\".", "Lo sentimos no puedes visualizar el manual", JOptionPane.WARNING_MESSAGE);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
         
            
        
         if(e.getSource()== JBTN_POSTURA){
            if (vista instanceof Vista){
                vista.alive = false;
                vista = null;
            }
            MAIN.remove(IMG_VISTA);//es necesario poner los 3
            MAIN.remove(IMG_POSTURA);
            cont.remove(JBTN_POSTURA);
            //JBTN_BACK.setBounds(5,260,90,40);
            //cont.add(JBTN_BACK);
            cont.remove(TXT_superior);
            cont.remove(TXT_inferior);
            video = new Videofx();
            MAIN.add(video);
            MAIN.add(video.getControls());
            JLabel JL_ADV = new JLabel("<html>Mantener una postura correcta al usar la computadora, le puede ayuda a:<br/>Facilitar la respiración, evitar el deterioro de sus discos lumbares, evitar una mala circulación,<br/>evitar enfermedades como Cervicalgia, Cifosis, Tortícolis y muchas otras derivadas de una mala postura</html>");
            MAIN.add(JL_ADV);
            
            cont.revalidate();
            cont.repaint();
            if(e.getSource()==JBTN_BACK){
            cont.remove(video);
            cont.add(MAIN);
            
            
            
             }
         }
        
         
         else if(e.getSource()==JBTN_VISTA){
            MAIN.remove(IMG_VISTA);//es necesario poner los 3
            MAIN.remove(IMG_POSTURA);
            cont.remove(JBTN_POSTURA);
            cont.remove(TXT_superior);
            cont.remove(TXT_inferior);
            if(video instanceof Videofx){
                MAIN.remove(video);
                MAIN.remove(video.getControls());
            }
            if (!(vista instanceof Vista)){
                vista = new Vista();
                vista.alive = true;
                MAIN.add(vista);
                MAIN.revalidate();
                MAIN.repaint();
                cont.revalidate();
                cont.repaint();
            }
         }
           else if(e.getSource()==JBTN_PDF){
               if (vista instanceof Vista){
                vista.alive = false;
                vista = null;
            }
        pdf("consejos - SpineWare.pdf");
            
}
        
    

}
    @Override
    public void dispose(){
        if (vista != null)
            vista.alive = false;
        Detection.exit();
        super.dispose();
    }
    public void pdf(String archivo){
        try {
            File myFile = new File(new File("").getAbsoluteFile()+"\\resources\\"+archivo);
            Desktop.getDesktop().open(myFile);
        } catch(IllegalArgumentException e){
            JOptionPane.showMessageDialog(null, "Falta el archivo \"consejos - SpineWare.pdf\" por favor no modifiques la carpeta \"resources\".", "Falta: \"consejos - SpineWare.pdf\"", JOptionPane.WARNING_MESSAGE);
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Ocurrió un error al intentar abrir el archivo", "Error al abrir: \"consejos - SpineWare.pdf\"", JOptionPane.WARNING_MESSAGE);
        }
    
    
    }
     
          
         }
*/
package manuals;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import spineware.Detection;
import spineware.LF;
import spineware.Loading;
import spineware.Principal;
import spineware.SideBar;
import spineware.Spineware;
import titlebar.TitleBar_Min_Max_Close;

/**
 *
 * @author Healthynnovation
 */

public class Manual extends JFrame{
    private SideBar sidebar;
    private final JPanel MAIN;
    private Postura post;
    private boolean postura = false, vista = false;
    public Manual(){
        MAIN = new JPanel();
        this.setUndecorated(true);
        this.setPreferredSize(new Dimension(1191, 546));
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("SpineWare - Manuales");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIconImage(Spineware.getIcon());
        this.setVisible(true);
        listeners();
    }
    private void initComponents(){
        JPanel root = new JPanel(new BorderLayout());
        BorderLayout bl = new BorderLayout();
        root.add(new TitleBar_Min_Max_Close(true, this), BorderLayout.NORTH);
        setSideBar();
        root.add(sidebar, BorderLayout.WEST);
        MAIN.setLayout(bl);
        MAIN.setBackground(LF.NATIVE);
        JLabel jl_bienvenido = new JLabel("Bienvenido al manual de consejos, las opciones se encuentran del lado izquierdo");
        JLabel jl_eye = new JLabel(new ImageIcon(Spineware.getDecodedFullPath("resources/eye.png")));
        JLabel jl_spine = new JLabel(new ImageIcon(Spineware.getDecodedFullPath("resources/spine.png")));
        JLabel jl_info = new JLabel("<html>Recuerda que...<br /><br /><p style=\"text-align: justify\">La postura corporal correcta, en definitiva, implica la alineación simétrica y proporcional de los segmentos corporales alrededor del eje de gravedad. De este modo, el sujeto no exagera la curva lumbar, dorsal o cervical, sino conserva las curvas fisiológicas propias de la columa vertebral.</p></html>");
        jl_bienvenido.setFont(LF.FONT_TXT);
        jl_info.setFont(LF.FONT_TXT);
        JPanel imgs = new JPanel(new GridLayout(1, 2, 20, 20));
        imgs.add(jl_eye);
        imgs.add(jl_spine);
        imgs.setBackground(LF.NATIVE);
        JPanel intro = new JPanel(new BorderLayout());
        intro.setBackground(LF.NATIVE);
        intro.add(jl_bienvenido, BorderLayout.NORTH);
        intro.add(imgs, BorderLayout.CENTER);
        intro.add(jl_info, BorderLayout.SOUTH);
        MAIN.add(intro, BorderLayout.CENTER);
        MAIN.setBorder(BorderFactory.createEmptyBorder(10, 20, 50, 10));
        MAIN.setBackground(LF.NATIVE);
        root.add(MAIN);
        this.getContentPane().add(root);
        pack();
    }
    private void setSideBar(){
        ArrayList<JButton> btns = new ArrayList<JButton>();
        btns.add(new JButton("Consejos"));
        btns.add(new JButton("Postura"));
        btns.add(new JButton("Vista"));
        btns.add(new JButton("<html><p style=\"text-align: center\">Página principal</p></html>"));
        sidebar = new SideBar(btns, 50, 20, 100, 20);
    }
    private void listeners(){
        ArrayList<JButton> buttons = sidebar.getButtons();
        buttons.get(0).addActionListener((ActionEvent evt) -> {
            exit();
            Manual.openUserManual();
        });
        buttons.get(1).addActionListener((ActionEvent evt) -> { //postura
            if (!postura){
                Loading loading = new Loading();
                if (Detection.isCamOn)
                    exit();
                MAIN.removeAll();
                try{
                    post = new Postura();
                } catch(Exception e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(Manual.this, "Parece haber ocurrido un error de concurrencia.\nAl parecer inició otro proceso mientras la cámara captaba imágenes o cargaba un video", "ERROR EN LA CÁMARA", 0);
                    loading.dispose();
                    this.dispose();
                    post = null;
                    new Manual();
                }
                loading.dispose();
                MAIN.add(post   , BorderLayout.CENTER);
                MAIN.revalidate();
                MAIN.repaint();
                postura = true;
            }
        });
        buttons.get(2).addActionListener((ActionEvent evt) -> {// vista
            if (!vista){
                MAIN.removeAll();
                Vista vistaGUI = new Vista();
                MAIN.add(vistaGUI, BorderLayout.CENTER);
                MAIN.revalidate();
                MAIN.repaint();
                vistaGUI.start();
                postura = false;
                vista = true;
            }
        });
        buttons.get(3).addActionListener((ActionEvent evt) -> {
            Manual.this.dispose();
            exit();
            new Principal();
        });
        sidebar.clear();
    }
    public static void openUserManual(){
        if (Desktop.isDesktopSupported())
            try{
                Desktop.getDesktop().open(new File(Spineware.getDecodedFullPath("resources/user manual - SpineWare.pdf")));
            } catch(IllegalArgumentException e){
                JOptionPane.showMessageDialog(null, "Falta el archivo \"user manual - SpineWare.pdf\" por favor no modifiques la carpeta \"resources\".", "Falta: \"user manual - SpineWare.pdf\"", JOptionPane.WARNING_MESSAGE);
            } catch(Exception e){
                JOptionPane.showMessageDialog(null, "Ocurrió un error al intentar abrir el archivo", "Error al abrir: \"user manual - SpineWare.pdf\"", JOptionPane.WARNING_MESSAGE);
            }
        else
            JOptionPane.showMessageDialog(null, "Lo sentimos no puedes visualizar \"user manual - SpineWare.pdf\".", "Lo sentimos no puedes visualizar el manual", JOptionPane.WARNING_MESSAGE);
    }
    private void exit(){
        if (Vista.thread != null)
            if (Vista.thread.isAlive()){
                Vista.thread.interrupt();
                Detection.exit();
            }
    }
    @Override
    public void dispose(){
        exit();
        super.dispose();
    }
}