/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fos.sw.cv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.management.InstanceAlreadyExistsException;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.fos.sw.SWMain;
import org.fos.sw.core.Loggers;
import org.fos.sw.gui.sections.BreaksPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

public class CVUtils implements AutoCloseable
{
	// min ratio for the detected face, any face with width less than this ratio (compared to the width of the frame)
	// will be ignored
	private static final float MIN_FACE_DETECTED_RATIO_WIDTH = 0.2f;
	// min ratio for the detected face, any face with height less than this ratio (compared to the height of the frame)
	// will be ignored
	private static final float MIN_FACE_DETECTED_RATIO_HEIGHT = 0.2f;
	private static boolean instantiated;
	private final VideoCapture camCapture;
	private final CascadeClassifier facesClassifier;

	public static final double INVALID_IDEAL_FOCAL_LENGTH = -1;

	/**
	 * Estimated height (length) from the eyebrows to the lips in centimeters
	 */
	public static final double ESTIMATED_FACE_HEIGHT_CM = 12;

	/**
	 * It is recommended to have a distance from the screen greater than 50cm
	 * That value may vary, but let's suppose 50cm is good
	 */
	public static final double SAFE_DISTANCE_CM = 50;

	// thresholds
	private Size minFaceDetectedSize;

	public CVUtils() throws InstanceAlreadyExistsException
	{
		super();

		if (instantiated)
			throw new InstanceAlreadyExistsException(
				"There must exist a single instance of " + BreaksPanel.class.getName()
			);

		instantiated = true;

		Loader.load(opencv_java.class);
		// OpenCV.loadShared();
		// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		camCapture = new VideoCapture();
		facesClassifier = new CascadeClassifier();

		Path tmpCascadePath = null;
		try (InputStream cascadeClassifier =
			     SWMain.getFileAsStream("/resources/cv/lbpcascade_frontalface_improved.xml")
		) {
			// copy the haar cascade from inside the jar to the tmp dir
			tmpCascadePath = Files.createTempFile("SpineWare_haar_cascade", ".xml");
			Files.copy(
				cascadeClassifier,
				tmpCascadePath,
				StandardCopyOption.REPLACE_EXISTING
			);

			// load the haar cascade from the tmp dir (loading it directly from the jar fails)
			if (!this.facesClassifier.load(tmpCascadePath.toAbsolutePath().toString()))
				Loggers.getErrorLogger().log(
					Level.WARNING,
					"Error while loading haar cascade face classifier: " + tmpCascadePath
				);
		} catch (IOException e) {
			Loggers.getErrorLogger().log(
				Level.WARNING,
				"Error while loading haar cascade face classifier: " + tmpCascadePath,
				e
			);
		} finally { // delete the temporary file if it was created
			try {
				if (tmpCascadePath != null)
					Files.deleteIfExists(tmpCascadePath);
			} catch (IOException e) {
				Loggers.getErrorLogger().log(
					Level.WARNING,
					"Error while deleting temporary file: " + tmpCascadePath,
					e
				);
			}
		}
	}

	/**
	 * Captures a single frame from the video source
	 * The code inside this method is synchronized to avoid problems, for example, calling {@link #close()} from
	 * another thread but at the same time calling this method from another thread
	 *
	 * @return the captured frame or null if the video source is not opened
	 * it may return an empty image if there was an error with the video source, check that with {@link Mat#empty()}
	 */
	@Nullable
	public Mat captureFrame()
	{
		Mat frame = new Mat();
		synchronized (camCapture) {
			if (!camCapture.isOpened())
				return null;

			camCapture.read(frame); // Mat.empty() == true if something went wrong
		}

		return frame;
	}

	/**
	 * Tries to detect faces appearing in the given frame
	 *
	 * @param frame the frame containing a photo with the face (if it does not contain any face the
	 *              returned list will be empty)
	 * @return a list of {@link Rect} describing the rectangles of the detected faces
	 */
	@NotNull
	public List<Rect> detectFaces(@NotNull Mat frame)
	{
		if (frame.empty())
			return Collections.emptyList();

		if (this.minFaceDetectedSize == null)
			this.computeThresholds(frame);

		// preprocess the frame
		Mat grayFrame = new Mat(frame.rows(), frame.cols(), frame.type());
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(grayFrame, grayFrame);

		// use the cascade classifier to detect faces
		MatOfRect detectedFaces = new MatOfRect();
		facesClassifier.detectMultiScale(
			grayFrame,
			detectedFaces,
			1.2,
			5,
			Objdetect.CASCADE_SCALE_IMAGE,
			minFaceDetectedSize
		);
		grayFrame.release();

		return detectedFaces.toList();
	}

