package org.plue.screenrecorderapplet.utils;

import it.sinossi.commons.systemexecutor.SystemCommandExecutor;
import it.sinossi.commons.systemexecutor.SystemProcessResult;
import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author p.cortis@sinossi.it
 */
public class FfmpegWindowsUtils
{
	private static final Logger logger = LoggerFactory.getLogger(FfmpegWindowsUtils.class);

	private FfmpegWindowsUtils()
	{
	}

	public static FfmpegDevices enumerateDirectshowDevices(String ffmpegPath) throws ScreenRecorderException
	{
		logger.debug("# called enumerateDirectshowDevices");

		String ffmpegListCommand = ffmpegPath + " -list_devices true -f dshow -i dummy 2>&1";
		logger.info("Executing " + ffmpegListCommand);

		SystemProcessResult result = SystemCommandExecutor.execute(parseParameters(ffmpegListCommand));
		logger.debug("enumerateDirectshowDevices - stderr");
		logger.debug(result.getError());
		logger.debug("enumerateDirectshowDevices - stdout");
		logger.debug(result.getOutput());

		if(result.getExitCode() != 1) {
			String message = "Error while listing devices. Exit code: " + result.getExitCode();
			logger.error(message);
			throw new RetrieveFFMpegCommandException(message);
		}

		String output = result.getError();
		String[] ffmpegSplittedOutput = output.split("DirectShow");
		if(ffmpegSplittedOutput.length < 3) {
			String message = "Error splitting output. Number of parts: " + ffmpegSplittedOutput.length
					+ ". Expected at least 3 parts.";
			logger.error(message);
			for(int i = 0; i < ffmpegSplittedOutput.length; i++) {
				logger.error("===================================");
				logger.error("Part " + i + ": ");
				logger.error(ffmpegSplittedOutput[i]);
			}
			throw new RetrieveFFMpegCommandException(message);
		}

		String video = ffmpegSplittedOutput[1];
		String audio = ffmpegSplittedOutput[2];

		FfmpegDevices ffmpegDevices = new FfmpegDevices();
		logger.info("Parsing audio devices");
		ffmpegDevices.audioDevices = parseWithIndexes(audio);
		logger.info("Parsing video devices");
		ffmpegDevices.videoDevices = parseWithIndexes(video);

		logger.debug("# completed enumerateDirectshowDevices");

		return ffmpegDevices;
	}

	private static List<FfmpegDevice> parseWithIndexes(String s)
	{
		logger.debug("# called parseWithIndexes");

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

		logger.info("Found " + ffmpegDevices.size() + " devices.");

		logger.debug("# completed parseWithIndexes");

		return ffmpegDevices;
	}

	private static String[] parseParameters(String commandLine)
	{
		return StringUtils.split(commandLine, " ");
	}

	public static class FfmpegDevice
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

	public static class FfmpegDevices
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

	public static class RetrieveFFMpegCommandException extends ScreenRecorderException
	{
		private RetrieveFFMpegCommandException()
		{
		}

		public RetrieveFFMpegCommandException(String message)
		{
			super(message);
		}

		private RetrieveFFMpegCommandException(String message, Throwable cause)
		{
			super(message, cause);
		}

		private RetrieveFFMpegCommandException(Throwable cause)
		{
			super(cause);
		}
	}
}
