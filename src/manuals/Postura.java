package manuals;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javax.swing.JOptionPane;
import spineware.LF;
import spineware.Spineware;

/**
 *
 * @author Healthynnovation
 */
public class Postura extends JFXPanel{
    private Thread t;
    private boolean ok;
    public Postura(){
        super();
        Media video = null;
        MediaPlayer media_player;
        try {
            video = new Media(new File(Spineware.getDecodedFullPath("resources"+File.separator+"video.mp4")).toURI().toString());
            media_player = new MediaPlayer(video);
            ok = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lo sentimos no pudimos cargar el video", "Error al cargar el video", WIDTH);
            ok = false;
            return;
        }
        MediaView media_view = new MediaView(media_player);
        media_player.setAutoPlay(true);
        //https://stackoverflow.com/questions/33773179/center-an-object-in-borderpane
        media_player.setOnReady(() -> {
            initComponents(media_view, media_player, media_player.getMedia().getDuration().toSeconds());
            DoubleProperty media_view_w = media_view.fitWidthProperty();
            DoubleProperty media_view_h = media_view.fitHeightProperty();
            media_view_w.bind(Bindings.selectDouble(media_view.sceneProperty(), "width"));
            media_view_h.bind(Bindings.selectDouble(media_view.sceneProperty(), "height").multiply(.9));//90%
            media_view.setPreserveRatio(true);
        });
        DoubleProperty media_view_w = media_view.fitWidthProperty();
        DoubleProperty media_view_h = media_view.fitHeightProperty();
        media_view_w.bind(Bindings.selectDouble(media_view.sceneProperty(), "width"));
        media_view_h.bind(Bindings.selectDouble(media_view.sceneProperty(), "height").multiply(.9));//90%
        media_view.setPreserveRatio(true);
    }
    private MediaControl initComponents(MediaView media_view, MediaPlayer mp, double seconds) {
        //ponerle al volumen un símbolo
        MediaControl mc = new MediaControl(mp, seconds);
        VBox root = new VBox(media_view, mc);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 480, 360);//medidas del video
        scene.getStylesheets().add(this.getClass().getClassLoader().getResource("media_controller.css").toExternalForm());
        root.setStyle("-fx-background-color: #777777");
        this.setScene(scene);
        this.setBackground(LF.NATIVE);
        return mc;
    }
    public boolean playerOk(){
        return this.ok;
    }
    /*public MediaControl getVideoControls(){
        return mediaControl;
    }
    private class MediaControl extends JPanel implements Runnable{
        private final JButton JBTN_PP;
        private final JLabel JL_TIME;
        private final JSlider JS_TIME;
        private final JSlider JS_VOL;
        private final int TL; //total length
        private final ImageIcon PLAY = new ImageIcon(this.getClass().getResource("play.png")), PAUSE = new ImageIcon(this.getClass().getResource("pause.png")), REPLAY = new ImageIcon(this.getClass().getResource("replay.png"));
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
            
            JBTN_PP.setBackground(LF.NATIVE);
            this.add(JBTN_PP, gbc);
            
            gbc.gridx = 1;
            JL_TIME.setBackground(LF.NATIVE);
            this.add(JL_TIME, gbc);
            
            gbc.gridx = 2;
            JS_TIME.setBackground(LF.NATIVE);
            JS_TIME.setPreferredSize(new Dimension(375, 40));
            this.add(JS_TIME, gbc);
            
            gbc.gridx = 3;
            JS_VOL.setBackground(LF.NATIVE);
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
    }*/
}