	/**
	 * Calibrates the camera (obtains the IDEAL, not real, focal length) with the given frame
	 * The frame must contain a face, and face detection is performed
	 * If no face is detected, {@link #INVALID_IDEAL_FOCAL_LENGTH} is returned
	 * <p>
	 * For details see the python notebook
	 *
	 * @param distance       the distance at which the frame was captured
	 * @param face_height_cm the face real height in cm
	 * @param frame          the captured frame
	 * @return {@link #INVALID_IDEAL_FOCAL_LENGTH} if no face was detected
	 */
	public double getIdealFocalLength(double distance, double face_height_cm, @Nullable Mat frame)
	{
		if (frame == null)
			return INVALID_IDEAL_FOCAL_LENGTH;

		List<Rect> detectedFaces = this.detectFaces(frame);
		if (detectedFaces.isEmpty())
			return INVALID_IDEAL_FOCAL_LENGTH;

		Rect detectedFace = detectedFaces.get(0);
		double face_projected_height = detectedFace.height;

		return distance * face_projected_height / face_height_cm;
	}

	/**
	 * Computes an approximation of the distance to the camera
	 *
	 * @param focal_length          the ideal focal length
	 * @param face_height_cm        the face height in cm
	 * @param projected_face_height the projected face height (this is the height of the {@link Rect} of the face)
	 * @return the computed (approximated) distance
	 */
	public double computeDistance(double focal_length, double projected_face_height, double face_height_cm)
	{
		return focal_length * face_height_cm / projected_face_height;
	}

	/**
	 * Computes an approximation of the distance to the camera
	 *
	 * @param focal_length          the ideal focal length
	 * @param projected_face_height the projected face height (this is the height of the {@link Rect} of the face)
	 * @return the computed (approximated) distance
	 */
	public double computeDistance(double focal_length, double projected_face_height)
	{
		return computeDistance(focal_length, projected_face_height, ESTIMATED_FACE_HEIGHT_CM);
	}

	/**
	 * This will compute the min required size for a face to be detected
	 * The size computed will depend on the configuration of the class, check the method to see more
	 *
	 * @param frame the frame used as reference to compute the size
	 */
	private void computeThresholds(@NotNull Mat frame)
	{
		this.minFaceDetectedSize = new Size(
			MIN_FACE_DETECTED_RATIO_WIDTH * frame.rows(),
			MIN_FACE_DETECTED_RATIO_HEIGHT * frame.cols()
		);
	}

	/**
	 * Tries to open the capture device at the given index
	 * This method is synchronized (the code in it) to avoid problems if 2 threads try to open the camera
	 * This method will also check if the cam is already open, if so it will not try to open it
	 *
	 * @param device_idx the index of the device (usually 0)
	 * @return true if the capturing device was successfully opened or is already opened
	 */
	public boolean open(int device_idx)
	{
		synchronized (camCapture) {
			return camCapture.isOpened() || camCapture.open(device_idx);
		}
	}

	/**
	 * Tries to open the capture device at idx 0
	 * (if just 1 camera is connected it will open it)
	 * This method is synchronized (the code in it) to avoid problems if 2 threads try to open the camera
	 * This method will also check if the cam is already open, in that case, this method will not try to open it
	 *
	 * @return true if the capturing device was successfully opened
	 */
	public boolean open()
	{
		return open(0);
	}

	/**
	 * Closes the opened video source (if any)
	 * This method is synchronized (the code in it) to avoid problems if 2 threads try to close the camera
	 */
	@Override
	public void close()
	{
		synchronized (camCapture) {
			if (camCapture.isOpened())
				camCapture.release();
		}
	}

	public VideoCapture getCamCapture()
	{
		return this.camCapture;
	}

	@Override
	public String toString()
	{
		return "CVUtils{" +
			"camCapture=" + camCapture +
			", facesClassifier=" + facesClassifier +
			", minFaceDetectedSize=" + minFaceDetectedSize +
			'}';
	}
}
