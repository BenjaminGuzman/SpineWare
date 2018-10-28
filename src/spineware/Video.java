package spineware;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author Healthynnovation
 */
public class Video extends JFXPanel{
    private MediaPlayer mediaPlayer;
    private MediaControl mediaControl;
    private final Color NATIVE;
    private Thread t;
    public Video(){
        super();
        mediaPlayer = new MediaPlayer(new Media(Spineware.getDecodedFullPath("/resources/video_production.mp4")));
        NATIVE = new Color(130, 130, 130);
        try{Thread.sleep(500);}catch(Exception e){}
        initComponents(new MediaView(mediaPlayer));
        this.setVisible(true);
        t = new Thread(mediaControl);
        try{Thread.sleep(500);}catch(Exception e){}
        t.start();
        
    }
    private void initComponents(MediaView mediaView) {
        mediaControl = new MediaControl();
        mediaPlayer.setAutoPlay(true);
        StackPane root = new StackPane();
        
        root.getChildren().add(mediaView);
        Scene scene = new Scene(root, 480, 360);//medidas del video
        this.setScene(scene);
        this.setBackground(NATIVE);
        mediaControl.setBackground(NATIVE);
    }      
    public MediaControl getControls(){
        return mediaControl;
    }
    private class MediaControl extends JPanel implements Runnable{
        private final JButton JBTN_PP;
        private final JLabel JL_TIME;
        private final JSlider JS_TIME;
        private final JSlider JS_VOL;
        private final int TL; //total length
        private final ImageIcon PLAY = new ImageIcon(Spineware.getDecodedFullPath("resources/play.png")), PAUSE = new ImageIcon(Spineware.getDecodedFullPath("resources/pause.png")), REPLAY = new ImageIcon(Spineware.getDecodedFullPath("resources/replay.png"));
        private boolean pause, uno;
        private boolean alive = true;
        private volatile int seconds;
        //https://docs.oracle.com/javase/8/docs/api/javax/swing/JSlider.html
        public MediaControl(){
            TL = (int)mediaPlayer.getTotalDuration().toSeconds();
            JBTN_PP = new JButton();
            JL_TIME = new JLabel("00:00");
            JS_TIME = new JSlider(0, (int)mediaPlayer.getTotalDuration().toSeconds(), 0);//min, max, initial
            JS_VOL = new JSlider(0, 20, 20);
            initComponents();
            listeners();
        }
        private void initComponents(){
            this.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            
            JBTN_PP.setIcon(PAUSE);
            JBTN_PP.setBorderPainted(false);
            JBTN_PP.setFocusPainted(false);
            
            JBTN_PP.setBackground(NATIVE);
            this.add(JBTN_PP, gbc);
            
            gbc.gridx = 1;
            JL_TIME.setBackground(NATIVE);
            this.add(JL_TIME, gbc);
            
            gbc.gridx = 2;
            JS_TIME.setBackground(NATIVE);
            JS_TIME.setPreferredSize(new Dimension(375, 40));
            this.add(JS_TIME, gbc);
            
            gbc.gridx = 3;
            JS_VOL.setBackground(NATIVE);
            JS_VOL.setPreferredSize(new Dimension(75, 40));
            this.add(JS_VOL, gbc);
        }
        private void listeners(){
            JBTN_PP.addActionListener((ActionEvent ActionEvent) -> {
                if (alive){
                    if (pause){
                        mediaPlayer.play();
                        JBTN_PP.setIcon(PAUSE);
                        synchronized(t){
                            try{
                                t.notify();
                            } catch(Exception e){}
                        }
                        pause = false;
                    } else{
                        mediaPlayer.pause();
                        JBTN_PP.setIcon(PLAY);
                        pause = true;
                    }
                } else{
                    mediaPlayer.stop();
                    mediaPlayer.play();
                    JBTN_PP.setIcon(PAUSE);
                    JS_TIME.setValue(0);
                    t = new Thread(this);
                    t.start();
                    pause = false;
                }
                    
            });
            JS_TIME.addChangeListener((ChangeEvent ce) -> {
                if (uno){
                    mediaPlayer.seek(Duration.seconds(JS_TIME.getValue()));
                    JL_TIME.setText(convertSeconds(JS_TIME.getValue()));
                    seconds = JS_TIME.getValue();
                } 
                if(!alive){
                    seconds = JS_TIME.getValue();
                    t = new Thread(this);
                    t.start();
                    alive = true;
                } 
            });
            JS_VOL.addChangeListener((ChangeEvent ce) -> {
                mediaPlayer.setVolume((double)JS_VOL.getValue()/20.0d);
            });
        }
        @Override
        public void run(){
            while(seconds != TL){
                //no le muevas
                //https://stackoverflow.com/questions/16623801/dont-know-how-use-wait-and-notify-in-java
                //este está en un hilo y t.notify() en el otro
                //a toda madre
                synchronized(t){
                    if (pause)
                        try{
                            t.wait();
                        } catch (Exception e){}
                }
                uno = false;
                JS_TIME.setValue(seconds++);
                uno = true;
                JL_TIME.setText(convertSeconds(seconds));
                try{
                    Thread.sleep(1000);
                } catch(Exception e){}
            }
            alive = false;
            JBTN_PP.setIcon(REPLAY);
        }
        private String convertSeconds(int seconds){
            int min = Math.round(seconds/60);
            int sec = seconds%60;
            String str = min < 10 ? "0"+min+":" : String.valueOf(min);
            str += sec < 10 ? "0"+sec : String.valueOf(sec);
            return str;
        }
    }
}