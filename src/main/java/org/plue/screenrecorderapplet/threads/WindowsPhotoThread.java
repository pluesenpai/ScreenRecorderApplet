package org.plue.screenrecorderapplet.threads;

import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.utils.FfmpegWindowsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author paolo86@altervista.org
 */
public class WindowsPhotoThread extends PhotoThread
{
	private static final Logger logger = LoggerFactory.getLogger(WindowsPhotoThread.class);

	protected WindowsPhotoThread(String outputFileFullPath)
	{
		super(outputFileFullPath);
	}

	@Override
	protected String getFFmpegCommand() throws ScreenRecorderException
	{
		logger.debug("# called getFFmpegCommand");

		String ffmpegBinaryPath = appletParameters.getFFmpegBinaryPath().getAbsolutePath();
		FfmpegWindowsUtils.FfmpegDevices directshowDevices = FfmpegWindowsUtils.enumerateDirectshowDevices(
				ffmpegBinaryPath);
		String inputs = getWebcamDeviceParameters(directshowDevices);
		String command = MessageFormat
				.format("{0} -y -loglevel info {1} {2}", ffmpegBinaryPath, inputs, outputFileFullPath);

		logger.debug("FFMpeg command: " + command);

		logger.debug("# completed getFFmpegCommand");

		return command;
	}

	private String getWebcamDeviceParameters(FfmpegWindowsUtils.FfmpegDevices ffmpegDevices)
			throws FfmpegWindowsUtils.RetrieveFFMpegCommandException
	{
		logger.debug("# called getWebcamDeviceParameters");

		String videoDeviceString = null;
		List<FfmpegWindowsUtils.FfmpegDevice> videoDevices = ffmpegDevices.getVideoDevices();
		for(int i = 0; i < videoDevices.size(); i++) {
			FfmpegWindowsUtils.FfmpegDevice ffmpegDevice = videoDevices.get(i);
			if(StringUtils.containsIgnoreCase(ffmpegDevice.getName(), "camera") ||
					StringUtils.containsIgnoreCase(ffmpegDevice.getName(), "webcam")) {
				videoDeviceString = "-f dshow -i video=\"";
				videoDeviceString += ffmpegDevice.getName();
				videoDeviceString += "\" -vframes 1";
			}
		}

		if(videoDeviceString == null) {
			logger.error("No webcam available");
			throw new FfmpegWindowsUtils.RetrieveFFMpegCommandException("No webcam available");
		}

		logger.info("FFMpeg video inputs: " + videoDeviceString);

		logger.debug("# completed getWebcamDeviceParameters");

		return videoDeviceString;
	}
}
