package org.plue.screenrecorderapplet.utils;

import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.exceptions.RetrieveFFMpegCommandException;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.executor.CommandExecutor;
import org.plue.screenrecorderapplet.executor.ProcessResult;
import org.plue.screenrecorderapplet.models.ffmpeg.FfmpegDevice;
import org.plue.screenrecorderapplet.models.ffmpeg.FfmpegDevices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author paolo86@altervista.org
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

		ProcessResult result;
		try {
			result = new CommandExecutor(StringUtils.split(ffmpegListCommand, " ")).run();
		} catch(Exception e) {
			String message = "Error while listing devices. Got exception: ";
			logger.error(message, e);
			throw new RetrieveFFMpegCommandException(message, e);
		}

		logger.debug("enumerateDirectshowDevices - stderr");
		logger.debug(result.getStderr());
		logger.debug("enumerateDirectshowDevices - stdout");
		logger.debug(result.getStdout());

		if(result.getReturnCode() != 1) {
			String message = "Error while listing devices. Exit code: " + result.getReturnCode();
			logger.error(message);
			throw new RetrieveFFMpegCommandException(message);
		}

		String output = result.getStderr();
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
		ffmpegDevices.setAudioDevices(parseWithIndexes(audio));
		logger.info("Parsing video devices");
		ffmpegDevices.setVideoDevices(parseWithIndexes(video));

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
}
