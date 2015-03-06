package org.plue.screenrecorderapplet.threads.photo;

import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.exceptions.FFMpegException;
import org.plue.screenrecorderapplet.exceptions.RetrieveFFMpegCommandException;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.executor.CommandExecutor;
import org.plue.screenrecorderapplet.executor.ProcessResult;
import org.plue.screenrecorderapplet.models.LinuxAppletParameters;
import org.plue.screenrecorderapplet.models.ffmpeg.FfmpegDevice;
import org.plue.screenrecorderapplet.models.ffmpeg.FfmpegDevices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author paolo86@altervista.org
 */
public class LinuxPhotoThread extends PhotoThread
{
	private static final Logger logger = LoggerFactory.getLogger(LinuxPhotoThread.class);

	public LinuxPhotoThread(String outputFileFullPath)
	{
		super(outputFileFullPath);
	}

	@Override
	protected String getFFmpegCommand() throws ScreenRecorderException
	{
		logger.debug("# called getFFmpegCommand");

		String ffmpegBinaryPath = appletParameters.getFFmpegBinaryPath().getAbsolutePath();
		FfmpegDevices v4LDevices = getV4LDevices(
				((LinuxAppletParameters) appletParameters).getV4l2CtlPath().getAbsolutePath());
		String inputs = getWebcamDeviceParameters(v4LDevices);
		String command = MessageFormat
				.format("{0} -y -loglevel info {1} {2}", ffmpegBinaryPath, inputs, outputFileFullPath);

		logger.debug("FFMpeg command: " + command);

		logger.debug("# completed getFFmpegCommand");

		return command;
	}

	public FfmpegDevices getV4LDevices(String v4l2CtlPath) throws ScreenRecorderException
	{
		logger.debug("# called getV4LDevices");

		String v4l2CtlCommand = v4l2CtlPath + " --list-devices";
		logger.info("Executing " + v4l2CtlCommand);

		ProcessResult result;
		try {
			result = new CommandExecutor(StringUtils.split(v4l2CtlCommand, " ")).run();
		} catch(Exception e) {
			String message = "Error while listing devices. Got exception: ";
			logger.error(message, e);
			throw new RetrieveFFMpegCommandException(message, e);
		}

		logger.debug("getV4LDevices - stderr");
		logger.debug(result.getStderr());
		logger.debug("getV4LDevices - stdout");
		logger.debug(result.getStdout());

		if(result.getReturnCode() != 1) {
			String message = "Error while listing devices. Exit code: " + result.getReturnCode();
			logger.error(message);
			throw new FFMpegException(message);
		}

		String output = result.getStderr();
		FfmpegDevices webcamDevices = getWebcamDevices(output);
		logger.info(MessageFormat.format("getV4LDevices - found {0} webcams", webcamDevices.getVideoDevices().size()));

		logger.debug("# completed getV4LDevices");
		return webcamDevices;
	}

	private String getWebcamDeviceParameters(FfmpegDevices ffmpegDevices) throws RetrieveFFMpegCommandException
	{
		logger.debug("# called getWebcamDeviceParameters");

		String videoDeviceString = null;
		List<FfmpegDevice> videoDevices = ffmpegDevices.getVideoDevices();
		for(FfmpegDevice ffmpegDevice : videoDevices) {
			if(StringUtils.containsIgnoreCase(ffmpegDevice.getName(), "camera") ||
					StringUtils.containsIgnoreCase(ffmpegDevice.getName(), "webcam")) {
				videoDeviceString = "-f v4l2 -i ";
				videoDeviceString += ffmpegDevice.getName();
				videoDeviceString += " -vframes 1";

				break;
			}
		}

		if(videoDeviceString == null) {
			logger.error("No webcam available");
			throw new RetrieveFFMpegCommandException("No webcam available");
		}

		logger.info("FFMpeg video inputs: " + videoDeviceString);

		logger.debug("# completed getWebcamDeviceParameters");

		return videoDeviceString;
	}

	protected FfmpegDevices getWebcamDevices(String output)
	{
		logger.debug("# called getWebcamDevices");

		FfmpegDevices ffmpegDevices = new FfmpegDevices();
		String[] v4lCtlSplittedOutput = output.split("\n\n");
		for(String device : v4lCtlSplittedOutput) {
			String[] splittedDevice = device.split("\n");

			FfmpegDevice ffmpegDevice = new FfmpegDevice(0, StringUtils.trim(splittedDevice[1]));
			ffmpegDevices.getVideoDevices().add(ffmpegDevice);
		}

		logger.debug("# completed getWebcamDevices");

		return ffmpegDevices;
	}
}
