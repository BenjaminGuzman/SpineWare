/*
 * Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
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

package org.fos.cv;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class PostureChecker implements AutoCloseable
{
	// min ratio for the detected face, any face with width less than this ratio (compared to the width of the frame)
	// will be ignored
	private static final float MIN_FACE_DETECTED_RATIO_WIDTH = 0.2f;

	// min ratio for the detected face, any face with height less than this ratio (compared to the height of the frame)
	// will be ignored
	private static final float MIN_FACE_DETECTED_RATIO_HEIGHT = 0.2f;

	private static boolean alreadyInstantiated = false;

	// thresholds
	private Size minFaceDetectedSize;

	// measurements
	private int screen_area;

	private VideoCapture videoCapture;
	private CascadeClassifier faceCascadeClassifier;

	public PostureChecker()
	{
		if (PostureChecker.alreadyInstantiated)
			throw new IllegalStateException("This class has already been instantiated");
		PostureChecker.alreadyInstantiated = true;
		OpenCV.loadShared();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public void start() throws RuntimeException
	{
		this.videoCapture = new VideoCapture();
		this.faceCascadeClassifier = new CascadeClassifier();

		if (!this.videoCapture.isOpened())
			this.videoCapture.open(0); // this will open the first capture device is found

		Mat frame = new Mat();
		byte i = 0;
		final byte MAX_TRIES = 100;
		while (!this.videoCapture.read(frame)) {
			if (!this.videoCapture.isOpened())
				this.videoCapture.open(0);

			if (i >= MAX_TRIES)
				throw new RuntimeException("Couldn't capture frame after " + i + " tries");
			++i;
		}

		this.computeThresholds(frame);
	}

	/**
	 * Captures the next frame in the video capture, saves it in the outputImage parameter
	 * Then tries to detect faces
	 *
	 * @param outputFrame the frame where the result will be placed
	 * @return true if the next frame was captured, false otherwise
	 */
	public boolean captureAndDetectFace(Mat outputFrame, boolean add_detected_faces_to_output)
	{
		if (!this.videoCapture.read(outputFrame))
			return false;

		if (this.minFaceDetectedSize == null)
			this.computeThresholds(outputFrame);

		Mat grayFrame = new Mat(outputFrame.rows(), outputFrame.cols(), outputFrame.type());
		Imgproc.cvtColor(outputFrame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(grayFrame, grayFrame);

		return true;
	}

	/**
	 * This will compute the min required size for a face to be detected
	 * The size computed will depend on the configuration of the class, check the method to see more
	 *
	 * @param frame the frame used as reference to compute the size
	 */
	private void computeThresholds(Mat frame)
	{
		this.minFaceDetectedSize = new Size(
			PostureChecker.MIN_FACE_DETECTED_RATIO_WIDTH * frame.rows(),
			PostureChecker.MIN_FACE_DETECTED_RATIO_HEIGHT * frame.cols()
		);
	}

	/**
	 * Closes the camera
	 * Even though this should be done automatically, this method was added to ensure
	 * the camera is closed, it is NOT required but recommended to call this method or to use try-with-resources
	 */
	@Override
	public void close()
	{
		this.videoCapture.release();
	}

	@Override
	public String toString()
	{
		return "PostureChecker{" +
			"minFaceDetectedSize=" + minFaceDetectedSize +
			", screen_area=" + screen_area +
			", videoCapture=" + videoCapture +
			", faceCascadeClassifier=" + faceCascadeClassifier +
			'}';
	}
}
