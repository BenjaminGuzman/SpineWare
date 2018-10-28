package spineware;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import user.User;

/**
 *
 * @author Healthynnovation
 */
public class Spineware {
    private static byte seconds;
    public static void main(String[] args) throws Exception{
        Loading loading = new Loading();
        try{
            Detection.startGrabber();
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Tenemos problemas para abrir la cámara", "No nos fue posible abrir la cámara", 0);
            loading.dispose();
            return;
        }
        if (!Detection.loadCascadeClassifier()){
            loading.dispose();
            return;
        }
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        if (dim.width > 1500 || dim.height > 1000)
            if (!(new File("~").exists()))
                if (JOptionPane.showConfirmDialog(null, "Su pantalla es muy grande, por lo que su experiencia con la interfaz gráfica puede verse un poco fuera de lo común.\nSin embargo, esto no afectará al rendimiento de SpineWare.\n¿Desea volver a ver este mensaje?", "PANTALLA GRANDE", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                    new File("~").createNewFile();
        Detection.screen_width = dim.width;
        Detection.screen_height = dim.height;
        File userdata = new File(".userdata");
        if (!(userdata.exists() && userdata.isDirectory())){
            userdata.mkdir();
            if (System.getProperty("os.name").toLowerCase().contains("windows"))
                Runtime.getRuntime().exec("attrib +H .userdata");
        }
        userdata = null;
        spineWare(loading);
    }
    private static void spineWare(Loading loading){
        Timer timer = new Timer();
        TimerTask increase_second = new TimerTask(){
            @Override
            public void run(){
                seconds++;
            }
        };
        timer.schedule(increase_second, 1000, 1000);
        String user = null;
        boolean log = false;
        while (seconds < 4){//intentar durante 4 segundos (o menos) reconocer usuario
            user = recognize();
            if (user != null){
                if (!user.equals("1")){
                    log = true;
                    break;
                }
            }
        }
        timer.cancel();
        timer.purge();
        loading.dispose();
        loading = null;
        if (user == null){
            new NotFound();
            log = false;
        }
        else if (user.equals("1")){ //se detecta pero no se reconoce
            new NotRecognized();
            log = false;
        }
        if (log){
            Monitoreo approved = new Monitoreo();
            approved.GUI(user);
            Detection.user = new User(new File(".userdata"+File.separator+user+".dat"));
        }
    }
    private static String recognize(){
        IplImage face = Detection.detectFace();
        if (face != null){//si sí hay cara
            //a bufferedimage para que identify() la pueda leer después (crear tiempo para que no falle)
            //ver las pruebas
            //comentarios propios
            OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
            Java2DFrameConverter iplConverter = new Java2DFrameConverter();
            Frame frame = grabberConverter.convert(face);
            try{
                //guardar la imagen grabada en un jpg
                //pero como es IplImage hay que convertirlo a un formato que se pueda guardar dicho formato es BufferedImage
                //------IplImage es de OpenCv
                //------BufferedImage es de Java
                ImageIO.write(iplConverter.getBufferedImage(frame), "jpg", new File("current_photo.jpg"));
                Thread.sleep(500);
            } catch(IOException | InterruptedException e){
                JOptionPane.showMessageDialog(null, "Ha ocurrido un error", "ERROR", 0);
            }
            String user_name = Detection.identify();//Aquí se regresa el string del usuario si se detecta
            if (user_name != null)//si se reconoce
                return user_name;
            else return "1";//1 es el código para cuando no se identificó algún usuario
        } else return null;//0 es el código si no hay cara
    }
    public static String getDecodedFullPath(String path_to_resource){
        File resource = new File(path_to_resource);
        String path = resource.getAbsolutePath();
        try{
            path = URLDecoder.decode(path, "UTF-8");//evitar %20 y así
        } catch (UnsupportedEncodingException e){}
        return path;
    }
    public static Image getIcon(){
        File icon = new File("./resources/icon.png");
        String path = icon.getAbsolutePath();
        try{
            path = URLDecoder.decode(path, "UTF-8");//evitar %20 y así
        } catch (UnsupportedEncodingException e){}
        return new ImageIcon(path).getImage();
    }
}