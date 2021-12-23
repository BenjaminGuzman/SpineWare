/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <9benjaminguzman@gmail.com>
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

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.fos.sw.SWMain;
import org.fos.sw.core.Loggers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class CVLoop implements Runnable
{
	public static double frame_width = 720, frame_height = 460;

	/**
	 * The hook {@link #onSeveralNoFaceDetected} will be invoked after this number of tries
	 */
	private static final int EXEC_HOOK_NO_FACE_DETECTED_AFTER_N_TRIES = 20;

	/**
	 * The hook {@link #onMultipleFacesDetected} will be invoked after this number of tries
	 */
	private static final int EXEC_HOOK_MULTIPLE_FACES_DETECTED_AFTER_N_TRIES = 5;

	/**
	 * The hook {@link #onUserPostureStateComputed} will be invoked after this number of iterations (invocations
	 * to {@link #run()})
	 */
	private static final int EXEC_POSTURE_UPDATED_AFTER_N_ITERATIONS = 2;

	/**
	 * Counter for the number of times a face was not detected while performing the algorithm
	 * Note: this variable does not require synchronization because it SHOULD only be used inside {@link #run()}
	 */
	private static int times_no_face_detected = 0;

	/**
	 * Counter for the number of times multiple faces were detected while executing the loop
	 * Note: this variable does not require synchronization because it SHOULD only be used inside {@link #run()}
	 */
	private static int times_multiple_faces_detected = 0;

	/**
	 * Counter for the number of iterations occurred since the last call to {@link #onUserPostureStateComputed}
	 * <p>
	 * {@link #onUserPostureStateComputed} will be invoked whenever the users posture is ok, disregarding this value
	 * <p>
	 * Note: this variable does not require synchronization because it SHOULD be used inside {@link #run()}
	 */
	private static int posture_updated_iterations = 0;

	@NotNull
	private final Consumer<PostureAnalytics> onUserPostureStateComputed;

	/**
	 * A reference to an object holding the user posture state
	 * It is preferred to keep a single object rather than creating a new one on each call to {@link #run()}
	 */
	private final PostureAnalytics postureAnalytics;

	/**
	 * Runnable to be invoked when {@link #times_no_face_detected} is equal to
	 * {@link #EXEC_HOOK_NO_FACE_DETECTED_AFTER_N_TRIES}
	 */
	@Nullable
	private Runnable onSeveralNoFaceDetected;

	/**
	 * Runnable to be invoked when {@link #times_multiple_faces_detected} is equal to
	 * {@link #EXEC_HOOK_MULTIPLE_FACES_DETECTED_AFTER_N_TRIES}
	 */
	@Nullable
	private Runnable onMultipleFacesDetected;

	private CVPrefs cvPrefs;
	private int min_acceptable_x, max_acceptable_x, min_acceptable_y, max_acceptable_y;

	public CVLoop(@NotNull Consumer<PostureAnalytics> onUserPostureStateComputed)
	{
		postureAnalytics = new PostureAnalytics();
		this.onUserPostureStateComputed = onUserPostureStateComputed;
	}

	public CVLoop(
		@NotNull Consumer<PostureAnalytics> onUserPostureStateComputed,
		@Nullable Runnable onSeveralNoFaceDetected
	)
	{
		this(onUserPostureStateComputed);
		this.onSeveralNoFaceDetected = onSeveralNoFaceDetected;
	}

	public CVLoop(
		@NotNull Consumer<PostureAnalytics> onUserPostureStateComputed,
		@Nullable Runnable onSeveralNoFaceDetected,
		@Nullable Runnable onMultipleFacesDetected
	)
	{
		this(onUserPostureStateComputed, onSeveralNoFaceDetected);
		this.onMultipleFacesDetected = onMultipleFacesDetected;
	}

	public void setCVPrefs(CVPrefs cvPrefs)
	{
		this.cvPrefs = cvPrefs;
		this.recomputeMarginThresholds();
	}

	private void recomputeMarginThresholds()
	{
		this.min_acceptable_x = (int) (this.cvPrefs.margin_x / 100.0 * frame_width);
		this.max_acceptable_x = (int) ((1 - this.cvPrefs.margin_x / 100.0) * frame_width);
		this.min_acceptable_y = (int) (this.cvPrefs.margin_y / 100.0 * frame_height);
		this.max_acceptable_y = (int) ((1 - this.cvPrefs.margin_y / 100.0) * frame_height);
	}

	@Override
	public void run()
	{
		Loggers.getDebugLogger().log(Level.FINER, "Checking posture...");
		CVUtils cvUtils = SWMain.getCVUtils();
		Mat frame = cvUtils.captureFrame();

		// try 10 times to capture a frame, if it succeeds stop trying and start checking posture
		int i = 10;
		while ((frame == null || frame.empty()) && --i > 0 && !Thread.currentThread().isInterrupted())
			frame = cvUtils.captureFrame();

		if (frame == null || frame.empty()) {
			Loggers.getErrorLogger().log(Level.WARNING, "Could NOT capture frame from camera");
			return;
		}

		if (frame_width == 0 || frame_height == 0) {
			frame_width = frame.width();
			frame_height = frame.height();
			this.recomputeMarginThresholds();
		}

		List<Rect> detectedFaces = cvUtils.detectFaces(frame);
		frame.release();

		if (detectedFaces.isEmpty()) {
			++times_no_face_detected;
			if (times_no_face_detected >= EXEC_HOOK_NO_FACE_DETECTED_AFTER_N_TRIES) {
				times_no_face_detected = 0;
				if (this.onSeveralNoFaceDetected != null)
					this.onSeveralNoFaceDetected.run();
			}

			return; // there is no point to continue if no face was detected
		}

		// show error message if no face was detected or more than 1 face was detected
		if (detectedFaces.size() > 1) {
			++times_multiple_faces_detected;
			if (times_multiple_faces_detected >= EXEC_HOOK_MULTIPLE_FACES_DETECTED_AFTER_N_TRIES) {
				times_multiple_faces_detected = 0;
				if (this.onMultipleFacesDetected != null)
					this.onMultipleFacesDetected.run();
			}

			return; // stop processing
		}
		// to show the no face detected/multiple faces detected notification
		// events must happen sequentially without interruption, therefore, once a single face was detected,
		// restart the counters
		times_no_face_detected = 0;
		times_multiple_faces_detected = 0;

		Rect faceRect = detectedFaces.get(0);

		// 1st checker: distance
		if (cvPrefs.ideal_f_length != CVUtils.INVALID_IDEAL_FOCAL_LENGTH)
			this.postureAnalytics.setDistance(
				cvUtils.computeDistance(cvPrefs.ideal_f_length, faceRect.height)
			);

		// 2nd checker: margins
		this.postureAnalytics.updateMargins(
			faceRect.x < min_acceptable_x,
			faceRect.x + faceRect.width > max_acceptable_x,
			faceRect.y < min_acceptable_y,
			faceRect.y + faceRect.height > max_acceptable_y
		);

		++posture_updated_iterations;
		if (posture_updated_iterations >= EXEC_POSTURE_UPDATED_AFTER_N_ITERATIONS) {
			this.onUserPostureStateComputed.accept(this.postureAnalytics);
			posture_updated_iterations = 0;
		} else if (postureAnalytics.isPostureOk()) {
			this.onUserPostureStateComputed.accept(this.postureAnalytics);
			posture_updated_iterations = 0;
		}

		// 3rd checker ratio of the face with respect to the screen size
		// TODO: add the 3rd checker

		Loggers.getDebugLogger().log(
			Level.FINER,
			"Checking posture... Done. Is posture ok? " + postureAnalytics.isPostureOk()
		);
	}
}
