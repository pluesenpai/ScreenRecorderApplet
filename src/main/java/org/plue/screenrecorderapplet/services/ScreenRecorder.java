package org.plue.screenrecorderapplet.services;

import org.apache.commons.io.FilenameUtils;
import org.plue.screenrecorderapplet.enums.NotificationType;
import org.plue.screenrecorderapplet.exceptions.UnknownOperatingSystemException;
import org.plue.screenrecorderapplet.threads.PhotoThread;
import org.plue.screenrecorderapplet.threads.RecorderThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author paolo86@altervista.org
 */
public class ScreenRecorder
{
	private static final Logger logger = LoggerFactory.getLogger(ScreenRecorder.class);

	private String saveFolder;

	private String filename;

	private RecorderThread recorderThread;

	private PhotoThread photoThread;

	public void recordScreen(RecordingInfoNotifier recordingInfoNotifier)
			throws IOException, UnknownOperatingSystemException
	{
		logger.debug("# called recordScreen");

		String outputFileFullPath = FilenameUtils.concat(saveFolder, filename);
		logger.info("Starting record. Output file path: " + outputFileFullPath);
		recorderThread = RecorderThread.newInstance(outputFileFullPath, recordingInfoNotifier);
		recorderThread.start();

		logger.debug("# completed recordScreen");
	}

	public void stopRecord()
	{
		logger.debug("# called stopRecord");

		logger.info("Stopping record");
		if(recorderThread == null) {
			logger.info("Not recording. Exiting");
			return;
		}

		recorderThread.stopRecording();

		logger.debug("# completed stopRecord");
	}

	public void takePhotoFromWebcam(String saveFolder, String filename)
			throws IOException, UnknownOperatingSystemException
	{
		logger.debug("# called screenRecorder.takePhoto");

		String outputFileFullPath = FilenameUtils.concat(saveFolder, filename);
		logger.info("Starting record. Output file path: " + outputFileFullPath);
		photoThread = PhotoThread.newInstance(outputFileFullPath);
		photoThread.start();

		logger.debug("# completed screenRecorder.takePhoto");
	}

	public void setSaveFolder(String saveFolder)
	{
		this.saveFolder = saveFolder;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public interface RecordingInfoNotifier
	{
		public void onRecordUpdate(NotificationType notificationType, String message);
	}
}
