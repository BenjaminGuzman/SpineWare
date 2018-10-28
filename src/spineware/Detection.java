package spineware;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;
import javax.swing.JOptionPane;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvRect;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import user.User;

/**
 *
 * @author Healthynnovation
 */
public class Detection{
    public static volatile OpenCVFrameGrabber grabber;
    public static boolean isCamOn;
    public static CvHaarClassifierCascade CASCADE;
    public static int screen_width, screen_height, grabber_width, grabber_height;
    public static User user;
    public static boolean start(){
        try{
            startGrabber();
        } catch (Exception e){
            return false;
        }
        if (CASCADE == null || CASCADE.isNull())
            return loadCascadeClassifier();
        return true;
    }
    public static void startGrabber() throws Exception{
        grabber = new OpenCVFrameGrabber(0);
        grabber.start();
        isCamOn = true;
        grabber_width = grabber.getImageWidth();
        grabber_height = grabber.getImageHeight();
    }
    public static boolean loadCascadeClassifier(){
        try{
            CASCADE = new CvHaarClassifierCascade(cvLoad(new File("").getAbsolutePath()+File.separator+"resources/haarcascade_frontalface_alt_tree.xml"));
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Error al abrir el clasificador \"haarcascade_frontalface_alt_tree.xml\".\nReemplaza el archivo \"haarcascade_frontalface_alt_tree.xml\" dentro de la carpeta resources por algún otro de OpenCV con el mismo nombre.\nTIP: Ve a nuestra página de GitHub o de JavaCV", "\"haarcascade_frontalface_alt_tree.xml\"", JOptionPane.ERROR_MESSAGE);
            Detection.exit();
            return false;
        }
        if (CASCADE.isNull()){
            JOptionPane.showMessageDialog(null, "Error al abrir el clasificador \"haarcascade_frontalface_alt_tree.xml\".\nReemplaza el archivo \"haarcascade_frontalface_alt_tree.xml\" dentro de la carpeta resources por algún otro de OpenCV con el mismo nombre.\nTIP: Ve a nuestra página de GitHub o de JavaCV", "\"haarcascade_frontalface_alt_tree.xml\"", JOptionPane.ERROR_MESSAGE);
            Detection.exit();
            return false;
        }
        return true;
    }
    public static IplImage detectFace(){
        /*seconds = 0;
        Timer timer = new Timer();
        TimerTask timertask = new TimerTask(){
            @Override
            public void run(){
                seconds++;
            }
        };
        CvMemStorage storage = opencv_core.CvMemStorage.create();
        OpenCVFrameConverter.ToIplImage frame2ipl = new OpenCVFrameConverter.ToIplImage();
        CvSeq sign;
        IplImage ipl = null;
        timer.schedule(timertask, 0, 1000);
        while (seconds < 6){
            try{
                ipl = frame2ipl.convert(grabber.grab());
            } catch(Exception e){
                System.err.println("No podemos captar la imagen");
            }
            //https://stackoverflow.com/questions/22249579/opencv-detectmultiscale-minneighbors-parameter
            sign = cvHaarDetectObjects(ipl, CASCADE, storage, 1.2, 4, CV_HAAR_DO_CANNY_PRUNING);
            cvClearMemStorage(storage);
            if (sign.total() == 1){
                timer.cancel();
                timer.purge();
                seconds = 0;
                return ipl;
            }
        }
        timer.cancel();
        timer.purge();
        return null;*/
        CvMemStorage storage = opencv_core.CvMemStorage.create();
        OpenCVFrameConverter.ToIplImage frame2ipl = new OpenCVFrameConverter.ToIplImage();
        CvSeq sign;
        IplImage ipl = null;
        try{
            ipl = frame2ipl.convert(grabber.grab());
        } catch(Exception e){
            System.err.println("No podemos captar la imagen");
        }
        sign = cvHaarDetectObjects(ipl, CASCADE, storage, 1.2, 4, CV_HAAR_DO_CANNY_PRUNING);
        cvClearMemStorage(storage);
        if (sign.total() == 1){
            /*CvRect rect = new CvRect(cvGetSeqElem(sign, 0));
            int x = rect.x(), y = rect.y(), w = rect.width(), h = rect.height();
            cvSetImageROI(ipl, cvRect(x, y, w, h));*/
            return ipl;
        }
        return null;
    }
    public static String identify(){
        File userdata = new File(".userdata");
        FilenameFilter xmlFilter = (File file, String string) -> {return string.endsWith(".xml");};
        File[] xmls = userdata.listFiles(xmlFilter);
        int i = 0, len = xmls.length;
        int predicted_label;
        double predicted_confidence;
        Mat m_img;
        //recorrer cada xml con la imagen
        //guardar la foto ayuda para reconocer
        //guardar la foto da tiempo a que se guarden todos los pixeles capturados
        //ver las pruebass
        while(i < len){
            m_img = imread(new File("current_photo.jpg").getAbsolutePath(), COLOR_BGRA2GRAY);
            IntPointer label = new IntPointer(1);
            DoublePointer confidence = new DoublePointer(1);
            //try-with-resources
            //es como un "with" de Python
            try (FaceRecognizer fr = createLBPHFaceRecognizer()) {
                fr.load(xmls[i].getAbsolutePath());
                cvtColor(m_img, m_img, COLOR_BGRA2GRAY);
                fr.predict(m_img, label, confidence);
            }
            predicted_label = label.get(0);
            predicted_confidence = confidence.get(0);
            if (predicted_confidence < 30 && predicted_label == 0)
                return xmls[i].getName().substring(0, xmls[i].getName().length()-4);
            i++;
        }
        return null;
    }
    public static IplImage detectFaceOnceRegistered(){
        //evitar que truene por que antes se ejecuto Detection.exit
        if (!Monitoreo.close){
            CvMemStorage storage = opencv_core.CvMemStorage.create();
            OpenCVFrameConverter.ToIplImage frame2ipl = new OpenCVFrameConverter.ToIplImage();
            CvSeq sign;
            IplImage ipl;
            try{
                ipl = frame2ipl.convert(grabber.grab());
            } catch(Exception e){
                try{
                    grabber.start();
                } catch(Exception er){}
                System.err.println("No podemos captar la imagen");
                return null;
            }
            //https://stackoverflow.com/questions/22249579/opencv-detectmultiscale-minneighbors-parameter
            sign = cvHaarDetectObjects(ipl, CASCADE, storage, 1.2, 4, CV_HAAR_DO_CANNY_PRUNING);
            cvClearMemStorage(storage);
            return sign.total() == 1 ? ipl : null;
        }
        return null;
    }
    private static int[] detect(IplImage face){
        CvMemStorage storage = opencv_core.CvMemStorage.create();
        CvSeq sign;
        //https://stackoverflow.com/questions/22249579/opencv-detectmultiscale-minneighbors-parameter
        sign = cvHaarDetectObjects(face, CASCADE, storage, 1.2, 4, CV_HAAR_DO_CANNY_PRUNING);
        //CvRect(iniciox, inicioy, width, height);
        CvRect r = new CvRect(cvGetSeqElem(sign, 0));
        int[] ret = new int[3];
        ret[0] = r.width();
        ret[1] = r.height();
        ret[2] = r.y();
        return ret;
    }
    public static boolean isBad(IplImage face){//algorithm
        int measurements[] = detect(face);
        //res = el área de la cara entre el área de lo que capta la cámara
        boolean so_close = ((measurements[0]*measurements[1])/(face.width()*face.height())) > 0.2;
        boolean bad = (face.height() - measurements[2]-measurements[1]) < 150;
        return so_close || bad;
    }
    public static double percent(IplImage face){//algorithm
        int measurements[] = detect(face);
        return (double)(measurements[0]*measurements[1])/(face.width()*face.height());
    }
    public static void bad(){
        Calendar calendar = Calendar.getInstance();
        user.addTimes(String.valueOf(calendar.get(Calendar.YEAR))+String.valueOf(calendar.get(Calendar.MONTH)+String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))));
    }
    public static void exit(){
        try{
            grabber.release();
            isCamOn = false;
            CASCADE = null;
        } catch(Exception e){}
    }
}