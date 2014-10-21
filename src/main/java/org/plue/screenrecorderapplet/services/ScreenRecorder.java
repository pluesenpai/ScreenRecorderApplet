package org.plue.screenrecorderapplet.services;

import org.apache.commons.io.FilenameUtils;
import org.plue.screenrecorderapplet.enums.NotificationType;
import org.plue.screenrecorderapplet.exceptions.UnknownOperatingSystemException;
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

	public void recordScreen(RecordingInfoNotifier recordingInfoNotifier)
			throws IOException, UnknownOperatingSystemException
	{
		logger.debug("Starting record");
		String outputFileFullPath = FilenameUtils.concat(saveFolder, filename);
		recorderThread = RecorderThread.newInstance(outputFileFullPath, recordingInfoNotifier);
		recorderThread.start();
	}

	public void stopRecord()
	{
		logger.debug("Stopping record");
		if(recorderThread == null) {
			logger.debug("Thread is not running. Exiting");
			return;
		}

		recorderThread.stopRecording();
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
