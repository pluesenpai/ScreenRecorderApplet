package org.plue.screenrecorderapplet.models;

import org.apache.commons.io.FilenameUtils;
import org.plue.screenrecorderapplet.exceptions.UnknownOperatingSystemException;
import org.plue.screenrecorderapplet.utils.OperatingSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * @author paolo86@altervista.org
 */
public abstract class AppletParameters
{
	private static final Logger logger = LoggerFactory.getLogger(AppletParameters.class);

	private static final String PROPERTIES_FILENAME = "config.properties";

	protected final Properties properties;

	protected File baseFolder;

	protected File binFolder;

	protected File tmpFolder;

	protected File logPath;

	private static AppletParameters instance;

	public static AppletParameters getInstance() throws IOException, UnknownOperatingSystemException
	{
		logger.debug("# called getInstance");

		if(instance == null) {
			logger.debug("Applet parameters instance was null. Creating new one");
			instance = getAppletParametersInstance();
		}

		logger.debug("# completed getInstance");

		return instance;
	}

	private static AppletParameters getAppletParametersInstance() throws UnknownOperatingSystemException, IOException
	{
		logger.debug("# called getAppletParametersInstance");

		AppletParameters appletParameters;
		if(OperatingSystemUtils.isWindows()) {
			logger.info("Using WindowsAppletParameters");
			appletParameters = new WindowsAppletParameters();
		} else if(OperatingSystemUtils.isLinux()) {
			logger.info("Using LinuxAppletParameters");
			appletParameters = new LinuxAppletParameters();
		} else {
			logger.info("Unknown or unsupported operating system: '" + OperatingSystemUtils.getOSName() + "'");
			throw new UnknownOperatingSystemException(MessageFormat.format("Unknown OS {0}", OperatingSystemUtils.getOSName()));
		}

		logger.debug("# completed getAppletParametersInstance");

		return appletParameters;
	}

	protected AppletParameters() throws IOException
	{
		logger.debug("# called constructor");

		logger.info("Loading properties file '" + PROPERTIES_FILENAME + "'");
		properties = new Properties();
		properties.load(AppletParameters.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));

		loadParametersInternal();

		logger.debug("# completed constructor");
	}

	private void loadParametersInternal()
	{
		logger.debug("# called loadParametersInternal");

		readBaseFolder();
		readLogPath();
		readBinFolder();
		readTmpFolder();

		logger.debug("# completed loadParametersInternal");
	}

	public File getBaseFolder()
	{
		return baseFolder;
	}

	public File getBinFolder()
	{
		return binFolder;
	}

	public File getLogPath()
	{
		return logPath;
	}

	public File getTmpFolder()
	{
		return tmpFolder;
	}

	protected void readLogPath()
	{
		logger.debug("# called readLogPath");

		this.logPath = new File(FilenameUtils.concat(getBaseFolder().getAbsolutePath(), "production.log"));

		logger.debug("# completed readLogPath");
	}

	protected abstract void readBaseFolder();

	protected abstract void readBinFolder();

	protected abstract void readTmpFolder();

	public abstract File getFFmpegBinaryPath();

	public abstract OperatingSystem getOperatingSystem();

	public enum OperatingSystem
	{
		WINDOWS,
		LINUX,
		MAC
	}
}
