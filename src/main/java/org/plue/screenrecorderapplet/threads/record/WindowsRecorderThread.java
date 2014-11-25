package org.plue.screenrecorderapplet.threads.record;

import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.models.ffmpeg.FfmpegDevice;
import org.plue.screenrecorderapplet.models.ffmpeg.FfmpegDevices;
import org.plue.screenrecorderapplet.services.ScreenRecorder;
import org.plue.screenrecorderapplet.utils.FfmpegWindowsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author paolo86@altervista.org
 */
public class WindowsRecorderThread extends RecorderThread
{
	private static final Logger logger = LoggerFactory.getLogger(WindowsRecorderThread.class);

	private static final String U_SCREEN_CAPTURE = "UScreenCapture";

	private static final String SCREEN_CAPTURE_RECORDER = "screen-capture-recorder";

	protected WindowsRecorderThread(String outputFileFullPath,
			ScreenRecorder.RecordingInfoNotifier recordingInfoNotifier)
	{
		super(outputFileFullPath, recordingInfoNotifier);
	}

	@Override
	protected String getFFmpegCommand() throws ScreenRecorderException
	{
		logger.debug("# called getFFmpegCommand");

		String ffmpegBinaryPath = appletParameters.getFFmpegBinaryPath().getAbsolutePath();
		FfmpegDevices directshowDevices = FfmpegWindowsUtils.enumerateDirectshowDevices(
				ffmpegBinaryPath);
		String inputs = combineDevicesForFfmpegInput(directshowDevices);
		String codecs = "-vcodec libx264 -pix_fmt yuv420p -preset ultrafast -bufsize 600k -threads 0 -tune zerolatency -vsync vfr -acodec libmp3lame";
		String command = MessageFormat.format("{0} -y -loglevel info -rtbufsize 2000M {1} {2} -f mp4 {3}",
				ffmpegBinaryPath, inputs, codecs, outputFileFullPath);

		logger.debug("FFMpeg command: " + command);

		logger.debug("# completed getFFmpegCommand");

		return command;
	}

	private String combineDevicesForFfmpegInput(FfmpegDevices ffmpegDevices)
	{
		logger.debug("# called combineDevicesForFfmpegInput");

		List<FfmpegDevice> audioDevices = ffmpegDevices.getAudioDevices();

		String audioDeviceString = "";

		for(FfmpegDevice audioDevice : audioDevices) {
			audioDeviceString += MessageFormat
					.format("-f dshow -audio_device_number {0} -i audio=\"{1}\" ", audioDevice.getIndex(),
							audioDevice.getName());
		}
		audioDeviceString += " -filter_complex amix=inputs=" + audioDevices.size() + " ";

		Integer uScreenCaptureKey = -1;
		Integer screenCaptureRecorderKey = -1;
		List<FfmpegDevice> videoDevices = ffmpegDevices.getVideoDevices();

		for(int i = 0; i < videoDevices.size(); i++) {
			if(StringUtils.equals(videoDevices.get(i).getName(), U_SCREEN_CAPTURE)) {
				uScreenCaptureKey = i;
			}
			if(StringUtils.equals(videoDevices.get(i).getName(), SCREEN_CAPTURE_RECORDER)) {
				screenCaptureRecorderKey = i;
			}
		}

		String videoSection = "-f dshow -framerate {0} -video_device_number {1} -i video=\"{2}\" ";
		String videoDeviceString;
		if(uScreenCaptureKey != -1) {
			logger.info("Using " + U_SCREEN_CAPTURE);
			videoDeviceString = MessageFormat
					.format(videoSection, FPS, videoDevices.get(uScreenCaptureKey).getIndex(), U_SCREEN_CAPTURE);
		} else if(screenCaptureRecorderKey != -1) {
			logger.info("Using " + SCREEN_CAPTURE_RECORDER);
			videoDeviceString = MessageFormat
					.format(videoSection, FPS, videoDevices.get(screenCaptureRecorderKey).getIndex(),
							SCREEN_CAPTURE_RECORDER);
		} else {
			videoDeviceString = "";
		}

		logger.info("FFMpeg audio inputs: " + audioDeviceString);
		logger.info("FFMpeg video inputs: " + videoDeviceString);

		logger.debug("# completed combineDevicesForFfmpegInput");

		return audioDeviceString + videoDeviceString;
	}
}
