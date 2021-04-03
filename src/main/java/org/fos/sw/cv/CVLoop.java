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
import org.fos.sw.Loggers;
import org.fos.sw.SWMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class CVLoop implements Runnable
{
	/**
	 * The number of seconds to wait before the {@link #run()} is invoked again
	 */
	public static final int UPDATE_FREQUENCY_S = 5;
	/**
	 * The alert "No face was detected" will be shown after this number of tries
	 */
	private static final int EXEC_HOOK_NO_FACE_DETECTED_AFTER_N_TRIES = 5;
	public static double frame_width = 720, frame_height = 460;
	/**
	 * Counter of the number of times a face was not detected while performing the algorithm
	 * Note: this variable does not require synchronization because it SHOULD be used inside {@link #run()}
	 */
	private static int NO_FACE_DETECTED_TIMES = 0;
	@NotNull
	private final Consumer<PostureState> onUserPostureStateComputed;
	/**
	 * A reference to an object holding the user posture state
	 * It is preferred to keep a single object rather than creating a new one on each call to {@link #run()}
	 */
	private final PostureState postureStateRef;
	/**
	 * Runnable to be invoked when {@link #NO_FACE_DETECTED_TIMES} is equal to
	 * {@link #EXEC_HOOK_NO_FACE_DETECTED_AFTER_N_TRIES}
	 */
	@Nullable
	private Runnable onSeveralNoFaceDetected;
	private CVPrefs cvPrefs;
	private int min_acceptable_x, max_acceptable_x, min_acceptable_y, max_acceptable_y;

	public CVLoop(@NotNull Consumer<PostureState> onUserPostureStateComputed)
	{
		postureStateRef = new PostureState();
		this.onUserPostureStateComputed = onUserPostureStateComputed;
	}

	public CVLoop(
		@Nullable Runnable onSeveralNoFaceDetected,
		@NotNull Consumer<PostureState> onUserPostureStateComputed
	)
	{
		this(onUserPostureStateComputed);
		this.onSeveralNoFaceDetected = onSeveralNoFaceDetected;
	}

	synchronized public void setCVPrefs(CVPrefs cvPrefs)
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
		if (Thread.currentThread().isInterrupted())
			return;

		CVUtils cvUtils = SWMain.getCVUtils();
		Mat frame = cvUtils.captureFrame();

		// try 50 times to capture a frame, if it succeeds stop trying
		int i = 50;
		while ((frame == null || frame.empty()) && i > 0 && !Thread.currentThread().isInterrupted()) {
			frame = cvUtils.captureFrame();
			++i;
		}

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

		if (detectedFaces.isEmpty()) {
			++NO_FACE_DETECTED_TIMES;
			if (NO_FACE_DETECTED_TIMES >= EXEC_HOOK_NO_FACE_DETECTED_AFTER_N_TRIES)
				if (this.onSeveralNoFaceDetected != null)
					this.onSeveralNoFaceDetected.run();

			return; // there is no point to continue if no face was detected
		}

		// show error message if no face was detected or more than 1 face was detected
		if (detectedFaces.size() > 1)
			Loggers.getDebugLogger().log(Level.INFO, "More than one face detected");

		Rect faceRect = detectedFaces.get(0);

		// 1st checker: distance
		if (cvPrefs.ideal_f_length != CVUtils.INVALID_IDEAL_FOCAL_LENGTH)
			this.postureStateRef.setDistance(
				cvUtils.computeDistance(cvPrefs.ideal_f_length, faceRect.height)
			);

		// 2nd checker: margins
		this.postureStateRef.updateMargins(
			faceRect.x < min_acceptable_x,
			faceRect.x + faceRect.width > max_acceptable_x,
			faceRect.y < min_acceptable_y,
			faceRect.y + faceRect.height > max_acceptable_y
		);

		this.onUserPostureStateComputed.accept(this.postureStateRef);
		// 3rd checker ratio of the face with respect to the screen size
		// TODO: add the 3rd checker
	}
}
