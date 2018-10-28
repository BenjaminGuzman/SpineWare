package manuals;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import org.bytedeco.javacv.OpenCVFrameConverter;
import spineware.Detection;
import spineware.LF;

/**
 *
 * @author Healthynnovation
 */
public class Vista extends JPanel{
    private final JLabel JL_STATE;
    public boolean alive;
    public static Thread thread;
    private opencv_core.CvMemStorage storage;
    private OpenCVFrameConverter.ToIplImage ipl_converter;
    public Vista(){
        JL_STATE = new JLabel("Cargando...");
        JLabel JL_INSTRUCTIONS = new JLabel("<html>Posicione su cara frente a la cámara, muévala hacia atrás y adelante, observe el porcentaje</html>");
        JLabel JL_INFO = new JLabel("<html>Esto es qué tanto ocupa su cara con respecto a la computadora, un valor saludable es menor al 20 - 25%</html>");
        personalize(JL_INSTRUCTIONS);
        personalize(JL_INFO);
        personalize(JL_STATE);
        JL_STATE.setFont(new Font("Arial", Font.PLAIN, 50));
        storage = opencv_core.CvMemStorage.create();
        ipl_converter = new OpenCVFrameConverter.ToIplImage();
        this.setBackground(LF.NATIVE);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalGlue());
        this.add(JL_INSTRUCTIONS);
        this.add(Box.createVerticalGlue());
        this.add(JL_STATE);
        this.add(Box.createVerticalGlue());
        this.add(JL_INFO);
        this.add(Box.createVerticalGlue());
        
    }
    public void start(){
        thread = new Thread(new Capture());
        thread.start();
    }
    private void personalize(JLabel jl){
        jl.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        jl.setFont(new Font("Arial", Font.PLAIN, 30));
    }
    private class Capture extends Thread{
        private byte seconds_multi, seconds_no;
        private byte continuo;
        private boolean ok = true, multi = true, no = true;
        @Override
        public void run(){
            Detection.start();
            while(!Vista.thread.isInterrupted()){
                try{
                    detect(ipl_converter.convert(Detection.grabber.grab()));
                } catch(Exception e){
                    e.printStackTrace();
                    //JOptionPane.showMessageDialog(null, "Algo ha salido mal al intentar tomar la foto", "ERROR EN CÁMARA", 0);
                }
            }
        }
        private void detect(opencv_core.IplImage img){
            //opencv_core.CvSeq sign = cvHaarDetectObjects(img, Detection.CASCADE, storage, 1.5, 3, CV_HAAR_DO_CANNY_PRUNING);
            opencv_core.CvSeq sign = cvHaarDetectObjects(img, Detection.CASCADE, storage, 1.2, 4, CV_HAAR_DO_CANNY_PRUNING);
            cvClearMemStorage(storage);
            int total_faces = sign.total();
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
                            JL_STATE.setText("<html>No se encontró a alguien o el algoritmo está fallando</html>");
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
                            JL_STATE.setText("<html>Se detectó más de una cara o el algoritmo está fallando</html>");
                            multi = false;
                        }
                        no = true;
                        ok = true;
                    } else seconds_multi++;
                } else continuo = 0;
            } else{
                JL_STATE.setForeground(Color.BLACK);
                JL_STATE.setText(String.valueOf(Math.floor(Detection.percent(img)*100))+"%");
                multi = true;
                no = true;
                continuo++;
                if (continuo > 35)
                    seconds_no = seconds_multi = 0;
            }
        }
    }
}
