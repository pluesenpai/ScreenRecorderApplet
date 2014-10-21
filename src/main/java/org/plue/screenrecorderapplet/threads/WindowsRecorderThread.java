package org.plue.screenrecorderapplet.threads;

import it.sinossi.commons.systemexecutor.SystemCommandExecutor;
import it.sinossi.commons.systemexecutor.SystemProcessResult;
import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.services.ScreenRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author paolo86@altervista.org
 */
public class WindowsRecorderThread extends RecorderThread
{
	private static final Logger logger = LoggerFactory.getLogger(WindowsRecorderThread.class);

	public static final String U_SCREEN_CAPTURE = "UScreenCapture";

	public static final String SCREEN_CAPTURE_RECORDER = "screen-capture-recorder";

	protected WindowsRecorderThread(String outputFileFullPath, ScreenRecorder.RecordingInfoNotifier recordingInfoNotifier)
	{
		super(outputFileFullPath, recordingInfoNotifier);
	}

	@Override
	protected String getFFmpegCommand()
	{
		String ffmpegBinaryPath = appletParameters.getFFmpegBinaryPath().getAbsolutePath();
		FfmpegDevices directshowDevices = enumerateDirectshowDevices(ffmpegBinaryPath);
		String inputs = combineDevicesForFfmpegInput(directshowDevices);
		String codecs = "-vcodec libx264 -pix_fmt yuv420p -preset ultrafast -bufsize 600k -threads 0 -crf 0 -tune zerolatency -vsync vfr -acodec libmp3lame";
		String command = MessageFormat .format("{0} -y -loglevel info -rtbufsize 2000M {1} {2} -f mp4 {3}",
				ffmpegBinaryPath, inputs, codecs, outputFileFullPath);

		return command;
	}

	private FfmpegDevices enumerateDirectshowDevices(String ffmpegPath)
	{
		String ffmpegListCommand = ffmpegPath + " -list_devices true -f dshow -i dummy 2>&1";

		SystemProcessResult result = SystemCommandExecutor.execute(parseParameters(ffmpegListCommand));
		logger.debug("enumerateDirectshowDevices - stderr");
		logger.debug(result.getError());
		logger.debug("enumerateDirectshowDevices - stdout");
		logger.debug(result.getOutput());

		if(result.getExitCode() != 1) {
			logger.error("Error while listing devices. Exit code: " + result.getExitCode());
			// FIXME: change exception
			throw new RuntimeException();
		}

		String output = result.getError();
		String[] ffmpegSplittedOutput = output.split("DirectShow");
		if(ffmpegSplittedOutput.length < 3) {
			logger.error("Error splitting output. Number of parts: " + ffmpegSplittedOutput.length);
			for(int i = 0; i < ffmpegSplittedOutput.length; i++) {
				logger.error("===================================");
				logger.error("Part " + i + ": ");
				logger.error(ffmpegSplittedOutput[i]);
			}
			throw new RuntimeException();
		}

		String video = ffmpegSplittedOutput[1];
		String audio = ffmpegSplittedOutput[2];

		FfmpegDevices ffmpegDevices = new FfmpegDevices();
		ffmpegDevices.audioDevices = parseWithIndexes(audio);
		ffmpegDevices.videoDevices = parseWithIndexes(video);

		return ffmpegDevices;
	}

	private List<FfmpegDevice> parseWithIndexes(String s)
	{
		List<FfmpegDevice> ffmpegDevices = new ArrayList<FfmpegDevice>();

		Integer index = 0;

		String lineSeparator = System.getProperty("line.separator");
		for(String line : StringUtils.split(s, lineSeparator)) {
			Pattern p = Pattern.compile("\"(.+)\"");
			final Matcher m = p.matcher(line);
			if(m.find()) {
				index = 0;
				ffmpegDevices.add(new FfmpegDevice(index, m.group(1)));
			} else {
				Pattern p2 = Pattern.compile("repeated (\\d+) times");
				final Matcher m2 = p2.matcher(line);
				if(m2.find()) {
					final String previousName = ffmpegDevices.get(ffmpegDevices.size() - 1).getName();
					Integer times = Integer.parseInt(m2.group(1));
					for(Integer i = 0; i < times; i++) {
						index++;
						ffmpegDevices.add(new FfmpegDevice(index, previousName));
					}
				}
			}
		}

		return ffmpegDevices;
	}

	private String combineDevicesForFfmpegInput(FfmpegDevices ffmpegDevices)
	{
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
			videoDeviceString = MessageFormat
					.format(videoSection, FPS, videoDevices.get(uScreenCaptureKey).getIndex(), U_SCREEN_CAPTURE);
		} else if(screenCaptureRecorderKey != -1) {
			videoDeviceString = MessageFormat
					.format(videoSection, FPS, videoDevices.get(screenCaptureRecorderKey).getIndex(),
							SCREEN_CAPTURE_RECORDER);
		} else {
			videoDeviceString = "";
		}

		return audioDeviceString + videoDeviceString;
	}

	private static String[] parseParameters(String commandLine)
	{
		return StringUtils.split(commandLine, " ");
	}

	private class FfmpegDevice
	{
		private int index;

		private String name;

		public FfmpegDevice(int index, String name)
		{
			this.index = index;
			this.name = name;
		}

		public int getIndex()
		{
			return index;
		}

		public void setIndex(int index)
		{
			this.index = index;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}

	private static class FfmpegDevices
	{
		private List<FfmpegDevice> audioDevices;

		private List<FfmpegDevice> videoDevices;

		public List<FfmpegDevice> getAudioDevices()
		{
			return audioDevices;
		}

		public void setAudioDevices(List<FfmpegDevice> audioDevices)
		{
			this.audioDevices = audioDevices;
		}

		public List<FfmpegDevice> getVideoDevices()
		{
			return videoDevices;
		}

		public void setVideoDevices(List<FfmpegDevice> videoDevices)
		{
			this.videoDevices = videoDevices;
		}
	}
}
