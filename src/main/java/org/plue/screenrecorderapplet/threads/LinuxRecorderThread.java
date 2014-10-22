package org.plue.screenrecorderapplet.threads;

import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.services.ScreenRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * @author paolo86@altervista.org
 */
public class LinuxRecorderThread extends RecorderThread
{
	private static final Logger logger = LoggerFactory.getLogger(LinuxRecorderThread.class);

	public LinuxRecorderThread(String outputFileFullPath, ScreenRecorder.RecordingInfoNotifier recordingInfoNotifier)
	{
		super(outputFileFullPath, recordingInfoNotifier);
	}

	@Override
	protected String getFFmpegCommand() throws ScreenRecorderException
	{
		logger.debug("# called getFFmpegCommand");

		String ffmpegBinaryPath = appletParameters.getFFmpegBinaryPath().getAbsolutePath();
		String inputs = "-s 1366x768 -r {0} -f x11grab -i :0.0+0,0 -f alsa -ac 2 -i default";
		String codecs = "-vcodec h264 -preset ultrafast -tune zerolatency -acodec libmp3lame";
		String command = MessageFormat.format("{0} -y -loglevel info -rtbufsize 2000M {1} {2} -f mp4 {3}",
				ffmpegBinaryPath, inputs, codecs, outputFileFullPath);

		logger.debug("FFMpeg command: " + command);

		logger.debug("# completed getFFmpegCommand");

		return command;
	}
}
