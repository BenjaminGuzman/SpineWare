package manuals;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 *
 * @author Benjamín Guzmán
 */
                                //BorderPane --- BorderLayout
public class MediaControl extends BorderPane{
    private final MediaPlayer mp;
    private boolean paused;
    private Slider slider_time;
    public MediaControl(MediaPlayer media_player, double seconds){
        Button btn = new Button();
        slider_time = new Slider(0, seconds, 0);
        Slider slider_vol = new Slider(0, 1, 1);
        this.setLeft(btn);
        this.setCenter(slider_time);
        this.setRight(slider_vol);
        this.setMargin(btn, new Insets(0, 10, 0, 0));
        this.setMargin(slider_time, new Insets(5, 10, 10, 10));
        this.setMargin(slider_vol, new Insets(5, 0, 0, 10));
        listeners(slider_time, slider_vol, btn);
        mp = media_player;
    }
    private void listeners(Slider time, Slider vol, Button btn){
        time.valueProperty().addListener((observable, old_value, new_value) -> {
            mp.seek(Duration.seconds(new_value.doubleValue()));
            if (paused){
                mp.play();
                paused = false;
            }
        });
        vol.valueProperty().addListener((observable, old_value, new_value) -> {
           mp.setVolume(new_value.doubleValue());
        });
        paused = false;
        btn.setOnAction((ActionEvent evt) -> {
            if (paused){
                btn.setStyle("-fx-background-image: url(\"/manuals/pause.png\");");
                mp.play();
            } else{
                btn.setStyle("-fx-background-image: url(\"/manuals/play.png\");");
                mp.pause();
            }
            paused = !paused;
        });
        btn.setPrefSize(25, 25);
    }
    public void seekTime(){
        slider_time.adjustValue(slider_time.getValue() + 0.5);
    }
}
