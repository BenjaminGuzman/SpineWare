package spineware;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.bytedeco.javacpp.opencv_core.IplImage;

/**
 *
 * @author Healthynnovation
 */
public class Monitoreo extends JDialog{
    private Timer timer, timer_vanish;
    private final TimerTask animation, vanish;
    private short count = 0;
    private byte seconds = 0;
    private IplImage face;
    private volatile boolean bad_active;
    public static boolean close;
    private JDialog jd_bad;
    public Monitoreo(){
        timer = new Timer();
        animation = new TimerTask(){
            @Override
            public void run(){
                if (count < 350)
                    Monitoreo.this.setLocation(Detection.screen_width-(count+=5), Detection.screen_height-150);
                else {
                    timer.cancel();
                    timer.purge();
                    timer_vanish = new Timer();
                    timer_vanish.schedule(vanish, 0, 1000);
                }
          }
        };
        vanish = new TimerTask(){
            @Override
            public void run(){
                if (seconds < 6)
                    seconds++;
                else{
                    Monitoreo.this.dispose();
                    timer_vanish.cancel();
                    timer_vanish.purge();
                    bad_active = false;
                    startCheck();
                }
            }
        };
        this.setIconImage(Spineware.getIcon());
        tray();
    }
    private void tray(){
        if (SystemTray.isSupported()){
            SystemTray systemtray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();
            MenuItem close_sw = new MenuItem("Cerrar SpineWare");
            popup.setFont(new Font("Arial", Font.PLAIN, 15));
            popup.add(close_sw);
            TrayIcon trayicon = new TrayIcon(Spineware.getIcon(), "SpineWare", popup);
            try{
                systemtray.add(trayicon);
            } catch(AWTException e){
                JOptionPane.showMessageDialog(null, "Hubo un problema al intentar poner el ícono de SpineWare en la barra de tareas para cerrarlo.\nSi en dado momento quisieras hacerlo, abre el cmd o terminal para cerrarlo, los pasos para ello están en el manual de uso.", "NO SYSTEMTRAY", JOptionPane.WARNING_MESSAGE);
            }
            trayicon.setImageAutoSize(true);
            close_sw.addActionListener((ActionEvent evt) -> {
                Monitoreo.close = true;
                systemtray.remove(trayicon);
                Monitoreo.this.dispose();
                Detection.exit();
                System.exit(0);
            });
        } else
            JOptionPane.showMessageDialog(null, "Tu computadora no soporta el tener un ícono de SpineWare en la barra de tareas para cerrarlo.\nSi en dado momento quisieras hacerlo, abre el cmd o terminal para cerrarlo, los pasos para ello están en el manual de uso.", "NO SYSTEMTRAY", JOptionPane.WARNING_MESSAGE);
    }
    private void showBad(){
        jd_bad = new JDialog();
        bad_active = true;
        BG info = new BG();
        info.setLayout(new BorderLayout());
        JLabel warning = new JLabel("¡CORRIJA SU POSTURA!");
        warning.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 25));
        warning.setHorizontalAlignment(SwingUtilities.CENTER);
        info.setBackground(LF.NATIVE);
        warning.setForeground(new Color(230, 230, 230));
        info.add(warning, BorderLayout.CENTER);
        jd_bad.getContentPane().add(info);
        jd_bad.setAlwaysOnTop(true);
        jd_bad.setUndecorated(true);
        jd_bad.setVisible(true);
        count = 0;
        jd_bad.setBounds(Detection.screen_width-350, Detection.screen_height-150, 350, 85);
        jd_bad.setVisible(true);
    }
    public void GUI(String user){
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);
        BG info = new BG();
        info.setLayout(new GridLayout(3, 1, 8, 8));
        JLabel welcome = new JLabel("Has iniciado sesión como:");
        welcome.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        welcome.setHorizontalAlignment(SwingUtilities.CENTER);
        JLabel us = new JLabel(user);
        us.setFont(new Font("Arial", Font.PLAIN, 17));
        us.setHorizontalAlignment(SwingUtilities.CENTER);
        JLabel label = new JLabel("Te estaremos monitoreando");
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        label.setHorizontalAlignment(SwingUtilities.CENTER);
        info.setBackground(LF.NATIVE);
        Color fg = new Color(230, 230, 230);
        welcome.setForeground(fg);
        us.setForeground(fg);
        label.setForeground(fg);
        info.add(welcome);
        info.add(us);
        info.add(label);
        this.getContentPane().add(info);
        this.pack();
        this.setVisible(true);
        count = 0;
        timer.schedule(animation, 0, 10);
        this.setBounds(Detection.screen_width, Detection.screen_height-100, 350, 85);
    }
    private void check(){
        face = Detection.detectFaceOnceRegistered();
        if (face != null){
            if (Detection.isBad(face)){
                if (!bad_active){
                    Detection.bad();
                    showBad();
                }
            } else {
                bad_active = false;
                if (jd_bad != null)
                    jd_bad.dispose();
            }
        }
    }
    private void startCheck(){
        count = 0;
        Detection.start();
        while(!Monitoreo.close){
            try{
                Thread.sleep(500);
            } catch(InterruptedException e){}
            check();
        }
    }
    private class BG extends JPanel{
        @Override
        protected void paintComponent(Graphics g) {
        super.paintComponent(g);
            try{
                g.drawImage(ImageIO.read(new File(Spineware.getDecodedFullPath("resources/SpineWare_Notification.png"))), 0, 0, null);
            } catch(IOException e){}
        }
    }
}
